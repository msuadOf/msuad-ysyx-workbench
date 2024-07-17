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

#ifndef __CPU_H__
#define __CPU_H__

#include "common.h"

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


// monitor
//extern unsigned char isa_logo[];
//void init_isa();

// // reg
extern CPU_state_diff_t* s;
void isa_reg_display();
void isa_reg_display(CPU_state_diff_t* s);
const char* reg_name(int idx);
// word_t isa_reg_str2val(const char *name, bool *success);
void isa_reg_display_byIndex(int i);

// // exec
// struct Decode;
// int isa_exec_once(struct Decode *s);

// // memory
// enum { MMU_DIRECT, MMU_TRANSLATE, MMU_FAIL };
// enum { MEM_TYPE_IFETCH, MEM_TYPE_READ, MEM_TYPE_WRITE };
// enum { MEM_RET_OK, MEM_RET_FAIL, MEM_RET_CROSS_PAGE };

// paddr_t isa_mmu_translate(vaddr_t vaddr, int len, int type);

// // interrupt/exception
// vaddr_t isa_raise_intr(word_t NO, vaddr_t epc);
// #define INTR_EMPTY ((word_t)-1)
// word_t isa_query_intr();

// // difftest
// bool isa_difftest_checkregs(CPU_state *ref_r, vaddr_t pc);
// void isa_difftest_attach();

#endif
