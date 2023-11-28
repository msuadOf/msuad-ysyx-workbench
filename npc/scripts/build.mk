WORK_DIR  = $(shell pwd)
BUILD_DIR = $(WORK_DIR)/build

include scripts/verilator.mk
include scripts/chisel.mk

# Vtop call args
VERI_RUNNING_ARGS+=--image $(IMAGE)
CHISEL_SRC_FILE+=$(shell find $(WORK_DIR)/hw -name *.scala) #search all hw/
CHISEL_SRC_FILE+=$(shell find $(WORK_DIR)/hw -name *.v) #search all hw/

CHISEL_GEN_VERILOG_FILE=$(BUILD_DIR)/top.v #build/top.v

verilog:$(CHISEL_GEN_VERILOG_FILE)
$(CHISEL_GEN_VERILOG_FILE):$(CHISEL_SRC_FILE)
	$(call git_commit, "generate verilog")
	mkdir -p $(BUILD_DIR)
	mill -i __.runMain Elaborate -td $(BUILD_DIR)

#===================================
#             verilator            =
#===================================

#input file


VERILATOR_INPUT_FILE += $(WORK_DIR)/hw/test/verilator/input.vc $(WORK_DIR)/hw/test/verilator/csrc/sim_main.cpp
#VERILATOR_INPUT_FILE += $(WORK_DIR)/hw/test/verilator/vsrc/top.v
VERILATOR_INPUT_FILE += $(shell find $(WORK_DIR)/hw/test/verilator/vsrc/ -name *.v)

# verilator build dictionaty
VERI_BUILD_DIR = $(BUILD_DIR)

# Check for sanity to avoid later confusion

ifneq ($(words $(CURDIR)),1)
 $(error Unsupported: GNU Make cannot build in directories containing spaces, build elsewhere: '$(CURDIR)')
endif

######################################################################
# Set up variables

# If $VERILATOR_ROOT isn't in the environment, we assume it is part of a
# package install, and verilator is in your path. Otherwise find the
# binary relative to $VERILATOR_ROOT (such as when inside the git sources).
ifeq ($(VERILATOR_ROOT),)
VERILATOR = verilator
VERILATOR_COVERAGE = verilator_coverage
else
export VERILATOR_ROOT
VERILATOR = $(VERILATOR_ROOT)/bin/verilator
VERILATOR_COVERAGE = $(VERILATOR_ROOT)/bin/verilator_coverage
endif

# Generate C++ in executable form
VERILATOR_FLAGS += -cc --exe
# Generate makefile dependencies (not shown as complicates the Makefile)
#VERILATOR_FLAGS += -MMD
# Optimize
VERILATOR_FLAGS += -x-assign fast
# Warn abount lint issues; may not want this on less solid designs
VERILATOR_FLAGS += -Wall
# Make waveforms
VERILATOR_FLAGS += --trace
# Check SystemVerilog assertions
VERILATOR_FLAGS += --assert
# Generate coverage analysis
VERILATOR_FLAGS += --coverage
# Run Verilator in debug mode
#VERILATOR_FLAGS += --debug
# Add this trace to get a backtrace in gdb
#VERILATOR_FLAGS += --gdbbt

# Input files for Verilator
VERILATOR_INPUT_FILE += 
VERILATOR_INPUT = -f $(VERILATOR_INPUT_FILE)

VERILATOR_INPUT_FILE += $(CHISEL_GEN_VERILOG_FILE)

verilator-run: verilog
	@echo
	@echo "-- Verilator tracing example"

	@echo
	@echo "-- VERILATE ----------------"
	@mkdir -p $(VERI_BUILD_DIR)
	$(VERILATOR) $(VERILATOR_FLAGS) $(VERILATOR_INPUT) --Mdir $(VERI_BUILD_DIR)

	@echo
	@echo "-- BUILD -------------------"
	$(MAKE) -j -C $(VERI_BUILD_DIR) -f ../Makefile_obj

	@echo
	@echo "-- RUN ---------------------"
	@rm -rf logs
	#@mkdir -p logs
	$(VERI_BUILD_DIR)/Vtop +trace $(VERI_RUNNING_ARGS)

	@echo
	@echo "-- COVERAGE ----------------"
	@rm -rf logs/annotated
	$(VERILATOR_COVERAGE) --annotate logs/annotated logs/coverage.dat


exe-gen: verilog