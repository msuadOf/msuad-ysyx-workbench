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
    ADDI -> List(Inst.I, FuType.alu, ALUOpType.add),
    SLLI -> List(Inst.I, FuType.alu, ALUOpType.sll),
    SLTI -> List(Inst.I, FuType.alu, ALUOpType.slt),
    SLTIU -> List(Inst.I, FuType.alu, ALUOpType.sltu),
    XORI -> List(Inst.I, FuType.alu, ALUOpType.xor),
    SRLI -> List(Inst.I, FuType.alu, ALUOpType.srl),
    ORI -> List(Inst.I, FuType.alu, ALUOpType.or),
    ANDI -> List(Inst.I, FuType.alu, ALUOpType.and),
    SRAI -> List(Inst.I, FuType.alu, ALUOpType.sra),
    ADD -> List(Inst.R, FuType.alu, ALUOpType.add),
    SLL -> List(Inst.R, FuType.alu, ALUOpType.sll),
    SLT -> List(Inst.R, FuType.alu, ALUOpType.slt),
    SLTU -> List(Inst.R, FuType.alu, ALUOpType.sltu),
    XOR -> List(Inst.R, FuType.alu, ALUOpType.xor),
    SRL -> List(Inst.R, FuType.alu, ALUOpType.srl),
    OR -> List(Inst.R, FuType.alu, ALUOpType.or),
    AND -> List(Inst.R, FuType.alu, ALUOpType.and),
    SUB -> List(Inst.R, FuType.alu, ALUOpType.sub),
    SRA -> List(Inst.R, FuType.alu, ALUOpType.sra),
    AUIPC -> List(Inst.U, FuType.alu, ALUOpType.add),
    LUI -> List(Inst.U, FuType.alu, ALUOpType.add)
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
    JAL -> List(Inst.J, FuType.bru, ALUOpType.jal),
    JALR -> List(Inst.I, FuType.bru, ALUOpType.jalr),
    BEQ -> List(Inst.B, FuType.bru, ALUOpType.beq),
    BNE -> List(Inst.B, FuType.bru, ALUOpType.bne),
    BLT -> List(Inst.B, FuType.bru, ALUOpType.blt),
    BGE -> List(Inst.B, FuType.bru, ALUOpType.bge),
    BLTU -> List(Inst.B, FuType.bru, ALUOpType.bltu),
    BGEU -> List(Inst.B, FuType.bru, ALUOpType.bgeu)
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
    LB -> List(Inst.I, FuType.lsu, LSUOpType.lb),
    LH -> List(Inst.I, FuType.lsu, LSUOpType.lh),
    LW -> List(Inst.I, FuType.lsu, LSUOpType.lw),
    LBU -> List(Inst.I, FuType.lsu, LSUOpType.lbu),
    LHU -> List(Inst.I, FuType.lsu, LSUOpType.lhu),
    SB -> List(Inst.S, FuType.lsu, LSUOpType.sb),
    SH -> List(Inst.S, FuType.lsu, LSUOpType.sh),
    SW -> List(Inst.S, FuType.lsu, LSUOpType.sw)
  )
}

object RVIInstr {
  val table = RV32I_ALUInstr.table ++ RV32I_BRUInstr.table ++ RV32I_LSUInstr.table
}
