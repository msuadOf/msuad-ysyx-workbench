import chisel3._
import chisel3.util._

class diffIO extends Bundle {
  val diff_en = Output(UInt(1.W))
  val pc      = Output(UInt(32.W))
  val dnpc    = Output(UInt(32.W))
  val snpc    = Output(UInt(32.W))
  val regs    = Output(Vec(32, UInt(32.W)))

  val mepc    = Output(UInt(32.W))
  val mcause  = Output(UInt(32.W))
  val mstatus = Output(UInt(32.W))
  val mtvec   = Output(UInt(32.W))
}
class MemIO extends Bundle {
  val rAddr  = Output(UInt(32.W))
  val rData  = Input(UInt(32.W))
  val ren    = Output(UInt(1.W))
  val rWidth = Output(UInt(4.W))
  val rValid = Input(UInt(1.W))
  val rReady = Output(UInt(1.W))

  val wAddr  = Output(UInt(32.W))
  val wData  = Output(UInt(32.W))
  val wen    = Output(UInt(1.W))
  val wWidth = Output(UInt(4.W))
  val wValid = Output(UInt(1.W))
  val wReady = Input(UInt(1.W))

  val rEvent = Output(UInt(1.W))
  val wEvent = Output(UInt(1.W))

  def IOinit() = {
    this.rAddr  := 0.U
    this.wAddr  := 0.U
    this.wData  := Fill(32, 1.U) //FFFF FFFF
    this.wen    := 0.U
    this.ren    := 0.U
    this.wWidth := 4.U
    this.rWidth := 4.U

    this.rReady := 0.U
    this.wValid := 0.U

    this.wEvent := 0.U
    this.rEvent := 0.U
  }
}

class InstIO extends Bundle {
  val rAddr  = Output(UInt(32.W))
  val rData  = Input(UInt(32.W))
  val rValid = Input(UInt(32.W))
  val Ien=Output(Bool())
}
trait AXI_WithValidReady {
  val Valid: UInt
  val Ready: UInt
  // require(Valid.getWidth == 1)
  // require(Ready.getWidth == 1)
  def fire(): Bool = {
    Valid.asBool && Ready.asBool
  }
}
trait AXI_WithIOInit {
  def IOinit[T <: Data](value: T): Unit
  def IOinit(): Unit = {
    this.IOinit(0.U)
  }
  def IODontCare(): Unit = {
    this.IOinit(DontCare)
  }
  def Flipped_IOinit[T <: Data](value: T): Unit
  def Flipped_IOinit(): Unit = {
    Flipped_IOinit(0.U)
  }
  def Flipped_IODontCare(): Unit = {
    Flipped_IOinit(DontCare)
  }
  def ALLDontCare(): Unit = {
    IODontCare()
    Flipped_IODontCare()
  }
}
class mmioAR extends Bundle with AXI_WithIOInit with AXI_WithValidReady {
  val Addr  = Input(UInt(32.W))
  val Width = Input(UInt(32.W))
  val Valid = Input(UInt(1.W))
  val Ready = Output(UInt(1.W))

  def IOinit[T <: Data](value: T): Unit = {
    Ready := value
  }
  def Flipped_IOinit[T <: Data](value: T): Unit = {
    Addr  := value
    Width := value
    Valid := value
  }
}
class mmioR extends Bundle with AXI_WithIOInit with AXI_WithValidReady{
  val Data  = Output(UInt(32.W))
  val Valid = Output(UInt(1.W))
  val Ready = Input(UInt(1.W))

  def IOinit[T <: Data](value: T): Unit = {
    Data  := value
    Valid := value
  }
  def Flipped_IOinit[T <: Data](value: T): Unit = {
    Ready := value
  }
}
class mmioAW extends Bundle with AXI_WithIOInit with AXI_WithValidReady{
  val Addr  = Input(UInt(32.W))
  val Port  = Input(UInt(2.W))
  val Valid = Input(UInt(1.W))
  val Ready = Output(UInt(1.W))
  def IOinit[T <: Data](value: T): Unit = {

    Ready := value
  }
  def Flipped_IOinit[T <: Data](value: T): Unit = {

    Addr  := value
    Port  := value
    Valid := value
  }
}
class mmioW extends Bundle with AXI_WithIOInit with AXI_WithValidReady{
  val Data  = Input(UInt(32.W))
  val Strb  = Input(UInt((32 / 8).W))
  val Valid = Input(UInt(1.W))
  val Ready = Output(UInt(1.W))
  def IOinit[T <: Data](value: T): Unit = {
    Ready := value
  }
  def Flipped_IOinit[T <: Data](value: T): Unit = {
    Data  := value
    Strb  := value
    Valid := value
  }
}
class mmioB extends Bundle with AXI_WithIOInit with AXI_WithValidReady{
  val Resp  = Output(UInt(32.W))
  val Valid = Output(UInt(1.W))
  val Ready = Input(UInt(1.W))
  def IOinit[T <: Data](value: T): Unit = {
    Resp  := value
    Valid := value
  }
  def Flipped_IOinit[T <: Data](value: T): Unit = {
    Ready := value
  }
}
class Mr_mmioIO extends Bundle with AXI_WithIOInit {
  val AR = new mmioAR
  val R  = new mmioR
  def IOinit[T <: Data](value: T): Unit = {
    R.IOinit(value)
    AR.IOinit(value)
  }
  def Flipped_IOinit[T <: Data](value: T): Unit = {
    R.Flipped_IOinit(value)
    AR.Flipped_IOinit(value)
  }
}
class Mw_mmioIO extends Bundle with AXI_WithIOInit {

  val AW = new mmioAW
  val W  = new mmioW
  val B  = new mmioB
  def IOinit[T <: Data](value: T): Unit = {
    AW.IOinit(value)
    W.IOinit(value)
    B.IOinit(value)
  }
  def Flipped_IOinit[T <: Data](value: T): Unit = {
    AW.Flipped_IOinit(value)
    W.Flipped_IOinit(value)
    B.Flipped_IOinit(value)
  }
}
class SUCtrl_IO extends Bundle{
  val wEn=Input(UInt(1.W))
  val wAddr=Input(UInt(32.W))
  val wData=Input(UInt(32.W))
  val wEop=Output(UInt(1.W))

  
}
class LSUCtrl_IO extends Bundle{
  val SUCtrl=new SUCtrl_IO
}