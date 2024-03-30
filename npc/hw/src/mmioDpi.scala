import chisel3._
import chisel3.util._

class mmio_dpi extends BlackBox {

  val io = IO(new Bundle {
    val clk   = Input(Clock())
    val reset = Input(Bool())

    val arAddr  = Input(UInt(32.W))
    val arWidth = Input(UInt(32.W))
    val arValid = Input(UInt(1.W))
    val arReady = Output(UInt(1.W))
    val rData   = Output(UInt(32.W))
    val rValid  = Output(UInt(1.W))
    val rReady  = Input(UInt(1.W))

    val awAddr  = Input(UInt(32.W))
    val awPort  = Input(UInt(2.W)) //DontCare
    val awValid = Input(UInt(1.W))
    val awReady = Output(UInt(1.W))

    val wData  = Input(UInt(32.W))
    val wStrb  = Input(UInt((32 / 8).W))
    val wValid = Input(UInt(1.W))
    val wReady = Output(UInt(1.W))

    val bResp  = Output(UInt(32.W))
    val bValid = Output(UInt(1.W))
    val bReady = Input(UInt(1.W))

  })
}
class mmio_dpi_wraper extends Module {
  val io = IO(new Bundle {
    val Mr = new Mr_mmioIO
    val Mw = new Mw_mmioIO
  })
  val mmio_dpi = Module(new mmio_dpi)

  mmio_dpi.io.clk   := clock
  mmio_dpi.io.reset := reset

  //In
  mmio_dpi.io.arAddr  := io.Mr.AR.Addr
  mmio_dpi.io.arWidth := io.Mr.AR.Width
  mmio_dpi.io.arValid := io.Mr.AR.Valid
  mmio_dpi.io.rReady  := io.Mr.R.Ready
  /*  */
  mmio_dpi.io.awAddr  := io.Mw.AW.Addr
  mmio_dpi.io.awPort  := io.Mw.AW.Port
  mmio_dpi.io.awValid := io.Mw.AW.Valid
  /*  */
  mmio_dpi.io.wData  := io.Mw.W.Data
  mmio_dpi.io.wStrb  := io.Mw.W.Strb
  mmio_dpi.io.wValid := io.Mw.W.Valid
  /*  */
  mmio_dpi.io.bReady := io.Mw.B.Ready
//-----

//Out
  io.Mr.AR.Ready := mmio_dpi.io.arReady
  io.Mr.R.Data   := mmio_dpi.io.rData
  io.Mr.R.Valid  := mmio_dpi.io.rValid

  io.Mw.AW.Ready := mmio_dpi.io.awReady
  io.Mw.W.Ready  := mmio_dpi.io.wReady
  io.Mw.B.Resp   := mmio_dpi.io.bResp
  io.Mw.B.Valid  := mmio_dpi.io.bValid

}
class mmio_dpi_inst extends Module{
    val io = IO(new Bundle {
    val Mr = new Mr_mmioIO
  })
  val mmio_dpi_wraper=Module(new mmio_dpi_wraper)
  mmio_dpi_wraper.io.Mr<>io.Mr
  mmio_dpi_wraper.io.Mw.Flipped_IOinit()
}
class mmio_dpi_data extends Module{
    val io = IO(new Bundle {
    // val Mr = new Mr_mmioIO
    val Mw = new Mw_mmioIO
  })
  val mmio_dpi_wraper=Module(new mmio_dpi_wraper)
  // mmio_dpi_wraper.io.Mr<>io.Mr
    mmio_dpi_wraper.io.Mr.Flipped_IOinit()
  mmio_dpi_wraper.io.Mw<>io.Mw
}