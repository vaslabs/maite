package org.vaslabs.maite

import eu.timepit.refined._
import eu.timepit.refined.api.Refined

package object geo {
  import eu.timepit.refined.auto._

  import eu.timepit.refined.numeric._
  import eu.timepit.refined.types.numeric._

  type LatRange = Interval.Closed[W.`-90.0`.T, W.`90.0`.T]

  type Latitude = Double Refined LatRange

  type LongRange = Interval.Closed[W.`-180.0`.T, W.`180.0`.T]

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

  trait PointChecker[Area <: TerrestrialArea] {
    def pointIsInArea(geoPoint: GeoPoint, area: Area): Boolean
  }

  object implicits {
    implicit val squarePointChecker: PointChecker[Square] = (point, area) =>
      point.lat >= area.minLat && point.lng > area.minLng &&
        point.lat <= area.maxLat && point.lng <= area.maxLng

  }

}
