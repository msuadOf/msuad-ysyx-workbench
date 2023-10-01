
module top (
    input            clk,
    input            rst,
    input  [    1:0] Y,
    input  [4*2-1:0] X,
    output [  2-1:0] F
);
  MuxKeyWithDefault #(4, 2, 2) i0 (
      F,
      Y,
      2'b0,
      {2'b00, X[1:0],
       2'b01, X[3:2],
       2'b10, X[5:4],
       2'b11, X[7:6]}
  );

endmodule
