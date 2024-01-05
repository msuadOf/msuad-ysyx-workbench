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
uint64_t ref_regs[33];
 
void hit_exit(int status) {}
//===

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
    // Return good completion status
    // Don't use exit() or destructor won't get called
    return 0;
}
