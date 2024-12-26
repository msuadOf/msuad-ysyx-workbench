package core.utils
import chisel3._
import chisel3.util._

trait OverrideIOinit {
  def IOinit[T <: Data](value:         T): Unit
  def Flipped_IOinit[T <: Data](value: T): Unit
}
trait WithIOInit {
  def getElements: Seq[Data]
  def IOinit[T <: Data](value: T): Unit = this.getElements.foreach(x =>
    x match {
      case b: chisel3.Bits   => if (chisel3.reflect.DataMirror.directionOf(b) == ActualDirection.Output) b := value
      case e: BundlePlus     => if (chisel3.reflect.DataMirror.directionOf(e) == ActualDirection.Output) e.IOinit(value)
      case v: OverrideIOinit => if (chisel3.reflect.DataMirror.directionOf(v) == ActualDirection.Output) v.IOinit(value)
      case _ => throw new IllegalArgumentException(s"Unknown type(${x}) in IOinit")
    }
  )
  def Flipped_IOinit[T <: Data](value: T): Unit = this.getElements.foreach(x =>
    x match {
      case b: chisel3.Bits   => if (chisel3.reflect.DataMirror.directionOf(b) == ActualDirection.Input) b := value
      case e: BundlePlus     => if (chisel3.reflect.DataMirror.directionOf(e) == ActualDirection.Input) e.IOinit(value)
      case v: OverrideIOinit => if (chisel3.reflect.DataMirror.directionOf(v) == ActualDirection.Output) v.IOinit(value)
      case _ => throw new IllegalArgumentException(s"Unknown type(${x}) in IOinit")
    }
  )
  def IOinit(): Unit = {
    this.IOinit(0.U)
  }
  def IODontCare(): Unit = {
    this.IOinit(DontCare)
  }
  def Flipped_IOinit(): Unit = {
    Flipped_IOinit(0.U)
  }
  def Flipped_IODontCare(): Unit = {
    Flipped_IOinit(DontCare)
  }
  def ALLDontCare(): Unit = {
    IODontCare()
    Flipped_IODontCare()
  }
  def ALL_IOinit(): Unit = {
    IOinit()
    Flipped_IOinit()
  }
}
abstract class BundlePlus extends Bundle with WithIOInit {
  def do_=>>[T <: BundlePlus](enable: Bool,clear:Bool)(that: T): Unit = {
    val this_wirelist = this.getElements
    val that_wirelist = that.getElements
    (this_wirelist.zip(that_wirelist)).foreach {
      case (thiswire, thatwire) => {
        thatwire := RegEnable(Mux(clear,0.U.asTypeOf(chiselTypeOf(thiswire)),thiswire), 0.U, enable)
      }
    }
  }
  // for =>> operator like "(A =>> B).enable(C.asBool)"
  class StageConnect_CallChain(left: BundlePlus, right: BundlePlus) {
    def enable(enable_bool: Bool,clear:Bool): Unit = {
      left.do_=>>(enable_bool,clear)(right)
    }
  }
  def =>>(that: BundlePlus) = new StageConnect_CallChain(this, that)
}

trait IOInitImpl {
  def IOIIInit[T <: Data](value: T): Unit = {}
  def Flipped_IOIIInit[T <: Data](value: T): Unit = {}
}
trait StageBeatsImpl {
  def _do_NOTUSED_=>>[T <: BundlePlus](enable: Bool)(that: T): Unit = {}
}
class BundlePlusImpl extends BundlePlus with StageBeatsImpl with IOInitImpl
