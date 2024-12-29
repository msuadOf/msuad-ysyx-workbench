import circt.stage._
import core._
object Elaborate extends App {
  def top       = new ysyx_23060093()
  val generator = Seq(chisel3.stage.ChiselGeneratorAnnotation(() => top))
  (new ChiselStage).execute(args, generator :+ CIRCTTargetAnnotation(CIRCTTarget.Verilog))

}
