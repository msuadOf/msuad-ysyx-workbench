NAME=npc
WORK_DIR  = $(shell pwd)
BUILD_DIR = $(WORK_DIR)/build

include scripts/verilator.mk
include scripts/chisel.mk

# Include all filelist.mk to merge file lists
FILELIST_MK = $(shell find -L ./hw -name "filelist.mk")
include $(FILELIST_MK)


VERILATOR_INCLUDES += -I $(WORK_DIR)/hw/test/verilator/csrc/monitor
VERILATOR_INCLUDES += -I $(WORK_DIR)/hw/test/verilator/csrc/include
VERILATOR_CFLAGS := $(VERILATOR_INCLUDES)

##########################33
#    verilator build(copied)
#############################
#INC_PATH += $(WORK_DIR)/hw/test/verilator/csrc/
INC_PATH := $(dirname $(VERILATOR_INCS)) $(INC_PATH)
INC_PATH := $(WORK_DIR)/include $(INC_PATH)
OBJ_DIR  = $(BUILD_DIR)/obj-$(NAME)
BINARY   = $(BUILD_DIR)/$(NAME)

CXX := g++
LD := $(CXX)
INCLUDES = $(addprefix -I, $(INC_PATH))
CFLAGS  := -O2 -MMD -Wall -Werror $(INCLUDES) $(CFLAGS)
LDFLAGS := -O2 $(LDFLAGS)

OBJS = $(VERILATOR_SRCS:%.c=$(OBJ_DIR)/%.o) $(VERILATOR_CPPSRC:%.cpp=$(OBJ_DIR)/%.o)

# Compilation patterns
$(OBJ_DIR)/%.o: %.c
	@echo + CC $<
	@mkdir -p $(dir $@)
	@$(CC) -fPIE $(CFLAGS) -c -o $@ $<
	$(call call_fixdep, $(@:.o=.d), $@)

$(OBJ_DIR)/%.o: %.cpp
	@echo + CXX $<
	@mkdir -p $(dir $@)
	@$(CXX) $(CFLAGS) $(CXXFLAGS) -c -o $@ $<
	$(call call_fixdep, $(@:.o=.d), $@)

# Depencies
-include $(OBJS:.o=.d)

$(BINARY): $(OBJS) $(ARCHIVES)
	@echo + LD $@
	@$(LD) -o $@ $(OBJS) $(LDFLAGS) $(ARCHIVES) $(LIBS)

verilator-app: $(BINARY)
######## end ################

# Vtop call args
ifneq ($(IMAGE), )
VERI_RUNNING_ARGS+=--image $(IMAGE)
endif

CHISEL_SRC_FILE+=$(shell find $(WORK_DIR)/hw -name *.scala) #search all hw/
CHISEL_SRC_FILE+=$(shell find $(WORK_DIR)/hw -name *.v) #search all hw/

CHISEL_GEN_VERILOG_FILE=$(BUILD_DIR)/top.v #build/top.v

C_SRC_FILE+=$(shell find $(WORK_DIR)/hw -name *.c) #search all hw/
CPP_SRC_FILE+=$(shell find $(WORK_DIR)/hw -name *.cpp) #search all hw/
C_HEAD_SRC_FILE+=$(shell find $(WORK_DIR)/hw -name *.h) #search all hw/

verilog:$(CHISEL_GEN_VERILOG_FILE)
$(CHISEL_GEN_VERILOG_FILE):$(CHISEL_SRC_FILE)
	$(call git_commit, "generate verilog")
	mkdir -p $(BUILD_DIR)
	mill -i __.runMain Elaborate -td $(BUILD_DIR)

# Input files for Verilator
VERILATOR_INPUT_FILE += $(C_SRC_FILE) $(CPP_SRC_FILE) 


VERILATOR_INPUT_FILE += $(CHISEL_GEN_VERILOG_FILE)
VERILATOR_INPUT = -f $(VERILATOR_INPUT_FILE)
verilator-binary: verilog
	@echo
	@echo "-- Verilator tracing example"

	@echo
	@echo "-- VERILATE ----------------"
	@mkdir -p $(VERI_BUILD_DIR)
	$(VERILATOR) $(VERILATOR_FLAGS) $(VERILATOR_INPUT) --Mdir $(VERI_BUILD_DIR)

	@echo
	@echo "-- BUILD -------------------"
	CPPFLAGS="$(VERILATOR_CFLAGS)" $(MAKE) -j -C $(VERI_BUILD_DIR) -f ../Makefile_obj

verilator-run: verilator-binary
	@echo
	@echo "-- RUN ---------------------"
	@rm -rf logs
	#@mkdir -p logs
	$(VERI_BUILD_DIR)/Vtop +trace $(VERI_RUNNING_ARGS)

	@echo
	@echo "-- COVERAGE ----------------"
	@rm -rf logs/annotated
	$(VERILATOR_COVERAGE) --annotate logs/annotated logs/coverage.dat

