import sbt._
import java.io.File

class ScalaBuffProject(info: ProjectInfo) extends DefaultProject(info) {

	override def compileOptions = super.compileOptions ++ compileOptions("-encoding", "utf8")
	override def javaCompileOptions = super.javaCompileOptions ++ javaCompileOptions("-source", "1.6") ++ javaCompileOptions("-encoding", "utf8")

	override def outputDirectoryName = "bin"

	override def mainScalaSourcePath = "src" / "main"
	override def mainResourcesPath = "src" / "resources"

	override def testScalaSourcePath = "src" / "test"
	override def testResourcesPath = "src" / "test" / "resources"

	override def mainDocPath = "doc"
	override def testDocPath = "doc" / "test"

	override def mainCompilePath = "bin"
	override def testCompilePath = "bin" / "test"

	override def dependencyPath = "lib"

	override def mainClass = Some("hr.sandrogrzicic.protobuf.ScalaBuff")

	val scalatest = "org.scalatest" % "scalatest_2.9.0" % "1.6.1"


}