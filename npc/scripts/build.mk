include verilator.mk
include chisel.mk

WORK_DIR  = $(shell pwd)
BUILD_DIR = $(WORK_DIR)/build
VERILATOR_INPUT_FILE += $(WORK_DIR)/test/verilator/input.vc $(WORK_DIR)/test/verilator/csrc/sim_main.cpp
VERILATOR_INPUT_FILE += $(WORK_DIR)/test/verilator/csrc/sim_main.cpp
# Vtop call args
VERI_RUNNING_ARGS+=--image $(IMAGE)

verilog:
	$(call git_commit, "generate verilog")
	mkdir -p $(BUILD_DIR)
	mill -i __.runMain Elaborate -td $(BUILD_DIR)


#===================================
#             verilator            =
#===================================

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

verilator-run:
	@echo
	@echo "-- Verilator tracing example"

	@echo
	@echo "-- VERILATE ----------------"
	@mkdir -p $(VERI_BUILD_DIR)
	$(VERILATOR) $(VERILATOR_FLAGS) $(VERILATOR_INPUT) --Mdir $(VERI_BUILD_DIR)

	@echo
	@echo "-- BUILD -------------------"
	$(MAKE) -j -C $(VERI_BUILD_DIR) -f ../Makefile_obj


exe-gen: verilog