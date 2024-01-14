/***************************************************************************************
* Copyright (c) 2014-2022 Zihao Yu, Nanjing University
*
* NEMU is licensed under Mulan PSL v2.
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

#include <isa.h>
#include <cpu/cpu.h>
#include <difftest-def.h>
#include <memory/paddr.h>

#include <difftest-def.h>

//enum { DIFFTEST_TO_DUT, DIFFTEST_TO_REF };
void print_mem(){

}
__EXPORT void difftest_memcpy(paddr_t addr, void *buf, size_t n, bool direction) {
  word_t* _buf=buf;
  
  assert(n>=0);
  n=n/4 ;
  Log("n=%ld",n);

  if(direction==DIFFTEST_TO_DUT){
    for(size_t i=0;i<n;i++){
      _buf[i]=paddr_read(addr+i*4,4);
    }
  }
  else if (direction==DIFFTEST_TO_REF){
    for(size_t i=0;i<n;i++){
      paddr_write(addr+i*4,4,_buf[i]);
    }
  }else{
    Log(ANSI_FG_RED "difftest_memcpy(paddr_t addr, void *buf, size_t n, bool direction): direction error");
    assert(0);
  }

}

typedef struct CPU_state_diff_t {
  vaddr_t pc;
  vaddr_t snpc; // static next pc
  vaddr_t dnpc; // dynamic next pc
  word_t regs[33];
} CPU_state_diff_t;
__EXPORT void difftest_regcpy(void *dut, bool direction) {
  CPU_state_diff_t* s=dut;
printf(ANSI_BG_BLUE "[nemu]:difftest_regcpy\n");
  if(direction==DIFFTEST_TO_DUT){
    for(int i=0;i<32;i++){
      s->regs[i]=cpu.gpr[i];
    }
    s->regs[32]=cpu.pc;
    s->pc=cpu.pc;
    return;
  }
  if(direction==DIFFTEST_TO_REF){
    for(int i=0;i<32;i++){
      cpu.gpr[i]=s->regs[i];
    }
    cpu.pc=s->regs[32];
    cpu.pc=s->pc; 
    return;
  }
  assert(0);
}

__EXPORT void difftest_exec(uint64_t n) {
  cpu_exec(n);
  assert(0);
}

__EXPORT void difftest_raise_intr(word_t NO) {
  assert(0);
}

__EXPORT void difftest_init(int port) {
  void init_mem();
  init_mem();
  /* Perform ISA dependent initialization. */
  init_isa();
}
