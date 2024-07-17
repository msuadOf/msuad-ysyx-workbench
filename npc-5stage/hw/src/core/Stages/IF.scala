package core.Stages

import chisel3._
import chisel3.util._
import core.utils._

import core._
import chisel3.util.experimental.BoringUtils


class InstFetchStage(_in: IFUIO, _out: IF2IDBundle) extends Stage(_in, _out) {
  val pc = RegInit("h80000000".U(32.W))
  override def build(): Unit = {
    super.build()
    val if_dpic = Module(new IF_DPIC)
    //TODO: this.in的IOinit

    out.bits.inst := if_dpic.io.inst
    out.bits.pc   := pc
    out.valid := if_dpic.io.inst_en
    if_dpic.io.pc_en:= 1.U
    if_dpic.io.pc:=pc
  }
}
class IF_DPIC extends BlackBox with HasBlackBoxInline {
  val io = IO(new Bundle {
    val pc      = Input(UInt(32.W))
    val pc_en   = Input(UInt(1.W))
    val inst    = Output(UInt(32.W))
    val inst_en = Output(UInt(1.W))
  })
  setInline(
    "IF_DPIC.v",
    """import "DPI-C" function  int mmio_read(input  int addr, input int len);
      |module IF_DPIC(
      |    input  [32-1:0] pc,
      |    input         pc_en,
      |    output reg [32-1:0] inst,
      |    output reg          inst_en
      |);
      |always @* begin
      |  if(pc_en) {inst_en,inst} = {1'b1,mmio_read(pc,4)};
      |  else {inst_en,inst} = {1'b0,32'h0};
      |end
      |endmodule
    """.stripMargin
  )
}
