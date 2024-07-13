package core
import chisel3._
import chisel3.util._
import core.utils._

class IFUIO extends BundleWithIOInitImpl {}
class LSUIO extends BundleWithIOInitImpl {}
class CoreIO extends BundleWithIOInit {
  val LSUIO = new LSUIO
  val IFUIO = new IFUIO

  val pc    = Input(UInt(32.W))
  val inst  = Input(UInt(32.W))
  val id=new IF2IDBundle
  def IOinit[T <: Data](value: T): Unit = {
    id.IOinit(value)
  }
  def Flipped_IOinit[T <: Data](value: T): Unit = {
    inst := 0.U
    pc := 0.U
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
  val IFStage = new PiplineStageWithoutDepth(new BundleWithIOInitImpl {}, IF2ID)
  val IDStage = new PiplineStageWithoutDepth(IF2ID, new BundleWithIOInitImpl {})
  IFStage.ALL_IOinit()
  IDStage.ALL_IOinit()

  IFStage.out.bits.inst := io.inst
  IFStage.out.bits.pc   := io.pc

  //这里！！！晕了睡觉了
  IDStage.in <> Wire(IFStage.out.bits.getRegEnable(IFStage.out.bits, true.B))
  io.id.pc:=IDStage.in.bits.pc 
   io.id.inst:=IDStage.in.bits.inst 

  // StageConnect(IFStage, IDStage)
  // StageConnect(IDStage, EXStage)
  // StageConnect(withRegBeats=false)(EXStage, WBStage)

}
