import chisel3._
import chisel3.util._

import chisel3.util.experimental._
import chisel3.experimental.prefix

import scala.reflect.runtime.universe._
object getVariableName {
  def apply(variable: Any): String = {
    val mirror         = runtimeMirror(variable.getClass.getClassLoader)
    val instanceMirror = mirror.reflect(variable)
    val symbol         = instanceMirror.symbol
    symbol.name.toString
  }
}

class Core(isa_info: String = "RISCV32") extends Module {
  val io = IO(new Bundle {
    val IMem = new InstIO
    val DMem = new MemIO

    val diff = new Bundle {
      val pc   = Output(UInt(32.W))
      val dnpc = Output(UInt(32.W))
      val snpc = Output(UInt(32.W))
      val regs = Output(Vec(32, UInt(32.W)))

      val mepc    = Output(UInt(32.W))
      val mcause  = Output(UInt(32.W))
      val mstatus = Output(UInt(32.W))
      val mtvec   = Output(UInt(32.W))
    }
  })

  io.DMem.IOinit()

  val R          = new RegFile("RISCV32")
  val csr        = new csr
  val pc         = RegInit("h80000000".U(32.W))
  val snpc, dnpc = Wire(UInt(32.W))
  snpc         := pc + 4.U
  dnpc         := pc + 4.U
  pc           := dnpc
  io.diff.dnpc := dnpc
  io.diff.snpc := snpc
  //fetch inst
  io.IMem.rAddr := pc
  val inst = Wire(UInt(32.W))
  inst := io.IMem.rData

  //  //(src1,src2,rd,imm,instType)
  //  def instDivsion(inst:UInt):(UInt,UInt,UInt,UInt,UInt)={

  //     (1.U,1.U,1.U,1.U,1.U)
  //  }

  //decode and exec
  // RVIInstr.table.map((m) => {
  //   when(m._1===inst){
  //     io.DMem.wData:=m._2(2)
  //   }

  // })

  // RVIInstr.table(0)._1 === inst
  // inst === RVIInstr.table(0)._1

  // RVIInstr.table.foreach((t: Tuple3[BitPat, Any, Object => Object]) => {
  //   prefix(s"InstMatch_${getVariableName(t._1)}") {
  //     when(t._1 === inst) {
  //       if (t._1 == RV32I_ALUInstr.ADDI) {
  //         printf("ADDI\n")
  //       }
  //       printf(p"Inst_Decode:${(t._1)}\n");
  //     }
  //   }
  // })

  val Decoder = new ExecEnv(inst, pc, R, csr, io.DMem)
//RVIInstr.table.map((t:Tuple2[BitPat,Any])=>if(t._1===inst) )
  //first inst:addi
  val rs1, rs2, rd, imm = Wire(UInt())
  rs1 := inst(19, 15)
  rs2 := inst(24, 20)
  imm := inst(31, 20)
  rd  := inst(11, 7)
  // src1 := R(rs1)
  // src2 := R(rs2)

  val decode_success = RegInit(1.U(1.W))

  when(io.IMem.rValid === 1.U) {
    decode_success := 0.U
    RVIInstr.table
      .asInstanceOf[Array[((BitPat, Any), ExecEnv => Any)]]
      .foreach((t: ((BitPat, Any), ExecEnv => Any)) => {
        prefix(s"InstMatch_${getVariableName(t._1._1)}") {
          when(t._1._1 === inst) {
            decode_success := 1.U //debug
            Decoder.IDLE()
            t._2(Decoder)
            if (t._1._1 == RV32I_ALUInstr.ADDI) {
              printf("ADDI\n")
            }
            printf(p"Inst_Decode:${(t._1)}\n");
          }
        }
      })
    when(decode_success === 0.U) //decode failed
    {
      chisel3.assert(0.B, p"decode failed @ pc=${pc}\n" + "\n")
    }

    val ebreakDpi = Module(new ebreakDpi)
    ebreakDpi.io.inst := inst
  }

  io.diff.pc      := pc
  io.diff.regs    := R.reg
  io.diff.mepc    := csr.mepc.read()
  io.diff.mcause  := csr.mcause.read()
  io.diff.mstatus := csr.mstatus.read()
  io.diff.mtvec   := csr.mtvec.read()

  printf("io.IMem.rAddr=%x\n", io.IMem.rAddr)
//  printf(p"test inst: inst=${io.IMem.rData},pc=${io.IMem.rAddr},R($rd)=${R(rd)},s0=${R(8)}\n")
  printf(p"top.scala: io.DMem.rData=${io.DMem.rData},clk=${clock.asBool},rst=${reset.asBool}\n")
}
