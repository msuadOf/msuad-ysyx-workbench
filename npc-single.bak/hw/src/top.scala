import chisel3._
import chisel3.util._

import chisel3.util.experimental._
import chisel3.experimental.prefix


class top(isa_info: String = "RISCV32") extends Module {
  val io = IO(new Bundle {
    val IMem = new Bundle {
      val rAddr = Output(UInt(32.W))
      val rData = Input(UInt(32.W))
    }

    val DMem = new MemIO

    val diff = new Bundle {
      val pc   = Output(UInt(32.W))
      val dnpc = Output(UInt(32.W))
      val snpc = Output(UInt(32.W))
      val regs = Output(Vec(32, UInt(32.W)))

      val mepc =    Output(UInt(32.W))
      val mcause =  Output(UInt(32.W))  
      val mstatus = Output(UInt(32.W))   
      val mtvec =   Output(UInt(32.W)) 
    }
  })

  val core=Module(new Core)
  core.io<>this.io
}