package core

import chisel3._
import chisel3.util._
import core.utils._


class Ebreak extends BlackBox with HasBlackBoxInline {
  val io = IO(new Bundle {
    val en=Input(Bool())
  })
  def apply(en:Bool):Unit={
    io.en:=en
  }
  setInline(
    "Ebreak.v",
    """module Ebreak(
      |    input         en
      |);
      |import "DPI-C" function void ebreak();
      |always @* begin
      |  if(en) ebreak();
      |end
      |endmodule
    """.stripMargin
  )
}