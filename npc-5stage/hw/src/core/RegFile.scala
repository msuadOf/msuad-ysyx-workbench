package core
import chisel3._
import chisel3.util._

class PC(init: UInt) {
  private val pc_en   = Wire(Bool())
  private val dnpc    = Wire(UInt(32.W))
  private val pc_wire = Wire(UInt(32.W))
  val pc      = RegEnable(Mux(pc_en, dnpc, pc_wire), "h80000000".U(32.W), 1.B)
  private val snpc    = pc + 4.U

  def init(): Unit = {
      pc_wire := pc
  dnpc    := snpc
  pc_en   := 1.B
  }
  def read = pc
//   def write(idx:UInt,data:UInt) = Mux( (idx===0.U) , reg(32) ,reg(idx) ) :=data
  def write(data: UInt) = dnpc := data
  def write(enable: Bool, data: UInt) = {
    when(enable){
      // pc_en := enable
      dnpc  := Mux(enable, data, snpc)
    }
  }
  def stopWhen(stop_en:Bool):Unit={
    when(stop_en){pc_en:=0.B}
  }
}
class RegFile(val ISet: String) {
  val regNum = ISet match {
    case "RISCV32E" => 16
    case "RISCV32"  => 32
    case "RISCV64"  => 64
    case _: String =>
      throw new IllegalArgumentException(
        "RegFile() args should be [RISCV32E] [RISCV32] [RISCV64]"
      )
  }
  val reg = RegInit(VecInit(Seq.tabulate(regNum)(i => 0.U(32.W))))

  def read(idx: UInt) = Mux((idx === 0.U), 0.U, reg(idx))

//   def write(idx:UInt,data:UInt) = Mux( (idx===0.U) , reg(32) ,reg(idx) ) :=data
  def write(idx: UInt, data: UInt) = reg(idx) := Mux((idx === 0.U), 0.U, data)
  def write(enable: Bool, idx: UInt, data: UInt) = {
    reg(idx) := Mux((idx === 0.U), 0.U, Mux(enable, data, reg(idx)))
  }

  private def apply(idx: UInt): UInt = {
    return Mux(idx === 0.U, RegEnable(0.U, 0.B), reg(idx)) //FIXME: 这啥逻辑？？应该是错的
  }
}
