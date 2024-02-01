import chisel3._
import chisel3.util._

object panic{
  def apply(info:String)={
    chisel3.assert(0.B, "033 [31m"+info+"033 [0m"+"\n")
  }
}
object TODO{
  def apply()={
    panic("[Error]:The inst is not impleted")
  }
}