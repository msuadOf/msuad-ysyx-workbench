#include <am.h>
#include <klib.h>
#include <klib-macros.h>
#include <stdarg.h>

#if !defined(__ISA_NATIVE__) || defined(__NATIVE_USE_KLIB__)

// static int atoi(const char* nptr) {
//   int x = 0;
//   while (*nptr == ' ') { nptr ++; }
//   while (*nptr >= '0' && *nptr <= '9') {
//     x = x * 10 + *nptr - '0';
//     nptr ++;
//   }
//   return x;
// }

static void reverse(char *s,int len)
{
  char *end = s + len - 1;
  char tmp;
  while(s < end)
  {
    tmp = *s;
    *s = *end;
    *end = tmp;
    s++;end--;
  }  
}

static int itoa(int n,char *s, int base)
{
    assert(base<=16);
    int i = 0;
    int sign = n<0 ? -1 : 1;
    int bit;
    n = n * sign;
    while(n!=0)
    {
        bit = n % base;
        n /= base;
        if(bit > 9) *s = bit - 10 + 'A';
        else *s = bit + '0';
        s++;
        i++;
    }
    if(sign == -1)
    {
        *s++ = '-';
        i++;
    }
    reverse(s-i,i);
    *s = '\0';
    return i;
}


static char sprintf_buf[1024];
// #include <stdio.h>
int printf(const char *fmt, ...) {
  va_list args;
  int n;

  va_start(args,fmt);
  n = vsprintf(sprintf_buf, fmt, args);
  va_end(args);
  putstr(sprintf_buf);
  return n;
}


int vsprintf(char *out, const char *fmt, va_list ap) {
  char *start = out;

  for(; *fmt != '\0'; ++fmt)
  {
    if(*fmt != '%')
    {
      *out = *fmt;
      ++out;
    }else 
    {
      switch (*(++fmt))
      {
      case '%': *out = *fmt;
                ++out; 
                break;
      case 'd': out += itoa(va_arg(ap,int),out,10);
                break;
      case 's': char *s = va_arg(ap,char *);



                strcpy(out,s);
                out += strlen(out);
                break;
      case 'c': char c = va_arg(ap,int);
                *out++ = c;
                // out += strlen(out);
                break;
      default:
        break;
      }
    }
  }  
  *out = '\0';
  return out - start;
}



int sprintf(char *out, const char *fmt, ...) {
  va_list pargs;
  char *start = out;
  va_start(pargs,fmt);
  while(*fmt != '\0')
  {
    if(*fmt != '%')//空格，固定字符串
    {
      *out = *fmt;
      ++out;
      fmt++;
    }
    else
    {
      switch(*(++fmt))
      {
        case '%': *out = *fmt; 
                  out++;
                  fmt++;
                  break;
        case 'd': out+=itoa(va_arg(pargs,int),out,10);
                  fmt++;
                  break;
        case 's': char *s = va_arg(pargs,char*);
                  strcpy(out,s);
                  out+= strlen(s);
                  fmt++;
                  break;
      }
      
    }
  }
  *out = '\0';
  va_end(pargs);

  return out - start;
}

int snprintf(char *out, size_t n, const char *fmt, ...) {
  panic("Not implemented");
}

int vsnprintf(char *out, size_t n, const char *fmt, va_list ap) {
  panic("Not implemented");
}

#endif
