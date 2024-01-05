// For std::unique_ptr
#include <memory>
// Include common routines

#include "verilated_vcd_c.h" //用于生成波形
#include <verilated.h>
// Include model header, generated from Verilating "top.v"
#include "Vtop.h"
#include <string.h>

//personal includes
// //dpi-c
// #include "Vtop__Dpi.h"
// #include <verilated_dpi.h>
//glibc
#include <stdio.h>
#include <stdlib.h>
#include <assert.h>
//readline
#include <readline/readline.h>
#include <readline/history.h>
//system time
#include <sys/time.h>

#include "monitor.h"
#include "common.h"


//=======cpu run time========

VerilatedContext* contextp;
Vtop* top;
 
VerilatedVcdC* tfp;
vluint64_t main_time = 0;  //initial 仿真时间
double sc_time_stamp()
{
	return main_time;
}
typedef struct diff_t {
  vaddr_t pc;
  vaddr_t snpc; // static next pc
  vaddr_t dnpc; // dynamic next pc
  word_t regs[33];
} diff_t;
diff_t* s;

static const uint32_t img [] = {
  0x00000297,  // auipc t0,0
  0x00028823,  // sb  zero,16(t0)
  0x0102c503,  // lbu a0,16(t0)
  0x00100073,  // ebreak (used as nemu_trap)
  0xdeadbeef,  // some data
};

 
void hit_exit(int status) {}
//===
void diff_cpuInfoUpdate(diff_t* s){
  /*
  for i in range(0,32):
    print(f"s->regs[{i}]=top->io_diff_regs_{i};",end="")
  print("\n")
  */
  s->regs[0]=top->io_diff_regs_0;s->regs[1]=top->io_diff_regs_1;s->regs[2]=top->io_diff_regs_2;s->regs[3]=top->io_diff_regs_3;s->regs[4]=top->io_diff_regs_4;s->regs[5]=top->io_diff_regs_5;s->regs[6]=top->io_diff_regs_6;s->regs[7]=top->io_diff_regs_7;s->regs[8]=top->io_diff_regs_8;s->regs[9]=top->io_diff_regs_9;s->regs[10]=top->io_diff_regs_10;s->regs[11]=top->io_diff_regs_11;s->regs[12]=top->io_diff_regs_12;s->regs[13]=top->io_diff_regs_13;s->regs[14]=top->io_diff_regs_14;s->regs[15]=top->io_diff_regs_15;s->regs[16]=top->io_diff_regs_16;s->regs[17]=top->io_diff_regs_17;s->regs[18]=top->io_diff_regs_18;s->regs[19]=top->io_diff_regs_19;s->regs[20]=top->io_diff_regs_20;s->regs[21]=top->io_diff_regs_21;s->regs[22]=top->io_diff_regs_22;s->regs[23]=top->io_diff_regs_23;s->regs[24]=top->io_diff_regs_24;s->regs[25]=top->io_diff_regs_25;s->regs[26]=top->io_diff_regs_26;s->regs[27]=top->io_diff_regs_27;s->regs[28]=top->io_diff_regs_28;s->regs[29]=top->io_diff_regs_29;s->regs[30]=top->io_diff_regs_30;s->regs[31]=top->io_diff_regs_31;
  
}
static inline int check_reg_idx(int idx) {
  if(!(idx >= 0 && idx < 32)){
    Log("Error:check_reg_idx(%d)",idx);
    assert(0);
  }
  
  return idx;
}
const char *regnames[] = {
    "$0", "ra", "sp", "gp", "tp", "t0", "t1", "t2",
    "s0", "s1", "a0", "a1", "a2", "a3", "a4", "a5",
    "a6", "a7", "s2", "s3", "s4", "s5", "s6", "s7",
    "s8", "s9", "s10", "s11", "t3", "t4", "t5", "t6"};
#define gpr(idx) (s->regs[check_reg_idx(idx)])
void isa_reg_display()
{
  int i = 0;
  for (i = 0; i < 32; i++)
  {
    printf("%-8s%-#20x%-20d\n", regnames[i], gpr(i), gpr(i));
  }
  printf("%-8s%-#20x%-20d\n", "pc", s->pc, s->pc);
  putchar('\n');
}
void cpu_init() {
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
}
void exec_once(VerilatedVcdC* tfp) {
  top->clock = 0;
  //printf("======clock shoule be 0 now %d\n",top->clock);
  // top->mem_inst = pmem_read(top->mem_addr);
  // printf("excute addr:0x%08lx inst:0x%08x\n",top->mem_addr,top->mem_inst);
  top->eval();
  tfp->dump(main_time);

  //====== cpu exec body begin ======
  top->io_IMem_rData=0xffc10113;
  int pc=top->io_IMem_rAddr;
  Log_level_1("pc=%08x\n",pc);
  //====== cpu exec body ends  ======

  main_time ++;
  top->clock = 1;
  //printf("======clock should be 1 now %d\n",top->clock); 
  top->eval(); 
	tfp->dump(main_time);
  main_time ++;
}
 
void cpu_exec(uint64_t n) {
  Log_level_2("cpu_exec(%ld)",n);
  for(int i; i < n; i++){
      exec_once(tfp);
      diff_cpuInfoUpdate(s);
      #ifdef CONFIG_DIFFTEST
        difftest_exec_once();
      #endif
  }
}

