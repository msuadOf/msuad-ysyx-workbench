package core.utils
import chisel3._
import chisel3.util._

trait WithIOInit {
  def IOinit[T <: Data](value:         T): Unit
  def Flipped_IOinit[T <: Data](value: T): Unit
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
  def do_=>>[T <: BundlePlus](enable: Bool)(that: T): Unit
  // for =>> operator like "(A =>> B).enable(C.asBool)"
  class StageConnect_CallChain(left: BundlePlus, right: BundlePlus) {
    def enable(enable_bool: Bool): Unit = {
      left.do_=>>(enable_bool)(right)
    }
  }
  def =>>(that: BundlePlus) = new StageConnect_CallChain(this, that)
}
trait IOInitImpl {
  def IOinit[T <: Data](value: T): Unit = {}
  def Flipped_IOinit[T <: Data](value: T): Unit = {}
}
trait StageBeatsImpl {
  def do_=>>[T <: BundlePlus](enable: Bool)(that: T): Unit = {}
}
class BundlePlusImpl extends BundlePlus with StageBeatsImpl with IOInitImpl
