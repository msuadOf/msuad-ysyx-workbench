/***************************************************************************************
 * Copyright (c) 2014-2022 Zihao Yu, Nanjing University
 *
 * NEMU is licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *          http://license.coscl.org.cn/MulanPSL2
 *
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 * EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 * MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
 *
 * See the Mulan PSL v2 for more details.
 ***************************************************************************************/

#include <stdint.h>
#include <stdio.h>
#include <stdlib.h>
#include <time.h>
#include <assert.h>
#include <string.h>

// this should be enough
static char buf[65536] = {'\0'};
static char code_buf[65536 + 128] = {}; // a little larger than `buf`
static char *code_format =
    "#include <stdio.h>\n"
    "int main() { "
    "  unsigned result = %s; "
    "  printf(\"%%u\", result); "
    "  return 0; "
    "}";

static char *buf_start = buf;
static volatile char *buf_end = buf + (sizeof(buf) / sizeof(buf[0]));

static int choose(int n)
{
  return rand() % n;
}

static void gen_space()
{
  int size = choose(4);
  if (buf_start < buf_end)
  {
    int n_writes = snprintf(buf_start, buf_end - buf_start, "%*s", size, "");
    if (n_writes > 0)
    {
      buf_start += n_writes;
    }
  }
}

static void gen_num()
{
  int num = choose(INT8_MAX);
  if (buf_start < buf_end)
  {
    int n_writes = snprintf(buf_start, buf_end - buf_start, "%d", num);
    if (n_writes > 0)
    {
      buf_start += n_writes;
    }
  }
  gen_space();
}

static void gen_char(char c)
{
  int n_writes = snprintf(buf_start, buf_end - buf_start, "%c", c);
  if (buf_start < buf_end)
  {
    if (n_writes > 0)
    {
      buf_start += n_writes;
    }
  }
}

static char ops[] = {'+', '-', '*', '/'};
static void gen_rand_op()
{
  int op_index = choose(sizeof(ops));
  char op = ops[op_index];
  gen_char(op);
}

static void gen_rand_expr()
{
  switch (choose(3))
  {
  case 0:
    gen_num();
    break;
  case 1:
    gen_char('(');
    gen_rand_expr();
    gen_char(')');
    break;
  default:
    gen_rand_expr();
    gen_rand_op();
    gen_rand_expr();
    break;
  }
}

// int gen_buf_index = 0;
// static void gen(char c)
// {
//   gen_buf_index=strlen(buf);

//   buf[gen_buf_index] = c;
//   buf[gen_buf_index + 1] = '\0';

// }

// void HAL_UART_Transmit(char* buffer,int length){
//   for(int i=0;i<length;i++){
//     gen(buffer[i]);
//   }
//   //strcat(buf,buffer);
// }
// #include "stdarg.h"
// #include "stdio.h"
// int gen_printf( const char *fmt, ...) {
//     va_list ap;
//     va_start(ap, fmt);
//     int length;
//     char buffer[128];
//     length = vsnprintf(buffer, 128, fmt, ap);
//     HAL_UART_Transmit( buffer, length);//HAL_MAX_DELAY
// //    CDC_Transmit_FS((uint8_t*)buffer,length);
//     va_end(ap);
//     return length;
// }

// static void gen_num(){

//   gen_printf( "%d", (int)((rand() / 31535) +1));

// }
// int choose(int n)
// {
//   return (int)(rand() % n);
// }
// static void gen_rand_op()
// {
//   char op[2];
//   op[1]='\0';
//   switch (choose(4))
//   {
//   case 0:
//     op[0] = '+';
//     break;
//   case 1:
//     op[0] = '-';
//     break;
//   case 2:
//     op[0] = '*';
//     break;
//   case 3:
//     op[0] = '/';
//     break;

//   default:
//     op[0] = '+';
//     break;
//   }
//   //op[0]='+';
//   gen_printf("%c",op[0]);
// }
// static void gen_rand_expr()
// {

//   switch (choose(3))
//   {
//   case 0:
//     gen_num();
//     break;
//   case 1:
//     gen('(');
//     gen_rand_expr();
//     gen(')');
//     break;
//   default:
//     gen_rand_expr();
//     gen_rand_op();
//     gen_rand_expr();
//     break;
//   }
// }

// static void gen_num(){
//   char str_tmp[500];
//   sprintf(str_tmp, "%d", (int)((rand() / 31535) +1));
//   strcat(buf, str_tmp);
// }
// int choose(int n)
// {
//   return (int)(rand() % n);
// }
// static void gen_rand_op()
// {
//   char op[2];
//   op[1]='\0';
//   switch (choose(4))
//   {
//   case 0:
//     op[0] = '+';
//     break;
//   case 1:
//     op[0] = '-';
//     break;
//   case 2:
//     op[0] = '*';
//     break;
//   case 3:
//     op[0] = '/';
//     break;

//   default:
//     op[0] = '+';
//     break;
//   }
//   op[1]='\0';
//   strcat(buf, op);
// }
// static void gen_rand_expr()
// {

//   switch (choose(3))
//   {
//   case 0:
//     gen_num();
//     break;
//   case 1:
//     gen('(');
//     gen_rand_expr();
//     gen(')');
//     break;
//   default:
//     gen_rand_expr();
//     gen_rand_op();
//     gen_rand_expr();
//     break;
//   }
// }

int main(int argc, char *argv[])
{
  int seed = time(0);
  srand(seed);
  // printf("======\n   %d    \n==========\n",choose(3));
  int loop = 1;
  if (argc > 1)
  {
    sscanf(argv[1], "%d", &loop);
  }
  int i;
  for (i = 0; i < loop; i++)
  {
    buf_start = buf;
    gen_rand_expr();

    sprintf(code_buf, code_format, buf);

    FILE *fp = fopen("/tmp/.code.c", "w");
    assert(fp != NULL);
    fputs(code_buf, fp);
    fclose(fp);

    int ret = system("gcc /tmp/.code.c -o /tmp/.expr");
    if (ret != 0)
      continue;

    fp = popen("/tmp/.expr", "r");
    assert(fp != NULL);

    int result;
    ret = fscanf(fp, "%d", &result);
    pclose(fp);

    printf("%u %s\n", result, buf);
  }
  return 0;
}
