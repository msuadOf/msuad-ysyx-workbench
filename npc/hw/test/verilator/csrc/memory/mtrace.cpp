#include  "common.h"
#include "mem.h"
#include <stdint.h>

typedef struct mtrace_t
{
uint32_t inst;
uint32_t pc;
paddr_t addr;
 int len;
 word_t data;
}mtrace_t;

#define MEM_OP_LEN 1 //设计成一个
mtrace_t Mem_op[MEM_OP_LEN]={0};

word_t mtrace_paddr_read(uint32_t inst,uint32_t pc,paddr_t addr, int len){
    
    return paddr_read(addr,len);
}
void mtrace_paddr_write(uint32_t inst,uint32_t pc,paddr_t addr, int len, word_t data) {
  paddr_write(addr,len,data);
}

//mtrace
void trace_mread(paddr_t addr, int len) {
  printf("(nemu) pread at " FMT_PADDR " len=%d\n", addr, len);
}

void trace_mwrite(paddr_t addr, int len, word_t data) {
  printf("(nemu) pwrite at " FMT_PADDR " len=%d, data=" FMT_WORD "\n", addr, len, data);
}