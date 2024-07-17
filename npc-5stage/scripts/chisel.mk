
export PATH := $(PATH):$(abspath ./utils)

test:
	mill -i __.test

help:
	mill -i __.test.runMain Elaborate --help

compile:
	mill -i __.compile

bsp:
	mill -i mill.bsp.BSP/install

reformat lint:
	mill -i __.reformat

checkformat:
	mill -i __.checkFormat
