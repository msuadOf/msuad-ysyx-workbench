package insts
import chisel3._
import chisel3.util._

import core.Stages.ExecEnv
object Inst {
  val N = "b0000"
  val I = "b0100"
  val R = "b0101"
  val S = "b0010"
  val B = "b0001"
  val U = "b0110"
  val J = "b0111"
}

object FuType {
  def num     = 5
  def alu     = "b000"
  def lsu     = "b001"
  def mdu     = "b010"
  def csr     = "b011"
  def mou     = "b100"
  def bru     = "b101"
  def apply() = UInt(log2Up(num).W)
}

object ALUOpType {
  def add  = "b1000000"
  def sll  = "b0000001"
  def slt  = "b0000010"
  def sltu = "b0000011"
  def xor  = "b0000100"
  def srl  = "b0000101"
  def or   = "b0000110"
  def and  = "b0000111"
  def sub  = "b0001000"
  def sra  = "b0001101"

  def addw = "b1100000"
  def subw = "b0101000"
  def sllw = "b0100001"
  def srlw = "b0100101"
  def sraw = "b0101101"

  def jal  = "b1011000"
  def jalr = "b1011010"
  def beq  = "b0010000"
  def bne  = "b0010001"
  def blt  = "b0010100"
  def bge  = "b0010101"
  def bltu = "b0010110"
  def bgeu = "b0010111"

}
object LSUOpType { //TODO: refactor LSU fuop
  def lb  = "b0000000"
  def lh  = "b0000001"
  def lw  = "b0000010"
  def ld  = "b0000011"
  def lbu = "b0000100"
  def lhu = "b0000101"
  def lwu = "b0000110"
  def sb  = "b0001000"
  def sh  = "b0001001"
  def sw  = "b0001010"
  def sd  = "b0001011"

  def lr      = "b0100000"
  def sc      = "b0100001"
  def amoswap = "b0100010"
  def amoadd  = "b1100011"
  def amoxor  = "b0100100"
  def amoand  = "b0100101"
  def amoor   = "b0100110"
  def amomin  = "b0110111"
  def amomax  = "b0110000"
  def amominu = "b0110001"
  def amomaxu = "b0110010"

}

object CSROpType {
  def jmp  = "b000"
  def wrt  = "b001"
  def set  = "b010"
  def clr  = "b011"
  def wrti = "b101"
  def seti = "b110"
  def clri = "b111"
}
object MOUOpType {
  def fence      = "b00"
  def fencei     = "b01"
  def sfence_vma = "b10"
}
object ALUExec {
  //def ADDI = (e: ExecEnv) => e.REG_WRITE(e.Rrd , (e.src1.asSInt + e.imm).asUInt
  def ADDI = (e: ExecEnv) => {
    e.REG_WRITE(e.Rrd, e.src1 + e.imm); printf("[ADDI]:ADDR=%x,src1=%x,imm=%x}\n", e.src1 + e.imm, e.src1, e.imm)
  }
  def SLLI  = (e: ExecEnv) => e.REG_WRITE(e.Rrd, e.src1 << e.imm(5, 0))
  def SLTI  = (e: ExecEnv) => e.REG_WRITE(e.Rrd, e.src1.asSInt < e.imm.asSInt)
  def SLTIU = (e: ExecEnv) => e.REG_WRITE(e.Rrd, e.src1 < e.imm)
  def XORI  = (e: ExecEnv) => e.REG_WRITE(e.Rrd, e.src1 ^ e.imm)
  def SRLI  = (e: ExecEnv) => e.REG_WRITE(e.Rrd, e.src1 >> e.imm(5, 0))
  def ORI   = (e: ExecEnv) => e.REG_WRITE(e.Rrd, e.src1 | e.imm)
  def ANDI  = (e: ExecEnv) => e.REG_WRITE(e.Rrd, e.src1 & e.imm)
  def SRAI  = (e: ExecEnv) => e.REG_WRITE(e.Rrd, (e.src1.asSInt >> e.imm(5, 0).asUInt).asUInt(31, 0))

