#include <utils.h>
#include <common.h>

#ifdef CONFIG_DEVICE_RTC



static uint32_t rtc_port_base[2]={0};

//--------get_time--------
#include <time.h>
static uint64_t boot_time = 0;

static uint64_t get_time_internal() {
  struct timespec now;
  clock_gettime(CLOCK_MONOTONIC_COARSE, &now);
  uint64_t us = now.tv_sec * 1000000 + now.tv_nsec / 1000;
  return us;
}

uint64_t get_time() {
  if (boot_time == 0) boot_time = get_time_internal();
  uint64_t now = get_time_internal();
  return now - boot_time;
}

static void rtc_io_handler(uint32_t offset, int len, bool is_write) {
  assert(offset == 0 || offset == 4);
  if (!is_write && offset == 4) {
    uint64_t us = get_time();
    rtc_port_base[0] = (uint32_t)us;
    rtc_port_base[1] = us >> 32;
  }
}


// static void timer_intr() {
//   if (nemu_state.state == NEMU_RUNNING) {
//     extern void dev_raise_intr();
//     dev_raise_intr();
//   }
// }

word_t mmio_read_RTC(paddr_t addr, int len) {

  rtc_io_handler(addr-CONFIG_RTC_MMIO,len,0);
  return rtc_port_base[addr-CONFIG_RTC_MMIO];

}

void mmio_write_RTC(paddr_t addr, int len, word_t data) {
  assert(0);
}

void init_timer() {

}

#endif // CONFIG_DEVICE_RTC