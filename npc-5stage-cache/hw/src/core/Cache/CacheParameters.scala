package core
import chisel3._
import chisel3.util._

import core.AXI4._
/**
 * cache config:
 *  cacheline: bits=2*32 words(32)=2 bytes=2*4=8 addr:[2:0]
 *  | tag[31:8] | index[7:3] | offset[2:0] |
 */
case class CacheParameters(val addrBits:Int,val cpuWordWidth:Int ,val cachelineBytes:Int,val wayNum:Int ,val setNum:Int) {
    // set === index
    require(isPow2( cachelineBytes),s"cachelineBytes=${cachelineBytes} must be a power of 2")
    val cachelineBitfield = log2Ceil(cachelineBytes) //eg. 8B len=log2(8)=3
    require(isPow2( setNum),s"setNum=${setNum} must be a power of 2")
    val indexBitfield = log2Ceil(setNum)
    require(isPow2( wayNum),s"wayNum=${wayNum} must be a power of 2")
    val wayBitfield = log2Ceil(wayNum)

    val tagBitfield = addrBits - cachelineBitfield - indexBitfield
    require(tagBitfield >= 0,s"addrBits is too small: cacheline(${cachelineBitfield}) + index(${indexBitfield}) + tag(${tagBitfield})=${cachelineBitfield+indexBitfield+tagBitfield} > addrBits(${addrBits})")


    val cachelineWidth = cachelineBytes*8 // x B = x*8 b
    require(isPow2( cpuWordWidth),s"cpuWordBytes=${cpuWordBytes} must be a power of 2")
    val cpuWordBytes = cpuWordWidth/8
    require(cachelineBytes%cpuWordBytes ==0,s"cachelineWidth(${cachelineWidth}) cpuWordWidth(${cpuWordWidth}) must be an integer multiple.")
    val cachelinePerWord = cachelineBytes/cpuWordBytes
    
    def report = {
        val cacheline= (0+cachelineBitfield-1):: 0::Nil
        val index= (cachelineBitfield+indexBitfield-1):: cachelineBitfield::Nil
        val tag= (cachelineBitfield+indexBitfield+tagBitfield-1):: index(0)+1 ::Nil
        f"""
                    |Memory address casts to cache:
                    |    ${tag(0)}%2d     ${tag(1)}%2d ${index(0)}%-2d    ${index(1)}%1d ${cacheline(0)}%1d      0
                    |   +---------+-------+--------+
                    |   |   tag   | index | offset |
                    |   +---------+-------+--------+
                    |       ${tagBitfield}        ${indexBitfield}       ${cachelineBitfield}       
      """.stripMargin}
      def printReport() = println(report)

}
object CacheParameters {
    // def apply(axi4param:AXI4Parameters) = {}
}