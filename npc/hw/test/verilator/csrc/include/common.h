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

#ifndef __COMMON_H__
#define __COMMON_H__

#include <stdint.h>
#include <inttypes.h>
#include <stdbool.h>
#include <string.h>

//#include <generated/autoconf.h>
#include <macro.h>
#include <assert.h>

#define CONFIG_DIFFTEST 1
//#define CONFIG_MTRACE 1
#define CONFIG_LOG 1

//---------------- device ---------------
#define CONFIG_DEVICE 1

#ifdef CONFIG_DEVICE
#define CONFIG_DEVICE_SERIAL 1 //SERIAL
#define CONFIG_DEVICE_RTC 1 //RTC
#endif // CONFIG_DEVICE

//SERIAL
#ifdef CONFIG_DEVICE_SERIAL
#define CONFIG_SERIAL_MMIO 0xa00003f8
#define CONFIG_SERIAL_MMIO_END (CONFIG_SERIAL_MMIO+8)

#endif // CONFIG_DEVICE_SERIAL

//RTC
#ifdef CONFIG_DEVICE_RTC
#define CONFIG_RTC_MMIO 0xa000048
#define CONFIG_RTC_MMIO_END (CONFIG_RTC_MMIO+8)

#endif // CONFIG_DEVICE_SERIAL


typedef MUXDEF(CONFIG_ISA64, uint64_t, uint32_t) word_t;
typedef MUXDEF(CONFIG_ISA64, int64_t, int32_t)  sword_t;
#define FMT_WORD MUXDEF(CONFIG_ISA64, "0x%016" PRIx64, "0x%08" PRIx32)

typedef word_t vaddr_t;
typedef MUXDEF(PMEM64, uint64_t, uint32_t) paddr_t;
#define FMT_PADDR MUXDEF(PMEM64, "0x%016" PRIx64, "0x%08" PRIx32)
typedef uint16_t ioaddr_t;

#include <debug.h>

#include "cpu.h"

int is_exit_status_bad();

#endif
