package Stages

import chisel3._
import chisel3.util._

class IDUMessage extends Bundle {
  val inst = Output(UInt(32.W))
}

class IDU extends Module {
  val io = IO(new Bundle {
    val in  = Flipped(Decoupled(new IFUMessage))
    val out = Decoupled(new IDUMessage)
  })

}
