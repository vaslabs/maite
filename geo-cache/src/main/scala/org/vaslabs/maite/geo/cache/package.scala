package org.vaslabs.maite.geo

package object cache {

  sealed trait AreaAction {
    val key: TerrestrialArea
  }

  abstract case class LazyAreaAction[A] private(key: TerrestrialArea) extends AreaAction {
    val action: TerrestrialArea => A
  }

  object LazyAreaAction {
    def apply[A](key: TerrestrialArea, f: TerrestrialArea => A) =
      new LazyAreaAction[A](key) {
        override val action: TerrestrialArea => A = f
      }
  }

  case class AreaResult[A](key: TerrestrialArea, data: A) extends AreaAction
}
