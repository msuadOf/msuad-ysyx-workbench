package npc.misc

import chisel3._
import chisel3.util._

class RegFile(val ISet: String) {
  val regNum = ISet match {
    case "RISCV32E" => 16
    case "RISCV32"  => 32
    case "RISCV64"  => 64
    case _: String => throw new IllegalArgumentException("RegFile() args should be [RISCV32E] [RISCV32] [RISCV64]")
  }
  val reg = RegInit(VecInit(Seq.tabulate(regNum)(i => 0.U(32.W))))

  def read(idx: UInt) = reg(idx)

//   def write(idx:UInt,data:UInt) = Mux( (idx===0.U) , reg(32) ,reg(idx) ) :=data
  def write(idx: UInt, data: UInt) = reg(idx) := Mux((idx === 0.U), 0.U, data)

  def apply(idx: Int): UInt = {
    reg(idx)
  }
  import chisel3.experimental.{prefix, SourceInfo}
  // final def :=(that: => Data)(implicit sourceInfo: SourceInfo): Unit = {
  //     this.:=(that)(sourceInfo)
  // }
}
