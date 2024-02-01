import chisel3._
import chisel3.util._

class ExecEnv(val inst:UInt,val pc:UInt,val R:RegFile,val DMem:MemIO) {
  //val rs1, rs2, rd, src1, src2, imm ,Rrd = Wire(UInt())
  val rs1  = inst(19, 15)
  val rs2  = inst(24, 20)

  val rd   = inst(11, 7)
  val src1 = R(rs1)
  val src2 = R(rs2) 
  val Rrd  = R(rd)

  val immI  = inst(31, 20).asSInt
  val immU  = inst(31, 12)<<12.U
  val immS  = inst(31, 20)
  val immJ  = inst(31, 20)
  val immB  = inst(31, 20)
}