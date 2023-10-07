import circt.stage._

object Elaborate extends App {
  def top = new GCD()
  val generator = Seq(chisel3.stage.ChiselGeneratorAnnotation(() => top))
  (new ChiselStage).execute(args ++ Array("-td","build"), generator :+ CIRCTTargetAnnotation(CIRCTTarget.Verilog))
}
