package core
import chisel3._
import chisel3.util._
import core.utils._
import core.Stages._

class IFUIO extends BundlePlusImpl {}
class LSUIO extends BundlePlusImpl {}
class MonitorIO extends BundlePlus {
  val if2id_if = Output(Handshake(new IF2IDBundle))
  val if2id_id = Output(Handshake(new IF2IDBundle))
  val id2ex_id = Output(Handshake(new ID2EXBundle))
  val id2ex_ex = Output(Handshake(new ID2EXBundle))
  val ex2wb_ex = Output(Handshake(new EX2WBBundle))
  val ex2wb_wb = Output(Handshake(new EX2WBBundle))
  val scoreBoard=Output(UInt(32.W))

  def connectStageIO(s: InstFetchStage): Unit = {
    s.out <> if2id_if
  }
  def connectStageIO(s: InstDecodeStage): Unit = {
    s.in <> if2id_id
    s.out <> id2ex_id
  }
  def connectStageIO(s: ExecStage): Unit = {
    s.in <> id2ex_ex
    s.out <> ex2wb_ex
  }
  def connectStageIO(s: WriteBackStage): Unit = {
    s.in <> ex2wb_wb
  }
}
class CoreIO extends BundlePlus {
  val LSUIO   = new LSUIO
  val IFUIO   = new IFUIO
  val monitor = new MonitorIO
  val diff=new diffIO

  def IOIIInit[T <: Data](value: T): Unit = {
    // idu.IOinit(value)
  }
  def Flipped_IOIIInit[T <: Data](value: T): Unit = {}
}

class Core extends Module {
  val io = IO(new CoreIO)
  io.IOinit()
  val RegFile = new RegFile("RISCV32")

  val inst: UInt = 1.U

  // val IF2ID = new IF2IDBundle
  // val ID2EX = new ID2EXBundle
  // val EX2WB = new EX2WBBundle

  // val IFStage = new PiplineStageWithoutDepth(new Bundle {}, IF2ID)
  // val IDStage = new PiplineStageWithoutDepth(IF2ID, ID2EX)
  // val EXStage = new PiplineStageWithoutDepth(ID2EX, EX2WB)
  // val WBStage = new PiplineStageWithoutDepth(EX2WB, new Bundle {})

  //IF
  val IO2IF = new IFUIO
  val IF2ID = new IF2IDBundle
  val ID2EX = new ID2EXBundle
  val EX2WB = new EX2WBBundle

  // val IFStage = new PiplineStageWithoutDepth(new BundlePlusImpl {}, IF2ID)
  val IFStage = new InstFetchStage(IO2IF, IF2ID)
  val IDStage = new InstDecodeStage(IF2ID, ID2EX)
  val EXStage = new ExecStage(ID2EX, EX2WB)
  val WBStage = new WriteBackStage(EX2WB, IDStage.regfile, IFStage.pc,io.diff)
  IFStage.ALL_IOinit()
  IDStage.ALL_IOinit()
  EXStage.ALL_IOinit()
  WBStage.ALL_IOinit()

  //  IFStage.out.bits .=>>(true.B)( IDStage.in.bits )

  IFStage.build()
  IDStage.build()
  EXStage.build()
  WBStage.build()

  val isInsertReg = true
  val piplineFlushSignal = WBStage.in.bits.dnpcEn
  StageConnect(withRegBeats = isInsertReg)(IFStage, IDStage,piplineFlushSignal)
  StageConnect(withRegBeats = isInsertReg)(IDStage, EXStage,piplineFlushSignal)
  StageConnect(withRegBeats = isInsertReg)(EXStage, WBStage,piplineFlushSignal)

  val scoreBoard=new ScoreBoard
  val id_out=IDStage.out.bits
  scoreBoard.id_record(id_out.rd,id_out.rd_en)
  scoreBoard.wb_record(WBStage.in.bits.rd,WBStage.in.bits.RrdEn)
  when(scoreBoard.id_judgeRAW(id_out.rs1,id_out.rs1_en,id_out.rs2,id_out.rs2_en)()){
      IDStage.in.ready := 0.B
  }
  io.monitor.scoreBoard:= Cat(scoreBoard.regfileBusy)
  io.monitor.connectStageIO(IFStage)
  io.monitor.connectStageIO(IDStage)
  io.monitor.connectStageIO(EXStage)
  io.monitor.connectStageIO(WBStage)

  // StageConnect(IFStage, IDStage)
  // StageConnect(IDStage, EXStage)
  // StageConnect(withRegBeats=false)(EXStage, WBStage)

}
