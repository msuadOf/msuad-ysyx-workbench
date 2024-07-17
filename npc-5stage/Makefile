BUILD_DIR ?= ./build

export PATH := $(PATH):$(abspath ./utils)

default: all

all: verilog compile bsp reformat checkformat

include scripts/build.mk

sim run:verilator-binary $(DIFF_REF_SO)
	$(call git_commit, "sim npc") # DO NOT REMOVE THIS LINE!!!
	@echo "-- RUN ---------------------"
	@rm -rf logs
	$(VERI_BUILD_DIR)/Vtop $(VERI_RUNNING_ARGS) +trace

-include ../Makefile
clean:
	-rm -rf obj_dir logs *.log *.dmp *.vpd coverage.dat build
clean-verilator clean-v vclean clean-c:
	cp build/top.v top.v.bak._tmp && rm -rf build/* && mv top.v.bak._tmp build/top.v
clean-chisel clean-verilog:
	-rm -rf out
clean-vscode:
	-rm -rf .metals/ .bloop/ .vscode/
distclean:clean clean-chisel clean-vscode

.DEFAULT_GOAL = verilator-binary
.PHONY: verilator-binary clean

include utils/testbench/build.mk