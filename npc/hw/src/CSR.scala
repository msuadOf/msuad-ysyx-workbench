import chisel3._
import chisel3.util._

abstract class csr_base(){
    protected val value = RegInit(0.U(32.W))
    def read = value
    def write(value:UInt):Unit = { this.value:=value }
}

class mepc_csr extends csr_base{
    override def write(value:UInt):Unit = {
         this.value:= value & -4.U(32.W)
        }
}
class mcause_csr extends csr_base{

}
class mstatus_csr extends csr_base{

}
class mtvec_csr extends csr_base{

}
class csr {
    val mepc = new mepc_csr
    val mcause = new mcause_csr
    val mstatus = new mstatus_csr
    val mtvec = new mtvec_csr
}
object csr{
    def apply = new csr
}