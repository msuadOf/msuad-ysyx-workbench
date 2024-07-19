package core

import chisel3._
import chisel3.util._

import core._
import core.utils._

class IF2IDBundle extends BundlePlus {
  val inst = Output(UInt(32.W))
  val pc   = Output(UInt(32.W))
  def toList: List[UInt] = {
    pc :: inst :: Nil
  }
  def IOIIInit[T <: Data](value: T): Unit = {
    inst := value
    pc   := value
  }
  def Flipped_IOIIInit[T <: Data](value: T): Unit = {}

  def _do_NOTUSED_=>>[T <: BundlePlus](enable: Bool)(that: T): Unit = {
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
  def toList: List[UInt] = {
    src1 :: src2 :: rd :: imm :: uops :: futype :: Nil
  }
  def IOIIInit[T <: Data](value: T): Unit = {}
  def Flipped_IOIIInit[T <: Data](value: T): Unit = {}
  def _do_NOTUSED_=>>[T <: BundlePlus](enable: Bool)(that: T): Unit = {}
}
class ID2EXBundle extends BundlePlus {
  val pc      = Output(UInt(32.W))
  val src1    = Output(UInt(32.W))
  val rs1_en = Output(Bool())
  val src2    = Output(UInt(32.W))
  val rs2_en = Output(Bool())
  val rd      = Output(UInt(5.W))
  val rd_en   = Output(Bool())
  val imm     = Output(UInt())
  val imm_en = Output(Bool())
  val inst_id = Output(UInt())
  def toList: List[UInt] = {
    pc :: src1 :: src2 :: rd :: imm :: inst_id :: Nil
  }
  def IOIIInit[T <: Data](value: T): Unit = {
    pc      := value
    src1    := value
    src2    := value
    rd      := value
    imm     := value
    inst_id := value

  }
  def Flipped_IOIIInit[T <: Data](value: T): Unit = {}
  def _do_NOTUSED_=>>[T <: BundlePlus](enable: Bool)(that: T): Unit = {
    val that_ = that.asInstanceOf[ID2EXBundle]

    val this_wirelist = this.toList
    val that_wirelist = that_.toList
    (this_wirelist.zip(that_wirelist)).foreach {
      case (thiswire, thatwire) => {
        thatwire := RegEnable(thiswire, 0.U, enable)
      }
    }
  }
}
class EX2WBBundle extends BundlePlus {
  val pc     = Output(UInt(32.W))
  val dnpc   = Output(UInt(32.W))
  val dnpcEn = Output(Bool())
  val rd     = Output(UInt(5.W))
  val Rrd    = Output(UInt(32.W))
  val RrdEn  = Output(Bool())
  val ebreak  = Output(Bool())
  def IOIIInit[T <: Data](value: T): Unit = {
    rd  := value
    Rrd := value
  }
  def Flipped_IOIIInit[T <: Data](value: T): Unit = {}
  def _do_NOTUSED_=>>[T <: BundlePlus](enable: Bool)(that: T): Unit = {}
}
