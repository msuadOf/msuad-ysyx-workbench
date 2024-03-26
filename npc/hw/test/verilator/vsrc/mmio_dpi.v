import "DPI-C" function void mmio_write(input  int addr, input int len, input  int data);
import "DPI-C" function  int mmio_read(input  int addr, input int len);
module mmio_dpi (
        input wire clk,
        input wire reset,

        /* AR */
        input wire  [31:0] arAddr,
        input wire [32-1:0] arWidth,
        input wire arValid,
        output reg arReady,
        /* R */
        output reg [31:0] rData,
        output reg rValid,
        input wire rReady,

        /* AW/W */
        input wire [31:0] wAddr ,
        input wire [31:0] wData ,
        input wire [32-1:0] wWidth,
        input wire wValid,
        output reg wReady

    );

    // always @(posedge clk) begin
    //     if(reset) begin
    //         rValid<=1;
    //     end
    //     else begin
    //         //...
    //     end
    // end

    //AR
    localparam AR_state_IDLE=3'b001;
    localparam AR_state_WAIT=3'b010;
    localparam AR_state_WORK=3'b100;
    reg [3-1:0] AR_state= AR_state_IDLE,AR_state_next;
    always @(posedge clk) begin
        if(reset) begin
            AR_state<=AR_state_IDLE;
        end
        else begin
            AR_state<=AR_state_next;
        end
    end
    always @(*) begin
        case (AR_state)
            AR_state_IDLE: begin
                AR_state_next=AR_state_WAIT;
            end
            AR_state_WAIT: begin
                if(arValid==1)
                    AR_state_next=AR_state_WORK;
                else
                    AR_state_next=AR_state_WAIT;
            end
            AR_state_WORK: begin
                //写入完成后进入IDLE
                if(1)
                    AR_state_next=AR_state_IDLE;
            end
            default:
                AR_state_next=AR_state_IDLE;
        endcase
    end
reg read_enbale;
    always @(*) begin
        case (AR_state)
            AR_state_IDLE: begin
                arReady=0;
                read_enbale=0;
            end
            AR_state_WAIT: begin
                arReady=1;
                read_enbale=0;
            end
            AR_state_WORK: begin
                arReady=0;
                read_enbale=1;
            end
            default: begin
                arReady=0;
                read_enbale=0;
            end
        endcase
    end
always @(posedge clk ) begin
  if(reset)
    begin
        rData<=0;
    end
    else begin
        if(read_enbale) begin
            rData<=mmio_read(arAddr,arWidth);
        end 
        else rData<=rData;
    end
end

//R
localparam R_state_IDLE=3'b001;
localparam R_state_WAIT=3'b010;
reg [3-1:0] R_state= R_state_IDLE,R_state_next;
always @(posedge clk) begin
    if(reset) begin
        R_state<=R_state_IDLE;
    end
    else begin
        R_state<=R_state_next;
    end
end
always @(*) begin
    case (R_state)
        R_state_IDLE: begin
            if (read_enbale==1) begin
                R_state_next=R_state_WAIT;
            end 
            else begin
                R_state_next=R_state_IDLE;
            end
        end
        R_state_WAIT: begin
            if(rReady==1)
                R_state_next=R_state_IDLE;
            else
                R_state_next=R_state_WAIT;
        end
        
        default:
            R_state_next=R_state_IDLE;
    endcase
end

always @(*) begin
    case (R_state)
        R_state_IDLE: begin
            rValid=0;
        end
        R_state_WAIT: begin
            rValid=1;
        end
        default: begin
            rValid=0;
        end
    endcase
end

    //W
    localparam W_state_IDLE=3'b001;
    localparam W_state_WAIT=3'b010;
    localparam W_state_WORK=3'b100;
    reg [3-1:0] W_state= W_state_IDLE,W_state_next;
    always @(posedge clk) begin
        if(reset) begin
            W_state<=W_state_IDLE;
        end
        else begin
            W_state<=W_state_next;
        end
    end
    always @(*) begin
        case (W_state)
            W_state_IDLE: begin
                W_state_next=W_state_WAIT;
            end
            W_state_WAIT: begin
                if(wValid==1)
                    W_state_next=W_state_WORK;
                else
                    W_state_next=W_state_WAIT;
            end
            W_state_WORK: begin
                //写入完成后进入IDLE
                if(1)
                    W_state_next=W_state_IDLE;
            end
            default:
                W_state_next=W_state_IDLE;
        endcase
    end

    always @(*) begin
        case (W_state)
            W_state_IDLE: begin
                wReady=0;
            end
            W_state_WAIT: begin
                wReady=1;
            end
            W_state_WORK: begin
                wReady=0;
                mmio_write(wAddr,wWidth,wData);
            end
            default: begin
                wReady=0;
            end
        endcase
    end

endmodule //mmio_dpi
