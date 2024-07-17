#include <utils.h>
#include <common.h>
/* http://en.wikibooks.org/wiki/Serial_Programming/8250_UART_Programming */
// NOTE: this is compatible to 16550
#ifdef CONFIG_DEVICE_SERIAL


#define CH_OFFSET 0

static uint8_t serial_base[2] = {0};


static void serial_putc(char ch) {
  MUXDEF(CONFIG_TARGET_AM, putch(ch), putc(ch, stderr));
}

static void serial_io_handler(uint32_t offset, int len, bool is_write) {
  assert(len == 1);
  switch (offset) {
    /* We bind the serial port with the host stderr in NEMU. */
    case CH_OFFSET:
      if (is_write) serial_putc(serial_base[0]);
      else panic("do not support read");
      break;
    default: panic("do not support offset = %d", offset);
  }
}
/* bus interface */

word_t mmio_read_SERIAL(paddr_t addr, int len) {
  //TODO:getc()
  assert(0);
  serial_io_handler(addr-CONFIG_SERIAL_MMIO,len,0);
}

void mmio_write_SERIAL(paddr_t addr, int len, word_t data) {
  //emmulate register

  if(addr==CONFIG_SERIAL_MMIO) serial_base[0]=data;
  serial_io_handler(addr-CONFIG_SERIAL_MMIO,len,1);
}

void init_serial() {



}

#endif // CONFIG_DEVICE_SERIAL