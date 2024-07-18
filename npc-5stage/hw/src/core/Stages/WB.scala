package core.Stages

import core.PiplineStageWithoutDepth
import core._
import core.utils.BundlePlus
import chisel3._

class WriteBackStage(_in: EX2WBBundle, regfile: RegFile, pc: PC) extends Stage(_in, new BundlePlus {}) {
  override def build(): Unit = {
    regfile.write(in.fire && in.bits.RrdEn, in.bits.rd, in.bits.Rrd)
    pc.write(in.fire && in.bits.dnpcEn, in.bits.dnpc)

  }
}
