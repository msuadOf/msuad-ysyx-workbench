package core

import chisel3._
import chisel3.util._

class ExecEnv extends Bundle {
  val src1 = UInt(32.W)
  val src2 = UInt(32.W)
  val pc   = UInt(32.W)
  val rd   = UInt(5.W)
  val Rrd  = UInt(32.W)
  val immI = UInt()
  val immS = UInt()
  val immB = UInt()
  val immU = UInt()
  val immJ = UInt()
  def Mr(addr: UInt, width: Int) = 0.U
  def Mw(addr: UInt, width: Int, data: UInt): Unit = {}

  val csr = new csr
  def mret_impl(): Unit = {}
  def CSR_READ(idx: UInt) = 0.U
  def CSR_WRITE(idx: UInt, data: UInt): Unit = {}
}
