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
    }
    val regFile=new RegFile()
  io.memReadAddress := regFile(1)

}