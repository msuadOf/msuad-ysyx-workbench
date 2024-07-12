#include "common.h"
#include "mem.h"
#include <stdint.h>

typedef struct mtrace_t
{
    uint32_t inst;
    uint32_t pc  ;
    paddr_t  addr;
    int      len ;
    word_t   data;
} mtrace_t;

#define MEM_OP_LEN 10000 // 设计成一个
mtrace_t Mem_wops[MEM_OP_LEN] = {0};
int mtrace_head = 0;

static void add_wlog(uint32_t inst, uint32_t pc, paddr_t addr, int len, word_t data)
{
    Mem_wops[mtrace_head].inst=inst;
    Mem_wops[mtrace_head].pc  =pc  ;
    Mem_wops[mtrace_head].addr=addr;
    Mem_wops[mtrace_head].len =len ;
    Mem_wops[mtrace_head].data=data;

    mtrace_head++;
}
void display_wlog(){
    for(int i=0;i<=mtrace_head;i++){
        printf("(npc)total write ops:[%d] addr=%08x data\n",i,Mem_wops[mtrace_head].addr);
    }
}
word_t mtrace_paddr_read(uint32_t inst, uint32_t pc, paddr_t addr, int len)
{

    return paddr_read(addr, len);
}
void mtrace_paddr_write(uint32_t inst, uint32_t pc, paddr_t addr, int len, word_t data)
{

    paddr_write(addr, len, data);
}

// mtrace
void trace_mread(paddr_t addr, int len, word_t data)
{
    if(addr!=0)
    printf("(npc) pread at " FMT_PADDR " len=%d, data=" FMT_WORD "\n", addr, len, data);
}

void trace_mwrite(paddr_t addr, int len, word_t data)
{
    printf("(npc) pwrite at " FMT_PADDR " len=%d, data=" FMT_WORD "\n", addr, len, data);
}