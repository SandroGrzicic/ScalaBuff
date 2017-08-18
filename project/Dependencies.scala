import sbt._

object Dependencies {

  lazy val common = Seq(
    "com.google.protobuf" % "protobuf-java" % "2.5.0",
    "org.scalatest" %% "scalatest" % "3.0.1" % "test"
  )

}
