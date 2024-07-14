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
abstract class BundleWithIOInit extends Bundle with WithIOInit {
  def =>>[T <: BundleWithIOInit](enable: Bool)(that: T): Unit
}
trait IOInitImpl {
  def IOinit[T <: Data](value: T): Unit = {}
  def Flipped_IOinit[T <: Data](value: T): Unit = {}
}
trait StageBeatsImpl {
  def =>>[T <: BundleWithIOInit](enable: Bool)(that: T): Unit = {}
}
class BundleWithIOInitImpl extends BundleWithIOInit with StageBeatsImpl with IOInitImpl
class StageConnect_CallChain(left: BundleWithIOInit, right: BundleWithIOInit) {
  def enable(enable_bool: Bool): Unit = {
    left.=>>(enable_bool)(right)
  }
}
