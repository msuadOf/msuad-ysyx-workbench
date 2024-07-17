# diplomacy精讲

## 前言

在做“一生一芯”的时候，碰见第一个学习坡度陡峭，而又无法避开的一点：diplomacy

这是一个包含在rocket-chip中的工具，首先如何导入就是一个难题；其次，diplomac其使用了非常多的scala高级语法，这需要对语言有一定的熟悉度。

根据过往经历来看，我敢肯定在我学会后再回过头看这个问题肯定是较为简单，也无法理解新手在这方面的疑惑。

故在学习时同步记下这篇文章，以希望留下一些记录，待以后查阅、后人借鉴。

————2024.5.4

## 准备思路

准备方面，我使用了ysyx余博的工程，可以较好地本地导入rocket-chip的包

省去了写mill的烦恼

用的是chisel7

## 然后看代码（代码+语法）

以这份翻译（官方的样例工程）作为展开[https://shili2017.github.io/posts/CHISEL1/](https://shili2017.github.io/posts/CHISEL1/)

先粘一份完整代码，然后听我慢慢解析。这份代码是对import的有些改动的。
```scala
package ysyx

import chisel3._
import chisel3.experimental.SourceInfo
import chisel3.util.random.FibonacciLFSR

import circt.stage.ChiselStage
import org.chipsalliance.cde.config.Parameters

import chisel3._
import org.chipsalliance.cde.config.Parameters
import freechips.rocketchip.system.DefaultConfig
import freechips.rocketchip.diplomacy._

case class UpwardParam(width: Int)
case class DownwardParam(width: Int)
case class EdgeParam(width: Int)

// PARAMETER TYPES:                       D              U            E          B
object AdderNodeImp extends SimpleNodeImp[DownwardParam, UpwardParam, EdgeParam, UInt] {
  def edge(pd: DownwardParam, pu: UpwardParam, p: Parameters, sourceInfo: SourceInfo) = {
    if (pd.width < pu.width) EdgeParam(pd.width) else EdgeParam(pu.width)
  }
  def bundle(e: EdgeParam) = UInt(e.width.W)
  def render(e: EdgeParam) = RenderedEdge("blue", s"width = ${e.width}")
}

/** node for [[AdderDriver]] (source) */
class AdderDriverNode(widths: Seq[DownwardParam])(implicit valName: ValName)
  extends SourceNode(AdderNodeImp)(widths)

/** node for [[AdderMonitor]] (sink) */
class AdderMonitorNode(width: UpwardParam)(implicit valName: ValName)
  extends SinkNode(AdderNodeImp)(Seq(width))

/** node for [[Adder]] (nexus) */
class AdderNode(dFn: Seq[DownwardParam] => DownwardParam,
                uFn: Seq[UpwardParam] => UpwardParam)(implicit valName: ValName)
  extends NexusNode(AdderNodeImp)(dFn, uFn)

/** adder DUT (nexus) */
class Adder(implicit p: Parameters) extends LazyModule {
  val node = new AdderNode (
    { case dps: Seq[DownwardParam] =>
      require(dps.forall(dp => dp.width == dps.head.width), "inward, downward adder widths must be equivalent")
      dps.head
    },
    { case ups: Seq[UpwardParam] =>
      require(ups.forall(up => up.width == ups.head.width), "outward, upward adder widths must be equivalent")
      ups.head
    }
  )
  lazy val module = new LazyModuleImp(this) {
    require(node.in.size >= 2)
    node.out.head._1 := node.in.unzip._1.reduce(_ + _)
  }

  override lazy val desiredName = "Adder"
}
/** driver (source)
  * drives one random number on multiple outputs */
class AdderDriver(width: Int, numOutputs: Int)(implicit p: Parameters) extends LazyModule {
  val node = new AdderDriverNode(Seq.fill(numOutputs)(DownwardParam(width)))

  lazy val module = new LazyModuleImp(this) {
    // check that node parameters converge after negotiation
    val negotiatedWidths = node.edges.out.map(_.width)
    require(negotiatedWidths.forall(_ == negotiatedWidths.head), "outputs must all have agreed on same width")
    val finalWidth = negotiatedWidths.head

    // generate random addend (notice the use of the negotiated width)
    val randomAddend = FibonacciLFSR.maxPeriod(finalWidth)

    // drive signals
    node.out.foreach { case (addend, _) => addend := randomAddend }
  }

  override lazy val desiredName = "AdderDriver"
}
/** monitor (sink) */
class AdderMonitor(width: Int, numOperands: Int)(implicit p: Parameters) extends LazyModule {
  val nodeSeq = Seq.fill(numOperands) { new AdderMonitorNode(UpwardParam(width)) }
  val nodeSum = new AdderMonitorNode(UpwardParam(width))

  lazy val module = new LazyModuleImp(this) {
    val io = IO(new Bundle {
      val error = Output(Bool())
    })

    // print operation
    printf(nodeSeq.map(node => p"${node.in.head._1}").reduce(_ + p" + " + _) + p" = ${nodeSum.in.head._1}")

    // basic correctness checking
    io.error := nodeSum.in.head._1 =/= nodeSeq.map(_.in.head._1).reduce(_ + _)
  }

  override lazy val desiredName = "AdderMonitor"
}

/** top-level connector */
class AdderTestHarness()(implicit p: Parameters) extends LazyModule {
  val numOperands = 2
  val adder = LazyModule(new Adder)
  // 8 will be the downward-traveling widths from our drivers
  val drivers = Seq.fill(numOperands) { LazyModule(new AdderDriver(width = 8, numOutputs = 2)) }
  // 4 will be the upward-traveling width from our monitor
  val monitor = LazyModule(new AdderMonitor(width = 4, numOperands = numOperands))

  // create edges via binding operators between nodes in order to define a complete graph
  drivers.foreach{ driver => adder.node := driver.node }

  drivers.zip(monitor.nodeSeq).foreach { case (driver, monitorNode) => monitorNode := driver.node }
  monitor.nodeSum := adder.node

  lazy val module = new LazyModuleImp(this) {
    // when(monitor.module.io.error) {
    //   printf("something went wrong")
    // }
  }

  override lazy val desiredName = "AdderTestHarness"
}
object Elaborate extends App {
//   (new ChiselStage).execute(args, Seq(chisel3.stage.ChiselGeneratorAnnotation(
//     () => LazyModule(new AdderTestHarness()(Parameters.empty)).module))
//   )
  val verilog = ChiselStage.emitSystemVerilog(
  LazyModule(new AdderTestHarness()(Parameters.empty)).module
)
println(verilog)
}

```

### 参数协商和传递

#### 参数

```scala
case class UpwardParam(width: Int)
case class DownwardParam(width: Int)
case class EdgeParam(width: Int)
```

看到这段代码，有一个问题，这个case class有什么用?

##### case class

case class是一种特殊类型的类

case class = class + 一坨

```scala
case class Person(name: String, age: Int)
```

等价于

```scala
class Person(val name: String, val age: Int) {
  override def toString = s"Person(name=$name, age=$age)"
  override def equals(other: Any) = other match {
    case that: Person => this.name == that.name && this.age == that.age
    case _ => false
  }
  override def hashCode() = scala.util.hashing.MurmurHash3.productHash(this)
}

object Person {
  def apply(name: String, age: Int) = new Person(name, age)
  def unapply(p: Person): Option[(String, Int)] = Some((p.name, p.age))
}
```

注意case class这里的参数列表，默认情况下，case clas的构造参数会转换成val类型的字段

#### 节点
在节点实现（即NodeImp中），我们描述参数如何在我们的图中流动，以及如何在节点之间协商参数。边参数（E）描述了需要在边上传递的数据类型，在这个例子中就是Int；捆绑参数（B）描述了模块之间硬件实现的参数化端口的数据类型，在这个例子中则为UInt。此处edge函数实际执行了节点之间的参数协商，比较了向上和向下传播的参数，并选择数据宽度较小的那个作为协商结果。

```scala
// PARAMETER TYPES:                       D              U            E          B
object AdderNodeImp extends SimpleNodeImp[DownwardParam, UpwardParam, EdgeParam, UInt] {
  def edge(pd: DownwardParam, pu: UpwardParam, p: Parameters, sourceInfo: SourceInfo) = {
    if (pd.width < pu.width) EdgeParam(pd.width) else EdgeParam(pu.width)
  }
  def bundle(e: EdgeParam) = UInt(e.width.W)
  def render(e: EdgeParam) = RenderedEdge("blue", s"width = ${e.width}")
}
```

```scala
/** node for [[AdderDriver]] (source) */
class AdderDriverNode(widths: Seq[DownwardParam])(implicit valName: ValName)
  extends SourceNode(AdderNodeImp)(widths)

/** node for [[AdderMonitor]] (sink) */
class AdderMonitorNode(width: UpwardParam)(implicit valName: ValName)
  extends SinkNode(AdderNodeImp)(Seq(width))

/** node for [[Adder]] (nexus) */
class AdderNode(dFn: Seq[DownwardParam] => DownwardParam,
                uFn: Seq[UpwardParam] => UpwardParam)(implicit valName: ValName)
  extends NexusNode(AdderNodeImp)(dFn, uFn)
```

### 创建LazyModule

>##### lazy
>scala中，lazy表示的是使用时初始化
>另外，懒惰初始化可以应用于val和def（虽然def默认就是懒惰的，但懒惰val和def在语义上有所不同，val初始化后值不>变，而def每次调用都可能有不同结果）。
>
>虽然从懒惰初始化的角度看，lazy val和没有具体实现的def看起来相似，但它们之间存在本质区别：
>- lazy val在首次访问后会缓存其结果，之后的访问直接返回缓存的值，适用于计算密集型或资源加载场景。
>- def则是每次调用时都执行其函数体，适合于那些结果随时间或上下文变化的情况。
>
>##### implicit
>
>这个概念比较庞杂
>
>- implicit method：类型转换
>    ```scala
>    implicit def intToDouble(i: Int): Double = i.toDouble
>    def processNumber(num: Double): Unit = println(num)
>
>    processNumber(5) // 由于存在隐式转换，这里可以传入Int类型
>    ```
>- implicit param：默认行为、实现策略模式或依赖注入
>    ```scala
>    case class LogLevel(level: String)
>
>    def log(message: String)(implicit level: LogLevel = LogLevel("INFO")) = 
>      println(s"${level.level}: $message")
>
>    log("This is an info message") // 使用默认的日志级别
>    implicit val debugLevel = LogLevel("DEBUG")
>    log("This is a debug message") // 使用隐式提供的DEBUG级别
>    ```
>- implicit class:
>    ```scala
>    implicit class RichString(s: String) {
>    def lengthSquared: Int = s.length * s.length
>    }
>
>    val str = "Hello"
>    println(str.lengthSquared) // 利用隐式转换调用新方法
>    ```
>

Lazy的意思是指将表达式的evaluation推迟到需要的时候。在创建Diplomacy图之后，参数协商是lazy完成的，因此我们想要参数化的硬件也必须延迟生成，因此需要使用LazyModule。需要注意的是，定义Diplomacy图的组件（在这个例子里为节点）的创建不是lazy的，模块硬件需要写在LazyModuleImp。

在这个例子中，我们希望driver将相同位宽的数据输入到加法器中，monitor的数据来自加法器的输出以及driver，所有这些数据位宽都应该相同。我们可以通过AdderNode的require来限制这些参数，将DownwardParam向下传递，以及将UpwardParam向上传递。
```scala
/** adder DUT (nexus) */
class Adder(implicit p: Parameters) extends LazyModule {
  val node = new AdderNode (
    { case dps: Seq[DownwardParam] =>
      require(dps.forall(dp => dp.width == dps.head.width), "inward, downward adder widths must be equivalent")
      dps.head
    },
    { case ups: Seq[UpwardParam] =>
      require(ups.forall(up => up.width == ups.head.width), "outward, upward adder widths must be equivalent")
      ups.head
    }
  )
  lazy val module = new LazyModuleImp(this) {
    require(node.in.size >= 2)
    node.out.head._1 := node.in.unzip._1.reduce(_ + _)
  }

  override lazy val desiredName = "Adder"
}
```

>##### Partial Function
>好了，又看不懂了
>```scala
>    { case dps: Seq[DownwardParam] =>
>      require(dps.forall(dp => dp.width == dps.head.width), "inward, downward adder widths must be equivalent")
>      dps.head
>    }
>```
>怎么传参的时候就这么一个东西就作为参数了呢
>```scala
>class AdderNode(dFn: Seq[DownwardParam] => DownwardParam,
>                uFn: Seq[UpwardParam] => UpwardParam)
>```
>可以看到入参是一个传名函数（传名函数是什么？还是百度吧）
>也就是说，那一坨花括号是一个函数
>
>{ case ... }的结构实际上是在定义一个部分函数（PartialFunction），它是一种特殊的函数类型，经常与模式匹配一起使用。部分函数可以理解为一个仅定义了部分情况（cases）的函数，对于未定义的情况，如果尝试调用则会抛出异常。
>
>- 让我们直接以更简单的例子说明部分函数的用法：
>
>简单例子：定义一个处理整数的匿名部分函数
>```scala
>val processNumbers: PartialFunction[Int, String] = {
>  case x if x > 0 => s"$x is positive"
>  case 0 => "Zero"
>}
>
>processNumbers(5) // 输出: "5 is positive"
>processNumbers(0) // 输出: "Zero"
>// processNumbers(-1) // 如果尝试调用，会抛出MatchError异常
>```
>在这个例子中，processNumbers是一个PartialFunction[Int, String]，它只定义了两个情况：当输入的整数大于0时和等于0时的处理逻辑。如果尝试传入一个负数，由于没有对应的case分支，Scala会抛出MatchError。
>
>回到原始代码片段：
>```scala
>{ case dps: Seq[DownwardParam] =>
>  require(dps.forall(dp => dp.width == dps.head.width), 
>          "inward, downward adder widths must be equivalent")
>  dps.head
>}
>```
>这里定义的就是这样一个部分函数，它只匹配Seq[DownwardParam]类型的输入，执行一系列操作后返回dps.head。虽然没有直接写出match关键字，但这种结构实质上是在做模式匹配的工作，是Scala中一种优雅的处理不同类型或情况的函数定义方式。
>

>##### initializer block
>又看不懂了，构造函数垢面后
>```scala
>  lazy val module = new LazyModuleImp(this) {
>    require(node.in.size >= 2
>    node.out.head._1 := no.in.unzip._1.reduce(_ + _)
>  }
>```
>在Scala中，当你在创建一个类的实例并立即跟随一个大括号 { ... } 时，这个大括号内的代码块实际上是该类构造函数的一部分，被称为初始化块（initializer block）。初始化块会在类的构造函数执行完毕后立即执行，常用于进行进一步的初始化设置或者运行一些初始化逻辑。初始化块可以访问到类的所有成员，包括由构造函数参数初始化的成员。
>```scala
>class Person(val name:String){
>  println(s"1.class Person($name)")
>  val age=10
>}
>val p=new Person("as"){
>  println(s"2.obj p ($name,$age)")
>}
>// 1.class Person(as)
>// 2.obj p (as,10)
>```

三要素：driver（驱动）、dut（功能模块）、monitor（检查）中已经完成了dut的编写，接下来是driver和monitor

AdderDriver随机生成位宽为finalWidth的数据，并传递到numOutputs个source。
```scala
/** driver (source)
  * drives one random number on multiple outputs */
class AdderDriver(width: Int, numOutputs: Int)(implicit p: Parameters) extends LazyModule {
  val node = new AdderDriverNode(Seq.fill(numOutputs)(DownwardParam(width)))

  lazy val module = new LazyModuleImp(this) {
    // check that node parameters converge after negotiation
    val negotiatedWidths = node.edges.out.map(_.width)
    require(negotiatedWidths.forall(_ == negotiatedWidths.head), "outputs must all have agreed on same width")
    val finalWidth = negotiatedWidths.head

    // generate random addend (notice the use of the negotiated width)
    val randomAddend = FibonacciLFSR.maxPeriod(finalWidth)

    // drive signals
    node.out.foreach { case (addend, _) => addend := randomAddend }
  }

  override lazy val desiredName = "AdderDriver"
}
```
AdderMonitor打印加法器输出并检测错误，有两个AdderMonitorNode节点从AdderDriver接收加法的两个输入，以及一个AdderMonitorNode节点从加法器接收加法的输出。
```scala
/** monitor (sink) */
class AdderMonitor(width: Int, numOperands: Int)(implicit p: Parameters) extends LazyModule {
  val nodeSeq = Seq.fill(numOperands) { new AdderMonitorNode(UpwardParam(width)) }
  val nodeSum = new AdderMonitorNode(UpwardParam(width))

  lazy val module = new LazyModuleImp(this) {
    val io = IO(new Bundle {
      val error = Output(Bool())
    })

    // print operation
    printf(nodeSeq.map(node => p"${node.in.head._1}").reduce(_ + p" + " + _) + p" = ${nodeSum.in.head._1}")

    // basic correctness checking
    io.error := nodeSum.in.head._1 =/= nodeSeq.map(_.in.head._1).reduce(_ + _)
  }

  override lazy val desiredName = "AdderMonitor"
}
```



