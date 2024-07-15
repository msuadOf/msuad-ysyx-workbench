sim_verilog_files = \
	$(BUILD_DIR)/Core.v 

sim_verilog:
	verilator -cc -trace --timing ${sim_verilog_files} -exe ${WORK_DIR}/utils/testbench/sim_main.cpp

sim_verilog_clean:
	-rm -rf obj_dir