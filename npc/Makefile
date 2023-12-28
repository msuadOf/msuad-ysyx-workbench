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
clean-verilator:
	cd build && rm -rf !top.v