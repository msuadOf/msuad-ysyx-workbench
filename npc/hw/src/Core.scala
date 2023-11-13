import chisel3._



class Core extends Module {
  val io = IO(new Bundle {
    // val instData        = Input(UInt(32.W))
    // val instAddress        = Output(UInt(32.W)) //pc

    // val memWriteAddress        = Output(UInt(32.W))
    // val memWriteData = Output(UInt(32.W))
    
    val memReadAddress        = Output(UInt(32.W))
    //val memReadData     = Input(UInt(32.W))
  })
    class RegFile(){
        val regs=Reg(Vec(32,UInt(32.W)))
        def apply(idx:Int):UInt={
            regs(idx)
        }
        val name2idx=Map[String,Int](
            
            "$0"->0, "ra"->1, "sp"->2, "gp"->3, "tp"->4, "t0"->5, "t1"->6, "t2"->7,
            "s0"->8, "s1"->9, "a0"->10, "a1"->11, "a2"->12, "a3"->13, "a4"->14, "a5"->15,
            "a6"->16, "a7"->17, "s2"->18, "s3"->19, "s4"->20, "s5"->21, "s6"->22, "s7"->23,
            "s8"->24, "s9"->25, "s10"->26, "s11"->27, "t3"->28, "t4"->29, "t5"->30, "t6"->31
        )
        def get_name_by_idx(idx:Int):String={
            ""
        }
        def get_idx_by_name(name:String):Int={
            name2idx.get(name).get
        }
        def apply(name:String):UInt={
            apply(get_idx_by_name(name))
        }
    }
    val regFile=new RegFile()
  io.memReadAddress := regFile(1)

}