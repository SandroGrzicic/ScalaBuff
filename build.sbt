name := "ScalaBuff"

version := "0.8"

scalaVersion := "2.9.0-1"

resolvers += "Akka Maven Repository" at "http://akka.io/repository"

libraryDependencies += "org.scalatest" % "scalatest_2.9.0-1" % "1.6.1"

libraryDependencies += "com.google.protobuf" % "protobuf-java" % "2.4.1"

parallelExecution in GlobalScope := true

unmanagedBase <<= baseDirectory { base => base / "lib" }

scalacOptions ++= Seq("-encoding", "utf8", "-unchecked", "-deprecation")

javacOptions ++= Seq("-source", "1.6", "-encoding", "utf8")

mainClass in (Compile, run) := Some("hr.sandrogrzicic.scalabuff.ScalaBuff")

scalaSource in Compile <<= baseDirectory(_ / "src/main")

scalaSource in Test <<= baseDirectory(_ / "src/test")

classDirectory in Compile <<= baseDirectory(_ / "bin/main")

classDirectory in Test <<= baseDirectory(_ / "bin/test")

docDirectory in Compile <<= baseDirectory(_ / "doc")

logLevel := Level.Info
