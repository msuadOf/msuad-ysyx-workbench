package core
import chisel3._
import chisel3.util._
import core.utils._

class StageBundle extends Bundle {}
class HandshakeIO[+T <: BundleWithIOInit](gen: T) extends BundleWithIOInit {

  /** Indicates that the consumer is ready to accept the data this cycle
    * @group Signals
    */
  val ready = Input(Bool())

  /** Indicates that the producer has put valid data in 'bits'
    * @group Signals
    */
  val valid = Output(Bool())

  /** The data to be transferred when ready and valid are asserted at the same
    * cycle
    * @group Signals
    */
  val bits = (gen)
  def IOinit[T <: Data](value: T): Unit = {
    this.valid := value
    this.bits.IOinit(value)
  }
  def Flipped_IOinit[T <: Data](value: T): Unit = {
    
    this.ready := value
    this.bits.Flipped_IOinit(value)
  }

  def fire: Bool = this.ready && this.valid

  /** A stable typeName for this `ValidReadyIO` and any of its implementations
    * using the supplied `Data` generator's `typeName`
    */
  override def typeName = s"${simpleClassName(this.getClass)}_${gen.typeName}"
}

object Handshake {
  def apply[T <: BundleWithIOInit](gen: T): HandshakeIO[T] = new HandshakeIO(gen)

  // use a proper empty data type, this is a quick and dirty solution
  private final class EmptyBundle extends BundleWithIOInit {
    def IOinit[T <: Data](value: T): Unit = {}
    def Flipped_IOinit[T <: Data](value: T): Unit = {}
  }

  def apply(): HandshakeIO[BundleWithIOInit] = apply(new EmptyBundle)
  def empty:   HandshakeIO[BundleWithIOInit] = Handshake()

}
object StageConnect {
  def apply[A <: BundleWithIOInit, B <: BundleWithIOInit](left: Stage[A, B], right: Stage[A, B]):Unit = {
    val arch = "pipeline"
    // 为展示抽象的思想, 此处代码省略了若干细节
    if (arch == "single") { apply(withRegBeats = false)(left,right) }
    else if (arch == "multi") { apply(withRegBeats = false)(left,right) }
    else if (arch == "pipeline") { apply(withRegBeats = true)(left,right) }
  }
  def apply[A <: BundleWithIOInit, B <: BundleWithIOInit](
    withRegBeats: Boolean = true
  )(left:         Stage[A, B],
    right:        Stage[A, B]
  ):Unit = {
    if (withRegBeats) { right.in <> RegEnable(left.out.bits, left.out.fire) }
    else { right.in <> left.out }
  }
}

// TODO: 将他抽象成 abstract class
abstract class Stage[+A <: BundleWithIOInit, +B <: BundleWithIOInit](_in: A, _out: B) {
  val in  = Wire(Flipped(Handshake(_in)))
  val out = Wire(Handshake(_out))
  def ALL_IOinit(): Unit = {
    in.ALL_IOinit()
    out.ALL_IOinit()
  }
  // val Busy = false.B
  def getBusy(): Bool = {
    // Busy
    false.B
  }
  def setBusy(busyOrNot: Bool): Unit = {
    // Busy := busyOrNot
  }
  def build(): Unit
}
class PiplineStage[+A <: BundleWithIOInit, +B <: BundleWithIOInit](_in: A, _out: B) extends Stage[A, B](_in, _out) {
  def build(): Unit = {

    // TODO: To Be validate... 有待验证逻辑的正确性。。
    // FIXME: in.ready  out.valid 组合逻辑形成了回环。。。肯定有问题
    /*
        Busy  in.ready  out.valid
        =========================
        0     1          in.fire
        1     out.fire   1
     */
    in.ready  := out.fire || !getBusy()
    out.valid := in.fire || getBusy()
  }
}
class PiplineStageWithoutDepth[+A <: BundleWithIOInit, +B <: BundleWithIOInit](_in: A, _out: B)
    extends Stage[A, B](_in, _out) {
  def build(): Unit = {
    /*
    @member :
    val in   = Handshake(_in)
    val out  = Handshake(_out)
     */
    in.ready  := out.ready
    out.valid := in.valid
  }
}
