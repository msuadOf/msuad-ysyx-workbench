import chisel3._
import chisel3.util._

class diffIO extends Bundle {
  val diff_en  = Output(UInt(1.W))
  val DMemInst = Output(UInt(1.W))
  val pc       = Output(UInt(32.W))
  val dnpc     = Output(UInt(32.W))
  val snpc     = Output(UInt(32.W))
  val regs     = Output(Vec(32, UInt(32.W)))

  val mepc    = Output(UInt(32.W))
  val mcause  = Output(UInt(32.W))
  val mstatus = Output(UInt(32.W))
  val mtvec   = Output(UInt(32.W))
}
class MemIO extends Bundle with WithIOInit {
  val rAddr  = Output(UInt(32.W))
  val rData  = Input(UInt(32.W))
  val ren    = Output(UInt(1.W))
  val rWidth = Output(UInt(4.W))
  val rStrb  = Output(UInt(4.W))
  val rValid = Input(UInt(1.W))
  val rReady = Output(UInt(1.W))

  val wAddr  = Output(UInt(32.W))
  val wData  = Output(UInt(32.W))
  val wen    = Output(UInt(1.W))
  val wWidth = Output(UInt(4.W))
  val wStrb  = Output(UInt(4.W))
  val wValid = Output(UInt(1.W))
  val wReady = Input(UInt(1.W))

  val rEvent = Output(UInt(1.W))
  val wEvent = Output(UInt(1.W))

  def IOinit[T <: Data](value: T): Unit = {
    this.rAddr  := value
    this.wAddr  := value
    this.wData  := value //FFFF FFFF
    this.wen    := value
    this.ren    := value
    this.wWidth := value
    this.rWidth := value
    this.rReady := value
    this.wValid := value
    this.wEvent := value
    this.rEvent := value
    rStrb       := value
    wStrb       := value
  }
  def Flipped_IOinit[T <: Data](value: T): Unit = {
    rData  := value
    rValid := value
    wReady := value
  }

}

class InstIO extends Bundle with WithIOInit {
  val rAddr  = Output(UInt(32.W))
  val rData  = Input(UInt(32.W))
  val rValid = Input(UInt(1.W))
  val Ien    = Output(Bool())
  def IOinit[T <: Data](value: T): Unit = {
    rAddr := value
    Ien   := value
  }
  def Flipped_IOinit[T <: Data](value: T): Unit = {
    rData  := value
    rValid := value
  }
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
trait WithIOInit {
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
class mmioAR extends Bundle with WithIOInit with AXI_WithValidReady {
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
class mmioR extends Bundle with WithIOInit with AXI_WithValidReady {
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
class mmioAW extends Bundle with WithIOInit with AXI_WithValidReady {
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
class mmioW extends Bundle with WithIOInit with AXI_WithValidReady {
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
class mmioB extends Bundle with WithIOInit with AXI_WithValidReady {
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
class Mr_mmioIO extends Bundle with WithIOInit {
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
class Mw_mmioIO extends Bundle with WithIOInit {

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
class AXIIO(datawidth: Int = 32) extends Bundle with WithIOInit {
  val AR              = Flipped(new mmioAR)
  val R               = Flipped(new mmioR)
  val AW              = Flipped(new mmioAW)
  val W               = Flipped(new mmioW)
  val B               = Flipped(new mmioB)
  def read_request()  = AR.Valid.asBool
  def write_request() = AW.Valid.asBool && W.Valid.asBool
  def rw_request()    = read_request() || write_request()
  def read_sop()      = AR.fire()
  def write_sop()     = AW.fire() && W.fire()
  def rw_sop()        = read_sop() || write_sop()
  def read_eop()      = R.fire().asBool
  def write_eop()     = B.fire().asBool
  def rw_eop()        = read_eop() || write_eop()
  def IOinit[T <: Data](value: T): Unit = {
    R.Flipped_IOinit(value)
    AR.Flipped_IOinit(value)
    AW.Flipped_IOinit(value)
    W.Flipped_IOinit(value)
    B.Flipped_IOinit(value)
  }
  def Flipped_IOinit[T <: Data](value: T): Unit = {
    R.IOinit(value)
    AR.IOinit(value)
    AW.IOinit(value)
    W.IOinit(value)
    B.IOinit(value)
  }
  def -->(Mr: Mr_mmioIO): Unit = {
    AR <> Mr.AR
    R <> Mr.R
  }
  def -->(Mw: Mw_mmioIO): Unit = {
    AW <> Mw.AW
    W <> Mw.W
    B <> Mw.B
  }
}
class SUCtrl_IO extends Bundle with WithIOInit {
  val wEn   = Input(UInt(1.W))
  val wAddr = Input(UInt(32.W))
  val wData = Input(UInt(32.W))
  val wStrb = Input(UInt(4.W))
  val wEop  = Output(UInt(1.W))

  def IOinit[T <: Data](value: T): Unit = {
    wEop := value
  }
  def Flipped_IOinit[T <: Data](value: T): Unit = {
    wEn   := value
    wAddr := value
    wData := value
  }
}
class LUCtrl_IO extends Bundle with WithIOInit {
  val rEn    = Input(UInt(1.W))
  val rAddr  = Input(UInt(32.W))
  val rData  = Output(UInt(32.W))
  val rStrb  = Input(UInt(4.W))
  val rValid = Output(UInt(1.W))
  val rEop   = Output(UInt(1.W))
  def IOinit[T <: Data](value: T): Unit = {
    rData  := value
    rValid := value
    rEop   := value

  }
  def Flipped_IOinit[T <: Data](value: T): Unit = {
    rEn   := value
    rAddr := value
  }
}