  def ADD  = (e: ExecEnv) => e.REG_WRITE(e.Rrd, e.src1 + e.src2)
  def SLL  = (e: ExecEnv) => e.REG_WRITE(e.Rrd, e.src1 << e.src2(4, 0))
  def SLT  = (e: ExecEnv) => e.REG_WRITE(e.Rrd, e.src1.asSInt < e.src2.asSInt) //.asUInt
  def SLTU = (e: ExecEnv) => e.REG_WRITE(e.Rrd, e.src1 < e.src2)
  def XOR  = (e: ExecEnv) => e.REG_WRITE(e.Rrd, e.src1 ^ e.src2)
  def SRL  = (e: ExecEnv) => e.REG_WRITE(e.Rrd, e.src1 >> e.src2(4, 0))
  def OR   = (e: ExecEnv) => e.REG_WRITE(e.Rrd, e.src1 | e.src2)
  def AND  = (e: ExecEnv) => e.REG_WRITE(e.Rrd, e.src1 & e.src2)
  def SUB  = (e: ExecEnv) => e.REG_WRITE(e.Rrd, e.src1 - e.src2)
  def SRA  = (e: ExecEnv) => e.REG_WRITE(e.Rrd, (e.src1.asSInt >> e.src2.asUInt(4, 0)).asUInt(31, 0))

  def AUIPC = (e: ExecEnv) => e.REG_WRITE(e.Rrd, e.pc + e.imm)
  def LUI   = (e: ExecEnv) => e.REG_WRITE(e.Rrd, e.imm)
}

object BRUExec {
  //def ADDI = (e: ExecEnv) => e.REG_WRITE(e.Rrd , (e.src1.asSInt + e.imm).asUInt)
  def JAL  = (e: ExecEnv) => { e.REG_WRITE(e.Rrd, e.pc + 4.U); e.REG_WRITE(e.dnpc, e.pc + e.imm); }
  def JALR = (e: ExecEnv) => { e.REG_WRITE(e.dnpc, (e.src1 + e.imm) & (-1.S(32.W)).asUInt); e.REG_WRITE(e.Rrd, e.pc + 4.U) }
  //s->dnpc = (src1 + imm) & ~(word_t)1; R(rd)= s->pc + 4 );
  def BEQ  = (e: ExecEnv) => when(e.src1 === e.src2) { e.REG_WRITE(e.dnpc, e.pc + e.imm) }
  def BNE  = (e: ExecEnv) => when(e.src1 =/= e.src2) { e.REG_WRITE(e.dnpc, e.pc + e.imm) }
  def BLT  = (e: ExecEnv) => when(e.src1.asSInt < e.src2.asSInt) { e.REG_WRITE(e.dnpc, e.pc + e.imm) }
  def BGE  = (e: ExecEnv) => when(e.src1.asSInt >= e.src2.asSInt) { e.REG_WRITE(e.dnpc, e.pc + e.imm) }
  def BLTU = (e: ExecEnv) => when(e.src1 < e.src2) { e.REG_WRITE(e.dnpc, e.pc + e.imm) }
  def BGEU = (e: ExecEnv) => when(e.src1 >= e.src2) { e.REG_WRITE(e.dnpc, e.pc + e.imm) }

}
object LSUExec {
  //def ADDI = (e: ExecEnv) => e.REG_WRITE(e.Rrd , (e.src1.asSInt + e.imm).asUInt) )
  def LB  = (e: ExecEnv) => e.REG_WRITE(e.Rrd, (e.Mr(e.src1 + e.imm, 4).asUInt(8 - 1, 0).asSInt + 0.S(32.W)).asUInt)
  def LH  = (e: ExecEnv) => e.REG_WRITE(e.Rrd, (e.Mr(e.src1 + e.imm, 4).asUInt(16 - 1, 0).asSInt + 0.S(32.W)).asUInt)
  def LW  = (e: ExecEnv) => e.REG_WRITE(e.Rrd, e.Mr(e.src1 + e.imm, 4))
  def LBU = (e: ExecEnv) => e.REG_WRITE(e.Rrd, e.Mr(e.src1 + e.imm, 1))
  def LHU = (e: ExecEnv) => e.REG_WRITE(e.Rrd, e.Mr(e.src1 + e.imm, 2))
  def SB = (e: ExecEnv) => {
    e.Mw(e.src1 + e.imm, 1, e.src2); /* printf("[SB]:ADDR=%x,src1=%x,imm=%x}\n",e.src1 + e.imm,e.src1 ,e.imm) */
  }
  def SH = (e: ExecEnv) => e.Mw(e.src1 + e.imm, 2, e.src2)
  def SW = (e: ExecEnv) => e.Mw(e.src1 + e.imm, 4, e.src2)

}
object ZicsrExec {
  def CSRRW  = (e: ExecEnv) => { e.REG_WRITE(e.Rrd, e.CSR_READ(e.imm)); e.CSR_WRITE(e.imm, e.src1) }
  def CSRRS  = (e: ExecEnv) => { e.REG_WRITE(e.Rrd, e.CSR_READ(e.imm)); e.CSR_WRITE(e.imm, e.CSR_READ(e.imm) | e.src1) }
  def CSRRC  = (e: ExecEnv) => assert(0.B, "The inst has not been impleted\n")
  def CSRRWI = (e: ExecEnv) => assert(0.B, "The inst has not been impleted\n")
  def CSRRSI = (e: ExecEnv) => assert(0.B, "The inst has not been impleted\n")
  def CSRRCI = (e: ExecEnv) => assert(0.B, "The inst has not been impleted\n")
}

