import chisel3._

// class RegFile(val width:Int){
//   val reg=RegInit(V)
// }

class Top extends Module {
  val io = IO(new Bundle {
    val a        = Input(UInt(16.W))
    val b        = Output(UInt(16.W))
  })


val reg=RegInit(Vec(3,0.U(8.W)))
reg(0):=io.a
io.b:=reg(0)
  // val regfile=new RegFile(32)
  // io.a:=regfile(2)
}
