Chisel Project Template
=======================

## Setup (Ubuntu Linux)

1.  Install the JVM
    ```bash
    sudo apt-get install default-jdk
    ```

1.  Install sbt according to the instructions from [sbt download](https://www.scala-sbt.org/download.html).
   or mill(recommand in `/usr/local/bin/`):
    ```shell
    curl -L https://raw.githubusercontent.com/lefou/millw/0.4.10/millw > mill && chmod +x mill
    ```

2.  Install Firtool

    Choose whatever version is being [used in continuous integration](.github/workflows/install-circt/action.yml)
    ```bash
    wget -q -O - https://github.com/llvm/circt/releases/download/firtool-1.38.0/firrtl-bin-ubuntu-20.04.tar.gz | tar -zx
    ```
    This will give you a directory called `firtool-1.38.0` containing the firtool binary, add this to your PATH as appropriate.
    ```bash
    export PATH=$PATH:$PWD/firtool-1.38.0/bin
    ```
    Alternatively, you can install the binary to a standard location by simply moving the binary (if you have root access).
    ```bash
    mv firtool-1.38.0/bin/firtool /usr/local/bin/
    ```


2.  Install Verilator.
    We currently recommend Verilator version v4.226.
    Follow these instructions to compile it from source.

    1.  Install prerequisites (if not installed already):
        ```bash
        sudo apt-get install git make autoconf g++ flex bison
        ```

    2.  Clone the Verilator repository:
        ```bash
        git clone https://github.com/verilator/verilator
        ```

    3.  In the Verilator repository directory, check out a known good version:
        ```bash
        git pull
        git checkout v4.226
        ```

    4.  In the Verilator repository directory, build and install:
        ```bash
        unset VERILATOR_ROOT # For bash, unsetenv for csh
        autoconf # Create ./configure script
        ./configure
        make
        sudo make install
        ```

## Getting Started

First, install mill by referring to the documentation [here](https://com-lihaoyi.github.io/mill).

To run all tests in this design (recommended for test-driven development):
```bash
make test
```

To generate Verilog:
```bash
make verilog
```

## Change FIRRTL Compiler

You can change the FIRRTL compiler between SFC (Scala-based FIRRTL compiler) and
MFC (MLIR-based FIRRTL compiler) by modifying the `useMFC` variable in `playground/src/Elaborate.scala`.
The latter one requires `firtool`, which is included under `utils/`.
