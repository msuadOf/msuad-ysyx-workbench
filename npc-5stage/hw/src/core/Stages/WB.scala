package core.Stages

import core.PiplineStageWithoutDepth
import core._
import core.utils.BundlePlus
import chisel3._

class WriteBackStage(_in: EX2WBBundle, regfile: RegFile, pc: PC, diff: diffIO) extends Stage(_in, new BundlePlus {}) {
  def this(_in: EX2WBBundle, regfile: RegFile, pc: PC) = {
    this(_in, regfile, pc, null)
  }
  override def build(): Unit = {
    in.ready := 1.B
    val ebreak = Module(new Ebreak)
    ebreak(in.fire && in.bits.ebreak)
    regfile.write(in.fire && in.bits.RrdEn, in.bits.rd, in.bits.Rrd)
    pc.write(in.fire && in.bits.dnpcEn, in.bits.dnpc)

    //if (diff != null) {
        // diff.regs    := regfile.reg
        (diff.regs zip  regfile.reg).foreach(x => x._1 := x._2)
        diff.pc      := RegNext(Mux (in.bits.dnpcEn, in.bits.dnpc,in.bits.pc+4.U))
        // diff.dnpc    := in.bits.dnpc
      
        diff.diff_en := RegNext(in.fire,0.B)
      
    //}
  }
}
