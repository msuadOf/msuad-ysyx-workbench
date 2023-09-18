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
#include <stdint.h>

/* We use the POSIX regex functions to process regular expressions.
 * Type 'man regex' for more information about POSIX regex functions.
 */
#include <regex.h>

enum
{
  TK_NOTYPE = 256,
  TK_EQ,

  /* TODO: Add more token types */
  TK_NUM,

};

static struct rule
{
  const char *regex;
  int token_type;
} rules[] = {

    /* TODO: Add more rules.
     * Pay attention to the precedence level of different rules.
     */
    // 十进制整数
    //  +, -, *, /
    //  (, )
    //  空格串(一个或多个空格)

    {" +", TK_NOTYPE}, // spaces
    {"\\+", '+'},      // plus
    {"==", TK_EQ},     // equal

    {"-", '-'},
    {"\\*", '*'},
    {"/", '/'},
    {"[0-9]+", TK_NUM},

    {"\\(", '('},
    {"\\)", ')'},
};

#define NR_REGEX ARRLEN(rules)

static regex_t re[NR_REGEX] = {};

/* Rules are used for many times.
 * Therefore we compile them only once before any usage.
 */
void init_regex()
{
  int i;
  char error_msg[128];
  int ret;

  for (i = 0; i < NR_REGEX; i++)
  {
    ret = regcomp(&re[i], rules[i].regex, REG_EXTENDED);
    if (ret != 0)
    {
      regerror(ret, &re[i], error_msg, 128);
      panic("regex compilation failed: %s\n%s", error_msg, rules[i].regex);
    }
  }
}

typedef struct token
{
  int type;
  char str[32];
} Token;

static Token tokens[32] __attribute__((used)) = {};
static int nr_token __attribute__((used)) = 0;

static bool make_token(char *e)
{
  int position = 0;
  int i;
  regmatch_t pmatch;

  nr_token = 0;

  while (e[position] != '\0')
  {
    /* Try all rules one by one. */
    for (i = 0; i < NR_REGEX; i++)
    {
      if (regexec(&re[i], e + position, 1, &pmatch, 0) == 0 && pmatch.rm_so == 0)
      {
        char *substr_start = e + position;
        int substr_len = pmatch.rm_eo;

        Log("match rules[%d] = \"%s\" at position %d with len %d: %.*s",
            i, rules[i].regex, position, substr_len, substr_len, substr_start);

        position += substr_len;

        /* TODO: Now a new token is recognized with rules[i]. Add codes
         * to record the token in the array `tokens'. For certain types
         * of tokens, some extra actions should be performed.
         */

        tokens[nr_token].type = rules[i].token_type;
        switch (rules[i].token_type)
        {
        case TK_NOTYPE:
        {
          break;
        }
        case '+':
        case '-':
        case '*':
        case '/':
        case '(':
        case ')':
        case TK_NUM:
        default:
        {
          strncpy(tokens[nr_token].str, substr_start, substr_len);
          Log("====== default: + - * / =====");
          break;
        }
        }
        nr_token++;
        break;
      }
    }

    if (i == NR_REGEX)
    {
      printf("no match at position %d\n%s\n%*.s^\n", position, e, position, "");
      return false;
    }
  }

  return true;
}
bool check_parentheses(int p, int q)
{
  int stack_cnt = 0;
  if (tokens[p].type == '(' && tokens[q].type == ')')
  {
    for (int i = p; i <= q; i++)
    {
      if (tokens[i].type == '(')
      {
        stack_cnt++;
      }
      else if (tokens[i].type == ')')
      {
        stack_cnt--;
      }
    }
    if (stack_cnt == 0)
    {
      return true;
    }
  }
  return false;
}
/**
 * @brief function: get_majorIndex() allow user get the index of major op
 *
 *
 * @param p
 * @param q
 * @return >0 sucess and the value is index
 *         -1 failed
 */

int get_majorIndex(int p, int q)
{
  int ret = -1, par = 0, op_type = 0;
  for (int i = p; i <= q; i++)
  {
    if (tokens[i].type == TK_NUM)
    {
      continue;
    }
    if (tokens[i].type == '(')
    {
      par++;
    }
    else if (tokens[i].type == ')')
    {
      if (par == 0)
      {
        return -1;
      }
      par--;
    }
    else if (par > 0)
    {
      continue;
    }
    else
    {
      int tmp_type = 0;
      switch (tokens[i].type)
      {
      case '*':
      case '/':
        tmp_type = 1;
        break;
      case '+':
      case '-':
        tmp_type = 2;
        break;
      default:
        assert(0);
      }
      if (tmp_type >= op_type)
      {
        op_type = tmp_type;
        ret = i;
      }
    }
  }
  if (par != 0)
    return -1;
  return ret;
}
uint32_t eval(int p, int q, bool *sucess)
{
  if (p > q)
  {
    /* Bad expression */
  }
  else if (p == q)
  {
    /* Single token.
     * For now this token should be a number.
     * Return the value of the number.
     */
    if (tokens[p].type != TK_NUM)
    {
      *sucess = false;
      printf("[Error]:What have you give me? where is number???");
      return -1;
    }
    return strtol(tokens[p].str, NULL, 10);
  }
  else if (check_parentheses(p, q) == true)
  {
    /* The expression is surrounded by a matched pair of parentheses.
     * If that is the case, just throw away the parentheses.
     */
    return eval(p + 1, q - 1, sucess);
  }
  else
  {
    int major_index = get_majorIndex(p, q);
    if (major_index < 0)
    {
      *sucess = false;
      return 0;
    }
    Log("major_index=%d", major_index);

    uint32_t val1 = eval(p, major_index - 1, sucess);
    uint32_t val2 = eval(major_index + 1, q, sucess);
    if (*sucess == false)
      return -1;

    switch (tokens[major_index].type)
    {
    case '+':
      return val1 + val2;
    case '-':
      return val1 - val2;
    case '*':
      return val1 * val2;
    case '/':
      if (val2 == 0)
      {
        *sucess = false;
        return 0;
      }
      return (sword_t)val1 / (sword_t)val2; // e.g. -1/2, may not pass the expr test
    default:
      assert(0);
    }
  }

  return -1;
}

static void clear_expr_once()
{
  nr_token = 0;
}
static void print_token()
{
  for (int i = 0; i < 32; i++)
  {
    Log("%d: %s,type=%c", i, tokens[i].str, tokens[i].type);
  }
}
word_t expr(char *e, bool *success)
{
  *success = true;
  if (!make_token(e))
  {
    *success = false;
    return 0;
  }

  /* TODO: Insert codes to evaluate the expression. */
  // TODO();
  uint32_t ret = eval(0, nr_token - 1, success);
  print_token();
  clear_expr_once(); // 对表达式求出值后进行复位
  return ret;
}
