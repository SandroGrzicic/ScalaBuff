import sbt._
import Keys._
import java.io.File

/**
 * ScalaBuff SBT build file.
 *
 * Useful SBT commands:
 *
 *      run (arguments)             Runs ScalaBuff inside SBT with the specified arguments.
 *      test                        Runs the tests.
 *      package                     Generates the main ScalaBuff compiler .JAR.
 *      update-test-resources       Regenerates the test resources using ScalaBuff.
 *
 *      project ScalaBuff           Switches to the main project (default).
 *      project ScalaBuffRuntime    Switches to the Runtime sub-project.
 *
 */
object ScalaBuffBuild extends Build {

	lazy val buildSettings = Defaults.defaultSettings ++ Seq[Setting[_]](
    organization := "net.sandrogrzicic.scalabuff",
		version := "0.10-SNAPSHOT",
	)

//	override lazy val settings = Defaults.defaultSettings ++ buildSettings

	lazy val defaultSettings =  buildSettings ++ Seq[Setting[_]](
    scalaVersion := "2.9.1",

		resolvers += "Akka Maven Repository" at "http://akka.io/repository",

		libraryDependencies += "org.scalatest" % "scalatest_2.9.1" % "1.6.1",
		libraryDependencies += "com.google.protobuf" % "protobuf-java" % "2.4.1",

		scalacOptions ++= Seq("-encoding", "utf8", "-unchecked", "-deprecation"),
		javacOptions ++= Seq("-encoding", "utf8", "-Xlint:unchecked", "-Xlint:deprecation"),

		parallelExecution in GlobalScope := true,

    compileOrder := CompileOrder.JavaThenScala
	)

  lazy val root = Project(
    id = "root",
    base = file("."),
    settings = buildSettings,
    aggregate = Seq(scalaBuffCompiler, scalaBuffRuntime, scalaBuffProtocCompat)
  )


	lazy val scalaBuffCompiler = Project(
		id = "compiler",
		base = file("scalabuff-compiler"),
		dependencies = Seq(scalaBuffRuntime % "test->compile"),
		settings = defaultSettings ++ Seq(
			mainClass in (Compile, run) := Some("net.sandrogrzicic.scalabuff.compiler.ScalaBuff"),
      fullRunTask(TaskKey[Unit]("update-test-resources"), Compile, "net.sandrogrzicic.scalabuff.compiler.tests.UpdateTestResources")
		)
	)

	lazy val scalaBuffRuntime = Project(
		id = "runtime",
		base = file("scalabuff-runtime"),
		settings = defaultSettings
	)

  lazy val scalaBuffProtocCompat = Project(
    id = "protoc-compat",
    base = file("scalabuff-protoc-compat"),
    settings = defaultSettings ++ Seq[Setting[_]](

    )
  )

}
