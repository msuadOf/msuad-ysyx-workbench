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

