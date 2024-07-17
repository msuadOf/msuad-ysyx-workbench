package core
import chisel3._
import chisel3.util._
import chisel3.experimental._
import scala.collection.mutable

class Stageable[T <: Data](val dataType: T) {}

class Stage {
  val inputs = mutable.LinkedHashMap[Stageable[Data], Data]()
  val outputs = mutable.LinkedHashMap[Stageable[Data], Data]()
  val signals = mutable.LinkedHashMap[Stageable[Data], Data]()
  val inserts = mutable.LinkedHashMap[Stageable[Data], Data]()

  val inputsDefault = mutable.LinkedHashMap[Stageable[Data], Data]()
  val outputsDefault = mutable.LinkedHashMap[Stageable[Data], Data]()

  def INPUT[T <: Data](key: Stageable[T]): T = {
    inputs
      .getOrElseUpdate(
        key.asInstanceOf[Stageable[Data]],
        
      )
      .asInstanceOf[T]
  }
}
