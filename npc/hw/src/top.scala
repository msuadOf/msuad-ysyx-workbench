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

  val mmio_dpi_inst = Module(new mmio_dpi_inst)
  val IFU      = Module(new IFU)


  IFU.io.Inst <> core.io.IMem //overwrite InstIO
  mmio_dpi_inst.io.Mr <> IFU.io.Mr

    val mmio_dpi_data = Module(new mmio_dpi_data)
  val LSU      = Module(new LSU)

  mmio_dpi_data.io.Mw <> LSU.io.Mw
}
