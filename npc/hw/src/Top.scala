import chisel3._

// class RegFile(val width:Int){
//   val reg=RegInit(V)
// }

class Top extends Module {
  val io = IO(new Bundle {
    val a        = Input(UInt(16.W))
    val b        = Output(UInt(16.W))
  })


val reg=RegInit(VecInit(0.U(8.W),1.U(8.W)))
reg(0):=io.a
reg(1):=io.a
io.b:=reg(0)+reg(1)+reg(2)
  // val regfile=new RegFile(32)
  // io.a:=regfile(2)
}
