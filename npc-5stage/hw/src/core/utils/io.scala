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
abstract class BundleWithIOInit extends Bundle with WithIOInit
class BundleWithIOInitImpl extends BundleWithIOInit {
  def IOinit[T <: Data](value: T): Unit = {}
  def Flipped_IOinit[T <: Data](value: T): Unit = {}
}
