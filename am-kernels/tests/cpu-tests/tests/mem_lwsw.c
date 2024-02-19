#include "trap.h"
//char buf[512]={0};
int main() {
  printf("121\n");
  volatile signed int x=-2147483648; volatile signed int y=-2147483648;
  volatile signed int buf[20]={-2147483648,-2147483648,-2147483648,-2147483648,-2147483648,-2147483648,-2147483648,-2147483648,-2147483648,-2147483648,-2147483648,-2147483648};
  for(int i=0;i<5;i++){
    //buf[i]=x;
    printf("(test)");
    printf("hh %d\n",-2147483648);
    panic("");
    check(y==buf[i]);
  }
   printf("t: %d \n",-2147483648);
   x=(volatile signed int)(x+y);
   printf("%d %d %d %d %d %d %d %d %d %d %d %d %d %d %d(end)\n",1,2,3,4,5,6,7,8,9,0,1,2,3,4,5,6);
   printf("%d %s %d %d %d\n",1,"2",3,4,5,6,7,8,9,0,1,2,3,4,5,6);
  
printf("121\n");
   printf("line %d: %s: %d  %s  %d  ==  %d =>  %s (%d)\n",__LINE__,"signed int",-2147483648,"+",-2147483648,0,(signed int)(x+y)==0?"PASS":"FAIL",(signed int)(x+y));
  return 0;
}