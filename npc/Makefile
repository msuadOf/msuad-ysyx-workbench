BUILD_DIR ?= ./build

export PATH := $(PATH):$(abspath ./utils)

default: all

all: verilog compile bsp reformat checkformat

include scripts/build.mk

run: verilator-run

sim:
	$(call git_commit, "sim RTL") # DO NOT REMOVE THIS LINE!!!
	@echo "Write this Makefile by yourself."

-include ../Makefile
clean:
	-rm -rf obj_dir logs *.log *.dmp *.vpd coverage.dat build
clean-verilator clean-v vclean:
	cp build/top.v top.v.bak._tmp && rm -rf build/* && mv top.v.bak._tmp build/top.v
clean-chisel clean-c:
	-rm -rf out
distclean:clean clean-chisel