object PriviledgedExec {
  def SRET       = (e: ExecEnv) => assert(0.B, "The inst has not been impleted\n")
  def SFANCE_VMA = (e: ExecEnv) => assert(0.B, "The inst has not been impleted\n")

  def ECALL = (e: ExecEnv) => {
    e.csr.mcause := 0xb.U; e.csr.mepc := e.pc; e.REG_WRITE(e.dnpc, e.csr.mtvec.read());
    e.csr.mstatus := ((((e.csr.mstatus.read() & (~(1.U << 7))) | ((e.csr.mstatus
      .read() & (1.U << 3)) << 4)) & (~(1.U << 3)))) | ((1.U << 11) + (1.U << 12));
  }
  def EBREAK = (e: ExecEnv) => e.REG_WRITE(e.dnpc, e.pc + 4.U) //stop npc
  def MRET = (e: ExecEnv) =>
    e.mret_impl() //{e.REG_WRITE(e.dnpc , e.csr.mepc.read());    e.csr.mstatus.MPRV:= Mux(e.csr.mstatus.MPP =/= 3.U,0.U,e.csr.mstatus.MPRV) ;e.csr.mstatus.MIE:=e.csr.mstatus.MPIE;e.csr.mstatus.MPIE:=1.U;e.csr.mstatus.MPP:=0.U;}
  def FENCE = (e: ExecEnv) => assert(0.B, "The inst has not been impleted\n")
  def WFI   = (e: ExecEnv) => assert(0.B, "The inst has not been impleted\n")
}
object RV32I_ALUInstr {
  def ADDI  = BitPat("b???????_?????_?????_000_?????_0010011")
  def SLLI  = BitPat("b0000000_?????_?????_001_?????_0010011")
  def SLTI  = BitPat("b???????_?????_?????_010_?????_0010011")
  def SLTIU = BitPat("b???????_?????_?????_011_?????_0010011")
  def XORI  = BitPat("b???????_?????_?????_100_?????_0010011")
  def SRLI  = BitPat("b0000000_?????_?????_101_?????_0010011")
  def ORI   = BitPat("b???????_?????_?????_110_?????_0010011")
  def ANDI  = BitPat("b???????_?????_?????_111_?????_0010011")
  def SRAI  = BitPat("b0100000_?????_?????_101_?????_0010011")

  def ADD  = BitPat("b0000000_?????_?????_000_?????_0110011")
  def SLL  = BitPat("b0000000_?????_?????_001_?????_0110011")
  def SLT  = BitPat("b0000000_?????_?????_010_?????_0110011")
  def SLTU = BitPat("b0000000_?????_?????_011_?????_0110011")
  def XOR  = BitPat("b0000000_?????_?????_100_?????_0110011")
  def SRL  = BitPat("b0000000_?????_?????_101_?????_0110011")
  def OR   = BitPat("b0000000_?????_?????_110_?????_0110011")
  def AND  = BitPat("b0000000_?????_?????_111_?????_0110011")
  def SUB  = BitPat("b0100000_?????_?????_000_?????_0110011")
  def SRA  = BitPat("b0100000_?????_?????_101_?????_0110011")

  def AUIPC = BitPat("b????????????????????_?????_0010111")
  def LUI   = BitPat("b????????????????????_?????_0110111")

