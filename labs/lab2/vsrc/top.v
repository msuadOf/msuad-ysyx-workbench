
module top (
    input         clk,
    input         rst,
    input  [ 8:0] sw,
    output [15:0] ledr,
    output [ 7:0] o_seg0,
    output [ 7:0] o_seg1,
    output [ 7:0] o_seg2,
    output [ 7:0] o_seg3,
    output [ 7:0] o_seg4,
    output [ 7:0] o_seg5,
    output [ 7:0] o_seg6,
    output [ 7:0] o_seg7
);



  reg [8-1:0] encoder_i;
  always @(*) begin
    if (!sw[8]) begin
      encoder_i=0;
      ledr[4]=1;
    end else begin
      ledr[4]=0;
      casez (sw[7:0])
        8'b0000_0001: encoder_i = 1 - 1;
        8'b0000_001?: encoder_i = 2 - 1;
        8'b0000_01??: encoder_i = 3 - 1;
        8'b0000_1???: encoder_i = 4 - 1;
        8'b0001_????: encoder_i = 5 - 1;
        8'b001?_????: encoder_i = 6 - 1;
        8'b01??_????: encoder_i = 7 - 1;
        8'b1???_????: encoder_i = 8 - 1;
        default: encoder_i = 0;
      endcase
    end

  end

assign ledr[2:0]=encoder_i[2:0];

  //segs
  wire [7:0] segs_lut[7:0];
  assign segs_lut[0] = 8'b11111101;
  assign segs_lut[1] = 8'b01100000;
  assign segs_lut[2] = 8'b11011010;
  assign segs_lut[3] = 8'b11110010;
  assign segs_lut[4] = 8'b01100110;
  assign segs_lut[5] = 8'b10110110;
  assign segs_lut[6] = 8'b10111110;
  assign segs_lut[7] = 8'b11100000;

  assign o_seg0      = ~segs_lut[encoder_i[2:0]];
  assign o_seg1      = ~segs_lut[3'd0];
  assign o_seg2      = ~segs_lut[3'd0];
  assign o_seg3      = ~segs_lut[3'd0];
  assign o_seg4      = ~segs_lut[3'd0];
  assign o_seg5      = ~segs_lut[3'd0];
  assign o_seg6      = ~segs_lut[3'd0];
  assign o_seg7      = ~segs_lut[3'd0];

endmodule
