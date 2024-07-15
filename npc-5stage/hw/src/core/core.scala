package core
import chisel3._
import chisel3.util._
import core.utils._

class IFUIO extends BundlePlusImpl {}
class LSUIO extends BundlePlusImpl {}
class CoreIO extends BundlePlus with StageBeatsImpl {
  val LSUIO = new LSUIO
  val IFUIO = new IFUIO

  val pc    = Input(UInt(32.W))
  val inst  = Input(UInt(32.W))
  val vld   = Input(Bool())
  val ready = Output(Bool())

  val id = new IF2IDBundle
  def IOinit[T <: Data](value: T): Unit = {
    id.IOinit(value)
    ready := value
  }
  def Flipped_IOinit[T <: Data](value: T): Unit = {
    inst := value
    pc   := value
  }
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
  val IF2ID   = new IF2IDBundle
  val IFStage = new PiplineStageWithoutDepth(new BundlePlusImpl {}, IF2ID)
  val IDStage = new PiplineStageWithoutDepth(IF2ID, new BundlePlusImpl {})
  IFStage.ALL_IOinit()
  IDStage.ALL_IOinit()

  IFStage.out.bits.inst := io.inst
  IFStage.out.bits.pc   := io.pc

  //  IFStage.out.bits .=>>(true.B)( IDStage.in.bits )

  StageConnect(IFStage, IDStage)

  io.ready          := IFStage.out.ready
  IFStage.out.valid := io.vld

  io.id <> IDStage.in.bits
  // StageConnect(IFStage, IDStage)
  // StageConnect(IDStage, EXStage)
  // StageConnect(withRegBeats=false)(EXStage, WBStage)

}
