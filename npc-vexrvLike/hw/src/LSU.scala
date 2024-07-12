import chisel3._
import chisel3.util._

trait With_ValidReadyFsmCreator {
  //BugFix Log(2024-4-20): fixed create_sender_fsm(wen: Bool,...) --> create_sender_fsm(sop: Bool,...)
  def create_sender_fsm(sop: Bool, Valid: UInt, Ready: UInt) = {
    val s_idle :: s_wait :: Nil = Enum(2)
    val state                   = RegInit(s_idle)
    state := MuxLookup(state, s_idle)(
      List(
        s_idle -> Mux(sop, s_wait, s_idle),
        s_wait -> Mux(Ready.asBool, s_idle, s_wait)
      )
    )
    Valid := MuxLookup(state, 0.U)(
      List(
        s_idle -> 0.U,
        s_wait -> 1.U
      )
    )
    (state, s_idle :: s_wait :: Nil)
  }
  def create_reciever_fsm(sop: Bool, Valid: UInt, Ready: UInt) = {
    val s_idle :: s_wait :: Nil = Enum(2)
    val state                   = RegInit(s_idle)
    state := MuxLookup(state, s_idle)(
      List(
        s_idle -> Mux(sop, s_wait, s_idle),
        s_wait -> Mux(Valid.asBool, s_idle, s_wait)
      )
    )
    Ready := MuxLookup(state, 0.U)(
      List(
        s_idle -> 0.U,
        s_wait -> 1.U
      )
    )
    (state, s_idle :: s_wait :: Nil)
  }
}
class SU extends Module with With_ValidReadyFsmCreator {
  val io = IO(new Bundle {
    // val Mr   = Flipped(new Mr_mmioIO)
    val Mw   = Flipped(new Mw_mmioIO)
    val Ctrl = new SUCtrl_IO
  })
  io.Mw.AW.Flipped_IOinit()
  io.Mw.AW.Port := DontCare

//init
// io.Mw.W.Data:=0.U
  io.Mw.W.Flipped_IOinit()
  io.Mw.B.Flipped_IOinit()

  val wen = Wire(UInt(1.W))
  wen := io.Ctrl.wEn

  /* BugFix Log(2024-4-20): fixed create_sender_fsm(wen: Bool,...) --> create_sender_fsm(sop: Bool,...)
   * so create_sender_fsm(wen.asBool, ...) --> create_sender_fsm(wen_pulse, ...)
  */
  val wen_pulse= wen.asBool && (~RegNext(wen.asBool))
  val (aw_state, s_aw_idle :: s_aw_wait :: Nil) = create_sender_fsm(wen_pulse, io.Mw.AW.Valid, io.Mw.AW.Ready)
  val (w_state, s_w_idle :: s_w_wait :: Nil)    = create_sender_fsm(wen_pulse, io.Mw.W.Valid, io.Mw.W.Ready)
  val (b_state, s_b_idle :: s_b_wait :: Nil) =
    create_reciever_fsm((aw_state === s_aw_wait) && (w_state === s_w_wait), io.Mw.B.Valid, io.Mw.B.Ready)

  val awAddr_r = RegInit(0.U) //"x80001008".U)
  io.Mw.AW.Addr := awAddr_r

  val wData_r = RegInit(0.U)
  io.Mw.W.Data := wData_r
  val wStrb_r = RegInit(0.U)
  io.Mw.W.Strb := wStrb_r

  when(wen === 1.U) {
    awAddr_r := io.Ctrl.wAddr
    wData_r  := io.Ctrl.wData
    wStrb_r  := io.Ctrl.wStrb
  }

  io.Ctrl.wEop := 0.U
  when(io.Mw.B.fire()) {
    io.Ctrl.wEop := 1.U
  }

}
class LU extends Module with With_ValidReadyFsmCreator {
  val io = IO(new Bundle {
    val Mr   = Flipped(new Mr_mmioIO)
    val Ctrl = new LUCtrl_IO
  })

  io.Mr.Flipped_IOinit()
  io.Ctrl.IOinit()

  val rEn = io.Ctrl.rEn

  val data_in_rValid_r = RegInit(0.U)
  /* !!! */
  when(rEn === 1.U) {
    data_in_rValid_r := 0.U
  }
  io.Ctrl.rValid := data_in_rValid_r //Inst data - vld
  //---- io reg ----
  val arAddr = RegInit(0.U)
  io.Mr.AR.Addr := arAddr
  val arWidth = RegInit(0.U)
  io.Mr.AR.Width := arWidth
  when(rEn === 1.U) {
    arAddr := io.Ctrl.rAddr;
    arWidth := MuxLookup(io.Ctrl.rStrb, 32.U)(
      List(
        1.U -> 1.U,
        3.U -> 2.U,
        15.U -> 4.U
      )
    )
  }
  val data_in = RegInit(0.U)
  io.Ctrl.rData := io.Mr.R.Data //data_in /* changelog:内存的数据直通 */
  //---------------

  val sIDLE :: sARwaiting :: sARcplt_Rwaiting :: sRcplt :: Nil = Enum(4)

  val R_state = RegInit(sIDLE)

  //跳转
// R_state:=sIDLE
  switch(R_state) {
    is(sIDLE) {
      when(rEn === 1.U) {
        R_state := sARwaiting
      }.otherwise {
        R_state := sIDLE
      }
    }
    is(sARwaiting) {
      when(io.Mr.AR.Ready === 1.U) {
        R_state := sARcplt_Rwaiting
      }.otherwise {
        R_state := sARwaiting
      }
    }
    is(sARcplt_Rwaiting) {
      when(io.Mr.R.Valid === 1.U) {
        R_state          := sRcplt
        data_in          := io.Mr.R.Data //data - satisfy timing
        data_in_rValid_r := 1.U //data vld - sysnc with data_in
      }.otherwise {
        R_state := sARcplt_Rwaiting
      }
    }
    is(sRcplt) {
      when(rEn === 1.U) {
        R_state := sARwaiting
      }.otherwise {
        R_state := sRcplt
      }
    }
  }

  switch(R_state) {
    is(sIDLE) {
      io.Mr.AR.Valid := 0.U
    }
    is(sARwaiting) {
      io.Mr.AR.Valid := 1.U
    }
    is(sARcplt_Rwaiting) {
      io.Mr.AR.Valid := 0.U
      io.Mr.R.Ready  := 1.U
    }
    is(sRcplt) {
      io.Mr.R.Ready  := 0.U
      io.Mr.AR.Valid := 0.U
    }
  }

  io.Ctrl.rEop := RegNext(Mux(io.Mr.R.fire(),1.U,0.U))

    printf("data_in=%d,data_in_rValid_r=%d,rEop=%d\n", data_in,data_in_rValid_r,io.Ctrl.rEop)
}
class LSU extends Module {
  val io = IO(new Bundle {
    val Mr     = Flipped(new Mr_mmioIO)
    val Mw     = Flipped(new Mw_mmioIO)
    val SUCtrl = new SUCtrl_IO
    val LUCtrl = new LUCtrl_IO
  })
  val LU = Module(new LU)
  val SU = Module(new SU)
  io.Mr <> LU.io.Mr
  io.Mw <> SU.io.Mw
  io.SUCtrl <> SU.io.Ctrl
  io.LUCtrl <> LU.io.Ctrl
}
