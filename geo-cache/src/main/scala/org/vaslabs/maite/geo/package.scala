package org.vaslabs.maite

import eu.timepit.refined._
import eu.timepit.refined.api.Refined
import eu.timepit.refined.auto._
import eu.timepit.refined.boolean.And

package object geo {

  import eu.timepit.refined.numeric._
  import eu.timepit.refined.types.numeric._

  type LatRange = GreaterEqual[W.`-90`.T] And LessEqual[W.`-90`.T]

  type Latitude = Double Refined LatRange

  type LongRange = GreaterEqual[W.`-180`.T] And LessEqual[W.`180`.T]

  type Longitude = Double Refined LongRange

  case class GeoPoint(lat: Latitude, lng: Longitude)

  case class SquareId(value: PosInt)

  trait TerrestrialArea

  case class Square(squareId: SquareId, minLat: Latitude, maxLat: Latitude, minLng: Longitude, maxLng: Longitude)
      extends TerrestrialArea
  {
    require(minLat < maxLat)
    require(minLng < maxLng)
  }

  object syntax {
    implicit final class LogicExtension[A <: TerrestrialArea](val area: A) extends AnyVal {
      def contains(geoPoint: GeoPoint)(implicit pointChecker: PointChecker[A]) =
        pointChecker.pointIsInArea(geoPoint, area)
    }
  }

  trait PointChecker[A <: TerrestrialArea] {
    def pointIsInArea(geoPoint: GeoPoint, area: A): Boolean
  }

  object implicits {
    implicit val squarePointChecker: PointChecker[Square] = (point, area) =>
      point.lat >= area.minLat && point.lng > area.minLng &&
        point.lat <= area.maxLat && point.lng <= area.maxLng
  }
}
