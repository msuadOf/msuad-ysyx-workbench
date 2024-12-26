package core

import chisel3._
import chisel3.util._

// private def findBitPositions(value: Long): List[Int] = {
//   (0 until 64).filter { i =>
//     // 检查第i位是否为1
//     ((value >> i) & 1) == 1
//   }.toList
// }
class csr_base() {
  val value:  UInt = RegInit(0.U(32.W))
  def read(): UInt = value
  def write(value: UInt): Unit = { this.value := value }
  def :=(value: UInt) = write(value)

}

class mepc_csr extends csr_base {
  override def write(value: UInt): Unit = {
    this.value := value & -4.S(32.W).asUInt //!!!!
  }
}
class mcause_csr extends csr_base {}
class mstatus_csr extends csr_base {
  override val value: UInt = RegInit(0x1800.U(32.W))
  def write_UIE(bit: UInt): Unit = { this.value(0) := bit }
  def write_SIE(bit: UInt): Unit = { this.value(1) := bit }
  def write_HIE(bit: UInt): Unit = { this.value(2) := bit }
  def write_MIE(bit: UInt): Unit = { this.value(3) := bit }
  def write_UPIE(bit: UInt): Unit = { this.value(4) := bit }
  def write_SPIE(bit: UInt): Unit = { this.value(5) := bit }
  def write_HPIE(bit: UInt): Unit = { this.value(6) := bit }
  def write_MPIE(bit: UInt): Unit = { this.value(7) := bit }
  def write_SPP(bit: UInt): Unit = { this.value(8) := bit }
  def write_HPP(bit: UInt): Unit = { this.value(10, 9) := bit }
  def write_VS(bit: UInt): Unit = { write_HPP(bit) }
  def write_MPP(bit: UInt): Unit = { this.value(12, 11) := bit }
  def write_FS(bit: UInt): Unit = { this.value(14, 13) := bit }
  def write_XS(bit: UInt): Unit = { this.value(16, 15) := bit }
  def write_MPRV(bit: UInt): Unit = { this.value(17) := bit }
  def write_PUM(bit: UInt): Unit = { this.value(18) := bit }
  def write_MXR(bit: UInt): Unit = { this.value(19) := bit }
  def write_TVM(bit: UInt): Unit = { this.value(20) := bit }
  def write_TW(bit: UInt): Unit = { this.value(21) := bit }
  def write_TSR(bit: UInt): Unit = { this.value(22) := bit }
  def write_SD(bit: UInt): Unit = { this.value(31) := bit }

  def UIE: UInt = { this.value(0) }
  def SIE: UInt = { this.value(1) }
  def HIE: UInt = { this.value(2) }
  def MIE: UInt = { this.value(3) }
  def UPIE: UInt = { this.value(4) }
  def SPIE: UInt = { this.value(5) }
  def HPIE: UInt = { this.value(6) }
  def MPIE: UInt = { this.value(7) }
  def SPP: UInt = { this.value(8) }
  def HPP: UInt = { this.value(10, 9) }
  def VS: UInt = { HPP }
  def MPP: UInt = { this.value(12, 11) }
  def FS: UInt = { this.value(14, 13) }
  def XS: UInt = { this.value(16, 15) }
  def MPRV: UInt = { this.value(17) }
  def PUM: UInt = { this.value(18) }
  def MXR: UInt = { this.value(19) }
  def TVM: UInt = { this.value(20) }
  def TW: UInt = { this.value(21) }
  def TSR: UInt = { this.value(22) }
  def SD: UInt = { this.value(31) }

}
class mtvec_csr extends csr_base {}
class csr {
  val mepc    = new mepc_csr
  val mcause  = new mcause_csr
  val mstatus = new mstatus_csr
  val mtvec   = new mtvec_csr
}

trait HasCSRConst {
  // User Trap Setup
  val Ustatus = 0x000
  val Uie     = 0x004
  val Utvec   = 0x005

  // User Trap Handling
  val Uscratch = 0x040
  val Uepc     = 0x041
  val Ucause   = 0x042
  val Utval    = 0x043
  val Uip      = 0x044

