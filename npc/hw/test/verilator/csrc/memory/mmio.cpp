#include "common.h"
#include "macro.h"
#include "debug.h"

#define IS_AT(DEVICE_NAME) concat(DEVICE_NAME,_mmio_is_at_serial)
#define DEF_IF_ADDR(DEVICE_NAME) static inline int IS_AT(DEVICE_NAME) (paddr_t addr, int len){\
                          if(addr<concat3(CONFIG_,DEVICE_NAME,_MMIO) || addr>concat3(CONFIG_,DEVICE_NAME,_MMIO_END) ){\
                            return 0;\
                          }\
                          if((addr+(len-1))<concat3(CONFIG_,DEVICE_NAME,_MMIO) || (addr+(len-1))>concat3(CONFIG_,DEVICE_NAME,_MMIO_END)){\
                            return 0;\
                          }\
                          return 1;\
                        }

/* add device addr checker here */
IFDEF(CONFIG_DEVICE_SERIAL,DEF_IF_ADDR(SERIAL)) 
IFDEF(CONFIG_DEVICE_RTC,DEF_IF_ADDR(RTC)) 

/* bus interface */
#define MMIO_IS_AT(DEVICE_NAME) IS_AT(DEVICE_NAME)(addr, len)
  // eg. ifdef CONFIG_DEVICE_SERIAL
#define doREAD(DEVICE_NAME) IFDEF(concat(CONFIG_DEVICE_,DEVICE_NAME),\
                              extern word_t mmio_read_SERIAL(paddr_t addr, int len);\
                              if(MMIO_IS_AT(DEVICE_NAME)) { return mmio_read_SERIAL(addr, len); }; \
                              )

#define doWRITE(DEVICE_NAME)  IFDEF(concat(CONFIG_DEVICE_,DEVICE_NAME),\
                                        extern void mmio_write_SERIAL(paddr_t addr, int len, word_t data);\
                                        if(MMIO_IS_AT(DEVICE_NAME)) {  mmio_write_SERIAL(addr, len,data); return; }; \
                                  )

word_t mmio_read(paddr_t addr, int len) {
    // extern word_t mmio_read_SERIAL(paddr_t addr, int len);
    // if(MMIO_IS_AT(SERIAL)) { return mmio_read_SERIAL(addr, len); };
    doREAD(SERIAL);
    doREAD(RTC);
    panic("[npc]mmio_read should not reach here!");
    return -1;
}

void mmio_write(paddr_t addr, int len, word_t data) {
    // extern void mmio_write_SERIAL(paddr_t addr, int len, word_t data);
    // if(MMIO_IS_AT(SERIAL)) {  mmio_write_SERIAL(addr, len,data); return; };
    doWRITE(SERIAL);
    doWRITE(RTC);
    panic("[npc]mmio_write should not reach here!");
}

/* 宏定义原型 */
/* bool mmio_is_at_serial(paddr_t addr, int len){
  if(addr>=CONFIG_SERIAL_MMIO || addr<=CONFIG_SERIAL_MMIO_END){
    return false;
  }
  if((addr+(len-1))>=CONFIG_SERIAL_MMIO || (addr+(len-1))<=CONFIG_SERIAL_MMIO_END){
    return false;
  }
  return true;
} */