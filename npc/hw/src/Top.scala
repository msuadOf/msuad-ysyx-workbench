import chisel3._

class RegFile(val width:Int){
  val reg=RegInit(VecInit(Seq.tabulate(32)(i => 0.U(32.W))))
  def apply(idx:Int): UInt ={
    reg(idx)
  }
  import chisel3.experimental.{prefix,SourceInfo}
  final def :=(that: => Data)(implicit sourceInfo: SourceInfo): Unit = {
    
      this.:=(that)(sourceInfo)
    
  }
}

class Top extends Module {
  val io = IO(new Bundle {
    val a        = Input(UInt(32.W))
    val b        = Output(UInt(32.W))
  })


    val reg=new RegFile(32)
  // io.a:=regfile(2)
// val reg=RegInit(VecInit(Seq.tabulate(32)(i => 0.U(32.W))))
for(i <- 0 to 31){
      reg(i):=io.a
}
// reg(0):=io.a
// reg(1):=io.a
// reg(2):=io.a
// reg(31):=io.a
io.b:=reg(0)+reg(1)+reg(2)+reg(31)

}
