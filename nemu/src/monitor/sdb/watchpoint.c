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

#include "sdb.h"
#include "utils.h"

#define NR_WP 32

typedef struct watchpoint
{
  int NO;
  struct watchpoint *next;

  char expr[200]; // possible bug...
  word_t val_now;
  word_t val_last;
} WP;

static WP wp_pool[NR_WP] = {};
static WP *head = NULL, *free_ = NULL;

void init_wp_pool()
{
  int i;
  for (i = 0; i < NR_WP; i++)
  {
    wp_pool[i].NO = i;
    wp_pool[i].next = (i == NR_WP - 1 ? NULL : &wp_pool[i + 1]);
  }

  head = NULL;
  free_ = wp_pool;
}

WP *new_wp()
{
  assert(free_);
  WP *ret = free_;
  free_ = free_->next;
  ret->next = head;
  head = ret;
  return ret;
}
void free_wp(WP *wp)
{
  WP *p = head;
  if (p == wp)
    head = head->next;
  else
  {
    while (p && p->next != wp)
      p = p->next;
    assert(p);
    p->next = wp->next;
  }
  wp->next = free_;
  free_ = wp;
}
WP *wp_getByNO(int no)
{
  WP *p = head;
  if (p == NULL)
  {
    return NULL;
  }
  do
  {
    if (p->NO == no)
    {
      return p;
    }
    p = p->next;
  } while (p != NULL);
  return NULL;
}

void wp_add(char *expr, word_t res)
{
  WP *wp = new_wp();
  strcpy(wp->expr, expr);
  wp->val_last = res;
  printf("Watchpoint %d: %s (=%u)\n", wp->NO, expr, res);
}
void wp_del(int no)
{
  if (no >= NR_WP)
  {
    printf("[Error](d N): N(=%d) should be [0-%d]\n", no, NR_WP - 1);
    return;
  }
  WP *wp = wp_getByNO(no);
  if (wp == NULL)
  {
    printf("[Error](watchpoint=%d):did not find it\n", no);
  }
  else
  {
    free_wp(wp);
    printf("Delete watchpoint %d: %s\n", wp->NO, wp->expr);
  }
}
void wp_display()
{
  WP *p = head;
  if (!p)
  {
    printf("No watchpoints\n");
    return;
  }
  printf("%-8s%-8s\n", "Num", "What");
  while (p)
  {
    printf("%-8d%-8s\n", p->NO, p->expr);
    p = p->next;
  }
}
void wp_difftest(){
  WP* p = head;
  while (p) {
    bool sucess;
    p->val_now = expr(p->expr, &sucess);
    if (p->val_last != p->val_now) {
      printf("Watchpoint %d: %s\n"
        "Old value = %d\n"
        "New value = %d\n"
        , p->NO, p->expr, p->val_last, p->val_now);
      p->val_last = p->val_now;
    }
    p = p->next;
  }
  //nemu_state.state=NEMU_STOP; //TODO:简单使用后会造成指令（ebreak）执行完不能正常停止
}
// void print_wp_node(WP *wp){
//   printf("[node:%d](val:%d)",wp,wp->NO);
// }