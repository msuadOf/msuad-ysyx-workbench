package core.AXI4

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