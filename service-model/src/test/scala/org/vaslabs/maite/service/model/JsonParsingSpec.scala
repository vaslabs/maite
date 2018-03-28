package org.vaslabs.maite.service.model

import java.time.{LocalDate, YearMonth}

import org.scalatest.{Matchers, WordSpec}

import scala.io.Source
import eu.timepit.refined.auto._

class JsonParsingSpec extends WordSpec with Matchers{

  "json decoders" can {
    import io.circe.parser._
    import io.circe.syntax._
    import json_parsing._
    "give us a street level crime request" in {
      val source = Source.fromResource("street_level_crime_request.json")
      val jsonString = source.mkString
      source.close()
      parse(jsonString).flatMap(_.as[StreetLevelCrimeRequest]) shouldBe Right(
        StreetLevelCrimeRequest(23.12312, 12.1321, YearMonth.of(2017, 11))
      )
    }

    "decode a street level crime response" in {
      import json_parsing.foreign_api._
      val source = Source.fromResource("street_level_crime_response.json")
      val jsonString = source.mkString
      source.close()
      val streetLevelCrimeEntries = parse(jsonString).flatMap(_.as[List[StreetLevelCrimeEntry]])
      streetLevelCrimeEntries should matchPattern {
        case Right(_) =>
      }
      val response = StreetLevelCrimeResponse(streetLevelCrimeEntries.right.get)

      import json_parsing.maite_api._
      val jsonResponse = response.asJson.noSpaces
      parse(jsonResponse).flatMap(_.as[StreetLevelCrimeResponse]).map(_.crimes) shouldBe streetLevelCrimeEntries
    }
  }

}
