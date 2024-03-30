import chisel3._
import chisel3.util._

import chisel3.util.experimental._
import chisel3.experimental.prefix

class top(isa_info: String = "RISCV32") extends Module {
  val io = IO(new Bundle {
    val IMem = new InstIO
    val DMem = new MemIO

    val diff = new diffIO
  })

  val core = Module(new Core)
  core.io <> this.io

  val mmio_dpi = Module(new mmio_dpi_wraper)
  val IFU      = Module(new IFU)

  IFU.io.Inst <> core.io.IMem //overwrite InstIO
  mmio_dpi.io <> IFU.io.mmio
}
