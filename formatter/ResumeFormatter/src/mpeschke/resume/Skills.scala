package mpeschke.resume

import cats.syntax.option._
import io.circe.syntax._
import io.circe.{Decoder, Encoder, Json}
import mpeschke.resume.Skills.{Concept, ConceptList, Framework, FrameworkList}

final case class Skills(concepts: ConceptList,
                        languages: LanguageList,
                        frameworks: FrameworkList)

object Skills {
  object Framework extends StringWrapper
  type Framework = Framework.Type

  object FrameworkList extends ListWrapper[Framework](none)
  type FrameworkList = FrameworkList.Type

  object Concept extends StringWrapper
  type Concept = Concept.Type

  object ConceptLines extends ListWrapper[Concept](48.some)
  type ConceptList = ConceptLines.Type

  implicit final val encoder: Encoder[Skills] = Encoder.instance[Skills] { s =>
    Json.obj(
      "concepts" -> s.concepts.asJson,
      "languages" -> s.languages.asJson,
      "frameworks" -> s.frameworks.asJson
    )
  }
  implicit final val decoder: Decoder[Skills] = Decoder.instance[Skills] { cursor =>
    for {
      concepts <- cursor.downField("concepts").as[ConceptList]
      languages <- cursor.downField("languages").as[LanguageList]
      frameworks <- cursor.downField("frameworks").as[FrameworkList]
    } yield Skills(concepts, languages, frameworks)
  }
}