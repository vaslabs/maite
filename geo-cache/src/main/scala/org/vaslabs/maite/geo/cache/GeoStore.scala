package org.vaslabs.maite.geo.cache

import akka.actor.Actor
import org.vaslabs.maite.geo.{PointChecker, TerrestrialArea}

class GeoStore[Res, Area >: TerrestrialArea](f: Area => Res)(implicit pointChecker: PointChecker[Area]) extends Actor {

  import GeoStore.Protocol._
  import org.vaslabs.maite.geo._

  override def receive: Receive = {
    case g: GetArea =>
      sender() ! NotFound
    case StoreArea(area) =>
      context.become(receiveWithInitialData(Set(LazyAreaAction[Res](area, f))))
  }

  import syntax._
  private[this] def receiveWithInitialData(cache: Set[AreaAction]): Receive = {

    case GetArea(point) =>
      cache.find(_.key.contains(point)).fold(
        sender() ! NotFound
      )(runAction)
    case StoreArea(area) =>
      context.become(receiveWithInitialData(cache + LazyAreaAction(area, f)))
    case UpdateCache(lazyAreaAction, areaResult: AreaResult[Res]) =>
      context.become(receiveWithInitialData((cache - lazyAreaAction) + areaResult))
  }

  //TODO delegate this to a sharded cluster
  def runAction(areaAction: AreaAction): Unit = {
    areaAction match {
      case action @ LazyAreaAction(key) =>
        val result = AreaResult(key, f())
        sender() ! result.data
        self ! UpdateCache(action, result)
      case AreaResult(key, res: Res) =>
        sender() ! res
    }
  }
}

object GeoStore {
  object Protocol {

    import org.vaslabs.maite.geo._

    case class GetArea(geoPoint: GeoPoint)
    case class StoreArea(area: TerrestrialArea)

    sealed trait ResponseError

    case object NotFound extends ResponseError
    private[GeoStore] case class UpdateCache[A](lazyAreaAction: LazyAreaAction[A], areaResult: AreaResult[A])
  }
}
