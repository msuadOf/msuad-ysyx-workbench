package core.Cache

import chisel3._
import chisel3.util._

import core._
import core.utils._
import core.AXI4._

class ICacheIO(cacheparam: CacheParameters, axi4param: AXI4Parameters) extends BundlePlus {
  val req = new BundlePlus {
    val ar = Handshake(new BundlePlus {
      val addr = (UInt(cacheparam.addrBits.W))
    })
    val r = Flipped(Handshake(new BundlePlus {
      val data = (UInt(cacheparam.cpuWordWidth.W))
    }))
  }
  val bus = AXI4Bundle(CPUAXI4BundleParameters())
}
class ICache(cacheparam: CacheParameters, axi4param: AXI4Parameters) extends Module {
  val io = IO(new ICacheIO(cacheparam, axi4param))

  val req = io.req
  val bus = io.bus
  req.ar.ready      := bus.ar.ready
  bus.ar.valid      := req.ar.valid
  bus.ar.bits.addr  := req.ar.bits.addr
  bus.ar.bits.len   := 0.U // 0+1=1次传输
  bus.ar.bits.size  := log2Ceil(64).U //log2(64) = 6 = 0b110
  bus.ar.bits.burst := AXI4Parameters.BURST_FIXED
  bus.ar.bits.id    := 0.U //?

  req.r.valid     := bus.r.valid
  bus.r.ready     := req.r.ready
  req.r.bits.data := bus.r.bits.data
  /*     bus.r.bits.id
    bus.r.bits.resp
    bus.r.bits.last  */
    
/*   val axi4=AXI4Controller()
  axi4.read(bus)
  axi4.write(bus) */
}
