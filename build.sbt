import play.api.libs.json._
import org.scalajs.core.tools.linker.standard._

name := "atom-ide-scala"
organization := "laughedelic"
description := "Scala & Dotty language support for Atom IDE"

homepage := Some(url(s"https://github.com/${organization.value}/${name.value}"))
scmInfo in ThisBuild := Some(ScmInfo(
  homepage.value.get,
  s"scm:git:git@github.com:${organization.value}/${name.value}.git"
))

licenses := Seq(
  "MIT" -> url("https://opensource.org/licenses/MIT")
)
developers := List(Developer(
  "laughedelic",
  "Alexey Alekhin",
  "laughedelic@gmail.com",
  url("https://github.com/laughedelic")
))

scalaVersion := "2.12.4"
scalacOptions ++= Seq(
  "-encoding", "utf8",
  "-feature",
  "-language:postfixOps",
  "-language:implicitConversions",
  "-language:higherKinds",
  "-unchecked",
  "-deprecation",
  "-Yrangepos",
  "-Ywarn-unused-import",
  "-P:scalajs:sjsDefinedByDefault"
)

enablePlugins(AtomPackage)

resolvers += Resolver.jcenterRepo
resolvers += Resolver.bintrayRepo("laughedelic", "maven")

libraryDependencies ++= Seq(
  "io.scalajs" %%% "nodejs" % "0.4.2",
  "laughedelic" %%% "scalajs-atom-api" % "0.6.0+7-0c17704c",
)

scalaJSLinkerConfig ~= { _.withSourceMap(true) }

lazy val getCoursier = taskKey[File]("Get coursier binary if missing")
getCoursier := {
  import sys.process._
  val log = streams.value.log
  val file = baseDirectory.value / "coursier"
  if (!file.exists) {
    log.info("Downloading coursier...")
    val exitCode = (new java.net.URL("https://git.io/vgvpD") #> file).!(log)
    if (exitCode != 0) sys.error("Couldn't download Coursier")
  }
  file
}

apmKeywords := Seq(
  "scala",
  "scalameta",
  "ide",
  "atom-ide",
  "lsp",
  "language-server",
  "language-server-protocol",
  "metals",
  "dotty",
  "ensime",
)

apmEngines := Map("atom" -> ">=1.21.0 <2.0.0")

apmDependencies := Map(
  "atom-languageclient" -> "0.9.5",
  "atom-package-deps" -> "4.6.1",
  "@atom/source-map-support" -> "0.3.4",
  "s-expression" -> "3.0.3",
)

apmConsumedServices := Map(
  "linter-indie"          -> Map("2.0.0" -> "consumeLinterV2"),
  "datatip"               -> Map("0.1.0" -> "consumeDatatip"),
  "atom-ide-busy-signal"  -> Map("0.1.0" -> "consumeBusySignal"),
  "signature-help"        -> Map("0.1.0" -> "consumeSignatureHelp"),
  "console"               -> Map("0.1.0" -> "consumeConsole"),
)

apmProvidedServices := Map(
  "definitions"           -> Map("0.1.0" -> "provideDefinitions"),
  "code-highlight"        -> Map("0.1.0" -> "provideCodeHighlight"),
  "code-actions"          -> Map("0.1.0" -> "provideCodeActions"),
  "hyperclick"            -> Map("0.1.0" -> "provideHyperclick"),
  "find-references"       -> Map("0.1.0" -> "provideFindReferences"),
  "autocomplete.provider" -> Map("2.0.0" -> "provideAutocomplete"),
  "code-format.range"     -> Map("0.1.0" -> "provideCodeFormat"),
  "outline-view"          -> Map("0.1.0" -> "provideOutlines")
)

apmJsonExtra := Json.obj(
  "enhancedScopes" -> Json.arr(
    "source.scala"
  ),
  "package-deps" -> Json.arr(
    "language-scala",
    "atom-ide-ui"
  ),
  "activationHooks" -> Json.arr(
    "language-scala:grammar-used"
  )
)

apmExtraFiles ++= Seq(
  getCoursier.value
)

// Main task that packages this Atom plugin
sbt.Keys.`package` in Compile :=
  packageBin.in(Compile)
    .dependsOn(getCoursier)
    .value

ghreleaseAssets := Seq(
  (artifactPath in (Compile, fullOptJS)).value,
  packageJson.value
)
