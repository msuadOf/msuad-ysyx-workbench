#ifndef _R_VERILATED_H_
#define _R_VERILATED_H_ 

// For std::unique_ptr
#include <memory>
// Include common routines

#include "verilated_vcd_c.h" //用于生成波形
#include <verilated.h>
// Include model header, generated from Verilating "top.v"
#include "Vtop.h"

#include <string.h>
#include "common.h"

//personal includes
//dpi-c
#include "Vtop__Dpi.h"
#include <verilated_dpi.h>

void diff_cpuInfoUpdate(CPU_state_diff_t* s);
void cpu_init();
void exec_once();
void verilator_runtime_init();
void verilator_runtime_init(int argc, char** argv);
void verilator_runtime_deinit();

#endif