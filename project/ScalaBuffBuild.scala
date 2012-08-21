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
 *      project compiler            Switches to the compiler project (default).
 *      project runtime             Switches to the runtime project.
 *
 */
object ScalaBuffBuild extends Build {

	lazy val buildSettings = Seq(
		name := "ScalaBuff",
		organization := "net.sandrogrzicic",
		version := "1.0.0",
		scalaVersion := "2.9.2",
		logLevel := Level.Info
	)

	override lazy val settings = super.settings ++ buildSettings

	lazy val defaultSettings = Defaults.defaultSettings ++ Seq(

		resolvers ++= Seq(
			"Akka Maven Repository" at "http://akka.io/repository",
			"Typesafe Maven Repository" at "http://repo.typesafe.com/typesafe/releases/"
		),
		
		libraryDependencies ++= Seq(
			"org.scalatest" %% "scalatest" % "1.8" % "test",
			"com.google.protobuf" % "protobuf-java" % "2.4.1"
		),

		crossScalaVersions ++= Seq("2.10.0-M6"), // doesn't work yet because of no 2.10 ScalaTest

		scalacOptions ++= Seq("-encoding", "utf8", "-unchecked", "-deprecation"),
		javacOptions ++= Seq("-encoding", "utf8", "-Xlint:unchecked", "-Xlint:deprecation"),

		parallelExecution in GlobalScope := true,

		scalaSource in Compile <<= baseDirectory(_ / "src/main"),
		scalaSource in Test <<= baseDirectory(_ / "src/test"),

		javaSource in Compile <<= baseDirectory(_ / "src/main"),
		javaSource in Test <<= baseDirectory(_ / "src/test"),

		classDirectory in Compile <<= baseDirectory(_ / "bin/main"),
		classDirectory in Test <<= baseDirectory(_ / "bin/test"),

		docDirectory in Compile <<= baseDirectory(_ / "doc"),

		compileOrder := CompileOrder.JavaThenScala,
		
		credentials += Credentials(Path.userHome / ".ivy2" / ".credentials")
//		publishTo <<= (version) { version: String =>
//			val nexus = "http://nexus.scala-tools.org/content/repositories/"
//			if (version.trim.endsWith("SNAPSHOT")) Some("snapshots" at nexus + "snapshots/")
//			else                                   Some("releases"  at nexus + "releases/")
//		}
	)

	lazy val compiler = Project(
		id = "compiler",
		base = file("compiler"),
		dependencies = Seq(runtime % "test->compile"),
		settings = defaultSettings ++ Seq(
			mainClass in (Compile, run) := Some("net.sandrogrzicic.scalabuff.compiler.ScalaBuff"),
			fullRunTask(TaskKey[Unit]("update-test-resources"), Compile, "net.sandrogrzicic.scalabuff.test.UpdateTestResources")
		)
	)

	lazy val runtime = Project(
		id = "runtime",
		base = file("runtime"),
		settings = defaultSettings
	)

}
