package core
import chisel3._
import chisel3.util._
import core.utils._

class StageBundle extends Bundle {}
class HandshakeIO[+T <: BundlePlus](gen: T) extends BundlePlus with StageBeatsImpl {

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
  def IOIIInit[T <: Data](value: T): Unit = {
    this.valid := value
    this.bits.IOinit(value)
  }
  def Flipped_IOIIInit[T <: Data](value: T): Unit = {

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
  def apply[T <: BundlePlus](gen: T): HandshakeIO[T] = new HandshakeIO(gen)

  // use a proper empty data type, this is a quick and dirty solution
  private final class EmptyBundle extends BundlePlusImpl

  def apply(): HandshakeIO[BundlePlus] = apply(new EmptyBundle)
  def empty:   HandshakeIO[BundlePlus] = Handshake()

}
object StageConnect {
  def apply[A <: BundlePlus, B <: BundlePlus](left: Stage[A, B], right: Stage[A, B]): Unit = {
    val arch = "pipeline"
    // 为展示抽象的思想, 此处代码省略了若干细节
    if (arch == "single") { apply(withRegBeats = false)(left, right) }
    else if (arch == "multi") { apply(withRegBeats = false)(left, right) }
    else if (arch == "pipeline") { apply(withRegBeats = true)(left, right) }
  }
  def apply[A <: BundlePlus, B <: BundlePlus](
    withRegBeats: Boolean = true
  )(left:         Stage[A, B],
    right:        Stage[A, B], clear:Bool = 0.B
  ): Unit = {
    if (withRegBeats) { connectWithRegBeats(left, right,clear) }
    else { right.in <> left.out }
  }
  //TODO: [clear] the Beat reg —— clear信号用于冲刷流水线
  def connectWithRegBeats[A <: BundlePlus, B <: BundlePlus](left_stage: Stage[A, B], right_stage: Stage[A, B]): Unit =connectWithRegBeats(left_stage,right_stage,0.B)
  def connectWithRegBeats[A <: BundlePlus, B <: BundlePlus](left_stage: Stage[A, B], right_stage: Stage[A, B],clear:Bool): Unit = {
    val beatReg_busy = RegInit(0.B)

    val left  = left_stage.out
    val right = right_stage.in
    left.ready  := right.ready//更改逻辑：下面没好，不准打拍（原来的是下面没好，先打一拍存着）left.ready  := !beatReg_busy || right.ready
    right.valid := beatReg_busy
    val beatReg_busy_wire = MuxLookup(Cat(left.fire, right.fire, beatReg_busy), 0.B)(
      Seq(
        "b000".U -> 0.B, // true
        "b001".U -> 1.B, // false
        //// "b010".U -> 1.B, // 不可能
        "b011".U -> 0.B, // false
        "b100".U -> 0.B,//更改逻辑：下面没好，不准打拍（原来的是下面没好，先打一拍存着） "b100".U -> 1.B, // true
        ////"b101".U -> 0.B, // 不可能
        ////"b110".U -> 1.B, // 不可能
        "b111".U -> 1.B // false
      )
    )
    beatReg_busy := beatReg_busy_wire && !clear
    (left_stage.out.bits =>> right_stage.in.bits).enable(beatReg_busy_wire && right.ready && (!right.fire && !left.fire && beatReg_busy)) //这个enable就是标志busy寄存器的wire，wire打一拍，数据打一拍，标志寄存器数据就和数据同步了
    //Change Log: enable参数，下面没准备好，就憋RegNext;也就是说，busy和enabel不是一个真值表
  }

}

abstract class Stage[+A <: BundlePlus, +B <: BundlePlus](_in: A, _out: B) {
  val in  = Wire(Flipped(Handshake(_in)))
  val out = Wire(Handshake(_out))

  def ALL_IOinit(): Unit = {
    in.ALL_IOinit()
    out.ALL_IOinit()
  }

//内部和valid ready逻辑
  val self_valid = Wire(Bool())
  val self_ready = Wire(Bool())
  self_valid := 0.B
  self_ready := 0.B
  private var if_self_valid_is_set = false
  private var if_self_ready_is_set = false
  def set_self_valid(valid: Bool): Unit = {
    self_valid := valid
    if_self_valid_is_set = true
  }
  def set_self_ready(ready: Bool): Unit = {
    self_ready := ready
    if_self_ready_is_set = true
  }
  def build_validready(): Unit = {
    require(
      if_self_valid_is_set && if_self_ready_is_set,
      "self_valid and self_ready must be set before build_validready"
    )
    def right = out
    def left  = in
    right.valid := self_valid
    left.ready  := right.ready || self_ready
  }

  def build(): Unit = {
    //this.build_validready() //初步逻辑
    //。。。请继续
  }
}
class PiplineStage[+A <: BundlePlus, +B <: BundlePlus](_in: A, _out: B) extends Stage[A, B](_in, _out) {
  private val Busy = RegInit(0.B)
  def getBusy(): Bool = Busy
  def full:      Bool = Busy
  def busy:      Bool = Busy
  def setBusy(busyOrNot: Bool): Unit = {
    Busy := busyOrNot
  }
  override def build(): Unit = {
    this.build_validready()
  }
}
class PiplineStageWithoutDepth[+A <: BundlePlus, +B <: BundlePlus](_in: A, _out: B) extends Stage[A, B](_in, _out) {
  override def build_validready(): Unit = {
    out.valid := in.fire
    in.ready  := out.ready
  }
  override def build(): Unit = {
    this.build_validready()

    /*
    @member :
    val in   = Handshake(_in)
    val out  = Handshake(_out)
     */

    //(IFStage.out.bits =>> IDStage.in.bits).enable(true.B)
    //val ID_busy = RegInit(0.B)

    /* val left  = IFStage.out
    val right = IDStage.in
    left.ready  := !ID_busy || right.ready
    right.valid := ID_busy
    ID_busy := MuxLookup(Cat(left.fire, right.fire, ID_busy), 0.U)(
      Seq(
        "b000".U -> 0.B, // true
        "b001".U -> 1.B, // false
        //// "b010".U -> 1.B, // 不可能
        "b011".U -> 0.B, // false
        "b100".U -> 1.B, // true
        ////"b101".U -> 0.B, // 不可能
        ////"b110".U -> 1.B, // 不可能
        "b111".U -> 1.B // false
      )
    ) */

  }
}
