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
void init_disasm(const char *triple);
void print_mem(){
  Log("0x%08x:0x%08x\n",cpu.pc,paddr_read(cpu.pc,4));
}
__EXPORT void difftest_memcpy(paddr_t addr, void *buf, size_t n, bool direction) {
  word_t* _buf=(word_t*)buf;
  
  assert(n>=0);
  n=n/4 ;
  //Log("n=%ld,buf[0]=0x%08x",n,((uint32_t*)buf)[0]);

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

typedef struct {
  word_t mcause;
  vaddr_t mepc;
  word_t mstatus;
  word_t mtvec;
} MUXDEF(CONFIG_RV64, riscv64_CSRs_diff, riscv32_CSRs_diff);

typedef struct CPU_state_diff_t {
  vaddr_t pc;
  vaddr_t snpc; // static next pc
  vaddr_t dnpc; // dynamic next pc
  word_t regs[33];
  MUXDEF(CONFIG_RV64, riscv64_CSRs_diff, riscv32_CSRs_diff) csr;
} CPU_state_diff_t;
__EXPORT void difftest_regcpy(void *dut, bool direction) {
  CPU_state_diff_t* s=(CPU_state_diff_t*)dut;

  if(direction==DIFFTEST_TO_DUT){
    for(int i=0;i<32;i++){
      s->regs[i]=cpu.gpr[i];
    }
    s->regs[32]=cpu.pc;
    s->pc=cpu.pc;
    memcpy(&(s->csr),&(cpu.csr),sizeof(riscv32_CSRs_diff));
    //s->dnpc=cpu.dnpc;
    //printf(ANSI_FG_BLUE "[nemu]:difftest_regcpy TO_DUT (nemu)pc=%x (dut)pc=%x\n" ANSI_NONE,cpu.pc,s->pc);
    return;
  }
  if(direction==DIFFTEST_TO_REF){
    for(int i=0;i<32;i++){
      cpu.gpr[i]=s->regs[i];
    }
    cpu.pc=s->regs[32];
    cpu.pc=s->pc; 
    memcpy(&(cpu.csr),&(s->csr),sizeof(riscv32_CSRs_diff));
    //cpu.dnpc=s->dnpc; 
    //printf(ANSI_FG_BLUE "[nemu]:difftest_regcpy TO_REF (nemu)pc=%x (dut)pc=%x\n" ANSI_NONE,cpu.pc,s->pc);
    return;
  }
  assert(0);
}

__EXPORT void difftest_exec(uint64_t n) {
  //printf("\n===========difftest_exec begin,n=%ld===========\n",n);
  //print_mem();
  cpu_exec(n);
  //printf("===========difftest_exec end===========\n");
}

__EXPORT void difftest_reg_display() {
  isa_reg_display();
}

__EXPORT void difftest_raise_intr(word_t NO) {
  assert(0);
}

__EXPORT void difftest_init(int port) {
  void init_mem();
  init_mem();
  /* Perform ISA dependent initialization. */
  init_isa();
  #ifndef CONFIG_ISA_loongarch32r
  IFDEF(CONFIG_ITRACE, init_disasm(
    MUXDEF(CONFIG_ISA_x86,     "i686",
    MUXDEF(CONFIG_ISA_mips32,  "mipsel",
    MUXDEF(CONFIG_ISA_riscv,
      MUXDEF(CONFIG_RV64,      "riscv64",
                               "riscv32"),
                               "bad"))) "-pc-linux-gnu"
  ));
#endif
}
