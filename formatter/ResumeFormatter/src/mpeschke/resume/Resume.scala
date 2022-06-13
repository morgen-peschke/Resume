package mpeschke.resume

import io.circe.syntax._
import io.circe.{Decoder, Encoder, Json}
import mpeschke.resume.Resume.Name

final case class Resume(
                       name: Name,
                       contact: ContactInfo,
                       skills: Skills,
                       projects: List[Project],
                       jobs: List[Job],
                       education: List[Education]
                       )
object Resume {
  object Name extends StringWrapper
  type Name = Name.Type

  implicit final val encoder: Encoder[Resume] = Encoder.instance[Resume] { r =>
    Json.obj(
      "name" -> r.name.asJson,
      "contact" -> r.contact.asJson,
      "skills" -> r.skills.asJson,
      "projects" -> r.projects.asJson,
      "jobs" -> r.jobs.asJson,
      "education" -> r.education.asJson
    )
  }
  implicit final val decoder: Decoder[Resume] = Decoder.instance[Resume] { cursor =>
    for {
      name <- cursor.downField("name").as[Name]
      contact <- cursor.downField("contact").as[ContactInfo]
      skills <- cursor.downField("skills").as[Skills]
      projects <- cursor.downField("projects").as[List[Project]]
      jobs <- cursor.downField("jobs").as[List[Job]]
      education <- cursor.downField("education").as[List[Education]]
    } yield Resume(name, contact, skills, projects, jobs, education)
  }
}