///=======cpu run time [ends]==================/
static char *rl_gets()
{
  static char *line_read = NULL;

  if (line_read)
  {
    free(line_read);
    line_read = NULL;
  }

  line_read = readline("(npc) ");

  if (line_read && *line_read)
  {
    add_history(line_read);
  }

  return line_read;
}
static int cmd_c(char *args)
{
  cpu_exec(-1);
  return -1;
}
static int cmd_q(char *args){
    return -1;
}
static int cmd_si(char *args)
{
  args = strtok(NULL, " ");
  int n;
  if (args == NULL)
  {
    n = 1;
  }
  else
  {
    n = atoi(args);
  }

  cpu_exec(n);
  return 0;
}
static int cmd_help(char *args);
static struct
{
  const char *name;
  const char *description;
  int (*handler)(char *);
} cmd_table[] = {
    {"help", "Display information about all supported commands", cmd_help},
    {"c", "Continue the execution of the program", cmd_c},
    {"q", "Exit NEMU", cmd_q},
    {"si", "让程序单步执行N条指令后暂停执行,当N没有给出时, 缺省为1", cmd_si},
    // {"x", "求出表达式EXPR的值, 将结果作为起始内存地址, 以十六进制形式输出连续的N个4字节", cmd_x},
    // {"info", "打印寄存器状态,打印监视点信息", cmd_info},
    // {"p", "求出表达式EXPR的值, EXPR支持的", cmd_p},
    // {"w", "w EXPR: 当表达式EXPR的值发生变化时, 暂停程序执行", cmd_w},
    // {"d", "d N: Exit NEMU删除序号为N的监视点", cmd_d},
    /* TODO: Add more commands */

};

#define NR_CMD ARRLEN(cmd_table)

static int cmd_help(char *args)
{
  /* extract the first argument */
  char *arg = strtok(NULL, " ");
  int i;

  if (arg == NULL)
  {
    /* no argument given */
    for (i = 0; i < NR_CMD; i++)
    {
      printf("%s - %s\n", cmd_table[i].name, cmd_table[i].description);
    }
  }
  else
  {
    for (i = 0; i < NR_CMD; i++)
    {
      if (strcmp(arg, cmd_table[i].name) == 0)
      {
        printf("%s - %s\n", cmd_table[i].name, cmd_table[i].description);
        return 0;
      }
    }
    printf("Unknown command '%s'\n", arg);
  }
  return 0;
}

int sdb_mainloop(){

  for (char *str; (str = rl_gets()) != NULL;)
  {
    char *str_end = str + strlen(str);

    /* extract the first token as the command */
    char *cmd = strtok(str, " ");
    if (cmd == NULL)
    {
      continue;
    }

    /* treat the remaining string as the arguments,
     * which may need further parsing
     */
    char *args = cmd + strlen(cmd) + 1;
    if (args >= str_end)
    {
      args = NULL;
    }

    int i;
    for (i = 0; i < NR_CMD; i++)
    {
      if (strcmp(cmd, cmd_table[i].name) == 0)
      {
        if (cmd_table[i].handler(args) < 0)
        {
          return -1;
        }
        break;
      }
    }

    if (i == NR_CMD)
    {
      printf("Unknown command '%s'\n", cmd);
    }
  }
    return 0;
}

int main(int argc, char** argv) {

    // This is a more complicated example, please also see the simpler examples/make_hello_c.

    // Prevent unused variable warnings
    if (false && argc && argv) {}

    for(int i=0;i<argc;i++){
        printf("%s ",argv[i]);
    }
    printf("%c",'\n');
    printf("%s\n",argv[1]);
    
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

    // Set Vtop's input signals
    diff_t d_npc;
    s=&d_npc;
  cpu_init();
    // top->in_small = 1;
    // top->in_quad = 0x1234;
    // top->in_wide[0] = 0x11111111;
    // top->in_wide[1] = 0x22222222;
    // top->in_wide[2] = 0x3;
    Log("Start simulation\n");
    // Simulate until $finish
    sdb_mainloop();
/*     while (!contextp->gotFinish()) {
        // Historical note, before Verilator 4.200 Verilated::gotFinish()
        // was used above in place of contextp->gotFinish().
        // Most of the contextp-> calls can use Verilated:: calls instead;
        // the Verilated:: versions just assume there's a single context
        // being used (per thread).  It's faster and clearer to use the
        // newer contextp-> versions.

        contextp->timeInc(1);  // 1 timeprecision period passes...
        // Historical note, before Verilator 4.200 a sc_time_stamp()
        // function was required instead of using timeInc.  Once timeInc()
        // is called (with non-zero), the Verilated libraries assume the
        // new API, and sc_time_stamp() will no longer work.

        // Toggle a fast (time/2 period) clock
        top->clock = !top->clock;

        // Toggle control signals on an edge that doesn't correspond
        // to where the controls are sampled; in this example we do
        // this only on a negedge of clock, because we know
        // reset is not sampled there.
        if (!top->clock) {
            if (contextp->time() > 1 && contextp->time() < 10) {
                top->reset = !1;  // Assert reset
            } else {
                top->reset = !0;  // Deassert reset
            }
            // Assign some other inputs
            //top->in_quad += 0x12;
        }

        // Evaluate model
        // (If you have multiple models being simulated in the same
        // timestep then instead of eval(), call eval_step() on each, then
        // eval_end_step() on each. See the manual.)
        top->eval();

        // Read outputs
        // VL_PRINTF("[%" PRId64 "] clock=%x rstl=%x iquad=%" PRIx64 " -> oquad=%" PRIx64
        //           " owide=%x_%08x_%08x\n",
        //           contextp->time(), top->clock, top->reset, top->in_quad, top->out_quad,
        //           top->out_wide[2], top->out_wide[1], top->out_wide[0]);
    }
 */
    // Final model cleanup
    top->final();

    // Coverage analysis (calling write only after the test is known to pass)


    Log("End simulation\n");
    delete top;
    delete contextp;
    delete tfp;
    // Return good completion status
    // Don't use exit() or destructor won't get called
    return 0;
}
