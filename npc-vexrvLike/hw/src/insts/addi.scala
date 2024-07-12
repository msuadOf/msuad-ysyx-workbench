package insts
import chisel3._
import chisel3.util._

import core.Stage
object ADDI extends Stage {
  def ID(inst: UInt) = {
    // INSERT(RS1)
    // INSERT(RS2)
    // OUTPUT(RS1):=INPUT(INST)(19, 15)
    // OUTPUT(RS2):=INPUT(INST)(24, 20)
  }
  def EX() = {
    // OUTPUT(RD):=INPUT(RS1)+INPUT(RS2)
  }
}
