package core

import chisel3._
import chisel3.util._

import core._
import core.utils._

class IF2IDBundle extends BundlePlus {
  val inst = Output(UInt(32.W))
  val pc   = Output(UInt(32.W))
  def IOinit[T <: Data](value: T): Unit = {
    inst := value
    pc   := value
  }
  def Flipped_IOinit[T <: Data](value: T): Unit = {}

  def do_=>>[T <: BundlePlus](enable: Bool)(that: T): Unit = {
    val that_IF2IDBundle = that.asInstanceOf[IF2IDBundle]

    that_IF2IDBundle.pc   := RegEnable(this.pc, 0.U, enable)
    that_IF2IDBundle.inst := RegEnable(this.inst, 0.U, enable)
  }

}

class another_ID2EXBundle extends BundlePlus {
  val src1 = Output(UInt(32.W))
  val src2 = Output(UInt(32.W))
  val rd   = Output(UInt(5.W))

  val imm    = Output(UInt())
  val uops   = Output(UInt())
  val futype = Output(UInt())
  def IOinit[T <: Data](value: T): Unit = {}
  def Flipped_IOinit[T <: Data](value: T): Unit = {}
  def do_=>>[T <: BundlePlus](enable: Bool)(that: T): Unit = {}
}
class ID2EXBundle extends BundlePlus {
  val src1 = Output(UInt(32.W))
  val src2 = Output(UInt(32.W))
  val rd   = Output(UInt(5.W))
  val imm = Output(UInt())
  val inst_id= Output(UInt())
  def IOinit[T <: Data](value: T): Unit = {
    src1 := value
    src2 := value
    rd   := value
    imm := value
    inst_id:= value

  }
  def Flipped_IOinit[T <: Data](value: T): Unit = {}
  def do_=>>[T <: BundlePlus](enable: Bool)(that: T): Unit = {}
}
class EX2WBBundle extends BundlePlus {
  val rd  = Output(UInt(5.W))
  val Rrd = Output(UInt(32.W))
  def IOinit[T <: Data](value: T): Unit = {
    rd  := value
    Rrd := value
  }
  def Flipped_IOinit[T <: Data](value: T): Unit = {}
  def do_=>>[T <: BundlePlus](enable: Bool)(that: T): Unit = {}
}
