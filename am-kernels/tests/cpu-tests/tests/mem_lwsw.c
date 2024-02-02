#include "trap.h"
#define Mem ((volatile uint32_t*) (0x80005000))
int main() {
    *Mem=10;
    check(1);
  return 0;
}