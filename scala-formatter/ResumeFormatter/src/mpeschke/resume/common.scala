package mpeschke.resume

import cats.Show
import cats.data.Chain
import cats.syntax.eq._
import cats.syntax.show._
import io.circe.syntax._
import io.circe.{Decoder, Encoder, Json}
import mpeschke.resume.DateRange.DateStr
import mpeschke.resume.Website.{Label, Url}
import supertagged.NewType

import scala.annotation.tailrec

trait StringWrapper extends NewType[String] {
  implicit final val decoder: Decoder[Type] = Decoder.decodeString.map(apply(_))
  implicit final val encoder: Encoder[Type] = Encoder.encodeString.contramap(raw)
  implicit final val show: Show[Type] = Show.show(raw)
}

abstract class ListWrapper[A: Encoder : Decoder: Show](maxColumnLengthOpt: Option[Int]) extends NewType[List[A]] {
  def empty: Type = apply(List.empty[A])

  implicit final val decoder: Decoder[Type] = Decoder.decodeList[A].map(apply(_))
  implicit final val encoder: Encoder[Type] =
    maxColumnLengthOpt match {
      case None =>
        Encoder.instance[Type] { t =>
          wrapRow(raw(t).map(_.asJson)).asJson
        }
      case Some(maxColumnLength) =>
        Encoder.instance[Type] { t =>
          @tailrec
          def loop(remaining: List[Json],
                   accum: Chain[Chain[Json]],
                   currentLine: Chain[Json],
                   currentLineLength: Int): List[List[Json]] =
            remaining match {
              case Nil =>
                val accumFinal = if (currentLine.isEmpty) accum else accum.append(currentLine)
                accumFinal.map(_.toList).toList

              case head :: remaining =>
                val headJson = head.asJson
                val headLength = headJson.fold[Int](
                  jsonNull = 0,
                  jsonBoolean = _ => 0,
                  jsonNumber = _ => 0,
                  jsonString = _.length,
                  jsonArray = _ => 0,
                  jsonObject = _ => 0
                )
                val updatedLength = currentLineLength + headLength
                if (updatedLength <= maxColumnLength)
                  loop(remaining, accum, currentLine.append(headJson), updatedLength)
                else loop(remaining, accum.append(currentLine), Chain.one(headJson), headLength)
            }

          loop(raw(t).map(_.asJson), Chain.empty, Chain.empty, 0)
            .map(wrapRow(_).asJson)
            .asJson
        }
    }

  private def wrapRow(as: List[Json]): List[Json] = {
    val lastIndex = as.length - 1
    as.zipWithIndex.map {
      case (a, i) => wrapEntry(a, i, i === 0, i === lastIndex)
    }
  }

  private def wrapEntry(value: Json, index: Int, isFirst: Boolean, isLast: Boolean): Json =
    Json.obj(
      "value" -> value,
      "index" -> Json.fromInt(index),
      "first" -> (if (isFirst) Json.True else Json.Null),
      "last" -> (if (isLast) Json.True else Json.Null),
      "delim" -> (if (isLast) Json.Null else Json.fromString(","))
    ).dropNullValues

  implicit final val show: Show[Type] = Show.show(raw(_).show)
}

final case class Website(label: Label, url: Url) {
  def short: Url = Url.raw(url).split(raw"https?://(?:www\.)?").drop(1).headOption.fold(url)(Url(_))
}
object Website {
  object Label extends StringWrapper
  type Label = Label.Type

  object Url extends StringWrapper
  type Url = Url.Type

  implicit final val encoder: Encoder[Website] = Encoder.instance[Website] { w =>
    Json.obj(
      "label" -> w.label.asJson,
      "url" -> w.url.asJson,
      "short" -> w.short.asJson
    )
  }
  implicit final val decoder: Decoder[Website] = Decoder.instance[Website] { cursor =>
    for {
      label <- cursor.downField("label").as[Label]
      url <- cursor.downField("url").as[Url]
    } yield Website(label, url)
  }
}

final case class Project(title: Title,
                         language: Language,
                         homepage: Website,
                         mirrors: List[Website],
                         bulletpoints: List[BulletPoint])
object Project {
  implicit final val encoder: Encoder[Project] = Encoder.instance[Project] { p =>
    Json.obj(
      "title" -> p.title.asJson,
      "language" -> p.language.asJson,
      "homepage" -> p.homepage.asJson,
      "mirrors" -> p.mirrors.asJson,
      "bulletpoints" -> p.bulletpoints.asJson
    )
  }
  implicit final val decoder: Decoder[Project] = Decoder.instance[Project] { cursor =>
    for {
      title <- cursor.downField("title").as[Title]
      language <- cursor.downField("language").as[Language]
      homepage <- cursor.downField("homepage").as[Website]
      mirrors <- cursor.downField("mirrors").as[Option[List[Website]]].map(_.getOrElse(Nil))
      bulletpoints <- cursor.downField("bulletpoints").as[List[BulletPoint]]
    } yield Project(title, language, homepage, mirrors, bulletpoints)
  }
}

final case class DateRange(start: DateStr, endOpt: Option[DateStr]) {
  val end: DateStr = endOpt.getOrElse(DateStr("Present"))
}
object DateRange {
  object DateStr extends StringWrapper
  type DateStr = DateStr.Type

  implicit final val encoder: Encoder[DateRange] = Encoder.instance[DateRange] { d =>
    Json.obj(
      "start" -> d.start.asJson,
      "end" -> d.end.asJson,
    )
  }
  implicit final val decoder: Decoder[DateRange] = Decoder.instance[DateRange] { cursor =>
    for {
      start <- cursor.downField("start").as[DateStr]
      endOpt <- cursor.downField("end").as[Option[DateStr]]
    } yield DateRange(start, endOpt)
  }
}