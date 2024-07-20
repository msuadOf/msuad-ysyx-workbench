#include "r_verilator.h"

extern void hit_exit(int status);

extern "C" void ebreak(){
    puts(ANSI_FG_GREEN);
    puts("npc:excute the ebreak inst\n");
    puts(ANSI_NONE);
    hit_exit(0);
}