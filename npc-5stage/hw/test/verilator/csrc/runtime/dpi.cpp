#include "r_verilator.h"

// extern void hit_exit(int status);

int is_ebreak=0;
extern "C" void ebreak(){
    puts(ANSI_FG_GREEN);
    puts("npc:excute the ebreak inst\n");
    puts(ANSI_NONE);
    is_ebreak=1;
    // hit_exit(0);
}