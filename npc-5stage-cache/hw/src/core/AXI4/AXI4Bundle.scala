package core.AXI4

import chisel3._
import chisel3.util._
import core.utils._
import core.IrrevocableNew

case class AXI4BundleParameters(
  addrBits: Int,
  dataBits: Int,
  idBits:   Int) {
  require(dataBits >= 8, s"AXI4 data bits must be >= 8 (got $dataBits)")
  require(addrBits >= 1, s"AXI4 addr bits must be >= 1 (got $addrBits)")
  require(idBits >= 1, s"AXI4 id bits must be >= 1 (got $idBits)")
  require(isPow2(dataBits), s"AXI4 data bits must be pow2 (got $dataBits)")

  val lenBits   = AXI4Parameters.lenBits
  val sizeBits  = AXI4Parameters.sizeBits
  val burstBits = AXI4Parameters.burstBits
  val lockBits  = 0 //AXI4Parameters.lockBits
  val cacheBits = 0 //AXI4Parameters.cacheBits
  val protBits  = 0 //AXI4Parameters.protBits
  val qosBits   = 0 //AXI4Parameters.qosBits
  val respBits  = AXI4Parameters.respBits

  def union(x: AXI4BundleParameters) = {}

}

object AXI4BundleParameters {
  val emptyBundleParams = AXI4BundleParameters(addrBits = 1, dataBits = 8, idBits = 1)
  //def union(x: Seq[AXI4BundleParameters]) = x.foldLeft(emptyBundleParams)((x,y) => x.union(y))

  // def apply(master: AXI4MasterPortParameters, slave: AXI4SlavePortParameters) = {}
}

abstract class AXI4BundleBase(val params: AXI4BundleParameters) extends BundlePlus

/**
  * Common signals of AW and AR channels of AXI4 protocol
  */
abstract class AXI4BundleA(params: AXI4BundleParameters) extends AXI4BundleBase(params) {
  val id    = UInt(params.idBits.W)
  val addr  = UInt(params.addrBits.W)
  val len   = UInt(params.lenBits.W) // number of beats - 1
  val size  = UInt(params.sizeBits.W) // bytes in beat = 2^size
  val burst = UInt(params.burstBits.W)
  val lock  = UInt(params.lockBits.W)
  val cache = UInt(params.cacheBits.W)
  val prot  = UInt(params.protBits.W)
  val qos   = UInt(params.qosBits.W) // 0=no QoS, bigger = higher priority
  // val user   = BundleMap(params.requestFields.filter(_.key.isControl))
  // val echo   = BundleMap(params.echoFields)
  // val region = UInt(4.W) // optional

  // Number of bytes-1 in this operation
  def bytes1(x: Int = 0) = {
    val maxShift = 1 << params.sizeBits
    val tail     = ((BigInt(1) << maxShift) - 1).U
    (Cat(len, tail) << size) >> maxShift
  }
}

/**
  * A non-standard bundle that can be both AR and AW
  */
class AXI4BundleARW(params: AXI4BundleParameters) extends AXI4BundleA(params) {
  val wen = Bool()
}

/**
  * AW channel of AXI4 protocol
  */
class AXI4BundleAW(params: AXI4BundleParameters) extends AXI4BundleA(params)

/**
  * AR channel of AXI4 protocol
  */
class AXI4BundleAR(params: AXI4BundleParameters) extends AXI4BundleA(params)

/**
  * W channel of AXI4 protocol
  */
class AXI4BundleW(params: AXI4BundleParameters) extends AXI4BundleBase(params) {
  // id ... removed in AXI4
  val data = UInt(params.dataBits.W)
  val strb = UInt((params.dataBits / 8).W)
  val last = Bool()
  // val user = BundleMap(params.requestFields.filter(_.key.isData))
}

/**
  * R channel of AXI4 protocol
  */
class AXI4BundleR(params: AXI4BundleParameters) extends AXI4BundleBase(params) {
  val id   = UInt(params.idBits.W)
  val data = UInt(params.dataBits.W)
  val resp = UInt(params.respBits.W)
  // val user = BundleMap(params.responseFields) // control and data
  // val echo = BundleMap(params.echoFields)
  val last = Bool()
}

/**
  * B channel of AXI4 protocol
  */
class AXI4BundleB(params: AXI4BundleParameters) extends AXI4BundleBase(params) {
  val id   = UInt(params.idBits.W)
  val resp = UInt(params.respBits.W)
  // val user = BundleMap(params.responseFields.filter(_.key.isControl))
  // val echo = BundleMap(params.echoFields)
}
class AXI4Bundle(params: AXI4BundleParameters) extends AXI4BundleBase(params) {
  val aw = IrrevocableNew(new AXI4BundleAW(params))
  val w  = IrrevocableNew(new AXI4BundleW(params))
  val b  = Flipped(IrrevocableNew(new AXI4BundleB(params)))
  val ar = IrrevocableNew(new AXI4BundleAR(params))
  val r  = Flipped(IrrevocableNew(new AXI4BundleR(params)))
}

object AXI4Bundle {
  def apply(params: AXI4BundleParameters) = new AXI4Bundle(params)
}