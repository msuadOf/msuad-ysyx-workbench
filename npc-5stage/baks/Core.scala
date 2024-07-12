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
    val IMem   = new InstIO
    val DMem   = new MemIO
    val SUCtrl = Flipped(new SUCtrl_IO)
    val LUCtrl = Flipped(new LUCtrl_IO)

    val diff = new diffIO
  })
  io.IMem.IOinit()
  io.DMem.IOinit()
  io.SUCtrl.Flipped_IOinit()

  val R          = new RegFile("RISCV32")
  val csr        = new csr
  val pc         = RegInit("h80000000".U(32.W))
  val snpc, dnpc = Wire(UInt(32.W))
  snpc         := pc
  dnpc         := pc
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

  /* TODO 需要完善的 */
  val DMEM_Inst = Wire(Bool())
  DMEM_Inst        := 0.U
  io.diff.DMemInst := DMEM_Inst
  /* end */

  val s_idle :: s_IF :: s_EX :: s_LS :: Nil = Enum(4)
  val state                                 = RegInit(s_idle)
  state := MuxLookup(state, s_IF)(
    List(
      s_idle -> s_IF,
      s_IF -> Mux(io.IMem.rValid.asBool, s_EX, s_IF),
      s_EX -> Mux(DMEM_Inst, s_LS, s_IF),
      s_LS -> Mux(io.SUCtrl.wEop.asBool || io.LUCtrl.rEop.asBool, s_IF, s_LS)
    )
  )
  //IF
  val Ien_w = Mux(state === s_IF, 1.U, 0.U)
  val Ien_r = RegNext(Ien_w)
  io.IMem.Ien := (~Ien_r) & (Ien_w) //是一个pulse

  io.IMem.rAddr := pc //??? 为什么前面还有
  //Store
  val SU_wEn   = Wire(UInt(1.W))
  val SU_wAddr = Wire(UInt(32.W))
  val SU_wData = Wire(UInt(32.W))
  val SU_wStrb = Wire(UInt(32.W))
  SU_wEn   := io.DMem.wen
  SU_wAddr := io.DMem.wAddr
  SU_wData := io.DMem.wData
  SU_wStrb := io.DMem.wStrb

  io.SUCtrl.wEn   := RegNext(Mux(state === s_LS, io.SUCtrl.wEn, SU_wEn))
  io.SUCtrl.wAddr := RegNext(Mux(state === s_LS, io.SUCtrl.wAddr, SU_wAddr)) //！！！数据可能保持时间太短
  io.SUCtrl.wData := RegNext(Mux(state === s_LS, io.SUCtrl.wData, SU_wData))
  io.SUCtrl.wStrb := RegNext(Mux(state === s_LS, io.SUCtrl.wStrb, SU_wStrb))
  //Load
  val LU_rEn    = Wire(UInt(1.W))
  val LU_rAddr  = Wire(UInt(32.W))
  val LU_rData  = Wire(UInt(32.W))
  val LU_rValid = Wire(UInt(1.W))
  val LU_rEop   = Wire(UInt(1.W))
  val LU_rStrb  = Wire(UInt(32.W))
  LU_rEn   := io.DMem.ren
  LU_rAddr := io.DMem.rAddr
  LU_rStrb := io.DMem.rStrb

  printf(p"LU_rAddr=${LU_rAddr}")

  io.LUCtrl.rEn   := RegNext(Mux(state === s_LS, io.LUCtrl.rEn, LU_rEn))
  io.LUCtrl.rAddr := RegNext(Mux(state === s_LS, io.LUCtrl.rAddr, LU_rAddr)) //！！！数据可能保持时间太短
  io.LUCtrl.rStrb := RegNext(Mux(state === s_LS, io.LUCtrl.rStrb, LU_rStrb))

  LU_rData := io.LUCtrl.rData
  val LU_rData_r = RegNext(LU_rData) //debug看的，还需要把数据写回reg里
  LU_rValid := io.LUCtrl.rValid
  val LU_rValid_r = RegNext(LU_rValid)
  LU_rEop := io.LUCtrl.rEop
  val LU_rEop_r = RegNext(LU_rEop)

  val decode_success = Wire(UInt(1.W))
  decode_success := 0.U

  val Decoder = new ExecEnv(inst, pc, R, csr, io.DMem, DMEM_Inst, io.LUCtrl)

  when(state === s_EX || state === s_LS) {
    snpc := pc + 4.U
    dnpc := Mux(state === s_EX, pc + 4.U, pc) // pc + 4.U

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
  }.otherwise {
    decode_success := 0.U
  }

// when(){
//   value := e.Mr(e.src1 + e.immI, 4)
// }
// regnext:=value

  io.diff.pc      := pc
  io.diff.regs    := R.reg
  io.diff.mepc    := csr.mepc.read()
  io.diff.mcause  := csr.mcause.read()
  io.diff.mstatus := csr.mstatus.read()
  io.diff.mtvec   := csr.mtvec.read()

  val diff_state   = ((state === s_IF || state === s_idle))
  val diff_state_r = RegNext(diff_state)
  io.diff.diff_en := diff_state && ~diff_state_r

  printf("io.IMem.rAddr=%x\n", io.IMem.rAddr)
//  printf(p"test inst: inst=${io.IMem.rData},pc=${io.IMem.rAddr},R($rd)=${R(rd)},s0=${R(8)}\n")
  printf(p"top.scala: io.DMem.rData=${io.DMem.rData},clk=${clock.asBool},rst=${reset.asBool}\n")
}
