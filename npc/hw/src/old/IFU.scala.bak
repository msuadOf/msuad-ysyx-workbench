import chisel3._
import chisel3.util._

class IFU extends Module {
  val io = IO(Flipped(new mmioIO))

  io.AR.arWidth := 4.U
  io.AR.arValid := 0.U

  io.R.rReady := 0.U

  io.simpleW.wAddr  := 0.U
  io.simpleW.wData  := 0.U
  io.simpleW.wWidth := 0.U
  io.simpleW.wValid := 0.U

  val I_en    = RegInit(1.U)
  val data_in = RegInit(0.U)

  val sIDLE :: sARwaiting :: sARcplt_Rwaiting :: sRcplt :: Nil = Enum(4)

  val R_state = RegInit(sIDLE)
//跳转
// R_state:=sIDLE
  switch(R_state) {
    is(sIDLE) {
      when(I_en === 1.U) {
        R_state := sARwaiting
      }.otherwise {
        R_state := sIDLE
      }
    }
    is(sARwaiting) {
      when(io.AR.arReady === 1.U) {
        R_state := sARcplt_Rwaiting
      }.otherwise {
        R_state := sARwaiting
      }
    }
    is(sARcplt_Rwaiting) {
      when(io.R.rValid === 1.U) {
        R_state := sRcplt
        data_in := io.R.rData //data - satisfy timing
      }.otherwise {
        R_state := sARcplt_Rwaiting
      }
    }
    is(sRcplt) {
      when(I_en === 1.U) {
        R_state := sARwaiting
      }.otherwise {
        R_state := sRcplt
      }
    }
  }
  //---- io reg ----
  val arAddr = RegInit(0.U)
  io.AR.arAddr := arAddr
  //---------------
  val addr_out   = Wire(UInt(32.W))
  val addr_out_r = RegInit("x80000000".U(32.W))
  addr_out   := addr_out_r
  addr_out_r := addr_out_r + 4.U

  switch(R_state) {
    is(sIDLE) {
      io.AR.arValid := 0.U
    }
    is(sARwaiting) {
      io.AR.arValid := 1.U
      arAddr        := addr_out //addr<-pc
    }
    is(sARcplt_Rwaiting) {
      io.AR.arValid := 0.U
      io.R.rReady   := 1.U
    }
    is(sRcplt) {
      io.R.rReady   := 0.U
      io.AR.arValid := 0.U
    }
  }
  printf("data_in=%d\n", data_in)
}