import chisel3._
import chisel3.util._

import chisel3.util.experimental._
import chisel3.experimental.prefix

class NPC extends Module {
  val io = IO(new Bundle {
    val AXI_Mem=new AXIIO(32)
    val diff = new diffIO
  })

  val core = Module(new Core)
  core.io.diff <> this.io.diff
  // core.io.IMem <> this.io.IMem
  core.io.DMem.Flipped_IOinit()

  val arbiter = Module(new AXI_Arbiter)  
  val IFU      = Module(new IFU)
val LSU      = Module(new LSU)

//core and LSU IFU
  IFU.io.Inst <> core.io.IMem //overwrite InstIO
    core.io.SUCtrl<>LSU.io.SUCtrl
  core.io.LUCtrl<>LSU.io.LUCtrl

  //给仲裁
  arbiter.io.in(1).IOinit()
  arbiter.io.in(1) --> IFU.io.Mr
  arbiter.io.in(0) --> LSU.io.Mr
  arbiter.io.in(0) --> LSU.io.Mw

  //仲裁后结果出去
  arbiter.io.out<>io.AXI_Mem


}
