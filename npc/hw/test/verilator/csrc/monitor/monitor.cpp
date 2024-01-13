#include <stdio.h>
#include <stdlib.h>
#include <assert.h>
#include <string.h>

//readline
#include <readline/readline.h>
#include <readline/history.h>

#include "common.h"

extern void cpu_exec(uint64_t n);

static char *rl_gets()
{
  static char *line_read = NULL;

  if (line_read)
  {
    free(line_read);
    line_read = NULL;
  }

  line_read = readline("(npc) ");

  if (line_read && *line_read)
  {
    add_history(line_read);
  }

  return line_read;
}
static int cmd_c(char *args)
{
  cpu_exec(-1);
  return -1;
}
static int cmd_q(char *args){
    return -1;
}
static int cmd_si(char *args)
{
  args = strtok(NULL, " ");
  int n;
  if (args == NULL)
  {
    n = 1;
  }
  else
  {
    n = atoi(args);
  }

  cpu_exec(n);
  return 0;
}
static int cmd_help(char *args);
static struct
{
  const char *name;
  const char *description;
  int (*handler)(char *);
} cmd_table[] = {
    {"help", "Display information about all supported commands", cmd_help},
    {"c", "Continue the execution of the program", cmd_c},
    {"q", "Exit NEMU", cmd_q},
    {"si", "让程序单步执行N条指令后暂停执行,当N没有给出时, 缺省为1", cmd_si},
    // {"x", "求出表达式EXPR的值, 将结果作为起始内存地址, 以十六进制形式输出连续的N个4字节", cmd_x},
    // {"info", "打印寄存器状态,打印监视点信息", cmd_info},
    // {"p", "求出表达式EXPR的值, EXPR支持的", cmd_p},
    // {"w", "w EXPR: 当表达式EXPR的值发生变化时, 暂停程序执行", cmd_w},
    // {"d", "d N: Exit NEMU删除序号为N的监视点", cmd_d},
    /* TODO: Add more commands */

};

#define NR_CMD ARRLEN(cmd_table)

static int cmd_help(char *args)
{
  /* extract the first argument */
  char *arg = strtok(NULL, " ");
  int i;

  if (arg == NULL)
  {
    /* no argument given */
    for (i = 0; i < NR_CMD; i++)
    {
      printf("%s - %s\n", cmd_table[i].name, cmd_table[i].description);
    }
  }
  else
  {
    for (i = 0; i < NR_CMD; i++)
    {
      if (strcmp(arg, cmd_table[i].name) == 0)
      {
        printf("%s - %s\n", cmd_table[i].name, cmd_table[i].description);
        return 0;
      }
    }
    printf("Unknown command '%s'\n", arg);
  }
  return 0;
}

int sdb_mainloop(){

  for (char *str; (str = rl_gets()) != NULL;)
  {
    char *str_end = str + strlen(str);

    /* extract the first token as the command */
    char *cmd = strtok(str, " ");
    if (cmd == NULL)
    {
      continue;
    }

    /* treat the remaining string as the arguments,
     * which may need further parsing
     */
    char *args = cmd + strlen(cmd) + 1;
    if (args >= str_end)
    {
      args = NULL;
    }

    int i;
    for (i = 0; i < NR_CMD; i++)
    {
      if (strcmp(cmd, cmd_table[i].name) == 0)
      {
        if (cmd_table[i].handler(args) < 0)
        {
          return -1;
        }
        break;
      }
    }

    if (i == NR_CMD)
    {
      printf("Unknown command '%s'\n", cmd);
    }
  }
    return 0;
}

int monitor(int argc, char** argv){
    
}