package org.vaslabs.maite.geo

import cats.effect.IO

package object cache {

  sealed trait AreaAction[+A <: TerrestrialArea] {
    val key: A
  }

  abstract case class LazyAreaAction[Area <: TerrestrialArea, Res] private(key: Area) extends AreaAction[Area] {
    val action: Area => IO[Res]
  }

  object LazyAreaAction {
    def apply[Area <: TerrestrialArea, Res](key: Area, f: Area => IO[Res]) =
      new LazyAreaAction[Area, Res](key) {
        override val action: Area => IO[Res] = f
      }
  }

  case class AreaResult[Area <: TerrestrialArea, Data](key: Area, data: Data) extends AreaAction[Area]
}
