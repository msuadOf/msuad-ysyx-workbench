#include "common.h"
#include "macro.h"

#define IS_AT(DEVICE_NAME) concat(DEVICE_NAME,_mmio_is_at_serial)
#define DEF_IF_ADDR(DEVICE_NAME) static inline bool IS_AT(DEVICE_NAME) (paddr_t addr, int len){\
                          if(addr>=concat3(CONFIG_,DEVICE_NAME,_MMIO) || addr<=concat3(CONFIG_,DEVICE_NAME,_MMIO_END) ){\
                            return false;\
                          }\
                          if((addr+(len-1))>=concat3(CONFIG_,DEVICE_NAME,_MMIO) || (addr+(len-1))<=concat3(CONFIG_,DEVICE_NAME,_MMIO_END)){\
                            return false;\
                          }\
                          return true;\
                        }

// IFDEF(CONFIG_SERIAL_MMIO,DEF_IF_ADDR(SERIAL))
DEF_IF_ADDR(SERIAL) 

/* bus interface */
#define MMIO_IS_AT(DEVICE_NAME) IS_AT(SERIAL)(addr, len)
word_t mmio_read(paddr_t addr, int len) {
  if(MMIO_IS_AT(SERIAL)) assert(0);
}

void mmio_write(paddr_t addr, int len, word_t data) {
  
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