package npc.Stages

import chisel3._
import chisel3.util._

class WBUMessage extends Bundle {
  val inst = Output(UInt(32.W))
}

class WBU extends Module {
  val io = IO(new Bundle {
    val in  = Flipped(Decoupled(new EXUMessage))
    val out = Decoupled(new WBUMessage)
  })

}
