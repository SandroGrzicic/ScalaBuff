name := "ScalaBuff"

version := "0.7"

scalaVersion := "2.9.0-1"

libraryDependencies += "org.scalatest" % "scalatest_2.9.0-1" % "1.6.1"

scalacOptions ++= Seq("-encoding", "utf8", "-unchecked", "-deprecation")

javacOptions ++= Seq("-source", "1.6", "-encoding", "utf8")

mainClass in (Compile, run) := Some("hr.sandrogrzicic.scalabuff.ScalaBuff")
//mainClass in (Compile, run) := Some("hr.sandrogrzicic.scalabuff.test.UpdateTestResources")
//mainClass in (Compile, run) := Some("Test")

scalaSource in Compile <<= baseDirectory(_ / "src/main")

scalaSource in Test <<= baseDirectory(_ / "src/test")

classDirectory in Compile <<= baseDirectory(_ / "bin/main")

classDirectory in Test <<= baseDirectory(_ / "bin/test")

logLevel := Level.Info