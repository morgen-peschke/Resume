package mpeschke.resume

import io.circe.syntax._
import cats.syntax.show._
import io.circe.{Decoder, Encoder, Json}
import mpeschke.resume.Address.{City, State, Street, Zip}

final case class Address(streetOpt: Option[Street],
                         city: City,
                         state: State,
                         zipOpt: Option[Zip]) {
  def line1: String = streetOpt.fold("")(Street.raw)

  def line2: String = zipOpt match {
    case Some(zip) => show"$city, $state $zip"
    case None => show"$city, $state"
  }

  def oneLine: String = (streetOpt, zipOpt) match {
    case (Some(street), Some(zip)) => show"$street, $city, $state $zip"
    case (Some(street), None) => show"$street, $city, $state"
    case (None, Some(zip)) => show"$city, $state, $zip"
    case (None, None) => show"$city, $state"
  }
}
object Address {
  object Street extends StringWrapper
  type Street = Street.Type

  object City extends StringWrapper
  type City = City.Type

  object State extends StringWrapper
  type State = State.Type

  object Zip extends StringWrapper
  type Zip = Zip.Type

  implicit final val encoder: Encoder[Address] = Encoder.instance[Address] { a =>
    Json.obj(
      "street" -> a.streetOpt.asJson,
      "city" -> a.city.asJson,
      "state" -> a.state.asJson,
      "zip" -> a.zipOpt.asJson,
      "first_line" -> a.line1.asJson,
      "second_line" -> a.line2.asJson,
      "oneLine" -> a.oneLine.asJson
    ).dropNullValues
  }
  implicit final val decoder: Decoder[Address] = Decoder.instance[Address] { cursor =>
    for {
      streetOpt <- cursor.downField("street").as[Option[Street]]
      city <- cursor.downField("city").as[City]
      state <- cursor.downField("state").as[State]
      zipOpt <- cursor.downField("zip").as[Option[Zip]]
    } yield Address(streetOpt, city, state, zipOpt)
  }
}
