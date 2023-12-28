// For std::unique_ptr
#include <memory>
// Include common routines
#include <verilated.h>
// Include model header, generated from Verilating "top.v"
#include "Vtop.h"
#include <string.h>

//personal includes
#include "verilated_vcd_c.h" //用于生成波形
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

#include "sim_main.h"


//================= Environment ===============
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

static void welcome() {
    hello_npc(1);
  Log("Trace: %s", MUXDEF(CONFIG_TRACE, ANSI_FMT("ON", ANSI_FG_GREEN), ANSI_FMT("OFF", ANSI_FG_RED)));
  IFDEF(CONFIG_TRACE, Log("If trace is enabled, a log file will be generated "
        "to record the trace. This may lead to a large log file. "
        "If it is not necessary, you can disable it in menuconfig"));
  Log("Build time: %s, %s", __TIME__, __DATE__);
  printf("Welcome to %s-npc!\n", ANSI_FMT(str(__GUEST_ISA__), ANSI_FG_YELLOW ANSI_BG_RED));
  printf("For help, type \"help\"\n");
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
    const std::unique_ptr<VerilatedContext> contextp{new VerilatedContext};
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
    const std::unique_ptr<Vtop> top{new Vtop{contextp.get(), "TOP"}};

    // Set Vtop's input signals
    top->reset = !0;
    top->clock = 0;
    // top->in_small = 1;
    // top->in_quad = 0x1234;
    // top->in_wide[0] = 0x11111111;
    // top->in_wide[1] = 0x22222222;
    // top->in_wide[2] = 0x3;
    Log("Start simulation\n");
    // Simulate until $finish
    welcome();
    init_monitor(argc, argv);
    sdb_mainloop();
    // while (!contextp->gotFinish()) {
    //     // Historical note, before Verilator 4.200 Verilated::gotFinish()
    //     // was used above in place of contextp->gotFinish().
    //     // Most of the contextp-> calls can use Verilated:: calls instead;
    //     // the Verilated:: versions just assume there's a single context
    //     // being used (per thread).  It's faster and clearer to use the
    //     // newer contextp-> versions.

    //     contextp->timeInc(1);  // 1 timeprecision period passes...
    //     // Historical note, before Verilator 4.200 a sc_time_stamp()
    //     // function was required instead of using timeInc.  Once timeInc()
    //     // is called (with non-zero), the Verilated libraries assume the
    //     // new API, and sc_time_stamp() will no longer work.

    //     // Toggle a fast (time/2 period) clock
    //     top->clock = !top->clock;

    //     // Toggle control signals on an edge that doesn't correspond
    //     // to where the controls are sampled; in this example we do
    //     // this only on a negedge of clock, because we know
    //     // reset is not sampled there.
    //     if (!top->clock) {
    //         if (contextp->time() > 1 && contextp->time() < 10) {
    //             top->reset = !1;  // Assert reset
    //         } else {
    //             top->reset = !0;  // Deassert reset
    //         }
    //         // Assign some other inputs
    //         //top->in_quad += 0x12;
    //     }

    //     // Evaluate model
    //     // (If you have multiple models being simulated in the same
    //     // timestep then instead of eval(), call eval_step() on each, then
    //     // eval_end_step() on each. See the manual.)
    //     top->eval();

    //     // Read outputs
    //     // VL_PRINTF("[%" PRId64 "] clock=%x rstl=%x iquad=%" PRIx64 " -> oquad=%" PRIx64
    //     //           " owide=%x_%08x_%08x\n",
    //     //           contextp->time(), top->clock, top->reset, top->in_quad, top->out_quad,
    //     //           top->out_wide[2], top->out_wide[1], top->out_wide[0]);
    // }

    // Final model cleanup
    top->final();

    // Coverage analysis (calling write only after the test is known to pass)


    Log("End simulation\n");
    // Return good completion status
    // Don't use exit() or destructor won't get called
    return 0;
}
void assert_fail_msg(){
    panic("Callback: void assert_fail_msg()");

panic("啊~啊~不要过来啊~~机器寄了QAQ");

}