#include "trap.h"
//char buf[512]={0};
int main() {
asm("li a1,0x80000211");
asm("sw a0,0(a1)");
asm("lw a0,0(a1)");
asm("sw a0,0(a1)");
asm("lw a0,0(a1)");

asm("li a0,0x80020213");
asm("sw a0,0(a1)");
asm("lw a0,0(a1)");
asm("sw a0,0(a1)");
asm("lw a0,0(a1)");

  return 0;
}