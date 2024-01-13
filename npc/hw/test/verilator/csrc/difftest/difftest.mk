CONFIG_DIFFTEST=1

ifdef CONFIG_DIFFTEST
DIFF_REF_SO = $(NEMU_HOME)/build/riscv32-nemu-interpreter-so
MKFLAGS ?= #GUEST_ISA=$(GUEST_ISA) SHARE=1 ENGINE=interpreter
ARGS_DIFF = --diff=$(DIFF_REF_SO)


$(DIFF_REF_SO):
	@echo + BUILD difftest-reference
	
	#$(MAKE) -s -C $(NEMU_HOME) $(MKFLAGS)

.PHONY: $(DIFF_REF_SO)
endif