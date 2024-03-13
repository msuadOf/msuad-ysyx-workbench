#include <am.h>
#include <klib.h>
#include <rtthread.h>

#define log_printf rt_kprintf

#include "debug.h"

#define STACK_ADDR (0)
// #define STACK_ADDR 0x800229C8UL
#define STACK_OFFSET(p) ((void*) (p) -(void*)STACK_ADDR)

static Context* ev_handler(Event e, Context *c) {
  switch (e.event) {
    case EVENT_YIELD:printf("ctx=%d\n",STACK_OFFSET(c)); break;
    default: printf("Unhandled event ID = %d\n", e.event); assert(0);
  }
  return c;
}

void __am_cte_init() {
  cte_init(ev_handler);
}

#define CSR_READ(csr, result) \
    asm volatile ("csrr %0, " # csr : "=r"(result) : )

#define CSR_WRITE(csr, value) \
    asm volatile ("csrw " # csr ", %0" : :  "r"(value))

void rt_hw_context_switch_to(rt_ubase_t to) {

    Context* to_c=(Context*)to;
Log("to=%d",to_c->mepc);
    CSR_WRITE(mepc,to_c->mepc);

    //asm("mv sp,a0");
    // 触发一次自陷，以便在事件处理回调函数中进行上下文切换
    yield();
}

void rt_hw_context_switch(rt_ubase_t from, rt_ubase_t to) {
  assert(0);
}

void rt_hw_context_switch_interrupt(void *context, rt_ubase_t from, rt_ubase_t to, struct rt_thread *to_thread) {
  assert(0);
}

// 假设每个gpr占用8字节（uintptr_t大小），NR_REGS为寄存器数量，额外加上wrap_func_params_t结构体的大小
#define CONTEXT_SIZE ((sizeof(uintptr_t) * 32) + sizeof(uintptr_t) * 4 /* + sizeof(wrap_func_params_t) */)

/* typedef struct _wrap_func_params {
    void (*tentry)(void *parameter);
    void *parameter;
    void (*texit)(void);
} wrap_func_params_t;

static void wrap_entry(void *params)
{
    wrap_func_params_t *p = (wrap_func_params_t *)params;

    p->tentry(p->parameter); // 调用入口函数
    p->texit();              // 在tentry返回后调用退出函数

    // 为了满足不从texit返回的要求，可以进入无限循环或者触发异常等操作
    while (1) {}
} */
rt_uint8_t *rt_hw_stack_init(void *tentry, void *parameter, rt_uint8_t *stack_addr, void *texit) {
  //对齐stack
  rt_uint8_t *unaligned_stack_addr = stack_addr; // 你获取到的原始堆栈地址
  rt_uint8_t *aligned_stack_addr = (rt_uint8_t *)(((uintptr_t)unaligned_stack_addr + sizeof(uintptr_t) - 1) & ~(sizeof(uintptr_t) - 1));

  aligned_stack_addr=stack_addr;
  Context *ctx = (Context *)(aligned_stack_addr - CONTEXT_SIZE);


    // 设置Context结构体中的其他字段
    ctx->mepc = (uintptr_t)tentry; // 设置新的入口点为包装函数
    ctx->mstatus = 0x1800; // 根据实际情况设置mstatus寄存器
    ctx->mcause = 0; // 初始化mcause，这里假设为用户模式下的初始值
    ctx->pdir = NULL; // 如果需要，初始化页表指针，此处假设为空

    // 在RISC-V架构下，由于我们没有直接通过寄存器传递参数，因此无需在这里设置gpr寄存器
    Log("aligned_stack_addr=%d ,stack_addr=%d ,ctx=%d,CONTEXT_SIZE=%d",STACK_OFFSET(aligned_stack_addr),STACK_OFFSET(stack_addr),STACK_OFFSET(ctx),CONTEXT_SIZE);
    return (rt_uint8_t *)ctx; // 返回对齐后的堆栈地址
}
/* chatgpt 写的 */
/* typedef struct _wrap_func_params {
    void (*tentry)(void *parameter);
    void *parameter;
    void (*texit)(void);
} wrap_func_params_t;

static void wrap_entry(void *params)
{
    wrap_func_params_t *p = (wrap_func_params_t *)params;

    p->tentry(p->parameter); // 调用入口函数
    p->texit();              // 在tentry返回后调用退出函数

    // 为了满足不从texit返回的要求，可以进入无限循环或者触发异常等操作
    while (1) {}
}

// 假设每个gpr占用8字节（uintptr_t大小），NR_REGS为寄存器数量，额外加上wrap_func_params_t结构体的大小
#define CONTEXT_SIZE ((sizeof(uintptr_t) * NR_REGS) + sizeof(uintptr_t) * 3 + sizeof(wrap_func_params_t))

rt_uint8_t *rt_hw_stack_init(void *tentry, void *parameter, rt_uint8_t *stack_addr, void *texit)
{
    uintptr_t aligned_stack_addr = stack_addr;
    aligned_stack_addr += sizeof(uintptr_t) - 1;
    aligned_stack_addr &= ~(sizeof(uintptr_t) - 1);

    Context *ctx = (Context *)(aligned_stack_addr - CONTEXT_SIZE);
    wrap_func_params_t *params_location = (wrap_func_params_t *)((uint8_t *)ctx + sizeof(Context));

    // 初始化包装函数参数并保存到堆栈中
    wrap_func_params_t params = { .tentry = tentry, .parameter = parameter, .texit = texit };
    memcpy(params_location, &params, sizeof(params)); // 将参数复制到堆栈上的指定位置

    // 设置Context结构体中的其他字段
    ctx->mepc = (uintptr_t)wrap_entry; // 设置新的入口点为包装函数
    ctx->mstatus = 0x1800; // 根据实际情况设置mstatus寄存器
    ctx->mcause = 0; // 初始化mcause，这里假设为用户模式下的初始值
    ctx->pdir = NULL; // 如果需要，初始化页表指针，此处假设为空

    // 在RISC-V架构下，由于我们没有直接通过寄存器传递参数，因此无需在这里设置gpr寄存器

    return (rt_uint8_t *)ctx; // 返回对齐后的堆栈地址
} */