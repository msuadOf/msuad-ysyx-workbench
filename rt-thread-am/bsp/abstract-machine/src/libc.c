#include <klib.h>
#include <rtthread.h>

int isdigit(int c)
{
  return (c >= '0' && c <= '9') ? 1 : 0;
}

// 检查字符是否为大写字母
int isupper(int c)
{
  return ((c >= 'A' && c <= 'Z')) ? 1 : 0;
}

// 检查字符是否为小写字母
int islower(int c)
{
  return ((c >= 'a' && c <= 'z')) ? 1 : 0;
}

// 检查字符是否为空白字符（通常是空格、制表符、换行符等）
int isspace(int c)
{
  switch (c)
  {
  case ' ':
  case '\t':
  case '\n':
  case '\v':
  case '\f':
  case '\r':
    return 1;
  default:
    return 0;
  }
}
int digit_to_value(char c, int base) {
    if (isdigit(c)) {
        return c - '0';
    } else if (isupper(c)) {
        return c - 'A' + 10;
    } else if (islower(c)) {
        return c - 'a' + 10;
    }
    return -1; // 非有效字符，返回错误标记
}
static long my_atol(const char *nptr, int base, char **endptr) {
    long result = 0;
    const char *p = nptr;

    // 跳过前导空白符
    while (isspace(*p)) {
        p++;
    }

    // 检查符号
    int negative = 0;
    if (*p == '-') {
        negative = 1;
        p++;
    } else if (*p == '+') {
        p++;
    }

    // 检查并确定基数
    if (base == 0) {
        if (*p == '0') {
            base = 8;
            if ((*(p + 1) == 'x' || *(p + 1) == 'X')) {
                base = 16;
                p += 2;
            } else {
                p++;
            }
        } else {
            base = 10;
        }
    }

    // 解析数字
    while (*p && digit_to_value(*p, base) != -1) {
        int value = digit_to_value(*p++, base);
        if (value == -1) break;
        result *= base;
        result += value;
    }

    // 设置endptr指向最后一个有效字符之后的位置
    if (endptr != NULL) {
        *endptr = (char *)p;
    }

    // 应用符号
    return negative ? -result : result;
}
// char *strchr(const char *s, int c) {
//   assert(0);
// }

// char *strrchr(const char *s, int c) {
//   assert(0);
// }

char *strstr(const char *haystack, const char *needle)
{
  return rt_strstr(haystack, needle);
}

long strtol(const char *restrict nptr, char **restrict endptr, int base)
{
  return my_atol(nptr, base, endptr);
  // assert(0);
}

// char *strncat(char *restrict dst, const char *restrict src, size_t sz) {
//   assert(0);
// }
