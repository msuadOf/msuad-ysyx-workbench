import chisel3._
import chisel3.util._

class IFU extends Module {
  val io = IO(new Bundle {
    val mmio = Flipped(new mmioIO)
    val Inst = Flipped(new InstIO)
  })

  io.mmio.AR.arWidth := 4.U
  io.mmio.AR.arValid := 0.U

  io.mmio.R.rReady := 0.U

  io.mmio.simpleW.wAddr  := 0.U
  io.mmio.simpleW.wData  := 0.U
  io.mmio.simpleW.wWidth := 0.U
  io.mmio.simpleW.wValid := 0.U

  val I_en    = RegInit(1.U)
  val data_in = RegInit(0.U)

  val sIDLE :: sARwaiting :: sARcplt_Rwaiting :: sRcplt :: Nil = Enum(4)

  val R_state = RegInit(sIDLE)

  val Inst_rValid_r = RegInit(0.U)
  Inst_rValid_r  := 0.U //Inst data - vld default
  io.Inst.rValid := Inst_rValid_r //Inst data - vld
  io.Inst.rData  := data_in //Inst data
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
      when(io.mmio.AR.arReady === 1.U) {
        R_state := sARcplt_Rwaiting
      }.otherwise {
        R_state := sARwaiting
      }
    }
    is(sARcplt_Rwaiting) {
      when(io.mmio.R.rValid === 1.U) {
        R_state       := sRcplt
        data_in       := io.mmio.R.rData //data - satisfy timing
        Inst_rValid_r := 1.U //data vld - sysnc with data_in
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
  io.mmio.AR.arAddr := arAddr
  //---------------
  val addr_out   = Wire(UInt(32.W))
  val addr_out_r = RegInit("x80000000".U(32.W))
  addr_out   := io.Inst.rAddr
  addr_out_r := addr_out_r + 4.U

  switch(R_state) {
    is(sIDLE) {
      io.mmio.AR.arValid := 0.U
    }
    is(sARwaiting) {
      io.mmio.AR.arValid := 1.U
      arAddr             := addr_out //addr<-pc
    }
    is(sARcplt_Rwaiting) {
      io.mmio.AR.arValid := 0.U
      io.mmio.R.rReady   := 1.U
    }
    is(sRcplt) {
      io.mmio.R.rReady   := 0.U
      io.mmio.AR.arValid := 0.U
    }
  }
  printf("data_in=%d\n", data_in)
}
