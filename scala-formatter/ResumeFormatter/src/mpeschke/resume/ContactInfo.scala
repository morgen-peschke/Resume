package mpeschke.resume

import io.circe.syntax._
import io.circe.{Decoder, Encoder, Json}
import mpeschke.resume.ContactInfo.{Email, PhoneNumber}

final case class ContactInfo(email: Email,
                             phone: PhoneNumber,
                             address: Address,
                             websites: List[Website]
                            )
object ContactInfo {
  object Email extends StringWrapper
  type Email = Email.Type

  object PhoneNumber extends StringWrapper
  type PhoneNumber = PhoneNumber.Type

  implicit final val encoder: Encoder[ContactInfo] = Encoder.instance[ContactInfo] { ci =>
    Json.obj(
      "email" -> ci.email.asJson,
      "phone" -> ci.phone.asJson,
      "address" -> ci.address.asJson,
      "websites" -> ci.websites.asJson
    )
  }
  implicit final val decoder: Decoder[ContactInfo] = Decoder.instance[ContactInfo] { cursor =>
    for {
      email <- cursor.downField("email").as[Email]
      phone <- cursor.downField("phone").as[PhoneNumber]
      address <- cursor.downField("address").as[Address]
      websites <- cursor.downField("websites").as[List[Website]]
    } yield ContactInfo(email, phone, address, websites)
  }
}