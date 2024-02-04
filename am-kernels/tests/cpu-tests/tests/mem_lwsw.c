#include "trap.h"
#define Mem ((volatile uint32_t*) (0x80005000))
void once(int idx){
      Mem[idx]=idx;
    check(Mem[idx]==idx);
}
int main() {
  for(int i=0xFF00;i<0xFFFF;i++){
    once(i);
  }
  return 0;
}