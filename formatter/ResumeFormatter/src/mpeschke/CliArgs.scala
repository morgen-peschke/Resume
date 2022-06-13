package mpeschke

import cats.data.{NonEmptyList, Validated}
import cats.syntax.applicative._
import cats.syntax.apply._
import com.monovore.decline.{Argument, Opts}
import mpeschke.CliArgs.OutputType
import os.Path

final case class CliArgs(source: os.Path, outputType: OutputType)
object CliArgs {
  implicit final val pathArgument: Argument[os.Path] =
    Argument.from("path") { raw =>
      Validated
        .catchNonFatal[os.Path] {
          os.Path.expandUser(raw, os.pwd)
        }
        .leftMap(_.getMessage.pure[NonEmptyList])
    }

  sealed abstract class OutputType extends Product with Serializable {
    def upcast: OutputType = this
  }
  object OutputType {
    final case class Json(raw: Boolean) extends OutputType
    object Json {
      val opts: Opts[OutputType] = {
        val flagOpt = Opts.flag(long = "json", help = "Output JSON, useful for debugging the helper")
        val rawOpt = Opts.flag(long = "raw", help = "Do not annotate the JSON before outputting").orFalse

        (flagOpt, rawOpt).mapN((_, annotate) => Json(annotate).upcast)
      }
    }

    final case class Render(template: os.Path, output: Option[os.Path]) extends OutputType
    object Render {
      val opts: Opts[OutputType] = {
        val template =
          Opts.option[os.Path](long = "render", help = "Render using a handlebars template")

        val output =
          Opts.option[os.Path](long = "to", help = "Output to a file, otherwise goes to stdout")
            .orNone

        (template, output).mapN(Render(_, _).upcast)
      }
    }

    val opts: Opts[OutputType] = Json.opts.orElse(Render.opts)
  }

  val opts: Opts[CliArgs] = {
    val sourceOpt: Opts[Path] =
      Opts.option[os.Path](long = "source", help = "Path to JSON resume")

    (sourceOpt, OutputType.opts).mapN(CliArgs(_, _))
  }
}
