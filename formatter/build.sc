import mill.scalalib.scalafmt.ScalafmtModule
import mill.scalalib._
import mill._

object ResumeFormatter extends ScalaModule with ScalafmtModule {
  def scalaVersion = "2.12.16"

  override def ivyDeps = Agg(
    ivy"com.lihaoyi::os-lib:0.2.7",
    ivy"org.typelevel::cats-effect:3.2.9",
    ivy"org.rudogma::supertagged:2.0-RC2",
    ivy"io.circe::circe-core:0.14.1",
    ivy"io.circe::circe-parser:0.14.1",
    ivy"com.monovore::decline:2.2.0",
    ivy"com.monovore::decline-effect:2.2.0",
    ivy"com.beachape::enumeratum:1.7.0",
    ivy"com.github.jknack:handlebars:4.3.0"
  )
}