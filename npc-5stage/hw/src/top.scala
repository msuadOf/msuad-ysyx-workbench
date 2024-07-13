import chisel3._
import chisel3.util._

import chisel3.util.experimental._
import chisel3.experimental.prefix

import core.Core
class top(isa_info: String = "RISCV32") extends Module {
  val io = IO(new Bundle {
    // val IMem = new InstIO
    // val DMem = new MemIO

    // val diff = new diffIO
  })

  val core = Module(new Core)

}
