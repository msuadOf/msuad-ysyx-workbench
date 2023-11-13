#include <am.h>
#include <klib.h>
#include <klib-macros.h>
#include <stdarg.h>

#if !defined(__ISA_NATIVE__) || defined(__NATIVE_USE_KLIB__)

int printf(const char *fmt, ...) {
  panic("Not implemented");
}

int vsprintf(char *out, const char *fmt, va_list ap) {
  panic("Not implemented");
}

int intToString(char *out, int num) {
  int idx = 0;

  if (num < 0) {
    out[idx++] = '-';
    num = -num;
  }

  int divisor = 1;
  while (num / divisor >= 10) {
    divisor *= 10;
  }

  while (divisor > 0) {
    int digit = num / divisor;
    out[idx++] = '0' + digit;
    num %= divisor;
    divisor /= 10;
  }
  
  out[idx] = '\0';
  return idx;
}

int stringCopy(char *out, const char *str) {
  int idx = 0;
  while (str[idx] != '\0') {
    out[idx] = str[idx];
    idx++;
  }
  out[idx] = '\0';
  return idx;
}
int sprintf(char *out, const char *fmt, ...) {
    va_list args;
  va_start(args, fmt);

  int written = 0;
  int idx = 0;

  while (fmt[idx] != '\0') {
    if (fmt[idx] == '%') {
      idx++;
      if (fmt[idx] == '\0') {
        break;
      } else if (fmt[idx] == '%') {
        out[written++] = '%';
      } else if (fmt[idx] == 'd') {
        int num = va_arg(args, int);
        written += intToString(out + written, num);
      } else if (fmt[idx] == 's') {
        const char *str = va_arg(args, const char *);
        written += stringCopy(out + written, str);
      } else {
        out[written++] = '%';
        out[written++] = fmt[idx];
      }
    } else {
      out[written++] = fmt[idx];
    }
    idx++;
  }

  out[written] = '\0';
  va_end(args);
  return written;
}

int snprintf(char *out, size_t n, const char *fmt, ...) {
  panic("Not implemented");
}

int vsnprintf(char *out, size_t n, const char *fmt, va_list ap) {
  panic("Not implemented");
}

#endif
