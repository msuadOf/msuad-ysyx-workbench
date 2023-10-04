
module top (
    input         clk,
    input         rst,
    input  [10:0] sw,
    output [15:0] ledr,

    output [7:0] o_seg0,
    output [7:0] o_seg1,
    output [7:0] o_seg2,
    output [7:0] o_seg3,
    output [7:0] o_seg4,
    output [7:0] o_seg5,
    output [7:0] o_seg6,
    output [7:0] o_seg7

);
  wire          clk_s;
  wire [24-1:0] num;

  assign clk_s = sw[0];

  reg [7:0] sR=8'b0100_0001;
  always @(posedge clk_s or posedge rst) begin
    if (rst) begin
      sR <= 8'b0100_0001;
    end else begin
      sR <= {sR[4] ^ sR[3] ^ sR[2] ^ sR[0], sR[7:1]};
    end
  end

assign  ledr= {8'hFF, sR};
  assign num = {16'd0, sR};

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

  assign o_seg0      = ~segs_lut[num[2:0]];
  assign o_seg1      = ~segs_lut[num[5:3]];
  assign o_seg2      = ~segs_lut[num[8:6]];
  assign o_seg3      = ~segs_lut[num[11:9]];
  assign o_seg4      = ~segs_lut[num[14:12]];
  assign o_seg5      = ~segs_lut[num[17:15]];
  assign o_seg6      = ~segs_lut[num[20:18]];
  assign o_seg7      = ~segs_lut[num[23:21]];

endmodule