  val table = Array(
    ADDI -> List(Inst.I, FuType.alu, ALUOpType.add) -> ALUExec.ADDI,
    SLLI -> List(Inst.I, FuType.alu, ALUOpType.sll) -> ALUExec.SLLI,
    SLTI -> List(Inst.I, FuType.alu, ALUOpType.slt) -> ALUExec.SLTI,
    SLTIU -> List(Inst.I, FuType.alu, ALUOpType.sltu) -> ALUExec.SLTIU,
    XORI -> List(Inst.I, FuType.alu, ALUOpType.xor) -> ALUExec.XORI,
    SRLI -> List(Inst.I, FuType.alu, ALUOpType.srl) -> ALUExec.SRLI,
    ORI -> List(Inst.I, FuType.alu, ALUOpType.or) -> ALUExec.ORI,
    ANDI -> List(Inst.I, FuType.alu, ALUOpType.and) -> ALUExec.ANDI,
    SRAI -> List(Inst.I, FuType.alu, ALUOpType.sra) -> ALUExec.SRAI,
    ADD -> List(Inst.R, FuType.alu, ALUOpType.add) -> ALUExec.ADD,
    SLL -> List(Inst.R, FuType.alu, ALUOpType.sll) -> ALUExec.SLL,
    SLT -> List(Inst.R, FuType.alu, ALUOpType.slt) -> ALUExec.SLT,
    SLTU -> List(Inst.R, FuType.alu, ALUOpType.sltu) -> ALUExec.SLTU,
    XOR -> List(Inst.R, FuType.alu, ALUOpType.xor) -> ALUExec.XOR,
    SRL -> List(Inst.R, FuType.alu, ALUOpType.srl) -> ALUExec.SRL,
    OR -> List(Inst.R, FuType.alu, ALUOpType.or) -> ALUExec.OR,
    AND -> List(Inst.R, FuType.alu, ALUOpType.and) -> ALUExec.AND,
    SUB -> List(Inst.R, FuType.alu, ALUOpType.sub) -> ALUExec.SUB,
    SRA -> List(Inst.R, FuType.alu, ALUOpType.sra) -> ALUExec.SRA,
    AUIPC -> List(Inst.U, FuType.alu, ALUOpType.add) -> ALUExec.AUIPC,
    LUI -> List(Inst.U, FuType.alu, ALUOpType.add) -> ALUExec.LUI
  )
}

object RV32I_BRUInstr {
  def JAL  = BitPat("b????????????????????_?????_1101111")
  def JALR = BitPat("b????????????_?????_000_?????_1100111")

  def BNE  = BitPat("b???????_?????_?????_001_?????_1100011")
  def BEQ  = BitPat("b???????_?????_?????_000_?????_1100011")
  def BLT  = BitPat("b???????_?????_?????_100_?????_1100011")
  def BGE  = BitPat("b???????_?????_?????_101_?????_1100011")
  def BLTU = BitPat("b???????_?????_?????_110_?????_1100011")
  def BGEU = BitPat("b???????_?????_?????_111_?????_1100011")

  val table = Array(
    JAL -> List(Inst.J, FuType.bru, ALUOpType.jal) -> BRUExec.JAL,
    JALR -> List(Inst.I, FuType.bru, ALUOpType.jalr) -> BRUExec.JALR,
    BEQ -> List(Inst.B, FuType.bru, ALUOpType.beq) -> BRUExec.BEQ,
    BNE -> List(Inst.B, FuType.bru, ALUOpType.bne) -> BRUExec.BNE,
    BLT -> List(Inst.B, FuType.bru, ALUOpType.blt) -> BRUExec.BLT,
    BGE -> List(Inst.B, FuType.bru, ALUOpType.bge) -> BRUExec.BGE,
    BLTU -> List(Inst.B, FuType.bru, ALUOpType.bltu) -> BRUExec.BLTU,
    BGEU -> List(Inst.B, FuType.bru, ALUOpType.bgeu) -> BRUExec.BGEU
  )

}

object RV32I_LSUInstr {
  def LB  = BitPat("b????????????_?????_000_?????_0000011")
  def LH  = BitPat("b????????????_?????_001_?????_0000011")
  def LW  = BitPat("b????????????_?????_010_?????_0000011")
  def LBU = BitPat("b????????????_?????_100_?????_0000011")
  def LHU = BitPat("b????????????_?????_101_?????_0000011")
  def SB  = BitPat("b???????_?????_?????_000_?????_0100011")
  def SH  = BitPat("b???????_?????_?????_001_?????_0100011")
  def SW  = BitPat("b???????_?????_?????_010_?????_0100011")

