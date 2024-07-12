package core

import chisel3._
import chisel3.util._

import insts._
import insts.Inst.U

class Decoder(val inst: UInt, val pc: UInt, R: RegFile) {
  val rs1 = inst(19, 15)
  val rs2 = inst(24, 20)

  val rd   = inst(11, 7)
  val src1 = R.read(rs1)
  val src2 = R.read(rs2)

  val immI = (inst(31, 20).asSInt + 0.S(32.W)).asUInt //imm = SEXT(BITS(i, 31, 20), 12);
  val immU = ((inst(31, 12) << 12).asSInt + 0.S(32.W)).asUInt //imm = SEXT(BITS(i, 31, 12), 20) << 12;
  val immS =
    (((inst(31, 25).asSInt << 5.U).asUInt | inst(11, 7)).asSInt + 0.S(
      32.W
    )).asUInt //imm = (SEXT(BITS(i, 31, 25), 7) << 5) | BITS(i, 11, 7);
  val immJ =
    (((inst(31) << 19 | inst(19, 12) << 11 | inst(20) << 10 | inst(30, 21)) << 1).asSInt + 0.S(
      32.W
    )).asUInt //imm = SEXT((   (BITS(i, 31, 31) << 19) | BITS(i, 30, 21) | (BITS(i, 20, 20) << 10) | (BITS(i, 19, 12) << 11) ) << 1, 21);
  val immB = (((inst(31, 31) << 12) | inst(30, 25) << 5 | (inst(11, 8) << 1) | (inst(7, 7) << 11)).asSInt + 0.S(
    32.W
  )).asUInt //*imm = SEXT((   (inst( 31, 31) << 12) | BITS(i, 30, 25)<<5 | (BITS(i, 11, 8) << 1) | (BITS(i, 7, 7) << 11) ) , 13);

  RVIInstr.table
    .foreach((t: ((BitPat, Any), ExecEnv => Any)) => {
      when(t._1._1 === inst) {
        
        if (t._1._1 == RV32I_ALUInstr.ADDI) {
          printf("ADDI\n")
        }
        printf(p"Inst_Decode:${(t._1)}\n");
      }
    })

}
object Decoder {
  def apply(inst: UInt, pc: UInt, R: RegFile): Decoder = {
    new Decoder(inst, pc, R)
  }

}
object Decode {
  def apply(inst: UInt, pc: UInt, R: RegFile) = {
    val decoder =new Decoder(inst, pc, R)

    val rs1  = decoder.rs1
    val rs2  = decoder.rs2
    val rd   = decoder.rd
    val src1 = decoder.src1
    val src2 = decoder.src2

    val imm    = decoder.imm
    val alu_op = decoder.alu_op
    val lsu_op = decoder.lsu_op

  }
  def apply(IDStage:Stage)={
        val decoder = Decoder(inst, pc, R)

    val rs1  = decoder.rs1
    val rs2  = decoder.rs2
    val rd   = decoder.rd
    val src1 = decoder.src1
    val src2 = decoder.src2

    val imm    = decoder.imm
    val alu_op = decoder.alu_op
    val lsu_op = decoder.lsu_op


    IDStage.bits.rd   := rd
    IDStage.bits.src1 := src1
    IDStage.bits.src2 := src2

    IDStage.bits.imm    := imm
    IDStage.bits.alu_op := alu_op
    IDStage.bits.lsu_op := lsu_op
  }
}
