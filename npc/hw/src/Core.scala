import chisel3._

object RegFile{
    val regs=Vec(32,RegInit(0.U(32.W)))
    def apply(idx:Int):UInt={
        regs(idx)
    }
}

class Core extends Module {
  val io = IO(new Bundle {
    val instData        = Input(UInt(16.W))
    val instAddress        = Output(UInt(16.W)) //pc

    val memWriteAddress        = Output(UInt(16.W))
    val memWriteData = Output(Bool())
    
    val memReadAddress        = Output(UInt(16.W))
    val memReadData     = Input(UInt(16.W))
  })

  io.memReadAddress:=RegFile(1)

}