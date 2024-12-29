package core

import chisel3._
import chisel3.util._

import core._
import core.utils._
import core.AXI4._
import core.Cache._

object Config {
    val core=new CoreConfig(
                            wordWidth = 32, addrWidth = 32,
                            axi4=AXI4Parameters(addrBits = 32, dataBits = 64, idBits = 4),
                            cache=Some(CacheParameters( addrBits=32,cpuWordWidth=32, cachelineBytes=8, setNum=32,wayNum=2))
                            )
}
case class CoreConfig(wordWidth: Int , addrWidth: Int,axi4:AXI4Parameters,cache: Option[CacheParameters] = None){
    def this(addrWidth: Int,cpu_wordWidth: Int , axi4_dataWidth: Int,axi4_idBits: Int,cachelineBytes: Int,setNum: Int,wayNum: Int)={
        this(wordWidth = cpu_wordWidth, addrWidth = addrWidth,
                            axi4=AXI4Parameters(addrBits = addrWidth, dataBits = cachelineBytes*8, idBits = axi4_idBits),
                            cache=Some(CacheParameters( addrBits=addrWidth,cpuWordWidth=cpu_wordWidth, cachelineBytes=cachelineBytes, setNum=setNum,wayNum=wayNum))
                            )
    }
    val cacheparam=cache.orNull
    if(cache==None) 
        require((addrWidth == axi4.addrBits),"cpu's addrWidth,axi4's addrBits must be same")
    else 
        require((addrWidth == axi4.addrBits)&& (if (cache==None)(addrWidth == cache.orNull.addrBits) else (true)),"cpu's addrWidth,axi4's addrBits,cache's addrBits must be same")
    if(cache!=None) require(wordWidth==cacheparam.cpuWordWidth,s"wordWidth(${wordWidth}),cache's cpuWordWidth(${cacheparam.cpuWordWidth}) must be same")
    def toAXI4BundleParameters=AXI4BundleParameters(addrBits = axi4.addrBits, dataBits = axi4.dataBits, idBits = axi4.idBits)

}
object CoreConfig {

}