package Stages

import chisel3._
import chisel3.util._

class EXUMessage extends Bundle {
  val inst = Output(UInt(32.W))
}

class EXU extends Module {
  val io = IO(new Bundle {
    val in  = Flipped(Decoupled(new IDUMessage))
    val out = Decoupled(new EXUMessage)
  })

}
