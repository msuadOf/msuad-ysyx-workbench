package core

import chisel3._
import chisel3.util._

import core._
import core.utils._

class IF2IDBundle extends BundleWithIOInit {
  val inst = Output(UInt(32.W))
  val pc   = Output(UInt(32.W))
  def IOinit[T <: Data](value: T): Unit = {
    inst := value
    pc   := value
  }
  def Flipped_IOinit[T <: Data](value: T): Unit = {}
  def getRegEnable(t:IF2IDBundle,enable:Bool): Bundle = new Bundle{
    val pc = RegEnable(t.pc,enable)
    val inst = RegEnable(t.inst,enable)
  }
}

class another_ID2EXBundle extends BundleWithIOInit {
  val src1 = Output(UInt(32.W))
  val src2 = Output(UInt(32.W))
  val rd   = Output(UInt(5.W))

  val imm    = Output(UInt())
  val uops   = Output(UInt())
  val futype = Output(UInt())
  def IOinit[T <: Data](value: T): Unit = {}
  def Flipped_IOinit[T <: Data](value: T): Unit = {}
}
class ID2EXBundle extends BundleWithIOInit {
  val src1 = Output(UInt(32.W))
  val src2 = Output(UInt(32.W))
  val rd   = Output(UInt(5.W))
  val immI = Output(UInt())
  val immS = Output(UInt())
  val immB = Output(UInt())
  val immU = Output(UInt())
  val immJ = Output(UInt())

  def IOinit[T <: Data](value: T): Unit = {
    src1 := value
    src2 := value
    rd   := value
    immI := value
    immS := value
    immB := value
    immU := value
    immJ := value
  }
  def Flipped_IOinit[T <: Data](value: T): Unit = {}
}
class EX2WBBundle extends BundleWithIOInit {
  val rd  = Output(UInt(5.W))
  val Rrd = Output(UInt(32.W))
  def IOinit[T <: Data](value: T): Unit = {
    rd  := value
    Rrd := value
  }
  def Flipped_IOinit[T <: Data](value: T): Unit = {}
}
