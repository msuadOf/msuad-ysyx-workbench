package core
import chisel3._
import chisel3.util._

class IFUIO extends Bundle{

}
class LSUIO extends Bundle{

}
class CoreIO extends Bundle{
    val LSUIO=new LSUIO
    val IFUIO=new IFUIO
} 

class Core extends Pipline{
    val io=new CoreIO

    val RegFile=new RegFile("RISCV32")
    object RS1 extends Stageable(UInt(32.W))
    object RS2 extends Stageable(UInt(32.W))
    // Frontend
    //TODO: IF stage

    //TODO: ID stage

    // Backend
    //TODO: EX stage = LSU + FU

    //TODO: WB stage
    
}