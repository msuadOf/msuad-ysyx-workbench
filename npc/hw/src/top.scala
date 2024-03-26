import chisel3._
import chisel3.util._

import chisel3.util.experimental._
import chisel3.experimental.prefix

class test_12 extends Module {
  val io = IO(Flipped(new mmioIO))

  io.AR.arWidth := 0.U
  io.AR.arValid := 0.U
  io.AR.arAddr  := 0.U

  io.R.rReady := 0.U

  io.simpleW.wAddr  := 0.U
  io.simpleW.wData  := 0.U
  io.simpleW.wWidth := 0.U
  io.simpleW.wValid := 0.U

  val I_en = RegInit(1.U)

  val sIDLE :: sARwaiting :: sARcplt_Rwaiting :: sRcplt :: Nil = Enum(4)
  val R_state                                                  = RegInit(sIDLE)
//跳转
// R_state:=sIDLE
  switch(R_state) {
    is(sIDLE) {
      when(I_en === 1.U) {
        R_state := sARwaiting
      }.otherwise {
        R_state := sIDLE
      }
    }
    is(sARwaiting) {
      when(io.AR.arReady===1.U){
        R_state:=sARcplt_Rwaiting
      }.otherwise{
        R_state:=sARwaiting
      }
    }
    is(sARcplt_Rwaiting) {
      when(io.R.rValid===1.U){
        R_state:=sRcplt
      }.otherwise{
        R_state:=sARcplt_Rwaiting
      }
    }
    is(sRcplt) {
      when(I_en===1.U){
        R_state:=sARwaiting
      }.otherwise{
        R_state:=sRcplt
      }
    }
  }
}
class top(isa_info: String = "RISCV32") extends Module {
  val io = IO(new Bundle {
    val IMem = new InstIO
    val DMem = new MemIO

    val diff = new Bundle {
      val pc   = Output(UInt(32.W))
      val dnpc = Output(UInt(32.W))
      val snpc = Output(UInt(32.W))
      val regs = Output(Vec(32, UInt(32.W)))

      val mepc    = Output(UInt(32.W))
      val mcause  = Output(UInt(32.W))
      val mstatus = Output(UInt(32.W))
      val mtvec   = Output(UInt(32.W))
    }
  })

  val core = Module(new Core)
  core.io <> this.io

  val mmio_dpi = Module(new mmio_dpi_wraper)
  val test_12  = Module(new test_12)

  mmio_dpi.io <> test_12.io
}
