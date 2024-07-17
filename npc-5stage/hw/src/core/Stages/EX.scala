package core.Stages

import chisel3._
import chisel3.util._

import core._
import insts.RVIInstr

class ExecStage(_in: ID2EXBundle, _out: EX2WBBundle) extends PiplineStageWithoutDepth(_in, _out) {
  private def getExecEnv() = {
    val env = new ExecEnv
    env.src1    := in.bits.src1
    env.src2    := in.bits.src2
    env.pc      := in.bits.pc
    env.rd      := in.bits.rd
    env.inst_id := in.bits.inst_id
    env.imm     := in.bits.imm
    env
  }
  override def build(): Unit = {
    val env = getExecEnv()

    RVIInstr.tabelWithIndex.foreach((t) => {
      val (elem, inst_index)                                                              = t
      val (bitpat -> (instType_onTable :: fuType_onTable :: fuOp_onTable :: Nil) -> exec) = elem
      when(inst_index.U === env.inst_id) {
        exec(env)
        printf(s"$bitpat")
      }
    })

  }
}

class ExecEnv extends Bundle {
  val src1    = UInt(32.W)
  val src2    = UInt(32.W)
  val pc      = UInt(32.W)
  val rd      = UInt(5.W)
  val Rrd     = UInt(32.W)
  val inst_id = UInt()
  val imm     = UInt()

  def Mr(addr: UInt, width: Int) = 0.U
  def Mw(addr: UInt, width: Int, data: UInt): Unit = {}

  val csr = new csr
  def mret_impl(): Unit = {}
  def CSR_READ(idx: UInt) = 0.U
  def CSR_WRITE(idx: UInt, data: UInt): Unit = {}
}
