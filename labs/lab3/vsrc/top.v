
module top (
    input         clk,
    input         rst,
    input  [10:0] sw,
    output [15:0] ledr

);
  reg [2:0] op;
  reg [3:0] a, b, c;
  assign op=sw[10:8];
  assign a=sw[7:4];
  assign b=sw[3:0];
  assign ledr[3:0]=c;
  always @(*) begin
    case (op)
      3'b000:  c = a + b;
      3'b001:  c = a - b;
      3'b010:  c = ~a;
      3'b011:  c = a & b;
      3'b100:  c = a | b;
      3'b101:  c = a ^ b;
      3'b110:  c = {3'b0,a < b};
      3'b111:  c = {3'b0,a == b};
      default: c = 0;
    endcase
  end
endmodule
