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

#include <isa.h>
#include <cpu/cpu.h>
#include <readline/readline.h>
#include <readline/history.h>
#include "sdb.h"
#include <stdlib.h>
#include <isa.h>
#include <memory/paddr.h>
#include <utils.h>

static int is_batch_mode = false;

void init_regex();
void init_wp_pool();

/* We use the `readline' library to provide more flexibility to read from stdin. */
static char* rl_gets() {
  static char *line_read = NULL;

  if (line_read) {
    free(line_read);
    line_read = NULL;
  }

  line_read = readline("(nemu) ");

  if (line_read && *line_read) {
    add_history(line_read);
  }

  return line_read;
}

static int cmd_c(char *args) {
  cpu_exec(-1);
  return 0;
}


static int cmd_q(char *args) {
  return -1;
}

static int cmd_help(char *args);

static int cmd_si(char *args) {
  args = strtok(NULL, " ");
  int n;
  if (args == NULL) {
    n = 1;
  } else {
    n=atoi(args);
  }

  cpu_exec(n);
  return 0;
}
extern void isa_reg_displayByIndex(int i);
static int cmd_info(char *args) {
  args=strtok(NULL, " ");
  
  if (strcmp(args, "r") == 0) {
    isa_reg_display();
  }else if (args[0]=='r')
  {
    char* new_args=args+1;
    int i = atoi(new_args);
    printf("r%-2d: ",i);
    isa_reg_displayByIndex(i);

  }
  
  return 0;
}

void memory_rw_test(paddr_t addr,int len){
    printf("====memory r&w test====\n  read: %d\n",paddr_read(addr, len) );
  paddr_write( addr,  len,  2) ;
  printf("read: %d \n",paddr_read(addr, len) );
  printf("====memory r&w test Failed!====");

}
void print_byte_without_0x(uint8_t n){
  char s[10];
  sprintf(s,"%#03x ", n);
  printf(s+1);
}
void HALHook_displayMem(paddr_t addr){
  print_byte_without_0x(3);
printf(ANSI_FMT("%#018x: ", ANSI_FG_CYAN), 1);
word_t word=paddr_read(addr, 4);
printf("%#018x ", word);
}
static int cmd_x(char *args) {
  HALHook_displayMem(0x80000000);
  memory_rw_test(0x80000000,1);
HALHook_displayMem(0x80000000);
paddr_write(0x80000000, 1, 1);
HALHook_displayMem(0x80000000);
  return 0;
}

static struct {
  const char *name;
  const char *description;
  int (*handler) (char *);
} cmd_table [] = {
  { "help", "Display information about all supported commands", cmd_help },
  { "c", "Continue the execution of the program", cmd_c },
  { "q", "Exit NEMU", cmd_q },
  { "si", "让程序单步执行N条指令后暂停执行,当N没有给出时, 缺省为1",cmd_si},
  { "x", "求出表达式EXPR的值, 将结果作为起始内存地址, 以十六进制形式输出连续的N个4字节", cmd_x },
  { "info", "	打印寄存器状态,打印监视点信息", cmd_info },
  /* TODO: Add more commands */

};

#define NR_CMD ARRLEN(cmd_table)

static int cmd_help(char *args) {
  /* extract the first argument */
  char *arg = strtok(NULL, " ");
  int i;

  if (arg == NULL) {
    /* no argument given */
    for (i = 0; i < NR_CMD; i ++) {
      printf("%s - %s\n", cmd_table[i].name, cmd_table[i].description);
    }
  }
  else {
    for (i = 0; i < NR_CMD; i ++) {
      if (strcmp(arg, cmd_table[i].name) == 0) {
        printf("%s - %s\n", cmd_table[i].name, cmd_table[i].description);
        return 0;
      }
    }
    printf("Unknown command '%s'\n", arg);
  }
  return 0;
}

void sdb_set_batch_mode() {
  is_batch_mode = true;
}

void sdb_mainloop() {
  if (is_batch_mode) {
    cmd_c(NULL);
    return;
  }

  for (char *str; (str = rl_gets()) != NULL; ) {
    char *str_end = str + strlen(str);

    /* extract the first token as the command */
    char *cmd = strtok(str, " ");
    if (cmd == NULL) { continue; }

    /* treat the remaining string as the arguments,
     * which may need further parsing
     */
    char *args = cmd + strlen(cmd) + 1;
    if (args >= str_end) {
      args = NULL;
    }

#ifdef CONFIG_DEVICE
    extern void sdl_clear_event_queue();
    sdl_clear_event_queue();
#endif

    int i;
    for (i = 0; i < NR_CMD; i ++) {
      if (strcmp(cmd, cmd_table[i].name) == 0) {
        if (cmd_table[i].handler(args) < 0) { return; }
        break;
      }
    }

    if (i == NR_CMD) { printf("Unknown command '%s'\n", cmd); }
  }
}

void init_sdb() {
  /* Compile the regular expressions. */
  init_regex();

  /* Initialize the watchpoint pool. */
  init_wp_pool();
}
