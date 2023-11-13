import chisel3._

object RegFile{
    val regs=Vec(32,RegInit(0.U(32.W)))
    def apply(idx:Int):UInt={
        regs(idx)
    }
}

class Core extends Module {
  val io = IO(new Bundle {
    // val instData        = Input(UInt(32.W))
    // val instAddress        = Output(UInt(32.W)) //pc

    // val memWriteAddress        = Output(UInt(32.W))
    // val memWriteData = Output(UInt(32.W))
    
    val memReadAddress        = Output(UInt(32.W))
    //val memReadData     = Input(UInt(32.W))
  })

  io.memReadAddress := RegFile(1)

}