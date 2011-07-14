package tests

import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers
import hr.sandrogrzicic.scalabuff.ScalaBuff
import java.io.File

/**
 * ScalaBuff CLI runner test.
 * @author Sandro Gržičić
 */

class ScalaBuffTest extends FunSuite with ShouldMatchers {

	val parsedExtension = ".txt"
	val protoDir = new File("src/test/resources/proto/")
	val parsedDir = "src/test/resources/parsed/"

	test("simple .proto file") {
		val output = io.Source.fromFile(new File(parsedDir + "simple" + parsedExtension)).mkString
		ScalaBuff("src/test/resources/proto/simple.proto") should equal (output)
	}

}