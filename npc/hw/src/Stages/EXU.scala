package npc.Stages

import npc.misc._

import chisel3._
import chisel3.util._

import chisel3.util.experimental._
import chisel3.experimental.prefix

import scala.reflect.runtime.universe._
object getVariableName {
  def apply(variable: Any): String = {
    val mirror         = runtimeMirror(variable.getClass.getClassLoader)
    val instanceMirror = mirror.reflect(variable)
    val symbol         = instanceMirror.symbol
    symbol.name.toString
  }
}

class EXUMessage extends Bundle {
  val inst = Output(UInt(32.W))
}

class EXU extends Module {
  val io = IO(new Bundle {
    val in  = Flipped(Decoupled(new IDUMessage))
    val out = Decoupled(new EXUMessage)
  })
  val inst = Wire(UInt(32.W))
  inst := io.in.bits.inst
    val Decoder = new ExecEnv(inst, pc, R, csr, io.DMem)
    
    RVIInstr.table
    .asInstanceOf[Array[((BitPat, Any), ExecEnv => Any)]]
    .foreach((t: ((BitPat, Any), ExecEnv => Any)) => {
      prefix(s"InstMatch_${getVariableName(t._1._1)}") {
        when(t._1._1 === inst) {
          Decoder.IDLE()
          t._2(Decoder)
          if (t._1._1 == RV32I_ALUInstr.ADDI) {
            printf("ADDI\n")
          }
          printf(p"Inst_Decode:${(t._1)}\n");
        }
      }
    })
}
