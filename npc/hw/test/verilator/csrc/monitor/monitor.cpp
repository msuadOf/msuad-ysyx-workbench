#include <stdio.h>
#include <stdlib.h>
#include <assert.h>
#include <string.h>

//readline
#include <readline/readline.h>
#include <readline/history.h>

#include "common.h"
#include "mem.h"


extern void cpu_exec(uint64_t n);

static int is_batch_mode = false;

void sdb_set_batch_mode()
{
  is_batch_mode = true;
}

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
  npc_state.state = NPC_QUIT;
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
static int cmd_info(char *args)
{
  args = strtok(NULL, " ");

  if (args == NULL)
  {
    printf("Usage: info SUBCMD(r/w)\n");
    return 0;
  }

  if (strcmp(args, "r") == 0)
  {
    isa_reg_display();
    extern int diff_en;
    printf("diff_en=%d\n",diff_en);
  }
  else if (args[0] == 'r') // r1;r2...r31;r32
  {
    char *new_args = args + 1;
    int i = atoi(new_args);
    printf("r%-2d: ", i);
    isa_reg_display_byIndex(i);
  }
  if (strcmp(args, "w") == 0)
  {
    // wp_display();
  }

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
    {"info", "打印寄存器状态,打印监视点信息", cmd_info},
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

  if (is_batch_mode)
  {
    cmd_c(NULL);
    return 0;
  }

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

static char *log_file = NULL;
static char *diff_so_file = NULL;
static char *img_file = NULL;

static long load_img() {
  if (img_file == NULL) {
    Log("No image is given. Use the default build-in image.");
    return 4096; // built-in image size
  }

  FILE *fp = fopen(img_file, "rb");
  Assert(fp, "Can not open '%s'", img_file);

  fseek(fp, 0, SEEK_END);
  long size = ftell(fp);

  Log("The image is %s, size = %ld", img_file, size);

  fseek(fp, 0, SEEK_SET);
  int ret = fread(guest_to_host(RESET_VECTOR), size, 1, fp);
  assert(ret == 1);

  fclose(fp);
  return size;
}

#include <getopt.h>

static int parse_args(int argc, char *argv[]) {
  const struct option table[] = {
    {"batch"    , no_argument      , NULL, 'b'},
    {"log"      , required_argument, NULL, 'l'},
    {"diff"     , required_argument, NULL, 'd'},
    {"port"     , required_argument, NULL, 'p'},
    {"help"     , no_argument      , NULL, 'h'},
    {0          , 0                , NULL,  0 },
  };
  int o;
  while ( (o = getopt_long(argc, argv, "-bhl:d:p:", table, NULL)) != -1) {
    switch (o) {
      case 'b': sdb_set_batch_mode(); break;
    //   case 'l': log_file = optarg; break;
      case 'd': diff_so_file = optarg; break;
      case 1: img_file = optarg; return 0;
      default:
        printf("Usage: %s [OPTION...] IMAGE [args]\n\n", argv[0]);
        printf("\t-b,--batch              run with batch mode\n");
        printf("\t-l,--log=FILE           output log to FILE\n");
        printf("\t-d,--diff=REF_SO        run DiffTest with reference REF_SO\n");
        printf("\t-p,--port=PORT          run DiffTest with port PORT\n");
        printf("\n");
        exit(0);
    }
  }
  return 0;
}

extern void init_difftest(char *ref_so_file, long img_size, int port);
extern void cpu_init();
int monitor(int argc, char** argv){
    for(int i=0;i<argc;i++){
        printf("%s ",argv[i]);
    }
    printf("%c",'\n');

    parse_args(argc, argv);
    long img_size = load_img();
    cpu_init();
    init_difftest(diff_so_file, 1, 123);
    

    Log("Start simulation\n");
    npc_state.state=NPC_RUNNING;
    return sdb_mainloop();
}