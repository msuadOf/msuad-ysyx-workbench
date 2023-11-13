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

int sprintf(char *out, const char *fmt, ...) {
  va_list pArgs;
  va_start(pArgs, fmt);
  char *start = out;
  
  for (; *fmt != '\0'; ++fmt) {
    if (*fmt != '%') {
      *out = *fmt;
      ++out;
    } else {
      switch (*(++fmt)) {
      case '%': *out = *fmt; ++out; break;
      case 'd': {
        int n = va_arg(pArgs, int);
        char buffer[32];
        int len = sprintf(buffer, "%d", n);
        memcpy(out, buffer, len);
        out += len;
        break;
      }
      case 's': {
        char *s = va_arg(pArgs, char*);
        strcpy(out, s);
        out += strlen(s);
        break;
      }
      }
    }
  }
  *out = '\0';
  va_end(pArgs);

  return out - start;
}

int snprintf(char *out, size_t n, const char *fmt, ...) {
  panic("Not implemented");
}

int vsnprintf(char *out, size_t n, const char *fmt, va_list ap) {
  panic("Not implemented");
}

#endif
