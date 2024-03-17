#include "common.h"
#include "macro.h"

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

IFDEF(CONFIG_DEVICE_SERIAL,DEF_IF_ADDR(SERIAL)) 
IFDEF(CONFIG_DEVICE_RTC,DEF_IF_ADDR(RTC)) 

/* bus interface */
#define MMIO_IS_AT(DEVICE_NAME) IS_AT(DEVICE_NAME)(addr, len)
#define doREAD(DEVICE_NAME) extern word_t mmio_read_SERIAL(paddr_t addr, int len);\
                            if(MMIO_IS_AT(DEVICE_NAME)) { return mmio_read_SERIAL(addr, len); }; 

#define doWRITE(DEVICE_NAME) extern void mmio_write_SERIAL(paddr_t addr, int len, word_t data);\
                                        if(MMIO_IS_AT(DEVICE_NAME)) {  mmio_write_SERIAL(addr, len,data); return; };

word_t mmio_read(paddr_t addr, int len) {
    // extern word_t mmio_read_SERIAL(paddr_t addr, int len);
    // if(MMIO_IS_AT(SERIAL)) { return mmio_read_SERIAL(addr, len); };
    doREAD(SERIAL);
    doREAD(RTC);
}

void mmio_write(paddr_t addr, int len, word_t data) {
    // extern void mmio_write_SERIAL(paddr_t addr, int len, word_t data);
    // if(MMIO_IS_AT(SERIAL)) {  mmio_write_SERIAL(addr, len,data); return; };
    doWRITE(SERIAL);
    doWRITE(RTC);
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