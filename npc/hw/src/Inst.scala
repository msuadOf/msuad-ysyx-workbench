import chisel3._
import chisel3.util._

object Inst {
  def N = "b0000".U
  def I = "b0100".U
  def R = "b0101".U
  def S = "b0010".U
  def B = "b0001".U
  def U = "b0110".U
  def J = "b0111".U
}

object FuType {
  def num     = 5
  def alu     = "b000".U
  def lsu     = "b001".U
  def mdu     = "b010".U
  def csr     = "b011".U
  def mou     = "b100".U
  def bru     = "b101".U
  def apply() = UInt(log2Up(num).W)
}

object ALUOpType {
  def add  = "b1000000".U
  def sll  = "b0000001".U
  def slt  = "b0000010".U
  def sltu = "b0000011".U
  def xor  = "b0000100".U
  def srl  = "b0000101".U
  def or   = "b0000110".U
  def and  = "b0000111".U
  def sub  = "b0001000".U
  def sra  = "b0001101".U

  def addw = "b1100000".U
  def subw = "b0101000".U
  def sllw = "b0100001".U
  def srlw = "b0100101".U
  def sraw = "b0101101".U

  def jal  = "b1011000".U
  def jalr = "b1011010".U
  def beq  = "b0010000".U
  def bne  = "b0010001".U
  def blt  = "b0010100".U
  def bge  = "b0010101".U
  def bltu = "b0010110".U
  def bgeu = "b0010111".U

}
object LSUOpType { //TODO: refactor LSU fuop
  def lb  = "b0000000".U
  def lh  = "b0000001".U
  def lw  = "b0000010".U
  def ld  = "b0000011".U
  def lbu = "b0000100".U
  def lhu = "b0000101".U
  def lwu = "b0000110".U
  def sb  = "b0001000".U
  def sh  = "b0001001".U
  def sw  = "b0001010".U
  def sd  = "b0001011".U

  def lr      = "b0100000".U
  def sc      = "b0100001".U
  def amoswap = "b0100010".U
  def amoadd  = "b1100011".U
  def amoxor  = "b0100100".U
  def amoand  = "b0100101".U
  def amoor   = "b0100110".U
  def amomin  = "b0110111".U
  def amomax  = "b0110000".U
  def amominu = "b0110001".U
  def amomaxu = "b0110010".U

}

object ALUExec {
  //def ADDI = (e: ExecEnv) => e.Rrd := (e.src1.asSInt + e.immI).asUInt
  def ADDI  = (e: ExecEnv) => e.Rrd := e.src1 + e.immI
  def SLLI  = (e: ExecEnv) => e.Rrd := e.src1 << e.immI(5, 0)
  def SLTI  = (e: ExecEnv) => e.Rrd := e.src1.asSInt < e.immI.asSInt
  def SLTIU = (e: ExecEnv) => e.Rrd := e.src1 < e.immI
  def XORI  = (e: ExecEnv) => e.Rrd := e.src1 ^ e.immI
  def SRLI  = (e: ExecEnv) => e.Rrd := e.src1 >> e.immI(5, 0)
  def ORI   = (e: ExecEnv) => e.Rrd := e.src1 | e.immI
  def ANDI  = (e: ExecEnv) => e.Rrd := e.src1 & e.immI
  def SRAI  = (e: ExecEnv) => e.Rrd := (e.src1.asSInt >> e.immI(5, 0).asUInt).asUInt(31,0)

  def ADD  = (e: ExecEnv) => e.Rrd := e.src1 + e.src2
  def SLL  = (e: ExecEnv) => e.Rrd := e.src1 << e.src2(4, 0)
  def SLT  = (e: ExecEnv) => e.Rrd := e.src1.asSInt < e.src2.asSInt //.asUInt
  def SLTU = (e: ExecEnv) => e.Rrd := e.src1 < e.src2
  def XOR  = (e: ExecEnv) => e.Rrd := e.src1 ^ e.src2
  def SRL  = (e: ExecEnv) => e.Rrd := e.src1 >> e.src2(4, 0)
  def OR   = (e: ExecEnv) => e.Rrd := e.src1 | e.src2
  def AND  = (e: ExecEnv) => e.Rrd := e.src1 & e.src2
  def SUB  = (e: ExecEnv) => e.Rrd := e.src1 - e.src2
  def SRA = (e: ExecEnv) =>  e.Rrd := (e.src1.asSInt >> e.src2.asUInt(4,0)).asUInt(31,0)

  def AUIPC = (e: ExecEnv) => e.Rrd := e.pc + e.immU
  def LUI   = (e: ExecEnv) => e.Rrd := e.immU
}

object BRUExec {
  //def ADDI = (e: ExecEnv) => e.Rrd := (e.src1.asSInt + e.immI).asUInt
  def JAL  = (e: ExecEnv) => { e.Rrd := e.pc + 4.U; e.pc := e.pc + e.immJ; }
  def JALR = (e: ExecEnv) => { e.pc := (e.src1 + e.immI) & (-1.S(32.W)).asUInt; e.Rrd := e.pc + 4.U }
  //s->dnpc = (src1 + imm) & ~(word_t)1; R(rd)= s->pc + 4 );
  def BEQ  = (e: ExecEnv) => when(e.src1 === e.src2) { e.pc := e.pc + e.immB }
  def BNE  = (e: ExecEnv) => when(e.src1 =/= e.src2) { e.pc := e.pc + e.immB }
  def BLT  = (e: ExecEnv) => when(e.src1.asSInt < e.src2.asSInt) { e.pc := e.pc + e.immB }
  def BGE  = (e: ExecEnv) => when(e.src1.asSInt >= e.src2.asSInt) { e.pc := e.pc + e.immB }
  def BLTU = (e: ExecEnv) => when(e.src1 < e.src2) { e.pc := e.pc + e.immB }
  def BGEU = (e: ExecEnv) => when(e.src1 >= e.src2) { e.pc := e.pc + e.immB }

}
object LSUExec {
  //def ADDI = (e: ExecEnv) => e.Rrd := (e.src1.asSInt + e.immI).asUInt
  def LB  = (e: ExecEnv) => e.Rrd := ( e.Mr(e.src1 + e.immI, 4).asUInt(8-1,0).asSInt + 0.S(32.W) ).asUInt
  def LH  = (e: ExecEnv) => e.Rrd := ( e.Mr(e.src1 + e.immI, 4).asUInt(16-1,0).asSInt + 0.S(32.W) ).asUInt
  def LW  = (e: ExecEnv) => e.Rrd := e.Mr(e.src1 + e.immI, 4)
  def LBU = (e: ExecEnv) => e.Rrd := e.Mr(e.src1 + e.immI, 1)
  def LHU = (e: ExecEnv) => e.Rrd := e.Mr(e.src1 + e.immI, 2)
  def SB  = (e: ExecEnv) => {e.Mw(e.src1 + e.immS, 1, e.src2);printf("[SB]:ADDR=%x,src1=%x,immS=%x}\n",e.src1 + e.immS,e.src1 ,e.immS)}
  def SH  = (e: ExecEnv) => e.Mw(e.src1 + e.immS, 2, e.src2)
  def SW  = (e: ExecEnv) => e.Mw(e.src1 + e.immS, 4, e.src2)

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

object RVIInstr {
  val table = RV32I_ALUInstr.table ++ RV32I_BRUInstr.table ++ RV32I_LSUInstr.table
}