  val table = Array(
    LB -> List(Inst.I, FuType.lsu, LSUOpType.lb) -> LSUExec.LB,
    LH -> List(Inst.I, FuType.lsu, LSUOpType.lh) -> LSUExec.LH,
    LW -> List(Inst.I, FuType.lsu, LSUOpType.lw) -> LSUExec.LW,
    LBU -> List(Inst.I, FuType.lsu, LSUOpType.lbu) -> LSUExec.LBU,
    LHU -> List(Inst.I, FuType.lsu, LSUOpType.lhu) -> LSUExec.LHU,
    SB -> List(Inst.S, FuType.lsu, LSUOpType.sb) -> LSUExec.SB,
    SH -> List(Inst.S, FuType.lsu, LSUOpType.sh) -> LSUExec.SH,
    SW -> List(Inst.S, FuType.lsu, LSUOpType.sw) -> LSUExec.SW
  )
}

object RVZicsrInstr {
  def CSRRW  = BitPat("b????????????_?????_001_?????_1110011")
  def CSRRS  = BitPat("b????????????_?????_010_?????_1110011")
  def CSRRC  = BitPat("b????????????_?????_011_?????_1110011")
  def CSRRWI = BitPat("b????????????_?????_101_?????_1110011")
  def CSRRSI = BitPat("b????????????_?????_110_?????_1110011")
  def CSRRCI = BitPat("b????????????_?????_111_?????_1110011")

  val table = Array(
    CSRRW -> List(Inst.I, FuType.csr, CSROpType.wrt) -> ZicsrExec.CSRRW,
    CSRRS -> List(Inst.I, FuType.csr, CSROpType.set) -> ZicsrExec.CSRRS,
    CSRRC -> List(Inst.I, FuType.csr, CSROpType.clr) -> ZicsrExec.CSRRC,
    CSRRWI -> List(Inst.I, FuType.csr, CSROpType.wrti) -> ZicsrExec.CSRRWI,
    CSRRSI -> List(Inst.I, FuType.csr, CSROpType.seti) -> ZicsrExec.CSRRSI,
    CSRRCI -> List(Inst.I, FuType.csr, CSROpType.clri) -> ZicsrExec.CSRRCI
  )
}
object Priviledged {
  def ECALL      = BitPat("b000000000000_00000_000_00000_1110011")
  def EBREAK     = BitPat("b000000000001_00000_000_00000_1110011")
  def MRET       = BitPat("b001100000010_00000_000_00000_1110011")
  def SRET       = BitPat("b000100000010_00000_000_00000_1110011")
  def SFANCE_VMA = BitPat("b0001001_?????_?????_000_00000_1110011")
  def FENCE      = BitPat("b????????????_?????_000_?????_0001111")
  def WFI        = BitPat("b0001000_00101_00000_000_00000_1110011")

  val table_s = Array(
    SRET -> List(Inst.I, FuType.csr, CSROpType.jmp) -> PriviledgedExec.SRET,
    SFANCE_VMA -> List(Inst.R, FuType.mou, MOUOpType.sfence_vma) -> PriviledgedExec.SFANCE_VMA
  )

  val table = Array(
    ECALL -> List(Inst.I, FuType.csr, CSROpType.jmp) -> PriviledgedExec.ECALL,
    EBREAK -> List(Inst.I, FuType.csr, CSROpType.jmp) -> PriviledgedExec.EBREAK,
    MRET -> List(Inst.I, FuType.csr, CSROpType.jmp) -> PriviledgedExec.MRET,
    FENCE -> List(Inst.S, FuType.mou, MOUOpType.fence) -> PriviledgedExec.FENCE, // nop    InstrS -> !wen
    WFI -> List(Inst.I, FuType.alu, ALUOpType.add) -> PriviledgedExec.WFI // nop
    // FENCE          -> List(InstrB, FuType.mou, MOUOpType.fencei) -> PriviledgedExec.FENCE
  ) //++ (if (!Settings.get("MmodeOnly")) table_s else Array.empty)
}
object RVIInstr {
  val table =
    RV32I_ALUInstr.table ++ RV32I_BRUInstr.table ++ RV32I_LSUInstr.table ++ RVZicsrInstr.table ++ Priviledged.table
  val tabelWithIndex = table.zipWithIndex
}
