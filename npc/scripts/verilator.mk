#===================================
#             verilator            =
#===================================

#input file

VERILATOR_INPUT_FILE += $(WORK_DIR)/hw/test/verilator/input.vc $(WORK_DIR)/hw/test/verilator/csrc/sim_main.cpp
#VERILATOR_INPUT_FILE += $(WORK_DIR)/hw/test/verilator/vsrc/top.v
#VERILATOR_INPUT_FILE += $(shell find $(WORK_DIR)/hw/test/verilator/vsrc/ -name *.v)

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
# VERILATOR_FLAGS += -Wall # change warning to error
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


####### file #######
VERILATOR_INCS+=$(shell find $(WORK_DIR)/hw/test/verilator/csrc/ -name *.h)
VERILATOR_HPPINCS+=$(shell find $(WORK_DIR)/hw/test/verilator/csrc/ -name *.hpp)
#VERILATOR_SRCS+=$(shell find $(WORK_DIR)/hw/test/verilator/csrc/ -name *.c)
VERILATOR_CPPSRC+=$(shell find $(WORK_DIR)/hw/test/verilator/csrc/ -name *.cpp)



