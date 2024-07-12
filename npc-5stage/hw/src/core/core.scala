package core
import chisel3._
import chisel3.util._

class IFUIO extends Bundle {}
class LSUIO extends Bundle {}
class CoreIO extends Bundle {
  val LSUIO = new LSUIO
  val IFUIO = new IFUIO
}

class Core extends Module {
  val io = new CoreIO

  val RegFile = new RegFile("RISCV32")

  val inst: UInt = 1.U

  val IF2ID = Wire(new Bundle {
    val inst = UInt(32.W)
  })
  val ID2EX = Wire(new Bundle {
    val src1 = UInt(32.W)
    val src2 = UInt(32.W)
    val rd   = UInt(5.W)

    val imm    = UInt()
    val alu_op = UInt()
    val lsu_op = UInt()
  })
  val EX2WB = Wire(new Bundle {
    val rd      = UInt(5.W)
    val rd_data = UInt(32.W)
  })
  val IDStage = new PiplineStageWithoutDepth(IF2ID, ID2EX)
  val EXStage = new PiplineStageWithoutDepth(ID2EX, EX2WB)
  val WBStage = new PiplineStageWithoutDepth(EX2WB, new Bundle {})

  //ID
  IDStage

  StageConnect(IDStage, EXStage)
  StageConnect(withRegBeats=false)(EXStage, WBStage)

  when(inst === ADDI) {
    //ID
    val src1_ID = RegFile.read(inst.rs1)
    val src2_ID = RegFile.read(inst.rs2)
    val rd_ID   = inst.rd

    //ID/EX
    val src1_EX = RegNext(src1_ID)
    val src2_EX = RegNext(src2_ID)
    val rd_EX   = RegNext(rd_ID)
    //EX
    val Rrd_EX = src1_EX + src2_EX

    //EX/Mem
    val Rrd_Mem = RegNext(Rrd_EX)
    val rd_Mem = RegNext(rd_EX)
    //Mem
    //nothing...

    //Mem/WB
    val Rrd_WB = RegNext(Rrd_Mem)
    val rd_WB = RegNext(rd_Mem)
    //WB
    RegFile.write(rd_WB, Rrd_WB)
  }
  // Frontend
  //TODO: IF stage

  // Insts pc
  //TODO: ID stage

  // uops rs1 rs2 rd alu_op
  // Backend
  //TODO: EX stage = LSU + FU

  //TODO: WB stage

}
