#include "common.h"
#include "macro.h"


#define DEF_IF_ADDR(DEVICE_NAME) static inline bool concat(DEVICE_NAME,_mmio_is_at_serial) (paddr_t addr, int len){\
                          if(addr>=concat3(CONFIG_,DEVICE_NAME,_MMIO) || addr<=concat3(CONFIG_,DEVICE_NAME,_MMIO_END) ){\
                            return false;\
                          }\
                          if((addr+(len-1))>=concat3(CONFIG_,DEVICE_NAME,_MMIO) || (addr+(len-1))<=concat3(CONFIG_,DEVICE_NAME,_MMIO_END)){\
                            return false;\
                          }\
                          return true;\
                        }

DEF_IF_ADDR(SERIAL)

/* bus interface */
word_t mmio_read(paddr_t addr, int len) {
  
}

void mmio_write(paddr_t addr, int len, word_t data) {
  
}