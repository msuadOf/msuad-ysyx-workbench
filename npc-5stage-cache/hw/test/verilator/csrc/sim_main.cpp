#include "r_verilator.h"

#include <string.h>

// glibc
#include <stdio.h>
#include <stdlib.h>
#include <assert.h>
// readline
#include <readline/readline.h>
#include <readline/history.h>
// system time
#include <sys/time.h>

#include "monitor.h"
#include "common.h"
#include "cpu.h"
#include "mem.h"

//=======cpu run time========

CPU_state_diff_t *s;

static const uint32_t img[] = {
    0x00000297, // auipc t0,0
    0x00028823, // sb  zero,16(t0)
    0x0102c503, // lbu a0,16(t0)
    0x00100073, // ebreak (used as nemu_trap)
    0xdeadbeef, // some data
};

void hit_exit(int status)
{
  if (status == 0)
  {
    // difftest_skip_ref();
    npc_state.state = NPC_END;
    npc_state.halt_pc = s->pc;
    npc_state.halt_ret = s->regs[10]; // R(10) is $a0
    return;
  }
  Assert(0, "hit_exit status(=%d) error!", status);
}
//===

static inline int check_reg_idx(int idx)
{
  if (!(idx >= 0 && idx < 32))
  {
    Log("Error:check_reg_idx(%d)", idx);
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
const char *reg_name(int idx)
{
  extern const char *regnames[];
  return regnames[check_reg_idx(idx)];
}
void isa_reg_display_byIndex(int i)
{
  printf("%-8s%-#20x%-20d\n", regnames[i], gpr(i), gpr(i));
}
void isa_reg_display()
{
  printf("> npc reg display:\n");
  int i = 0;
  for (i = 0; i < 32; i++)
  {
    printf("%-8s%-#20x%-20d\n", regnames[i], gpr(i), gpr(i));
  }
  printf("%-8s%-#20x%-20d\n", "pc", s->pc, s->pc);
  printf("%-8s%-#20x%-20d\n", "dnpc", s->dnpc, s->dnpc);
  printf("%-8s%-#20x%-20d\n", "mcause", s->csr.mcause, s->csr.mcause);
  printf("%-8s%-#20x%-20d\n", "mepc", s->csr.mepc, s->csr.mepc);
  printf("%-8s%-#20x%-20d\n", "mstatus", s->csr.mstatus, s->csr.mstatus);
  printf("%-8s%-#20x%-20d\n", "mtvec", s->csr.mtvec, s->csr.mtvec);
  putchar('\n');
}
void isa_reg_display(CPU_state_diff_t *s)
{
  printf("> npc reg display:\n");
  int i = 0;
  for (i = 0; i < 32; i++)
  {
    printf("%-8s%-#20x%-20d\n", regnames[i], gpr(i), gpr(i));
  }
  printf("%-8s%-#20x%-20d\n", "pc", s->pc, s->pc);
  printf("%-8s%-#20x%-20d\n", "dnpc", s->dnpc, s->dnpc);
  putchar('\n');
}
void assert_fail_msg()
{
  isa_reg_display();
}

extern void difftest_step(CPU_state_diff_t *s, CPU_state_diff_t *s_bak);
void execute(uint64_t n)
{

  for (int i; i < n; i++)
  {
#ifdef CONFIG_DIFFTEST
    CPU_state_diff_t npc_state_bak;
    memcpy(&npc_state_bak, s, sizeof(CPU_state_diff_t));
#endif
    exec_once();
    diff_cpuInfoUpdate(s);

    if (npc_state.state == NPC_STOP)
      break; // inst:ebreak

#ifdef CONFIG_DIFFTEST
    difftest_step(s, &npc_state_bak);
#endif

    // 寄了以后就别运行了
    if (npc_state.state == NPC_ABORT || npc_state.state == NPC_END)
    {
      Log("Program execution has ended. To restart the program, exit NPC and run again.\n");
      return;
    }
  }
}
void cpu_exec(uint64_t n)
{
  Log_level_2("cpu_exec(%ld)", n);
  // 寄了以后就别运行了
  if (npc_state.state == NPC_ABORT || npc_state.state == NPC_END)
  {
    Log("Program execution has ended. To restart the program, exit NPC and run again.\n");
    return;
  }

  execute(n);

  switch (npc_state.state)
  {
  case NPC_RUNNING:
    npc_state.state = NPC_STOP;
    break;

  case NPC_END:Log("NPC END");
  case NPC_ABORT:
    Log("npc: %s at pc = " FMT_WORD,
        (npc_state.state == NPC_ABORT ? ANSI_FMT("ABORT", ANSI_FG_RED) : (npc_state.halt_ret == 0 ? ANSI_FMT("HIT GOOD TRAP", ANSI_FG_GREEN) : ANSI_FMT("HIT BAD TRAP", ANSI_FG_RED))),
        npc_state.halt_pc);
    // fall through
  case NPC_QUIT:
    Log("NPC Quited");
  }
}
///=======cpu run time [ends]==================/

int main(int argc, char **argv)
{

  // This is a more complicated example, please also see the simpler examples/make_hello_c.

  // Prevent unused variable warnings
  if (false && argc && argv)
  {
  }

  for (int i = 0; i < argc; i++)
  {
    printf("%s ", argv[i]);
  }
  printf("%c", '\n');
  printf("%s\n", argv[1]);

  verilator_runtime_init(argc, argv);
  argc--;
  // Set Vtop's input signals
  CPU_state_diff_t cpu_state;
  s = &cpu_state;

  monitor(argc, argv);



  Log("End simulation\n");
  verilator_runtime_deinit();
  // Return good completion status
  // Don't use exit() or destructor won't get called
  return is_exit_status_bad();
}
