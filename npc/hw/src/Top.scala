import chisel3._

// class RegFile(val width:Int){
//   val reg=RegInit(V)
// }

class Top extends Module {
  val io = IO(new Bundle {
    val a        = Input(UInt(16.W))
    val b        = Output(UInt(16.W))
  })

  val x = Reg(UInt())
  val y = Reg(UInt())

val reg=RegInit(VecInit(0.U(8.W),0.U(8.W)))

  // val regfile=new RegFile(32)
  // io.a:=regfile(2)
}
