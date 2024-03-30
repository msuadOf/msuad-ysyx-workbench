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

    /* AW */
    input wire [31:0] awAddr ,
    input wire [1:0]awPort,
    input wire awValid,
    output reg awReady,

    /* W */
    input wire [31:0] wData ,
    input wire [(32/8)-1:0] wStrb,
    input wire wValid,
    output reg wReady,

    /* B */
    output reg [1:0] bResp,
    output reg bValid,
    input wire bReady

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
  always @(posedge clk)
  begin
    if(reset)
    begin
      AR_state<=AR_state_IDLE;
    end
    else
    begin
      AR_state<=AR_state_next;
    end
  end
  always @(*)
  begin
    case (AR_state)
      AR_state_IDLE:
      begin
        AR_state_next=AR_state_WAIT;
      end
      AR_state_WAIT:
      begin
        if(arValid==1)
          AR_state_next=AR_state_WORK;
        else
          AR_state_next=AR_state_WAIT;
      end
      AR_state_WORK:
      begin
        //写入完成后进入IDLE
        if(1)
          AR_state_next=AR_state_IDLE;
      end
      default:
        AR_state_next=AR_state_IDLE;
    endcase
  end
  reg read_enbale;
  always @(*)
  begin
    case (AR_state)
      AR_state_IDLE:
      begin
        arReady=0;
        read_enbale=0;
      end
      AR_state_WAIT:
      begin
        arReady=1;
        read_enbale=0;
      end
      AR_state_WORK:
      begin
        arReady=0;
        read_enbale=1;
      end
      default:
      begin
        arReady=0;
        read_enbale=0;
      end
    endcase
  end
  always @(posedge clk )
  begin
    if(reset)
    begin
      rData<=0;
    end
    else
    begin
      if(read_enbale)
      begin
        rData<=mmio_read(arAddr,arWidth);
      end
      else
        rData<=rData;
    end
  end

  //R
  localparam R_state_IDLE=3'b001;
  localparam R_state_WAIT=3'b010;
  reg [3-1:0] R_state= R_state_IDLE,R_state_next;
  always @(posedge clk)
  begin
    if(reset)
    begin
      R_state<=R_state_IDLE;
    end
    else
    begin
      R_state<=R_state_next;
    end
  end
  always @(*)
  begin
    case (R_state)
      R_state_IDLE:
      begin
        if (read_enbale==1)
        begin
          R_state_next=R_state_WAIT;
        end
        else
        begin
          R_state_next=R_state_IDLE;
        end
      end
      R_state_WAIT:
      begin
        if(rReady==1)
          R_state_next=R_state_IDLE;
        else
          R_state_next=R_state_WAIT;
      end

      default:
        R_state_next=R_state_IDLE;
    endcase
  end

  always @(*)
  begin
    case (R_state)
      R_state_IDLE:
      begin
        rValid=0;
      end
      R_state_WAIT:
      begin
        rValid=1;
      end
      default:
      begin
        rValid=0;
      end
    endcase
  end

  /* Mw */
  reg Mr_ok;
  reg [31:0] Mw_addr,Mw_data;
  //B
  localparam RESP_OKAY=2'b00,
             RESP_EXOKAY=2'b01,
             RESP_SLVERR=2'b10,
             RESP_DECERR=2'b11;
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
                  if(bValid&&bReady)
                      W_state_next=W_state_IDLE;
                  else 
                    W_state_next=W_state_WORK;
              end
              default:
                  W_state_next=W_state_IDLE;
          endcase
      end
   
      always @(posedge clk) begin
        if(reset) begin
            wReady<=0;
            Mw_data<=0;
        end
        else begin
          
          case (W_state_next)
              W_state_IDLE: begin
                  wReady<=0;
              end
              W_state_WAIT: begin
                  wReady<=1;
              end
              W_state_WORK: begin
                  wReady<=0;
                  Mw_data<=wData;
                //   mmio_write(wAddr,wWidth,wData);
              end
              default: begin
                  wReady<=0;
              end
          endcase


        end
      end
//AW
      localparam AW_state_IDLE=3'b001;
      localparam AW_state_WAIT=3'b010;
      localparam AW_state_WORK=3'b100;
      reg [3-1:0] AW_state= AW_state_IDLE,AW_state_next;
      always @(posedge clk) begin
          if(reset) begin
              AW_state<=AW_state_IDLE;
          end
          else begin
              AW_state<=AW_state_next;
          end
      end
      always @(*) begin
          case (AW_state)
              AW_state_IDLE: begin
                  AW_state_next=AW_state_WAIT;
              end
              AW_state_WAIT: begin
                  if(awValid==1)
                      AW_state_next=AW_state_WORK;
                  else
                      AW_state_next=AW_state_WAIT;
              end
              AW_state_WORK: begin
                  //写入完成后进入IDLE
                if(bValid&&bReady)
                    AW_state_next=AW_state_IDLE;
                else 
                    AW_state_next=AW_state_WORK;
              end
              default:
                  AW_state_next=AW_state_IDLE;
          endcase
      end
   
      always @(posedge clk) begin
        if(reset) begin
            awReady<=0;
            Mw_addr<=0;
        end
        else begin

          case (AW_state_next)
              AW_state_IDLE: begin
                  awReady<=0;
              end
              AW_state_WAIT: begin
                  awReady<=1;
              end
              AW_state_WORK: begin
                  awReady<=0;
                  Mw_addr<=awAddr;
              end
              default: begin
                  awReady<=0;
              end
          endcase

          end
      end
//write
      always @(* ) begin

            Mr_ok=(AW_state==AW_state_WORK&&W_state==W_state_WORK);

      end
      always @(posedge clk ) begin
        if(reset)
          begin
            
          end
          else begin
            if(Mr_ok) mmio_write(Mw_addr,(wStrb==4'b0001)?(32'd8):(
                (wStrb==4'b0011)?(32'd16):(
                    (wStrb==4'b1111)?(32'd32):(32'd0)
                )
            ),Mw_data);
          end
      end

      //B
      localparam B_state_IDLE=3'b001;
      localparam B_state_WAIT=3'b010;
      reg [3-1:0] B_state= B_state_IDLE,B_state_next;
      always @(posedge clk) begin
          if(reset) begin
              B_state<=B_state_IDLE;
          end
          else begin
              B_state<=B_state_next;
          end
      end
      always @(*) begin
          case (B_state)
              B_state_IDLE: begin
                if(Mr_ok)
                  B_state_next=B_state_WAIT;
                  else 
                    B_state_next=B_state_IDLE;
              end
              B_state_WAIT: begin
                  if(awValid==1)
                      B_state_next=B_state_IDLE;
                  else
                      B_state_next=B_state_WAIT;
              end
              default:
                  B_state_next=B_state_IDLE;
          endcase
      end
   
      always @(*) begin
          case (B_state)
              B_state_IDLE: begin
                  bValid=0;
                  bResp=RESP_OKAY;
              end
              B_state_WAIT: begin
                bValid=1;
                  bResp=RESP_OKAY;
              end
              default: begin
                bValid=0;
                  bResp=RESP_OKAY;
              end
          endcase
      end
endmodule //mmio_dpi
