// import "DPI-C" function void paddr_write(paddr_t addr, int len, word_t data);
// import "DPI-C" function word_t paddr_read(paddr_t addr, int len);
// module mmio_dpi (
//         input wire clk,
//         input wire reset,

//         /* AR */
//         input wire  [31:0] arAddr,
//         input wire [4-1:0] arWidth,
//         input wire arValid,
//         output reg arReady,
// /* R */
//         output wire [31:0] rDdata,
//         output reg rValid,
//         input wire rReady,

// /* AW/W */
//         input wire [31:0] wAddr ,
//         input wire [31:0] wData ,
//         input wire [4-1:0] wWidth,
//         input wire wValid,
//         output reg wReady

//     );

//     // always @(posedge clk) begin
//     //     if(reset) begin
//     //         rValid<=1;
//     //     end
//     //     else begin
//     //         //...
//     //     end
//     // end
// //W

//     localparam W_state_IDLE=3'b001;
//     localparam W_state_WAIT=3'b010;
//     localparam W_state_WORK=3'b100;
//     reg [3-1:0] W_state= W_state_IDLE,W_state_next;
//     always @(posedge clk) begin
//         if(reset) begin
//             W_state<=W_state_IDLE;
//         end
//         else begin
//             W_state<=W_state_next;
//         end
//     end
//     always @(*) begin
//         case (W_state)
//             W_state_IDLE: begin   
//                 W_state_next=W_state_WAIT;
//             end
//             W_state_WAIT: begin
//                 if(wValid==1) W_state_next=W_state_WORK;
//                 else W_state_next=W_state_WAIT;
//             end
//             W_state_WORK: begin
//                 //写入完成后进入IDLE
//                 if(1) W_state_next=W_state_IDLE;
//             end
//             default: W_state_next=W_state_IDLE;
//         endcase
//     end

//     always @(*) begin
// case (W_state)
// W_state_IDLE: begin   
//     wReady=0;
// end
// W_state_WAIT: begin
//     wReady=1;
// end
// W_state_WORK: begin
//     wReady=0;
// end
// endcase
//     end

// endmodule //mmio_dpi
