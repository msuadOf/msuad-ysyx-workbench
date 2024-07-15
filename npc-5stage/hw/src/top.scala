import chisel3._
import chisel3.util._

import chisel3.util.experimental._
import chisel3.experimental.prefix

import core._

class diffIO extends Bundle with core.utils.WithIOInit {
  val diff_en  = Output(UInt(1.W))
  val DMemInst = Output(UInt(1.W))
  val pc       = Output(UInt(32.W))
  val dnpc     = Output(UInt(32.W))
  val snpc     = Output(UInt(32.W))
  val regs     = Output(Vec(32, UInt(32.W)))
  val mepc     = Output(UInt(32.W))
  val mcause   = Output(UInt(32.W))
  val mstatus  = Output(UInt(32.W))
  val mtvec    = Output(UInt(32.W))

  def IOinit[T <: Data](value: T): Unit = {
    diff_en  := value
    DMemInst := value
    pc       := value
    dnpc     := value
    snpc     := value
    regs.foreach(_:=0.U)
    mepc     := value
    mcause   := value
    mstatus  := value
    mtvec    := value
  }
  def Flipped_IOinit[T <: Data](value: T): Unit = {}
}
class top(isa_info: String = "RISCV32") extends Module {
  val io = IO(new Bundle {
    // val IMem = new InstIO
    // val DMem = new MemIO

    val diff = new diffIO
    val core = new CoreIO
  })
  io.diff.IOinit()
  val core = Module(new Core)
  core.io <> io.core

}
