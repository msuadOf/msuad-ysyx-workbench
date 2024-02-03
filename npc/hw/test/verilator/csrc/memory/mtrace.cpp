#include <stdint.h>

typedef struct mtrace_t
{
    uint32_t inst;
    uint32_t addr ;
    uint32_t data ;
    uint32_t len ;
}mtrace_t;

#define MEM_OP_LEN 1 //设计成一个
mtrace_t Mem_op[MEM_OP_LEN]={0};

void mtrace(uint32_t inst,uint32_t addr ,uint32_t data ,uint32_t len){

}