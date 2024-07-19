package core.Stages

import chisel3._
import chisel3.util._

import core._
import insts.RVIInstr

class ExecStage(_in: ID2EXBundle, _out: EX2WBBundle) extends PiplineStageWithoutDepth(_in, _out) {

  override def build(): Unit = {
    super.build()
    val env = ExecEnv(in.bits, out.bits)

    RVIInstr.tabelWithIndex.foreach((t) => {
      val (elem, inst_index)                                                              = t
      val (bitpat -> (instType_onTable :: fuType_onTable :: fuOp_onTable :: Nil) -> exec) = elem
      when(inst_index.U === env.inst_id) {
        exec(env)
      }
    })
    out.bits.pc:=in.bits.pc
    out.bits.rd:=in.bits.rd
  }
}

case class ExecEnv(in: ID2EXBundle, out: EX2WBBundle) extends Bundle {
  val src1    = in.src1
  val src2    = in.src2
  val pc      = in.pc
  val rd      = in.rd
  val inst_id = in.inst_id
  val imm     = in.imm
  val dnpc    = out.dnpc
  val Rrd     = out.Rrd
  val RrdEn   = out.RrdEn
  val dnpcEn  = out.dnpcEn
  val ebreak  = out.ebreak

  def REG_WRITE(reg: UInt, data: UInt): Unit = {
    reg match {
      case Rrd => {    Rrd   := data; RrdEn := true.B}
      case `dnpc` => {dnpc   := data; dnpcEn := true.B}
      case _   => {println("need to impl REG_WRITE"); require(false,"need to impl REG_WRITE")}
    }

  }
  def REG_READ(reg: UInt) = require(false,"need to impl REG_READ")

  def Mr(addr: UInt, width: Int) = 0.U
  def Mw(addr: UInt, width: Int, data: UInt): Unit = {}

  val csr = new csr
  def mret_impl(): Unit = {}
  def CSR_READ(idx: UInt) = 0.U
  def CSR_WRITE(idx: UInt, data: UInt): Unit = {}
  def EBREAK(): Unit = ebreak := 1.B
}
