import chisel3._
import chisel3.util._

class MemIO extends Bundle {
  val rAddr  = Output(UInt(32.W))
  val rData  = Input(UInt(32.W))
  val ren    = Output(UInt(1.W))
  val rWidth = Output(UInt(4.W))
  val rValid = Input(UInt(1.W))
  val rReady = Output(UInt(1.W))

  val wAddr  = Output(UInt(32.W))
  val wData  = Output(UInt(32.W))
  val wen    = Output(UInt(1.W))
  val wWidth = Output(UInt(4.W))
  val wValid = Output(UInt(1.W))
  val wReady = Input(UInt(1.W))

  def IOinit() = {
    this.rAddr  := 0.U
    this.wAddr  := 0.U
    this.wData  := Fill(32, 1.U) //FFFF FFFF
    this.wen    := 0.U
    this.ren    := 0.U
    this.wWidth := 4.U
    this.rWidth := 4.U

    this.rReady := 0.U
    this.wValid := 0.U
  }
}

class InstIO extends Bundle {
  val rAddr = Output(UInt(32.W))
  val rData = Input(UInt(32.W))
}
