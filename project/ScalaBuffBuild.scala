import sbt._
import Keys._
import java.io.File

object ScalaBuffBuild extends Build {

	lazy val buildSettings = Seq(
		version := "0.9",
		scalaVersion := "2.9.0-1",
		logLevel := Level.Info
	)

	override lazy val settings = super.settings ++ buildSettings

	lazy val defaultSettings = Defaults.defaultSettings ++ Seq(
		resolvers += "Akka Maven Repository" at "http://akka.io/repository",

		libraryDependencies += "org.scalatest" % "scalatest_2.9.0-1" % "1.6.1",
		libraryDependencies += "com.google.protobuf" % "protobuf-java" % "2.4.1",

		scalacOptions ++= Seq("-encoding", "utf8", "-unchecked", "-deprecation"),
		javacOptions ++= Seq("-encoding", "utf8", "-Xlint:unchecked", "-Xlint:deprecation"),

		parallelExecution in GlobalScope := true,

		scalaSource in Compile <<= baseDirectory(_ / "src/main"),
		scalaSource in Test <<= baseDirectory(_ / "src/test"),

		classDirectory in Compile <<= baseDirectory(_ / "bin/main"),
		classDirectory in Test <<= baseDirectory(_ / "bin/test"),

		docDirectory in Compile <<= baseDirectory(_ / "doc")
	)

	lazy val scalaBuff = Project(
		id = "ScalaBuff",
		base = file("."),
		dependencies = Seq(scalaBuffRuntime % "test->compile"),
		settings = defaultSettings
	)

	lazy val scalaBuffRuntime = Project(
		id = "ScalaBuffRuntime",
		base = file("scalabuff-runtime"),
		settings = defaultSettings
	)

}
