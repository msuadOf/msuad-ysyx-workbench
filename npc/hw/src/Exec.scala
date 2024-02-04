import chisel3._
import chisel3.util._

class ExecEnv(val inst: UInt, val pc: UInt, val R: RegFile, val DMem: MemIO) {
  //val rs1, rs2, rd, src1, src2, imm ,Rrd = Wire(UInt())
  val rs1 = inst(19, 15)
  val rs2 = inst(24, 20)

  val rd   = inst(11, 7)
  val src1 = R.read(rs1)
  val src2 = R.read(rs2)
  object Rrd {
    def :=(that: UInt): Unit = {
      R.write(rd, that)
    }
  }

  //val Rrd  = R.read(rd)

  val immI = (inst(31, 20).asSInt + 0.S(32.W)).asUInt //imm = SEXT(BITS(i, 31, 20), 12);
  val immU = ((inst(31, 12) << 12).asSInt + 0.S(32.W)).asUInt //imm = SEXT(BITS(i, 31, 12), 20) << 12;
  val immS = (inst(31, 25).asSInt << 5.U).asUInt | inst(11, 7) //imm = (SEXT(BITS(i, 31, 25), 7) << 5) | BITS(i, 11, 7);
  val immJ =
    (((inst(31) << 19 | inst(19, 12) << 11 | inst(20) << 10 | inst(30, 21)) << 1).asSInt + 0.S(
      32.W
    )).asUInt //imm = SEXT((   (BITS(i, 31, 31) << 19) | BITS(i, 30, 21) | (BITS(i, 20, 20) << 10) | (BITS(i, 19, 12) << 11) ) << 1, 21);
  val immB = (((inst(31, 31) << 12) | inst(30, 25) << 5 | (inst(11, 8) << 1) | (inst(7, 7) << 11)).asSInt + 0.S(
    32.W
  )).asUInt //*imm = SEXT((   (inst( 31, 31) << 12) | BITS(i, 30, 25)<<5 | (BITS(i, 11, 8) << 1) | (BITS(i, 7, 7) << 11) ) , 13);

  object Mem {
    def wIDLE() = {
      DMem.wAddr := Fill(32, 1.U)
      DMem.wData := Fill(32, 1.U)
      DMem.wen   := 0.U
    }
    def rIDLE() = {
      DMem.ren := 0.U
    }
    def IDLE() = {
      wIDLE()
      rIDLE()
    }
    def write(addr: UInt, len: Int, data: UInt) = {
      DMem.wen   := 1.U
      DMem.wAddr := addr
      //DMem.wData := data

      DMem.wData := (len match {
        case 1 => { DMem.wWidth := 1.U;data(8 - 1, 0) }
        case 2 => { DMem.wWidth := 2.U;data(8 * 2 - 1, 0) }
        case 4 => { DMem.wWidth := 4.U;data(8 * 4 - 1, 0) }
        case _: Int => throw new IllegalArgumentException("write(addr,len,data) args \"len\" should be [1] [2] [4]")
      })

      //DMem.wWidth := len.asUInt

    }
    def read(addr: UInt, len: Int): UInt = {
      DMem.ren   := 1.U
      DMem.rAddr := addr
      //DMem.rData
      (len match {
        case 1 => { DMem.rWidth := 1.U;DMem.rData(8 - 1, 0)}
        case 2 => { DMem.rWidth := 2.U;DMem.rData(8 * 2 - 1, 0) }
        case 4 => { DMem.rWidth := 4.U;DMem.rData(8 * 4 - 1, 0) }
        case _: Int => throw new IllegalArgumentException("write(addr,len,data) args \"len\" should be [1] [2] [4]")
      })

    }
  }
  object Reg {
    def IDLE() = {
      R(0)  := 0.U
      R.reg := R.reg
    }
  }

  def IDLE() = { Mem.IDLE(); Reg.IDLE() }
  def Mw(addr: UInt, len: Int, data: UInt) = Mem.write(addr, len, data)
  def Mr(addr: UInt, len: Int): UInt = Mem.read(addr, len)
}
