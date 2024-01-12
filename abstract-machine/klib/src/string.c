#include <klib.h>
#include <klib-macros.h>
#include <stdint.h>

#if !defined(__ISA_NATIVE__) || defined(__NATIVE_USE_KLIB__)

size_t strlen(const char *s) {
  const char *p = s;  // 使用一个指针p来迭代字符串s
  while (*p) {        // 当p指向的字符不为NULL时
    p++;              // 指针p向后移动一位
  }
  return p - s;  
}

char *strcpy(char *dst, const char *src) {
  char *p = dst;  // 使用一个指针p来迭代目标字符串dst

  while ((*p++ = *src++)) {}  // 逐个复制源字符串src中的字符到目标字符串dst，并在遇到NULL字符时停止复制

  return dst;  // 返回目标字符串dst的起始地址
}

char *strncpy(char *dst, const char *src, size_t n) {
  char *p = dst;
  
  while (n > 0 && (*p++ = *src++)) {
    n--;
  }
  
  while (n > 0) {
    *p++ = '\0';
    n--;
  }
  
  return dst;
}

char *strcat(char *dst, const char *src) {
  char *p = dst;
  
  while (*p) {
    p++;
  }
  
  while ((*p++ = *src++)) {}
  
  return dst;
}

int strcmp(const char *s1, const char *s2) {
  while (*s1 && (*s1 == *s2)) {
    s1++;
    s2++;
  }
  
  return *(unsigned char *)s1 - *(unsigned char *)s2;
}

int strncmp(const char *s1, const char *s2, size_t n) {
  if (n == 0) {
    return 0;
  }
  
  while (--n && *s1 && (*s1 == *s2)) {
    s1++;
    s2++;
  }
  
  return *(unsigned char *)s1 - *(unsigned char *)s2;
}

void *memset(void *s, int c, size_t n) {
  unsigned char *p = s;
  
  while (n > 0) {
    *p++ = (unsigned char)c;
    n--;
  }
  
  return s;
}

void *memmove(void *dst, const void *src, size_t n) {
  unsigned char *d = dst;
  const unsigned char *s = src;

  if (d < s) {
    while (n > 0) {
      *d++ = *s++;
      n--;
    }
  } else if (d > s) {
    d += n;
    s += n;
    
    while (n > 0) {
      *--d = *--s;
      n--;
    }
  }
  
  return dst;
}

void *memcpy(void *out, const void *in, size_t n) {
  unsigned char *o = out;
  const unsigned char *i = in;
  
  while (n > 0) {
    *o++ = *i++;
    n--;
  }
  
  return out;
}

int memcmp(const void *s1, const void *s2, size_t n) {
  const unsigned char *p1 = s1, *p2 = s2;
  
  while (n > 0) {
    if (*p1 != *p2) {
      return *p1 - *p2;
    }
    p1++;
    p2++;
    n--;
  }
  
  return 0;
}

#endif
