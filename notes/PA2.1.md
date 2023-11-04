1. exec_once(&s, cpu.pc)其中Decode s是局部变量
```c
typedef struct Decode {
  vaddr_t pc;
  vaddr_t snpc;
  vaddr_t dnpc;
  ISADecodeInfo isa;
  char logbuf[128];
} Decode;
```
其中，ISADecodeInfo指向riscv32_ISADecodeInfo
```c
typedef struct {
  union {
    uint32_t val;
  } inst;
} riscv32_ISADecodeInfo;
```
主要代码：初始化pc和static next pc为当前pc值
```c
  s->pc = pc;
  s->snpc = pc;
  isa_exec_once(s);
```
2. `isa_exec_once(Decode *s)`在`src/isa/riscv32/inst.c`
（1）IF：`s->isa.inst.val = inst_fetch(&s->snpc, 4);`
调`paddr_read`取4字节的物理字节（后面应该要改？），然后pc+4
现在取指完成，假设pc应该的值是PC，则
s.pc=PC
snpc=PC+4
dnpc=??
isa.inst.val=PC
（2）ID&EX：`decode_exec(s)`
解码：`decode_operand(s, &rd, &src1, &src2, &imm, concat(TYPE_, type));`
EX：`INSTPAT`
3. 在`exec_once(Decode *s, vaddr_t pc)` 从`isa_exec_once(s);`出来后，
   将dnpc更新给pc

