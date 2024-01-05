import chisel3._
import chisel3.util._

class ebreakDpi extends BlackBox with HasBlackBoxInline{
    val io=IO(new Bundle {
        val inst=Input(UInt(32.W))
    })
    setInline("chisel_ebreak_dpi.v",
        """
        |import "DPI-C" function void ebreak();
        |module EBREAK(
        |    input wire [31:0] inst_i
        |);
        |always @(*) begin
        |    if(inst_i == 32'b0000000_00001_00000_000_00000_11100_11) 
        |    ebreak();       
        |end
        |endmodule
        """.stripMargin
    )
}