package mpeschke.resume

import cats.syntax.option._
import mpeschke.resume.DateRange.DateStr
import mpeschke.resume.Education.{CourseList, School}
import io.circe.syntax._
import io.circe.{Decoder, Encoder, Json}

final case class Education(title: Title,
                           source: School,
                           date: DateStr,
                           location: Address,
                           courses: CourseList,
                           projects: List[Website]
                          )
object Education {
  object School extends StringWrapper
  type School = School.Type

  object Course extends StringWrapper
  type Course = Course.Type

  object CourseList extends ListWrapper[Course](40.some)
  type CourseList = CourseList.Type

  implicit final val encoder: Encoder[Education] = Encoder.instance[Education] { e =>
    Json.obj(
      "title" -> e.title.asJson,
      "source" -> e.source.asJson,
      "date" -> e.date.asJson,
      "location" -> e.location.asJson,
      "courses" -> e.courses.asJson,
      "projects" -> e.projects.asJson
    )
  }
  implicit final val decoder: Decoder[Education] = Decoder.instance[Education] { cursor =>
    for {
      title <- cursor.downField("title").as[Title]
      source <- cursor.downField("source").as[School]
      date <- cursor.downField("date").as[DateStr]
      location <- cursor.downField("location").as[Address]
      courses <- cursor.downField("courses").as[CourseList]
      projects <- cursor.downField("projects").as[List[Website]]
    } yield Education(title, source, date, location, courses, projects)
  }
}