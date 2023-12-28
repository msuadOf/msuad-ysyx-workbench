NPC
=========
           
## 简介

相信大家都知道一生一芯，那么一生一芯究竟是怎么回事呢？下面就。。。（误）

## Get Start

## 目录结构

1. 本项目为ysyx-workbench子项目
2. Makfile为构建用
3. Chisel_Startup.md为chisel6.0官方仓库档的
4. utils/目录存放了firtool工具，为Xiangshan-playground（Chisel的模版）使用的，本来是放在makefile里面的，不放在编译chisel的时候会报firtool的错，但按照chisel官网的方法，下载firtool包然后加到环境变量一次解决，我觉得更好
5. hw/为项目源码
   1. hw/src/为硬件设计的HDL代码
   2. hw/test/为硬件测试用代码，测试可分为chiseltest和verilator仿真测试
      1. hw/test/src Chisel
      2. hw/test/verilator目录下，csrc放diff-test相关，源自于nemu代码，vsrc会放辅助仿真使用的一些verilog代码