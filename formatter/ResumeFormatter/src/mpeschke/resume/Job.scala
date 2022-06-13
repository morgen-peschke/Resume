package mpeschke.resume

import io.circe.syntax._
import io.circe.{Decoder, Encoder, Json}
import mpeschke.resume.Job.{Accomplishment, Company}

final case class Job(company: Company,
                     title: Title,
                     location: Address,
                     dates: DateRange,
                     languages: LanguageList,
                     accomplishments: List[Accomplishment])
object Job {
  object Company extends StringWrapper
  type Company = Company.Type

  object Summary extends StringWrapper
  type Summary = Summary.Type

  final case class Accomplishment(summary: Summary, details: List[BulletPoint])
  object Accomplishment {
    implicit final val encoder: Encoder[Accomplishment] = Encoder.instance[Accomplishment] { a =>
      Json.obj(
        "summary" -> a.summary.asJson,
        "details" -> a.details.asJson,
      )
    }
    implicit final val decoder: Decoder[Accomplishment] = Decoder.instance[Accomplishment] { cursor =>
      for {
        summary <- cursor.downField("summary").as[Summary]
        details <- cursor.downField("details").as[Option[List[BulletPoint]]].map(_.getOrElse(Nil))
      } yield Accomplishment(summary, details)
    }
  }

  implicit final val encoder: Encoder[Job] = Encoder.instance[Job] { j =>
    Json.obj(
      "company" -> j.company.asJson,
      "title" -> j.title.asJson,
      "location" -> j.location.asJson,
      "dates" -> j.dates.asJson,
      "languages" -> j.languages.asJson,
      "accomplishments" -> j.accomplishments.asJson
    )
  }
  implicit final val decoder: Decoder[Job] = Decoder.instance[Job] { cursor =>
    for {
      company <- cursor.downField("company").as[Company]
      title <- cursor.downField("title").as[Title]
      location <- cursor.downField("location").as[Address]
      dates <- cursor.downField("dates").as[DateRange]
      languages <- cursor.downField("languages").as[LanguageList]
      accomplishments <- cursor.downField("accomplishments").as[Option[List[Accomplishment]]].map(_.getOrElse(Nil))
    } yield Job(company, title, location, dates, languages, accomplishments)
  }
}
