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
  def ADDI = (e: ExecEnv) => e.Rrd := e.src1+ e.immI
  def SLLI  = (e: ExecEnv) => printf( "[Error]:The inst is not be impleted!!!!"+"\n")
  def SLTI  = (e: ExecEnv) => printf( "[Error]:The inst is not be impleted!!!!"+"\n")
  def SLTIU = (e: ExecEnv) => printf( "[Error]:The inst is not be impleted!!!!"+"\n")
  def XORI  = (e: ExecEnv) => printf( "[Error]:The inst is not be impleted!!!!"+"\n")
  def SRLI  = (e: ExecEnv) => printf( "[Error]:The inst is not be impleted!!!!"+"\n")
  def ORI   = (e: ExecEnv) => printf( "[Error]:The inst is not be impleted!!!!"+"\n")
  def ANDI  = (e: ExecEnv) => printf( "[Error]:The inst is not be impleted!!!!"+"\n")
  def SRAI  = (e: ExecEnv) => printf( "[Error]:The inst is not be impleted!!!!"+"\n")

  def ADD  = (e: ExecEnv) => printf( "[Error]:The inst is not be impleted!!!!"+"\n")
  def SLL  = (e: ExecEnv) => printf( "[Error]:The inst is not be impleted!!!!"+"\n")
  def SLT  = (e: ExecEnv) => printf( "[Error]:The inst is not be impleted!!!!"+"\n")
  def SLTU = (e: ExecEnv) => printf( "[Error]:The inst is not be impleted!!!!"+"\n")
  def XOR  = (e: ExecEnv) => printf( "[Error]:The inst is not be impleted!!!!"+"\n")
  def SRL  = (e: ExecEnv) => printf( "[Error]:The inst is not be impleted!!!!"+"\n")
  def OR   = (e: ExecEnv) => printf( "[Error]:The inst is not be impleted!!!!"+"\n")
  def AND  = (e: ExecEnv) => printf( "[Error]:The inst is not be impleted!!!!"+"\n")
  def SUB  = (e: ExecEnv) => printf( "[Error]:The inst is not be impleted!!!!"+"\n")
  def SRA  = (e: ExecEnv) => printf( "[Error]:The inst is not be impleted!!!!"+"\n")

  def AUIPC = (e: ExecEnv) => e.Rrd := e.pc + e.immU
  def LUI   = (e: ExecEnv) => printf( "[Error]:The inst is not be impleted!!!!"+"\n")
}

object BRUExec {
  //def ADDI = (e: ExecEnv) => e.Rrd := (e.src1.asSInt + e.immI).asUInt
  def JAL  = (e: ExecEnv) => {e.Rrd := e.pc + 4.U ; e.pc:=e.pc + e.immJ; }
  def JALR = (e: ExecEnv) => {e.pc:=(e.src1 + e.immI)&(-1.S(32.W)).asUInt; e.Rrd:= e.pc + 4.U} //s->dnpc = (src1 + imm) & ~(word_t)1; R(rd)= s->pc + 4 ); 
  def BEQ  = (e: ExecEnv) => printf( "[Error]:The inst is not be impleted!!!!"+"\n") 
  def BNE  = (e: ExecEnv) => printf( "[Error]:The inst is not be impleted!!!!"+"\n") 
  def BLT  = (e: ExecEnv) => printf( "[Error]:The inst is not be impleted!!!!"+"\n") 
  def BGE  = (e: ExecEnv) => printf( "[Error]:The inst is not be impleted!!!!"+"\n") 
  def BLTU = (e: ExecEnv) => printf( "[Error]:The inst is not be impleted!!!!"+"\n") 
  def BGEU = (e: ExecEnv) => printf( "[Error]:The inst is not be impleted!!!!"+"\n") 

}
object LSUExec {
  //def ADDI = (e: ExecEnv) => e.Rrd := (e.src1.asSInt + e.immI).asUInt
def LB  = (e: ExecEnv) => printf( "[Error]:The inst is not be impleted!!!!"+"\n") 
def LH  = (e: ExecEnv) => printf( "[Error]:The inst is not be impleted!!!!"+"\n") 
def LW  = (e: ExecEnv) => printf( "[Error]:The inst is not be impleted!!!!"+"\n") 
def LBU = (e: ExecEnv) => printf( "[Error]:The inst is not be impleted!!!!"+"\n") 
def LHU = (e: ExecEnv) => printf( "[Error]:The inst is not be impleted!!!!"+"\n") 
def SB  = (e: ExecEnv) => printf( "[Error]:The inst is not be impleted!!!!"+"\n") 
def SH  = (e: ExecEnv) => printf( "[Error]:The inst is not be impleted!!!!"+"\n") 
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
    JAL  -> List(Inst.J, FuType.bru, ALUOpType.jal)  -> BRUExec.JAL,
    JALR -> List(Inst.I, FuType.bru, ALUOpType.jalr) -> BRUExec.JALR,
    BEQ  -> List(Inst.B, FuType.bru, ALUOpType.beq)  -> BRUExec.BEQ,
    BNE  -> List(Inst.B, FuType.bru, ALUOpType.bne)  -> BRUExec.BNE,
    BLT  -> List(Inst.B, FuType.bru, ALUOpType.blt)  -> BRUExec.BLT,
    BGE  -> List(Inst.B, FuType.bru, ALUOpType.bge)  -> BRUExec.BGE,
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
    LB  -> List(Inst.I, FuType.lsu, LSUOpType.lb) -> LSUExec.LB ,
    LH  -> List(Inst.I, FuType.lsu, LSUOpType.lh) -> LSUExec.LH ,
    LW  -> List(Inst.I, FuType.lsu, LSUOpType.lw) -> LSUExec.LW ,
    LBU -> List(Inst.I, FuType.lsu, LSUOpType.lbu)-> LSUExec.LBU ,
    LHU -> List(Inst.I, FuType.lsu, LSUOpType.lhu)-> LSUExec. LHU,
    SB  -> List(Inst.S, FuType.lsu, LSUOpType.sb) -> LSUExec.SB ,
    SH  -> List(Inst.S, FuType.lsu, LSUOpType.sh) -> LSUExec.SH ,
    SW  -> List(Inst.S, FuType.lsu, LSUOpType.sw) -> LSUExec. SW 
  )
}

object RVIInstr {
  val table = RV32I_ALUInstr.table ++ RV32I_BRUInstr.table ++ RV32I_LSUInstr.table
}
