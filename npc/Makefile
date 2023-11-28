BUILD_DIR ?= ./build

export PATH := $(PATH):$(abspath ./utils)

all: test verilog compile bsp reformat checkformat

include scripts/build.mk

sim:
	$(call git_commit, "sim RTL") # DO NOT REMOVE THIS LINE!!!
	@echo "Write this Makefile by yourself."

-include ../Makefile
clean:
	-rm -rf obj_dir logs *.log *.dmp *.vpd coverage.dat build