module ebreakDpi(
    input wire [31:0] inst
);
import "DPI-C" function void ebreak();
always @(*) begin
    if(inst == 32'b0000000_00001_00000_000_00000_11100_11)
    begin 
        ebreak(); 
    end
end
endmodule