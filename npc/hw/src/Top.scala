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
  def apply(idx: Int): UInt = {
    reg(idx)
  }
  def apply(idx: UInt): UInt = {
    reg(idx)
  }
  import chisel3.experimental.{prefix, SourceInfo}
  // final def :=(that: => Data)(implicit sourceInfo: SourceInfo): Unit = {
  //     this.:=(that)(sourceInfo)
  // }
}

class Top extends Module {
  val io = IO(new Bundle {
    val IMem = new Bundle {
      val readAddr = Output(UInt(32.W))
      val readData = Input(UInt(32.W))
    }

    val DMem = new Bundle {
      val readAddr  = Output(UInt(32.W))
      val readData  = Input(UInt(32.W))
      val writeAddr = Output(UInt(32.W))
      val writeData = Output(UInt(32.W))
    }
  })

  io:= DontCare

  val reg = new RegFile("RISCV32E")
  val pc  = RegInit(0.U(32.W))

  //fetch inst
  io.IMem.readAddr := pc
  val inst = io.IMem.readData

  //decode
  RVIInstr.table.map((m) => {
    println(m._1, m._2)
  })
  //exec

}
