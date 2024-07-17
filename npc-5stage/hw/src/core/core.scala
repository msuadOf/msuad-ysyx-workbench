package core
import chisel3._
import chisel3.util._
import core.utils._
import core.Stages._

class IFUIO extends BundlePlusImpl {}
class LSUIO extends BundlePlusImpl {}
class CoreIO extends BundlePlus with StageBeatsImpl {
  val LSUIO = new LSUIO
  val IFUIO = new IFUIO

  val ifu = Output(Handshake(new IF2IDBundle))
  val idu = Output(Handshake(new IF2IDBundle))

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
  
  // val IFStage = new PiplineStageWithoutDepth(new BundlePlusImpl {}, IF2ID)
  val IFStage = new InstFetchStage(IO2IF, IF2ID)
  val IDStage = new PiplineStageWithoutDepth(IF2ID, ID2EX)
  val EXStage = new PiplineStageWithoutDepth(ID2EX, new EX2WBBundle)
  IFStage.ALL_IOinit()
  IDStage.ALL_IOinit()
  EXStage.ALL_IOinit()

// IDStage.out.getElements.foreach(x => println(chiselTypeOf(x)== x ))
// IDStage.out.getElements.foreach(x => println(chisel3.reflect.DataMirror.checkTypeEquivalence(x,Bits()  ) ))
// IDStage.out.getElements.foreach(x => println(chisel3.reflect.DataMirror.directionOf(x)== ActualDirection.Output))

IDStage.out.getElements.foreach(x => x match {
  case b:chisel3.Bits => if(chisel3.reflect.DataMirror.directionOf(b) == ActualDirection.Output) b:=0.U
  case e:BundlePlus => if(chisel3.reflect.DataMirror.directionOf(e) == ActualDirection.Output) e.IOinit(0.U)
  case _ => println("Unknown")
})
/* IDStage.out.getElements.foreach(x => chisel3.reflect.DataMirror.directionOf(x) match {
  case ActualDirection.Output => x.IOinit(0.U) ;println("Output")
  case ActualDirection.Input => println("Input")
  case _ => println("Unknown")
}) */
  //  IFStage.out.bits .=>>(true.B)( IDStage.in.bits )

  IFStage.build()
  IDStage.build()
  EXStage.build()
  StageConnect(withRegBeats = true)(IFStage, IDStage)
  StageConnect(withRegBeats =  true)(IDStage, EXStage)

  io.idu <> IDStage.in
  io.ifu <> IFStage.out
  // StageConnect(IFStage, IDStage)
  // StageConnect(IDStage, EXStage)
  // StageConnect(withRegBeats=false)(EXStage, WBStage)

}
