FROM ubuntu:22.04

ENV WORKPLACE=/workplace
ENV PATH=$RISCV/bin:$PATH
ENV MAKEFLAGS=-j6

WORKDIR $WORKPLACE

# 基本工具
RUN printf "deb http://mirrors.tuna.tsinghua.edu.cn/ubuntu/ jammy main restricted universe multiverse \ndeb http://mirrors.tuna.tsinghua.edu.cn/ubuntu/ jammy-updates main restricted universe multiverse \ndeb http://mirrors.tuna.tsinghua.edu.cn/ubuntu/ jammy-backports main restricted universe multiverse \ndeb http://security.ubuntu.com/ubuntu/ jammy-security main restricted universe multiverse \n" > /etc/apt/sources.list && \
    apt update && \
	apt install -y autoconf automake autotools-dev \
                curl libmpc-dev libmpfr-dev libgmp-dev \
                gawk build-essential bison flex texinfo \
                gperf libtool patchutils bc zlib1g-dev \
                libexpat-dev pkg-config git \
                libusb-1.0-0-dev device-tree-compiler \
                default-jdk gnupg vim \
                man gcc-doc gdb git \
                libreadline-dev libsdl2-dev llvm llvm-dev \
                vim tmux ssh openssh-server\
                git perl python3 make autoconf g++ flex bison ccache \
                libgoogle-perftools-dev numactl perl-doc help2man \
                libfl2 libfl-dev zlib1g zlib1g-dev \
                g++-riscv64-linux-gnu gcc-riscv64-linux-gnu binutils-riscv64-linux-gnu \
                libstdc++6-riscv64-cross libc6-riscv64-cross gcc-riscv64-unknown-elf binutils-riscv64-unknown-elf && \
                cat /etc/ssh/sshd_config | sed 's/#PermitRootLogin prohibit-password/PermitRootLogin yes/g' | sed 's/#PermitRootLogin without-password/PermitRootLogin yes/g' > /etc/ssh/sshd_config 

# mill and change source
RUN curl -L http://raw.gitmirror.com/lefou/millw/0.4.11/millw | sed 's/github.com/hub.fgit.cf/g' | sed "s/repo1.maven.org\/maven2/maven.aliyun.com\/repository\/central/g" > /bin/mill && \
    chmod +x /bin/mill && \
    mill --help

# verilator v5.008 
RUN git clone https://github.com/verilator/verilator.git verilator && \
    unset VERILATOR_ROOT && \
    cd verilator && git pull && git checkout v5.008 && \
    autoconf && \
    ./configure && \
    make && \
    make install && \
    cd .. && rm -rf verilator




