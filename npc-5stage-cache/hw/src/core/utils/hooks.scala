package chisel3 {
  object getDirection {
    def apply[T <: Data](source: => T): T = {
      SpecifiedDirection.specifiedDirection(source)(x => SpecifiedDirection.flip(x.specifiedDirection))
    }
  }
}

package core.utils {
  import chisel3._
  object getDirection {
    def apply[T <: Data](source: => T): T = {
      getDirection(source)
    }
  }
}
