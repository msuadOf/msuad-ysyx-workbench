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

#ifndef __SDB_H__
#define __SDB_H__

<<<<<<< Updated upstream:npc/hw/test/verilator/csrc/monitor/sdb/sdb.h
#include <common.h>
=======
// #include <stdint.h>
// #include <macro.h>
// #include <generated/autoconf.h>

// #define __EXPORT __attribute__((visibility("default")))
// enum { DIFFTEST_TO_DUT, DIFFTEST_TO_REF };



// #define RISCV_GPR_TYPE MUXDEF(CONFIG_RV64, uint64_t, uint32_t)
// #define RISCV_GPR_NUM  MUXDEF(CONFIG_RVE , 16, 32)
// #define DIFFTEST_REG_SIZE (sizeof(RISCV_GPR_TYPE) * (RISCV_GPR_NUM + 1)) // GPRs + pc
>>>>>>> Stashed changes:npc/hw/test/verilator/csrc/include/difftest-def.h

word_t expr(char *e, bool *success);
void wp_add(char *expr, word_t res);
void wp_del(int no);
void wp_display();
void wp_difftest();

#endif
