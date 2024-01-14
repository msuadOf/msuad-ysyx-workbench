/***************************************************************************************
* Copyright (c) 2014-2022 Zihao Yu, Nanjing University
*
* NPC is licensed under Mulan PSL v2.
* You can use this software according to the terms and conditions of the Mulan PSL v2.
* You may obtain a copy of Mulan PSL v2 at:
*          http://license.coscl.org.cn/MulanPSL2
*
* THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
* EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
* MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
*
* See the Mulan PSL v2 for more details.
***************************************************************************************/

#include <dlfcn.h>

#include <cpu.h>

#include <utils.h>
#include <difftest-def.h>
#include "common.h"

#include "mem.h"

void (*ref_difftest_memcpy)(paddr_t addr, void *buf, size_t n, bool direction) = NULL;
void (*ref_difftest_regcpy)(void *dut, bool direction) = NULL;
void (*ref_difftest_exec)(uint64_t n) = NULL;
void (*ref_difftest_raise_intr)(uint64_t NO) = NULL;

enum { DIFFTEST_TO_DUT, DIFFTEST_TO_REF };




#ifdef CONFIG_DIFFTEST

static bool is_skip_ref = false;
static int skip_dut_nr_inst = 0;

// this is used to let ref skip instructions which
// can not produce consistent behavior with NPC
void difftest_skip_ref() {
  is_skip_ref = true;
  // If such an instruction is one of the instruction packing in QEMU
  // (see below), we end the process of catching up with QEMU's pc to
  // keep the consistent behavior in our best.
  // Note that this is still not perfect: if the packed instructions
  // already write some memory, and the incoming instruction in NPC
  // will load that memory, we will encounter false negative. But such
  // situation is infrequent.
  skip_dut_nr_inst = 0;
}

// this is used to deal with instruction packing in QEMU.
// Sometimes letting QEMU step once will execute multiple instructions.
// We should skip checking until NPC's pc catches up with QEMU's pc.
// The semantic is
//   Let REF run `nr_ref` instructions first.
//   We expect that DUT will catch up with REF within `nr_dut` instructions.
void difftest_skip_dut(int nr_ref, int nr_dut) {
  skip_dut_nr_inst += nr_dut;

  while (nr_ref -- > 0) {
    ref_difftest_exec(1);
  }
}

void init_difftest(char *ref_so_file, long img_size, int port) {
  assert(ref_so_file != NULL);

  void *handle;
  handle = dlopen(ref_so_file, RTLD_LAZY);
  Log("====");
  assert(handle);

  ref_difftest_memcpy = ( void (*)(unsigned int, void*, long unsigned int, bool) ) dlsym(handle, "difftest_memcpy");
  assert(ref_difftest_memcpy);

  ref_difftest_regcpy = ( void (*)(void*, bool) ) dlsym(handle, "difftest_regcpy");
  assert(ref_difftest_regcpy);

  ref_difftest_exec = ( void (*)(long unsigned int) ) dlsym(handle, "difftest_exec");
  assert(ref_difftest_exec);


  Log("Differential testing: %s", ANSI_FMT("ON", ANSI_FG_GREEN));
  Log("The result of every instruction will be compared with %s. ", ref_so_file);


  ref_difftest_memcpy(RESET_VECTOR, guest_to_host(RESET_VECTOR), CONFIG_MSIZE, DIFFTEST_TO_REF);
  //ref_difftest_memcpy(RESET_VECTOR, guest_to_host(RESET_VECTOR), img_size, DIFFTEST_TO_REF);
  extern CPU_state_diff_t* s;
  ref_difftest_regcpy(s, DIFFTEST_TO_REF);
}


bool isa_difftest_checkregs(CPU_state_diff_t *ref_r, vaddr_t pc) {
  int state=true;
  int reg_num = 32;
  for (int i = 0; i < reg_num; i++) {
    if (ref_r->regs[i] != s->regs[i]) {
      printf(ANSI_FG_RED "[Error]" ANSI_NONE " \"%s\" is diffrent: (npc-false)= %08x ,(ref-yes)= %08x.\n",reg_name(i),s->regs[i],ref_r->regs[i]);
      state=false;
      //return false;
    }
  }
  if (ref_r->pc != s->pc) {
    printf(ANSI_FG_RED "[Error]" ANSI_NONE " \"pc\" is diffrent: (npc-false)= %08x ,(ref-yes)= %08x.\n",s->pc,ref_r->pc);
    state=false;
    //return false;
  }
  return state;
  //return true;
}

static void checkregs(CPU_state_diff_t *ref, vaddr_t pc) {
  if (!isa_difftest_checkregs(ref, pc)) {
    npc_state.state = NPC_ABORT;
    npc_state.halt_pc = pc;
    isa_reg_display();
  }
}

void difftest_step(CPU_state_diff_t* s) {
  CPU_state_diff_t ref_r;

ref_difftest_regcpy(&ref_r, DIFFTEST_TO_DUT);
  ref_difftest_exec(1);
  

  checkregs(&ref_r, s->pc);
}
#else
void init_difftest(char *ref_so_file, long img_size, int port) { }
#endif
