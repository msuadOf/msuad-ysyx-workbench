package core
import chisel3._
import chisel3.util._

class StageBundle extends Bundle {}
abstract class ValidReadyIO[+T <: Data](gen: T) extends Bundle {

  /** Indicates that the consumer is ready to accept the data this cycle
    * @group Signals
    */
  val ready = Bool()

  /** Indicates that the producer has put valid data in 'bits'
    * @group Signals
    */
  val valid = Bool()

  /** The data to be transferred when ready and valid are asserted at the same
    * cycle
    * @group Signals
    */
  val bits = gen

  /** A stable typeName for this `ValidReadyIO` and any of its implementations
    * using the supplied `Data` generator's `typeName`
    */
  override def typeName = s"${simpleClassName(this.getClass)}_${gen.typeName}"
}

object ValidReadyIO {

  implicit class AddMethodsToValidReady[T <: Data](target: ValidReadyIO[T]) {

    /** Indicates if IO is both ready and valid
      */
    def fire: Bool = target.ready && target.valid

    /** Push dat onto the output bits of this interface to let the consumer know
      * it has happened.
      * @param dat
      *   the values to assign to bits.
      * @return
      *   dat.
      */
    def enq(dat: T): T = {
      target.valid := true.B
      target.bits  := dat
      dat
    }

    /** Indicate no enqueue occurs. Valid is set to false, and bits are
      * connected to an uninitialized wire.
      */
    def noenq(): Unit = {
      target.valid := false.B
      target.bits  := DontCare
    }

    /** Assert ready on this port and return the associated data bits. This is
      * typically used when valid has been asserted by the producer side.
      * @return
      *   The data bits.
      */
    def deq(): T = {
      target.ready := true.B
      target.bits
    }

    /** Indicate no dequeue occurs. Ready is set to false.
      */
    def nodeq(): Unit = {
      target.ready := false.B
    }
  }
}

class HandshakeIO[+T <: Data](gen: T) extends ValidReadyIO[T](gen) {

  def map[B <: Data](f: T => B): HandshakeIO[B] = {
    val _map_bits = f(bits)
    val _map      = Wire(Handshake(chiselTypeOf(_map_bits)))
    _map.bits  := _map_bits
    _map.valid := valid
    ready      := _map.ready
    _map
  }
}

object Handshake {
  def apply[T <: Data](gen: T): HandshakeIO[T] = new HandshakeIO(gen)

  // use a proper empty data type, this is a quick and dirty solution
  private final class EmptyBundle extends Bundle

  def apply(): HandshakeIO[Data] = apply(new EmptyBundle)
  def empty:   HandshakeIO[Data] = Handshake()

}
object StageConnect {
  def apply(left: Stage, right: Stage) = {
    val arch = "pipeline"
    // 为展示抽象的思想, 此处代码省略了若干细节
    if (arch == "single") { right.in.bits := left.in.bits }
    else if (arch == "multi") { right.in <> left.in }
    else if (arch == "pipeline") { right.in <> RegEnable(left.in, left.in.fire) }
  }
  def apply(withRegBeats:Boolean = true)(left: Stage, right: Stage) = {
    if (withRegBeats) { right.in <> RegEnable(left.in, left.in.fire) }
    else { right.in <> left.in }
  }
}

// TODO: 将他抽象成 abstract class
class Stage(_in: Bundle, _out: Bundle) {
  val in   = Handshake(_in)
  val out  = Handshake(_out)
  val Busy = false.B
  def setBusy(busyOrNot: Bool): Unit = {
    Busy := busyOrNot
  }
}
class PiplineStage(_in: Bundle, _out: Bundle) extends Stage(_in, _out) {
  // TODO: To Be validate... 有待验证逻辑的正确性。。
  // FIXME: in.ready  out.valid 组合逻辑形成了回环。。。肯定有问题
  /*
        Busy  in.ready  out.valid
        =========================
        0     1          in.fire
        1     out.fire   1
   */
  in.ready  := out.fire || !Busy
  out.valid := in.fire || Busy
}
class PiplineStageWithoutDepth(_in: Bundle, _out: Bundle) extends Stage(_in, _out) {
  in.ready  := out.ready
  out.valid := in.valid
}
