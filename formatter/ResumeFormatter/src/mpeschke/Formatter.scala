package mpeschke

import cats.effect.std.Console
import cats.effect.{ExitCode, IO}
import com.github.jknack.handlebars.{Context, Handlebars}
import com.monovore.decline.Opts
import com.monovore.decline.effect.CommandIOApp
import io.circe.{Json, Printer}
import io.circe.syntax._
import mpeschke.CliArgs.OutputType
import mpeschke.handlebars.{CirceHelpers, CirceResolver}
import mpeschke.resume.Resume

object Formatter extends CommandIOApp(
  name = "Formatter",
  header = "Convert JSON resume into various formats"
){

  def read(path: os.Path): IO[String] =
    IO.blocking {
      val input = os.read.inputStream(path)
      try {
        new String(input.readAllBytes())
      } finally {
        input.close()
      }
    }

  def write(path: os.Path, contents: String): IO[ExitCode] = IO.blocking {
    val mungedContents = if(path.baseName.endsWith(".tex")) laTeXFixes(contents) else contents
    os.write.over(path, mungedContents, createFolders = true)
    ExitCode.Success
  }

  def laTeXFixes(input: String): String = {
    input
      .replaceAllLiterally(raw"\", raw"\t-e-x-t-b-a-c-k-s-l-a-s-h")
      .replaceAll(raw"([#$$%&_{}])", raw"\\\1")
      .replaceAllLiterally("^",raw"\textasciicircum{}")
      .replaceAllLiterally("~",raw"\textasciitilde{}")
      .replaceAll(raw"\t-e-x-t-b-a-c-k-s-l-a-s-h([^{])", raw"\textbackslash{}\1")
  }

  def render(json: Json, template: String): IO[String] =
    for {
      handlebars <- IO.delay(new Handlebars().registerHelpers(CirceHelpers))
      context    <- IO.delay(Context.newBuilder(json).resolver(CirceResolver).build())
      result     <- IO.delay(handlebars.compileInline(template)(context))
    } yield result

  override def main: Opts[IO[ExitCode]] = CliArgs.opts.map { cliArgs =>
    def print(s: String) = Console[IO].println(s)
    val source = read(cliArgs.source)
    def parseResume(jsonText: String) = IO.fromEither(io.circe.parser.parse(jsonText).flatMap(_.as[Resume]))
    def resumeToJson(resume: Resume) = IO.pure(resume.asJson)
    def prettyJson (json: Json) = IO.pure(Printer.spaces2.print(json))

    val loadResume = source.flatMap(parseResume).flatMap(resumeToJson)

    cliArgs.outputType match {
      case OutputType.Json(raw) =>
        if (raw) source.flatMap(print).as(ExitCode.Success)
        else loadResume.flatMap(prettyJson).flatMap(print).as(ExitCode.Success)

      case OutputType.Render(template, destOpt) =>
        val output = for {
          resume <- loadResume
          template <- read(template)
          rendered <- render(resume, template)
        } yield rendered

        val printOutput: IO[ExitCode] = output.flatMap(print).as(ExitCode.Success)
        destOpt.fold(printOutput) { dest =>
          output.flatMap(write(dest, _))
        }
    }
  }
}
