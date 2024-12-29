package core.Cache

import chisel3._
import chisel3.util._

import core._
import core.utils._

class SRAMRIO(addrWidth: Int, dataWidth: Int) extends BundlePlus {
  val addr   = Output(UInt(addrWidth.W))
  val en = Output(Bool())
  val data   = Input(UInt(dataWidth.W))

}
class SRAMWIO(addrWidth: Int, dataWidth: Int) extends BundlePlus {
  val addr = Output(UInt(addrWidth.W))
  val data = Output(UInt(dataWidth.W))
  val en   = Output(Bool())
}
class SRAMIO(addrWidth: Int, dataWidth: Int) extends BundlePlus {
  val r = new SRAMRIO(addrWidth, dataWidth)
  val w = new SRAMWIO(addrWidth, dataWidth)
}
class SRAMValidReadyIO(addrWidth: Int, dataWidth: Int) extends BundlePlus {
  val r=Handshake(new SRAMRIO(addrWidth, dataWidth))
  val w=Handshake(new SRAMWIO(addrWidth, dataWidth))
}
class SRAMValidReadyWrapper(addrWidth: Int, dataWidth: Int) extends Module {
  val io = IO(Flipped(new SRAMValidReadyIO(addrWidth, dataWidth)))

}
class SRAMWrapper(addrWidth: Int, dataWidth: Int) extends Module {
  val io = IO(Flipped(new SRAMIO(addrWidth, dataWidth)))
    /* val sram_module = Module(new SRAM(addrWidth, dataWidth))
    io<>sram_module.io */
    val sram_module = Module(new ModuleWithSRAM(addrWidth, dataWidth))
    sram_module.io.readPorts(0).address := io.r.addr
    sram_module.io.readPorts(0).enable := io.r.en
    io.r.data:= sram_module.io.readPorts(0).data
    sram_module.io.writePorts(0).address := io.w.addr
    sram_module.io.writePorts(0).data := io.w.data
    sram_module.io.writePorts(0).enable := io.w.en
}
class SRAM(addrWidth: Int, dataWidth: Int) extends Module {
  val io = IO(Flipped(new SRAMIO(addrWidth, dataWidth)))
  val mem = SyncReadMem(1 << addrWidth, UInt(dataWidth.W))
  when(io.w.en) {
    mem.write(io.w.addr, io.w.data)
  }
  io.r.data := mem.read(io.r.addr, io.r.en)

}

class ModuleWithSRAM(addrWidth: Int, dataWidth: Int) extends Module {
  val io = IO(new SRAMInterface(1024, UInt(dataWidth.W), 1, 1, 0))

  // Generate a SyncReadMem representing an SRAM with an explicit number of read, write, and read-write ports
  io :<>= SRAM(BigInt(1) << addrWidth, UInt(dataWidth.W), 1, 1, 0)
}
