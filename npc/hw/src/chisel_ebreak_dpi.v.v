import "DPI-C" function void ebreak();
module ebreakDpi(
    input wire [31:0] inst_i
);
always @(*) begin
    if(inst_i == 32'b0000000_00001_00000_000_00000_11100_11) 
    ebreak();       
end
endmodule