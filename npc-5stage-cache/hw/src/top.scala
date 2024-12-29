package core
import chisel3._
import chisel3.util._

import chisel3.util.experimental._
import chisel3.experimental.prefix

import core._
import core.utils._

import core.AXI4._

class diffIO extends BundlePlus with core.utils.OverrideIOinit {
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

  override def IOinit[T <: Data](value: T): Unit = {
    diff_en        := value
    DMemInst       := value
    pc             := value
    dnpc           := value
    snpc           := value
    regs.foreach(_ := 0.U)
    mepc           := value
    mcause         := value
    mstatus        := value
    mtvec          := value
  }
  override def Flipped_IOinit[T <: Data](value: T): Unit = {}
}
class top(isa_info: String = "RISCV32") extends Module {
  val io = IO(new Bundle {
    // val IMem = new InstIO
    // val DMem = new MemIO
    val piplinetrace = new MonitorIO
    val diff         = new diffIO

    val interrupt = Input(Bool())
    val master    = AXI4Bundle(CPUAXI4BundleParameters())
    val slave     = Flipped(AXI4Bundle(CPUAXI4BundleParameters()))
  })
  io.diff.IOinit()
  val core = Module(new Core)
  core.io.monitor <> io.piplinetrace
  core.io.diff <> io.diff

  io.master.IOinit()
  io.slave.IOinit()

  val cacheparam=CacheParameters( addrBits=32,cpuWordWidth=32, cachelineBytes=8, setNum=32,wayNum=2)
  cacheparam.printReport()


}

object CPUAXI4BundleParameters {
  def apply() :AXI4BundleParameters=Config.core.toAXI4BundleParameters
}
class ysyx_23060093 extends Module {
  val io = IO(new Bundle {
    val interrupt = Input(Bool())
    val master    = AXI4Bundle(CPUAXI4BundleParameters())
    val slave     = Flipped(AXI4Bundle(CPUAXI4BundleParameters()))
    val sram= Flipped(new Cache.SRAMIO(addrWidth=32,dataWidth=32))
  })

    val sram_wrapper = Module(new Cache.SRAMWrapper(addrWidth=32,dataWidth=32))
  sram_wrapper.io.Flipped_IOinit(0.U)
  sram_wrapper.io<>io.sram

  val top = Module(new top)
  io.master.IOinit()
  io.slave.IOinit()

  io.master <> top.io.master
  io.slave <> top.io.slave
  top.io.interrupt := io.interrupt

}
