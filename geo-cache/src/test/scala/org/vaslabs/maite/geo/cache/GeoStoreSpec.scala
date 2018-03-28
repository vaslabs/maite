package org.vaslabs.maite.geo.cache

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestActorRef, TestKit}
import org.scalatest.{Matchers, WordSpecLike}
import org.vaslabs.maite.geo.{GeoPoint, Latitude, Square, SquareId}
import eu.timepit.refined.auto._
import eu.timepit.refined.numeric._
import cats.effect._

class GeoStoreSpec extends TestKit(ActorSystem("GeoCacheSpec")) with WordSpecLike with Matchers with ImplicitSender{

  val dummyData = List("point1", "point2", "point3")


  import org.vaslabs.maite.geo.implicits._

  val dummyTerrestrialArea = Square(SquareId(1), 0.0, 5.0, 0.0, 5.0)


  val datastore: Map[Square, List[String]] = Map((dummyTerrestrialArea -> dummyData))

  "a geo store" can {
    val geoStore: TestActorRef[GeoStore[List[String], Square]] =
      TestActorRef(GeoStore.props[List[String], Square](square => IO.pure(datastore.get(square).getOrElse(List.empty))))

    "update its state" in {
      geoStore ! GeoStore.Protocol.StoreArea(dummyTerrestrialArea)
      geoStore ! GeoStore.Protocol.GetArea(GeoPoint(2.3, 2.3))
      expectMsg(dummyData)
      geoStore ! GeoStore.Protocol.GetArea(GeoPoint(2.3, -1.0))
      expectMsg(GeoStore.Protocol.NotFound)
    }
  }

}
