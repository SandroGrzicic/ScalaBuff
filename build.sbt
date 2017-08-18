import sbt._
import sbt.Keys._
import ReleaseTransformations._

/**
 * ScalaBuff SBT build file.
 *
 * Useful SBT commands:
 *
 * run (arguments)             Runs ScalaBuff inside SBT with the specified arguments.
 * test                        Runs the tests.
 * package                     Generates the main ScalaBuff compiler .JAR.
 * update-test-resources       Regenerates the test resources using ScalaBuff.
 *
 * project scalabuffCompiler  Switches to the compiler project (default).
 * project scalabuffRuntime   Switches to the runtime project.
 *
 */

lazy val buildSettings = Seq(
  name := "ScalaBuff",
  organization := "net.sandrogrzicic",
  version := "1.4.0",
  scalaVersion := "2.11.4",
  logLevel := Level.Info
)

lazy val releaseSettings = Seq(
  releaseCrossBuild := true,
  releaseProcess := Seq[ReleaseStep](
    checkSnapshotDependencies,
    inquireVersions,
    runClean,
    runTest,
    setReleaseVersion,
    commitReleaseVersion,
    tagRelease,
    releaseStepCommand("publishSigned"),
    setNextVersion,
    commitNextVersion,
    releaseStepCommand("sonatypeReleaseAll"),
    pushChanges
  )
)

lazy val defaultSettings = Seq(

  scalaVersion := "2.12.3",
  organization := "uk.me.sandro",
  startYear := Option(2011),
//  credentials += Credentials(Path.userHome / ".sbt" / ".credentials"),

  resolvers ++= Seq(
    "Akka Maven Repository" at "http://akka.io/repository",
    "Typesafe Maven Repository" at "http://repo.typesafe.com/typesafe/releases/",
    "Sonatype OSS Repository" at "https://oss.sonatype.org/content/groups/public/"
  ),

  libraryDependencies ++= Dependencies.common ++ (
    // TODO put this in a separate value
     CrossVersion.partialVersion(scalaVersion.value) match {
      case Some((2, scalaMajor)) if scalaMajor >= 11 =>
        Seq("org.scala-lang.modules" %% "scala-parser-combinators" % "1.0.6")
      case _ =>
        Seq()
    }
  ),

  crossScalaVersions ++= Seq("2.10.6", "2.11.11", "2.12.3"),

  scalacOptions ++= Seq(
    "-encoding", "utf8", "-unchecked", "-deprecation", "-feature",
    "-Xlog-reflective-calls"
  ) ++ (CrossVersion.partialVersion(scalaVersion.value) match {
    case Some((2, scalaMajor)) if scalaMajor >= 12 =>
      Seq("-Xlint:-unused,-missing-interpolator,_", "-Ywarn-unused:-imports")
    case _ =>
      Seq()
  }),
  javacOptions ++= Seq("-encoding", "utf8", "-Xlint:unchecked", "-Xlint:deprecation"),

  parallelExecution in GlobalScope := true,

  scalaSource in Compile := baseDirectory(_ / "src/main").value,
  scalaSource in Test := baseDirectory(_ / "src/test").value,

  javaSource in Compile := baseDirectory(_ / "src/main").value,
  javaSource in Test := baseDirectory(_ / "src/test").value,

  compileOrder := CompileOrder.Mixed,

  credentials += Credentials(Path.userHome / ".ivy2" / ".credentials")
)

lazy val updateTestResourcesTask = fullRunTask(TaskKey[Unit]("update-test-resources"), Compile, "net.sandrogrzicic.scalabuff.test.UpdateTestResources")

lazy val compilerProjectSettings = Seq(
  mainClass in(Compile, run) := Some("net.sandrogrzicic.scalabuff.compiler.ScalaBuff"),
  mainClass in(Compile, packageBin) := Some("net.sandrogrzicic.scalabuff.compiler.ScalaBuff"),
  updateTestResourcesTask
)

lazy val scalabuffCompiler = project.in(file("scalabuff-compiler"))
  .settings(defaultSettings)
  .settings(compilerProjectSettings)
  .dependsOn(scalabuffRuntime % Test)

lazy val scalabuffRuntime = project.in(file("scalabuff-runtime"))
  .settings(defaultSettings)

// load the Compiler project at sbt startup
onLoad in Global := (Command.process("project scalabuffCompiler", _: State)) compose (onLoad in Global).value
