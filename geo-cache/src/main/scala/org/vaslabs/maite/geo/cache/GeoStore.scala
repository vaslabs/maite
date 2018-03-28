package org.vaslabs.maite.geo.cache

import akka.actor.{Actor, Props}
import org.vaslabs.maite.geo.{PointChecker, TerrestrialArea}
import cats.effect._
class GeoStore[+Res, +Area <: TerrestrialArea] private(f: Area => IO[Res])(implicit pointChecker: PointChecker[Area]) extends Actor {

  import GeoStore.Protocol._
  import org.vaslabs.maite.geo._

  override def receive: Receive = {
    case g: GetArea =>
      sender() ! NotFound
    case StoreArea(area: Area) =>
      context.become(receiveWithInitialData(Set(LazyAreaAction[Area, Res](area, f))))
  }

  import syntax._
  private[this] def receiveWithInitialData(cache: Set[AreaAction[Area]]): Receive = {

    case GetArea(point) =>
      cache.find(_.key.contains(point)).fold(
        sender() ! NotFound
      )(runAction)
    case StoreArea(area: Area) =>
      context.become(receiveWithInitialData(cache + LazyAreaAction(area, f)))
    case uc: UpdateCache[Area, Res] =>
      context.become(receiveWithInitialData((cache - uc.lazyAreaAction) + uc.areaResult))
  }

  //TODO delegate this to a sharded cluster
  def runAction[A <: TerrestrialArea](areaAction: AreaAction[A]): Unit = {
    areaAction match {
      case action: LazyAreaAction[Area, Res] =>
        val result = AreaResult[Area, Res](action.key, f(action.key).unsafeRunSync())
        sender() ! result.data
        self ! UpdateCache[Area, Res](action, result)
      case AreaResult(key, res: Res) =>
        sender() ! res
    }
  }
}

object GeoStore {
  def props[Res, Area <: TerrestrialArea](f: Area => IO[Res])(implicit pointChecker: PointChecker[Area]): Props = Props(new GeoStore[Res, Area](f))

  object Protocol {

    import org.vaslabs.maite.geo._

    case class GetArea(geoPoint: GeoPoint)
    case class StoreArea[A <: TerrestrialArea](area: A)

    sealed trait ResponseError

    case object NotFound extends ResponseError
    private[GeoStore] case class UpdateCache[Area <: TerrestrialArea, Res](
        lazyAreaAction: LazyAreaAction[Area, Res], areaResult: AreaResult[Area, Res])
  }
}
