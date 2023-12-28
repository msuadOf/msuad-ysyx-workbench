BUILD_DIR ?= ./build

export PATH := $(PATH):$(abspath ./utils)

default: all

all: verilog compile bsp reformat checkformat

include scripts/build.mk

sim run:verilator-run
	$(call git_commit, "sim RTL") # DO NOT REMOVE THIS LINE!!!
	@echo "-- RUN ---------------------"
	@rm -rf logs
	$(VERI_BUILD_DIR)/Vtop +trace $(VERI_RUNNING_ARGS)

-include ../Makefile
clean:
<<<<<<< Updated upstream
	-rm -rf obj_dir logs *.log *.dmp *.vpd coverage.dat build
=======
	-rm -rf obj_dir logs *.log *.dmp *.vpd coverage.dat build
clean-verilator clean-v vclean:
	cp build/top.v top.v.bak._tmp && rm -rf build/* && mv top.v.bak._tmp build/top.v
clean-chisel clean-c:
	-rm -rf out
distclean:clean clean-chisel


.DEFAULT_GOAL = verilator-binary
>>>>>>> Stashed changes
