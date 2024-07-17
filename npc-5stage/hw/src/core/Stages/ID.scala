package core.Stages

import chisel3._
import chisel3.util._

import core._
import core.utils._

import insts._
import insts.Inst
import chisel3.internal.throwException

class InstDecodeStage(_in: IF2IDBundle, _out: ID2EXBundle) extends PiplineStageWithoutDepth(_in, _out) {
  val regfile=new RegFile("RISCV32")
  override def build(): Unit = {
    super.build()
    Decode(this,regfile)
  }
}

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

  val instType=0.U
  val imm = 0.U
  RVIInstr.tabelWithIndex.foreach((t) => {
    val (elem,inst_index)=t
    val (bitpat -> (instType_onTable::fuType_onTable::fuOp_onTable::Nil) -> exec )=elem

    when(bitpat === inst) {
      instType := instType_onTable.U
      imm := (instType_onTable match {
        case Inst.I => immI
        case Inst.S => immS
        case Inst.B => immB
        case Inst.U => immU
        case Inst.J => immJ
        case _      => throw new IllegalArgumentException("Unkown Inst type not supported(Let us see see where goes wrong ~)")
      })
    }

  })


}
object Decoder {
  def apply(inst: UInt, pc: UInt, R: RegFile): Decoder = {
    new Decoder(inst, pc, R)
  }

}
object Decode {

  def apply(IDStage: Stage[IF2IDBundle, ID2EXBundle], R: RegFile) = {
    val IDStage_in  = IDStage.in //.asInstanceOf[HandshakeIO[IF2IDBundle]]
    val IDStage_out = IDStage.out //.asInstanceOf[HandshakeIO[ID2EXBundle]]

    val decoder = Decoder(IDStage_in.bits.inst, IDStage_in.bits.pc, R)

    IDStage_out.bits.rd   := decoder.rd
    IDStage_out.bits.src1 := decoder.src1
    IDStage_out.bits.src2 := decoder.src2
    IDStage_out.bits.imm := decoder.imm


  }
}

// class another_Decoder(val inst: UInt, val pc: UInt, R: RegFile) {
//   val rs1 = inst(19, 15)
//   val rs2 = inst(24, 20)

//   val rd   = inst(11, 7)
//   val src1 = R.read(rs1)
//   val src2 = R.read(rs2)

//   val immI = (inst(31, 20).asSInt + 0.S(32.W)).asUInt //imm = SEXT(BITS(i, 31, 20), 12);
//   val immU = ((inst(31, 12) << 12).asSInt + 0.S(32.W)).asUInt //imm = SEXT(BITS(i, 31, 12), 20) << 12;
//   val immS =
//     (((inst(31, 25).asSInt << 5.U).asUInt | inst(11, 7)).asSInt + 0.S(
//       32.W
//     )).asUInt //imm = (SEXT(BITS(i, 31, 25), 7) << 5) | BITS(i, 11, 7);
//   val immJ =
//     (((inst(31) << 19 | inst(19, 12) << 11 | inst(20) << 10 | inst(30, 21)) << 1).asSInt + 0.S(
//       32.W
//     )).asUInt //imm = SEXT((   (BITS(i, 31, 31) << 19) | BITS(i, 30, 21) | (BITS(i, 20, 20) << 10) | (BITS(i, 19, 12) << 11) ) << 1, 21);
//   val immB = (((inst(31, 31) << 12) | inst(30, 25) << 5 | (inst(11, 8) << 1) | (inst(7, 7) << 11)).asSInt + 0.S(
//     32.W
//   )).asUInt //*imm = SEXT((   (inst( 31, 31) << 12) | BITS(i, 30, 25)<<5 | (BITS(i, 11, 8) << 1) | (BITS(i, 7, 7) << 11) ) , 13);

//   val inst_type = Inst.N
//   val imm       = 0.U

//   RVIInstr.table
//   .asInstanceOf[Array[((BitPat, Any), ExecEnv => Any)]]
//     .foreach((t: ((BitPat, Any), ExecEnv => Any)) => {
//       when(t._1._1 === inst) {
//         val inst_type_onTable = t._1._2(0)
//         inst_type := inst_type_onTable
//         imm := inst_type_onTable match {
//           case Inst.I => immI
//           case Inst.S => immS
//           case Inst.B => immB
//           case Inst.U => immU
//           case Inst.J => immJ
//           case _      => throwException("Unkown Inst type not supported(Let us see see where goes wrong ~)")
//         }

//         val futype = t._2(1)
//         val uops   = t._2(2)

//         printf(p"Inst_Decode:${(t._1)}\n");
//         if (t._1._1 == RV32I_ALUInstr.ADDI) {
//           printf("ADDI\n")
//         }

//       }
//     })

// }
