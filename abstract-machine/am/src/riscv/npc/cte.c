#include <am.h>
#include <riscv/riscv.h>
#include <klib.h>

static Context* (*user_handler)(Event, Context*) = NULL;

Context* __am_irq_handle(Context *c) {
  if (user_handler) {
    Event ev = {0};
    switch (c->mcause) {
      case 0xbUL : ev.event = EVENT_YIELD; break;
      default: ev.event = EVENT_ERROR; break;
    }

    c = user_handler(ev, c);
    assert(c != NULL);

    //ps:神奇，三种都行，第三种是trap.S里面 addi t2,t2,4 # for mret: mepc+=4
    /* 法一 */
    c->mepc=(uintptr_t)((uint8_t*)(c->mepc)+4*sizeof(uint8_t));
    /* 法二 */
    // c->mepc+=1;
    // c->mepc&=~0x1;

    if(ev.event == EVENT_ERROR){
      assert(c != NULL);//只写了yield,没有对错误操作的处理，不应该到这里
    }
  }
  else{
    assert(c != NULL);//只写了有user_handler的情况，不应该到这里
  }


  return c;
}

extern void __am_asm_trap(void);

bool cte_init(Context*(*handler)(Event, Context*)) {
  // initialize exception entry
  asm volatile("csrw mtvec, %0" : : "r"(__am_asm_trap));

  // register event handler
  user_handler = handler;

  return true;
}

Context *kcontext(Area kstack, void (*entry)(void *), void *arg) {
  uint32_t *ed = kstack.end;
  Context *ctx = (Context *) (ed - 36); // 36 = 32 + 3 + 1
  ctx->mepc = (uintptr_t) entry;
  ctx->mstatus = 0x1800;

  ctx->gpr[10] = (uintptr_t) arg;
  return ctx;
}

void yield() {
#ifdef __riscv_e
  asm volatile("li a5, -1; ecall");
#else
  asm volatile("li a7, -1; ecall");
#endif
}

bool ienabled() {
  return false;
}

void iset(bool enable) {
}
