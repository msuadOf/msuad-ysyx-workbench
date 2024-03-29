import chisel3._
import chisel3.util._

import chisel3.util.experimental._
import chisel3.experimental.prefix

import scala.reflect.runtime.universe._

import Stages._

class NPCIO extends Bundle {}

class NPC extends Module {
  val io = IO(new NPCIO)

  val ifu = Module(new IFU)
  val idu = Module(new IDU)
  val exu = Module(new EXU)
  val wbu = Module(new WBU)

  StageConnect(ifu.io.out, idu.io.in)
  StageConnect(idu.io.out, exu.io.in)
  StageConnect(exu.io.out, wbu.io.in)
  // ...
}

object StageConnect {
  def apply[T <: Data](left: DecoupledIO[T], right: DecoupledIO[T]) = {
    val arch = "single"
    // 为展示抽象的思想, 此处代码省略了若干细节
    if (arch == "single") { right.bits := left.bits }
    else if (arch == "multi") { right <> left }
    else if (arch == "pipeline") { right <> RegEnable(left, left.fire) }
    else if (arch == "ooo") { right <> Queue(left, 16) }
  }
}
