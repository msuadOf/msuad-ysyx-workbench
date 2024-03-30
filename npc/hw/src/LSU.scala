import chisel3._
import chisel3.util._

class LSU extends Module {
  val io = IO(new Bundle {
    // val Mr   = Flipped(new Mr_mmioIO)
    val Mw = Flipped(new Mw_mmioIO)
  })
  io.Mw.AW.Port := DontCare

//init
// io.Mw.W.Data:=0.U
  io.Mw.W.Flipped_IOinit()
  io.Mw.B.Flipped_IOinit()

  val wen = Wire(UInt(1.W))
  wen := 1.U

  def create_sender_fsm(wen:UInt,Valid: UInt, Ready: UInt) = {
    val s_idle :: s_wait :: Nil = Enum(2)
    val state                      = RegInit(s_idle)
    state := MuxLookup(state, s_idle)(
      List(
        s_idle -> Mux(wen.asBool, s_wait, s_idle),
        s_wait -> Mux(Ready.asBool, s_idle, s_wait)
      )
    )
    Valid := MuxLookup(state, 0.U)(
      List(
        s_idle -> 0.U,
        s_wait -> 1.U
      )
    )
    state
  }
  def c()={
      val s_idle :: s_wait_ready :: Nil = Enum(2)
  val state = RegInit(s_idle)
  state := MuxLookup(state, s_idle)(List(
    s_idle       -> Mux(io.out.valid, s_wait_ready, s_idle),
    s_wait_ready -> Mux(io.out.ready, s_idle, s_wait_ready)
  ))


  }
val aw_state=create_sender_fsm(wen,io.Mw.AW.Valid,io.Mw.AW.Ready)
  val w_state=create_sender_fsm(wen,io.Mw.W.Valid,io.Mw.W.Ready)

  
  val awAddr_r = RegInit("x80000008".U)
  io.Mw.AW.Addr := awAddr_r

  io.Mw.W.Data:=12.U
}
