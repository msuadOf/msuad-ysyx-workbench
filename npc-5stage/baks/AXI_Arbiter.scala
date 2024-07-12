import chisel3._
import chisel3.util._

import chisel3.util.experimental._

class AXI_Arbiter(val datawidth: Int = 32, val n: Int = 2) extends Module {
  val io = IO(new Bundle {
    val in  = Flipped(Vec(n, new AXIIO(datawidth)))
    val out = new AXIIO(datawidth)
  })
  for (i <- n - 1 to 0 by -1) {
    io.in(i).Flipped_IOinit()
  }
  io.out.IOinit()

  val sel  = RegInit(0.U(log2Ceil(n).W))
  val busy = RegInit(0.B)

  val chosen = Wire(UInt(log2Ceil(n).W))
  //request:AW/W、AR
  chosen := (n - 1).asUInt
  for (i <- n - 2 to 0 by -1) {
    when(io.in(i).rw_request()) {
      chosen := i.asUInt
    }
  }
  for (i <- n - 1 to 0 by -1) {
    when(io.in(i).rw_request() && busy === 0.B) {
      sel  := chosen
      busy := 1.B
    }
  }
  //end resp:R、B
  when(io.out.rw_eop()) {
    busy := 0.B
  }

  for (i <- n - 1 to 0 by -1) {
    when(sel === i.U) {
      io.out <> io.in(i)
    }
  }

}