  // User Floating-Point CSRs (not implemented)
  val Fflags = 0x001
  val Frm    = 0x002
  val Fcsr   = 0x003

  // User Counter/Timers
  val Cycle   = 0xc00
  val Time    = 0xc01
  val Instret = 0xc02

  // Supervisor Trap Setup
  val Sstatus    = 0x100
  val Sedeleg    = 0x102
  val Sideleg    = 0x103
  val Sie        = 0x104
  val Stvec      = 0x105
  val Scounteren = 0x106

  // Supervisor Trap Handling
  val Sscratch = 0x140
  val Sepc     = 0x141
  val Scause   = 0x142
  val Stval    = 0x143
  val Sip      = 0x144

  // Supervisor Protection and Translation
  val Satp = 0x180

  // Machine Information Registers
  val Mvendorid = 0xf11
  val Marchid   = 0xf12
  val Mimpid    = 0xf13
  val Mhartid   = 0xf14

  // Machine Trap Setup
  val Mstatus    = 0x300
  val Misa       = 0x301
  val Medeleg    = 0x302
  val Mideleg    = 0x303
  val Mie        = 0x304
  val Mtvec      = 0x305
  val Mcounteren = 0x306

  // Machine Trap Handling
  val Mscratch = 0x340
  val Mepc     = 0x341
  val Mcause   = 0x342
  val Mtval    = 0x343
  val Mip      = 0x344

  // Machine Memory Protection
  // TBD
  val Pmpcfg0     = 0x3a0
  val Pmpcfg1     = 0x3a1
  val Pmpcfg2     = 0x3a2
  val Pmpcfg3     = 0x3a3
  val PmpaddrBase = 0x3b0

  // Machine Counter/Timers
  // Currently, NutCore uses perfcnt csr set instead of standard Machine Counter/Timers
  // 0xB80 - 0x89F are also used as perfcnt csr

  // Machine Counter Setup (not implemented)
  // Debug/Trace Registers (shared with Debug Mode) (not implemented)
  // Debug Mode Registers (not implemented)

  def privEcall  = 0x000.U
  def privEbreak = 0x001.U
  def privMret   = 0x302.U
  def privSret   = 0x102.U
  def privUret   = 0x002.U

  def ModeM = 0x3.U
  def ModeH = 0x2.U
  def ModeS = 0x1.U
  def ModeU = 0x0.U

  def IRQ_UEIP = 0
  def IRQ_SEIP = 1
  def IRQ_MEIP = 3

  def IRQ_UTIP = 4
  def IRQ_STIP = 5
  def IRQ_MTIP = 7

  def IRQ_USIP = 8
  def IRQ_SSIP = 9
  def IRQ_MSIP = 11

  val IntPriority = Seq(
    IRQ_MEIP,
    IRQ_MSIP,
    IRQ_MTIP,
    IRQ_SEIP,
    IRQ_SSIP,
    IRQ_STIP,
    IRQ_UEIP,
    IRQ_USIP,
    IRQ_UTIP
  )
}

trait HasExceptionNO {
  def instrAddrMisaligned = 0
  def instrAccessFault    = 1
  def illegalInstr        = 2
  def breakPoint          = 3
  def loadAddrMisaligned  = 4
  def loadAccessFault     = 5
  def storeAddrMisaligned = 6
  def storeAccessFault    = 7
  def ecallU              = 8
  def ecallS              = 9
  def ecallM              = 11
  def instrPageFault      = 12
  def loadPageFault       = 13
  def storePageFault      = 15

  val ExcPriority = Seq(
    breakPoint, // TODO: different BP has different priority
    instrPageFault,
    instrAccessFault,
    illegalInstr,
    instrAddrMisaligned,
    ecallM,
    ecallS,
    ecallU,
    storeAddrMisaligned,
    loadAddrMisaligned,
    storePageFault,
    loadPageFault,
    storeAccessFault,
    loadAccessFault
  )
}
