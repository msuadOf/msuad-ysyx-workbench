#include "trap.h"
//char buf[512]={0};
int main() {

asm("sw a0,0(a1)");
asm("lw a0,0(a1)");

  return 0;
}