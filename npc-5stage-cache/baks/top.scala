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
  this.io.DMem.IOinit()
  this.io.IMem.IOinit()

//   val core = Module(new Core)
//   core.io.diff <> this.io.diff
//   // core.io.IMem <> this.io.IMem
//   core.io.DMem.Flipped_IOinit()

//   val mmio_dpi_inst = Module(new mmio_dpi_inst)
//   val IFU      = Module(new IFU)

// //Inst
//   IFU.io.Inst <> core.io.IMem //overwrite InstIO
//   mmio_dpi_inst.io.Mr <> IFU.io.Mr

//   val mmio_dpi_data = Module(new mmio_dpi_data)
//   val LSU      = Module(new LSU)
//   mmio_dpi_data.io.Mr <>LSU.io.Mr
//   mmio_dpi_data.io.Mw <> LSU.io.Mw

//   core.io.SUCtrl<>LSU.io.SUCtrl
//   core.io.LUCtrl<>LSU.io.LUCtrl
  val NPC = Module(new NPC)
  NPC.io.diff <> this.io.diff
  val mmio_dpi = Module(new mmio_dpi_AXI)
  NPC.io.AXI_Mem <> mmio_dpi.io

}
