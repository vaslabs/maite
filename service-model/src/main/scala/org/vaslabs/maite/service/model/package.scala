package org.vaslabs.maite.service

import java.time.YearMonth

import org.vaslabs.maite.geo.{Latitude, Longitude}
package object model {
  import eu.timepit.refined.types.numeric._
  case class StreetLevelCrimeRequest(lat: Latitude, lng: Longitude, date: YearMonth)

  /*
  {
    "category": "anti-social-behaviour",
    "location_type": "Force",
    "location": {
      "latitude": "52.640961",
      "street": {
        "id": 884343,
        "name": "On or near Wharf Street North"
      },
      "longitude": "-1.126371"
    },
    "context": "",
    "outcome_status": null,
    "persistent_id": "",
    "id": 54164419,
    "location_subtype": "",
    "month": "2017-01"
  }
   */
  sealed trait CrimeCategory
  case object AntiSocialBehaviour extends CrimeCategory
  case object BicycleTheft extends CrimeCategory
  case object Burglary extends CrimeCategory
  case object CriminalDamageArson extends CrimeCategory
  case object Drugs extends CrimeCategory
  case object PossessionOfWeapons extends CrimeCategory
  case object PublicOrder extends CrimeCategory
  case object Robbery extends CrimeCategory
  case object Shoplifting extends CrimeCategory
  case object TheftFromPerson extends CrimeCategory
  case object VehicleCrime extends CrimeCategory
  case object ViolentCrime extends CrimeCategory
  case object OtherTheft extends CrimeCategory
  case object OtherCrime extends CrimeCategory

  case class UnclassifiedCrimeCategory(`type`: String) extends CrimeCategory
  sealed trait LocationType
  case object Force extends LocationType
  case object BTB extends LocationType
  case class UnclassifiedLocationType(`type`: String) extends LocationType

  case class Location(latitude: Latitude, longitude: Longitude, street: Street)
  case class Street(id: PosLong, name: String)
  case class StreetLevelCrimeEntry(
    category: CrimeCategory,
    location_type: LocationType,
    location: Location,
    id: PosLong,
    month: YearMonth
  )

  case class StreetLevelCrimeResponse(crimes: List[StreetLevelCrimeEntry])


  object json_parsing {
    import io.circe._
    import io.circe.generic.auto._
    import io.circe.generic.semiauto._
    import io.circe.java8._
    import io.circe.refined._
    implicit val dateDecoder: Decoder[YearMonth] = time.decodeYearMonthDefault

    implicit val streetLevelCrimeRequestDecoder: Decoder[StreetLevelCrimeRequest] = deriveDecoder[StreetLevelCrimeRequest]

    object maite_api {

      private implicit val streetLevelCrimeEntryDecoder: Decoder[StreetLevelCrimeEntry] = deriveDecoder[StreetLevelCrimeEntry]
      implicit val streetLevelCrimeResponseDecoder: Decoder[StreetLevelCrimeResponse] = deriveDecoder[StreetLevelCrimeResponse]

      private implicit val dateEncoder: Encoder[YearMonth] = time.encodeYearMonthDefault
      private implicit val streetLevelCrimeEntryEncoder: Encoder[StreetLevelCrimeEntry] = deriveEncoder[StreetLevelCrimeEntry]
      implicit val streetLevelCrimeResponseEncoder: Encoder[StreetLevelCrimeResponse] = deriveEncoder[StreetLevelCrimeResponse]

    }

    object foreign_api {
      implicit val crimeCategoryDecoder: Decoder[CrimeCategory] =
        Decoder.decodeString.map(
          _ match {
            case "anti-social-behaviour" => AntiSocialBehaviour
            case "bicycle-theft" => BicycleTheft
            case "criminal-damage-arson" => CriminalDamageArson
            case "drugs" => Drugs
            case "other-theft" => OtherTheft
            case "possession-of-weapons" => PossessionOfWeapons
            case "public-order" => PublicOrder
            case "robbery" => Robbery
            case "shoplifting" => Shoplifting
            case "theft-from-the-person" => TheftFromPerson
            case "vehicle-crime" => VehicleCrime
            case "violent-crime" => ViolentCrime
            case "other-crime" => OtherCrime
            case other: String => UnclassifiedCrimeCategory(other)
          }
        )
      implicit val locationTypeDecoder: Decoder[LocationType] = Decoder.decodeString.map(
        _ match {
          case "Force" => Force
          case "BTB" => BTB
          case other: String => UnclassifiedLocationType(other)
        }
      )

      implicit val streetLevelCrimeEntryDecoder: Decoder[StreetLevelCrimeEntry] = deriveDecoder[StreetLevelCrimeEntry]
    }
  }
}
