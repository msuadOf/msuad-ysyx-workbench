import chisel3._
import chisel3.util._

trait With_ValidReadyFsmCreator {
  def create_sender_fsm(wen: Bool, Valid: UInt, Ready: UInt) = {
    val s_idle :: s_wait :: Nil = Enum(2)
    val state                   = RegInit(s_idle)
    state := MuxLookup(state, s_idle)(
      List(
        s_idle -> Mux(wen, s_wait, s_idle),
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
  io.Mw.AW.Port := DontCare

//init
// io.Mw.W.Data:=0.U
  io.Mw.W.Flipped_IOinit()
  io.Mw.B.Flipped_IOinit()

  val wen = Wire(UInt(1.W))
  wen := io.Ctrl.wEn

  val (aw_state, s_aw_idle :: s_aw_wait :: Nil) = create_sender_fsm(wen.asBool, io.Mw.AW.Valid, io.Mw.AW.Ready)
  val (w_state, s_w_idle :: s_w_wait :: Nil)    = create_sender_fsm(wen.asBool, io.Mw.W.Valid, io.Mw.W.Ready)
  val (b_state, s_b_idle :: s_b_wait :: Nil) =
    create_reciever_fsm((aw_state === s_aw_wait) && (w_state === s_w_wait), io.Mw.B.Valid, io.Mw.B.Ready)

  val awAddr_r = RegInit(0.U) //"x80001008".U)
  io.Mw.AW.Addr := awAddr_r

  val wData_r = RegInit(0.U)
  io.Mw.W.Data := wData_r

  when(wen === 1.U) {
    awAddr_r := io.Ctrl.wAddr
    wData_r  := io.Ctrl.wData
  }

  io.Ctrl.wEop := 0.U
  when(io.Mw.B.fire()) {
    io.Ctrl.wEop := 1.U
  }

}
class LU extends Module with With_ValidReadyFsmCreator {
  val io = IO(new Bundle {
    val Mr = Flipped(new Mr_mmioIO)
  })

}
class LSU extends Module {
  val io = IO(new Bundle {
    val Mr      = Flipped(new Mr_mmioIO)
    val Mw      = Flipped(new Mw_mmioIO)
    val SU_ctrl = new SUCtrl_IO
  })
  val LU = new LU
  val SU = new SU
  io.Mr <> LU.io.Mr
  io.Mw <> SU.io.Mw
}
