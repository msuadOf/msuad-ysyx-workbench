import chisel3._
import chisel3.util._

import chisel3.util.experimental._

class RegFile(val ISet: String) {
  val regNum = ISet match {
    case "RISCV32E" => 16
    case "RISCV32"  => 32
    case "RISCV64"  => 64
    case _: String => throw new IllegalArgumentException("RegFile() args should be [RISCV32E] [RISCV32] [RISCV64]")
  }
  val reg = RegInit(VecInit(Seq.tabulate(regNum)(i => 0.U(32.W))))
  def apply(idx: Int): UInt = {
    reg(idx)
  }
  def apply(idx: UInt): UInt = {
    reg(idx)
  }
  import chisel3.experimental.{prefix, SourceInfo}
  // final def :=(that: => Data)(implicit sourceInfo: SourceInfo): Unit = {
  //     this.:=(that)(sourceInfo)
  // }
}

class top extends Module {
  
  val io = IO(new Bundle {
    val IMem = new Bundle {
      val readAddr = Output(UInt(32.W))
      val readData = Input(UInt(32.W))
    }

    val DMem = new Bundle {
      val readAddr  = Output(UInt(32.W))
      val readData  = Input(UInt(32.W))
      val writeAddr = Output(UInt(32.W))
      val writeData = Output(UInt(32.W))
    }
  })
  io.DMem.readAddr := 0.U
  io.DMem.writeAddr:=0.U 

  val R = new RegFile("RISCV32E")
  val pc  = RegInit(0.U(32.W))

  //fetch inst
  io.IMem.readAddr := pc
  val inst = Wire(UInt(32.W))
  inst := io.IMem.readData

  //  //(src1,src2,rd,imm,instType)
  //  def instDivsion(inst:UInt):(UInt,UInt,UInt,UInt,UInt)={

  //     (1.U,1.U,1.U,1.U,1.U)
  //  }

  //decode and exec
  // RVIInstr.table.map((m) => {
  //   when(m._1===inst){
  //     io.DMem.writeData:=m._2(2)
  //   }

  // })

  //first inst:addi
  val rs1,rs2,rd,src1,src2,imm=Wire(UInt())
  rs1 := inst(19, 15)
  rs2 := inst(24, 20)
  imm:=inst(31,20)
  rd:=inst(11,7)
  src1:=R(rs1)
  src2:=R(rs2)
  println(rs1)
  println(src1)

  //addi exec
  R(rd) := src1 + imm
  io.DMem.writeData:=R(rd)
}
