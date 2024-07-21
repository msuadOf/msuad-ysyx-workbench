package core

import chisel3._
import chisel3.util._

class ScoreBoard {
  val regfileBusy = RegInit(VecInit(Seq.tabulate(32)(i => 0.B)))
  def readRegfileBusy(rd: UInt): Bool = Mux(rd===0.U,0.B,regfileBusy(rd))

  def id_record(rd: UInt, rd_en: Bool): Unit = {
    when(rd_en === 1.B) {
      regfileBusy(rd) := 1.U
    }
  }
  def id_judgeRAW(rs1: UInt, rs1_en: Bool, rs2: UInt, rs2_en: Bool)() = (rs1_en && readRegfileBusy(rs1) )|| (rs2_en && readRegfileBusy(rs2))
  def wb_record(rd: UInt, rd_en: Bool): Unit = {
    when(rd_en === 1.B) {
      regfileBusy(rd) := 0.U
    }
  }
}
