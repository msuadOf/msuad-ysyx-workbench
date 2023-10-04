
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
    output [7:0] o_seg7,

    input      ps2_clk,
    input wire ps2_data
);


  //ps_val 2 ASCII LUT
  wire [7:0] ps2_val2ascii_lut[255:0];
  assign ps2_val2ascii_lut[8'h1c] = "A";
  assign ps2_val2ascii_lut[8'h32] = "B";
  assign ps2_val2ascii_lut[8'h21] = "C";
  assign ps2_val2ascii_lut[8'h23] = "D";
  assign ps2_val2ascii_lut[8'h24] = "E";
  assign ps2_val2ascii_lut[8'h2B] = "F";
  assign ps2_val2ascii_lut[8'h34] = "G";
  assign ps2_val2ascii_lut[8'h33] = "H";
  assign ps2_val2ascii_lut[8'h43] = "I";
  assign ps2_val2ascii_lut[8'h3B] = "J";
  assign ps2_val2ascii_lut[8'h42] = "K";
  assign ps2_val2ascii_lut[8'h4B] = "L";
  assign ps2_val2ascii_lut[8'h3A] = "M";
  assign ps2_val2ascii_lut[8'h31] = "N";
  assign ps2_val2ascii_lut[8'h44] = "O";
  assign ps2_val2ascii_lut[8'h4D] = "P";
  assign ps2_val2ascii_lut[8'h15] = "Q";
  assign ps2_val2ascii_lut[8'h2D] = "R";
  assign ps2_val2ascii_lut[8'h1B] = "S";
  assign ps2_val2ascii_lut[8'h2C] = "T";
  assign ps2_val2ascii_lut[8'h3C] = "U";
  assign ps2_val2ascii_lut[8'h2A] = "V";
  assign ps2_val2ascii_lut[8'h1D] = "W";
  assign ps2_val2ascii_lut[8'h22] = "X";
  assign ps2_val2ascii_lut[8'h35] = "Y";
  assign ps2_val2ascii_lut[8'h1A] = "Z";
  assign ps2_val2ascii_lut[8'h16] = "1";
  assign ps2_val2ascii_lut[8'h1E] = "2";
  assign ps2_val2ascii_lut[8'h26] = "3";
  assign ps2_val2ascii_lut[8'h25] = "4";
  assign ps2_val2ascii_lut[8'h2E] = "5";
  assign ps2_val2ascii_lut[8'h36] = "6";
  assign ps2_val2ascii_lut[8'h3D] = "7";
  assign ps2_val2ascii_lut[8'h3E] = "8";
  assign ps2_val2ascii_lut[8'h46] = "9";
  assign ps2_val2ascii_lut[8'h45] = "0";

  //ps2 vars for use
  wire [7:0] ps_val;
  wire [7:0] ps_ascii;
  assign ps_val   = buffer[8:1];
  assign ps_ascii = ps2_val2ascii_lut[ps_val];


  //ps2
  reg [9:0] buffer;  // ps2_data bits
  reg [3:0] count;  // count ps2_data bits
  reg [2:0] ps2_clk_sync;

  always @(posedge clk) begin
    ps2_clk_sync <= {ps2_clk_sync[1:0], ps2_clk};
  end

  wire sampling = ps2_clk_sync[2] & ~ps2_clk_sync[1];

  always @(posedge clk) begin
    if (rst) begin  // reset
      count <= 0;
    end else begin
      if (sampling) begin
        if (count == 4'd10) begin
          if ((buffer[0] == 0) &&  // start bit
              (ps2_data) &&  // stop bit
              (^buffer[9:1])) begin  // odd  parity
            //$display("receive %x", buffer[8:1]);
            $display("receive %s", (ps_val != 8'hF0) ? {ps_ascii, 8'd0} : "||");
          end
          count <= 0;  // for next
        end else begin
          buffer[count] <= ps2_data;  // store ps2_data
          count         <= count + 3'b1;
        end
      end
    end
  end


  //ps2_single_pulse
  reg ps2_single_pulse = 0;
  reg ps2_single_ready = 0;
  reg ps2_single_ready_pipe = 0;
  always @(posedge clk) begin
    if (rst) begin
      ps2_single_ready <= 0;
    end else begin
      if (count == 4'd10) begin
        ps2_single_ready <= 1;
        //$display("==========");

      end else begin
        ps2_single_ready <= 0;
      end
    end
  end
  always @(posedge clk) begin
    if (rst) begin
      ps2_single_ready_pipe <= 0;
    end else begin
      ps2_single_ready_pipe <= ps2_single_ready;
    end
  end
  assign ps2_single_pulse = ps2_single_ready & ~ps2_single_ready_pipe;

  reg [ 7:0] ps2_data_buffer      [2:0];
  reg [ 7:0] ps2_now_click;
  reg        ps2_now_click_en = 0;

  reg [31:0] ps_press_cnt = 0;
  always @(posedge clk) begin
    if (rst) begin
      ps2_data_buffer[0] <= 0;
      ps2_data_buffer[1] <= 0;
      ps2_data_buffer[2] <= 0;

      ps2_now_click      <= 0;
      ps2_now_click_en   <= 0;

      ps_press_cnt       <= 0;
    end else begin
      if (ps2_single_pulse) begin
        $display("==ps2_single_pulse==");
        ps2_data_buffer[0] <= ps_val;
        ps2_data_buffer[1] <= ps2_data_buffer[0];
        ps2_data_buffer[2] <= ps2_data_buffer[1];
        if (ps2_data_buffer[0] == 8'hF0 && ps2_data_buffer[1] == ps_val) begin
          ps2_now_click_en <= 0;
          ps_press_cnt     <= ps_press_cnt + 1;
          $display("==%s:%d==", ps_ascii, ps_press_cnt);  //按下按键触发一次
        end else begin
          ps2_now_click_en <= 1;
          ps2_now_click    <= ps_val;
        end

      end
    end
  end


  //segs
  wire [7:0] segs_lut[15:0];
  assign segs_lut[0]  = 8'b11111101;
  assign segs_lut[1]  = 8'b01100000;
  assign segs_lut[2]  = 8'b11011010;
  assign segs_lut[3]  = 8'b11110010;
  assign segs_lut[4]  = 8'b01100110;
  assign segs_lut[5]  = 8'b10110110;
  assign segs_lut[6]  = 8'b10111110;
  assign segs_lut[7]  = 8'b11100000;
  assign segs_lut[8]  = 8'b11111111;
  assign segs_lut[9]  = 8'b11110111;
  assign segs_lut[10] = 8'b1110_1111;
  assign segs_lut[11] = 8'b0011_1111;
  assign segs_lut[12] = 8'b1001_1101;
  assign segs_lut[13] = 8'b0111_1011;
  assign segs_lut[14] = 8'b1001_1111;
  assign segs_lut[15] = 8'b1000_1111;

  assign o_seg0       = ~segs_lut[ps_val[3:0]];//
  assign o_seg1       = ~segs_lut[ps_val[7:4]];
  assign o_seg2       = ~segs_lut[ps_ascii[3:0]];
  assign o_seg3       = ~segs_lut[ps_ascii[7:4]];
  assign o_seg4       = ~segs_lut[0];
  assign o_seg5       = ~segs_lut[0];
  assign o_seg6       = ~segs_lut[0];
  assign o_seg7       = ~segs_lut[0];

endmodule
