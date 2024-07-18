#include "r_verilator.h"

int diff_en=0;

VerilatedContext* contextp;
Vtop* top;
 
VerilatedVcdC* tfp;
vluint64_t main_time = 0;  //initial 仿真时间
double sc_time_stamp()
{
	return main_time;
}
void verilator_runtime_init(int argc, char** argv){
        // Create logs/ directory in case we have traces to put under it
    Verilated::mkdir("logs");

    // Construct a VerilatedContext to hold simulation time, etc.
    // Multiple modules (made later below with Vtop) may share the same
    // context to share time, or modules may have different contexts if
    // they should be independent from each other.

    // Using unique_ptr is similar to
    // "VerilatedContext* contextp = new VerilatedContext" then deleting at end.
    // const std::unique_ptr<VerilatedContext> contextp{new VerilatedContext};
    contextp = new VerilatedContext;
    // Do not instead make Vtop as a file-scope static variable, as the
    // "C++ static initialization order fiasco" may cause a crash

    // Set debug level, 0 is off, 9 is highest presently used
    // May be overridden by commandArgs argument parsing
    contextp->debug(0);

    // Randomization reset policy
    // May be overridden by commandArgs argument parsing
    contextp->randReset(2);

    // Verilator must compute traced signals
    contextp->traceEverOn(true);

    // Pass arguments so Verilated code can see them, e.g. $value$plusargs
    // This needs to be called before you create any model
    contextp->commandArgs(argc, argv);
    argc--; //屎

    // Construct the Verilated model, from Vtop.h generated from Verilating "top.v".
    // Using unique_ptr is similar to "Vtop* top = new Vtop" then deleting at end.
    // "TOP" will be the hierarchical name of the module.
    // const std::unique_ptr<Vtop> top{new Vtop{contextp.get(), "TOP"}};
    top = new Vtop{contextp};

    
  //VCD波形设置  start
  Verilated::traceEverOn(true);
  tfp = new VerilatedVcdC;
  top->trace(tfp, 0);
  tfp->open("build/wave.vcd");
  //VCD波形设置  end


}
void verilator_runtime_deinit(){
      top->final();
      delete top;
  delete contextp;
  delete tfp;
}
void diff_cpuInfoUpdate(CPU_state_diff_t* s){
  /*
  for i in range(0,32):
    print(f"s->regs[{i}]=top->io_diff_regs_{i};",end="")
  print("\n")
  print("s->dnpc=s->regs[32];")
  */
  s->regs[0]=top->io_diff_regs_0;s->regs[1]=top->io_diff_regs_1;s->regs[2]=top->io_diff_regs_2;s->regs[3]=top->io_diff_regs_3;s->regs[4]=top->io_diff_regs_4;s->regs[5]=top->io_diff_regs_5;s->regs[6]=top->io_diff_regs_6;s->regs[7]=top->io_diff_regs_7;s->regs[8]=top->io_diff_regs_8;s->regs[9]=top->io_diff_regs_9;s->regs[10]=top->io_diff_regs_10;s->regs[11]=top->io_diff_regs_11;s->regs[12]=top->io_diff_regs_12;s->regs[13]=top->io_diff_regs_13;s->regs[14]=top->io_diff_regs_14;s->regs[15]=top->io_diff_regs_15;s->regs[16]=top->io_diff_regs_16;s->regs[17]=top->io_diff_regs_17;s->regs[18]=top->io_diff_regs_18;s->regs[19]=top->io_diff_regs_19;s->regs[20]=top->io_diff_regs_20;s->regs[21]=top->io_diff_regs_21;s->regs[22]=top->io_diff_regs_22;s->regs[23]=top->io_diff_regs_23;s->regs[24]=top->io_diff_regs_24;s->regs[25]=top->io_diff_regs_25;s->regs[26]=top->io_diff_regs_26;s->regs[27]=top->io_diff_regs_27;s->regs[28]=top->io_diff_regs_28;s->regs[29]=top->io_diff_regs_29;s->regs[30]=top->io_diff_regs_30;s->regs[31]=top->io_diff_regs_31;
  //s->pc = s->dnpc;
  //s->dnpc=top->io_IMem_rAddr;
  s->dnpc=top->io_diff_dnpc;
  s->snpc=top->io_diff_snpc;
  s->pc=top->io_diff_pc;
  s->csr.mcause=top->io_diff_mcause;
  s->csr.mepc=top->io_diff_mepc;
  s->csr.mstatus=top->io_diff_mstatus;
  s->csr.mtvec=top->io_diff_mtvec;

  diff_en=top->io_diff_diff_en;
}
void cpu_init() {
  // s->pc=RESET_VECTOR;
  // s->dnpc=RESET_VECTOR;
  // s->snpc=RESET_VECTOR;

  //cpu_gpr[32] = CONFIG_MBASE;
  top -> clock = 0;
  top -> reset = 1;
  top -> eval();
  tfp->dump(main_time);
  main_time ++;
  top -> clock = 1;
  top -> reset = 1;
  top -> eval();
  tfp->dump(main_time);
  main_time ++;
  top -> reset = 0;

  diff_cpuInfoUpdate(s);
}
void tick_once() {
  top->clock = 0;
  top->eval();
  tfp->dump(main_time);

  main_time ++;
  top->clock = 1;
  //printf("======clock should be 1 now %d\n",top->clock); 
  top->eval(); 
	tfp->dump(main_time);
  main_time ++;
  
}
void exec_once(){
  // do
  // {
    tick_once();
    diff_cpuInfoUpdate(s);
  //}while (!diff_en);
  
}