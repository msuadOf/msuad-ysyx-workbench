import chisel3._

// class RegFile(val width:Int){
//   val reg=RegInit(V)
// }

class Top extends Module {
  val io = IO(new Bundle {
    val a        = Input(UInt(32.W))
    val b        = Output(UInt(32.W))
  })


val reg=RegInit(VecInit(Seq.tabulate(32)(i => 0.U(32.W))))
reg(0):=io.a
reg(1):=io.a
reg(2):=io.a
io.b:=reg(0)+reg(1)+reg(2)
  // val regfile=new RegFile(32)
  // io.a:=regfile(2)
}
