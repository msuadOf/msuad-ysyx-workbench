package npc.dpic

import chisel3._
import chisel3.util._

import npc.misc._

class mmio_dpi extends BlackBox {

  val io = IO(new Bundle {
    val clk     = Input(Clock())
    val reset   = Input(Bool())
    val arAddr  = Input(UInt(32.W))
    val arWidth = Input(UInt(32.W))
    val arValid = Input(UInt(1.W))
    val arReady = Output(UInt(1.W))
    val rData   = Output(UInt(32.W))
    val rValid  = Output(UInt(1.W))
    val rReady  = Input(UInt(1.W))
    val wAddr   = Input(UInt(32.W))
    val wData   = Input(UInt(32.W))
    val wWidth  = Input(UInt(32.W))
    val wValid  = Input(UInt(1.W))
    val wReady  = Output(UInt(1.W))
  })
}
class mmio_dpi_wraper extends Module {
  val io       = IO(new mmioIO)
  val mmio_dpi = Module(new mmio_dpi)

  mmio_dpi.io.clk   := clock
  mmio_dpi.io.reset := reset

  mmio_dpi.io.arAddr  := io.AR.arAddr
  mmio_dpi.io.arWidth := io.AR.arWidth
  mmio_dpi.io.arValid := io.AR.arValid
  mmio_dpi.io.rReady  := io.R.rReady
  mmio_dpi.io.wAddr   := io.simpleW.wAddr
  mmio_dpi.io.wData   := io.simpleW.wData
  mmio_dpi.io.wWidth  := io.simpleW.wWidth
  mmio_dpi.io.wValid  := io.simpleW.wValid

  io.simpleW.wReady := mmio_dpi.io.wReady
  io.AR.arReady     := mmio_dpi.io.arReady
  io.R.rData        := mmio_dpi.io.rData
  io.R.rValid       := mmio_dpi.io.rValid

}
