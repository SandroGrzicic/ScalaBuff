package parser

import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers
import hr.sandrogrzicic.scalabuff.Parser
import java.io._

/**
 * ScalaTest Parser test.
 * @author Sandro Gržičić
 */

class ParserTest extends FunSuite with ShouldMatchers {

	val protoExtension = ".proto"
	val parsedExtension = ".txt"

	val protoFileFilter = new FileFilter {
		def accept(filtered: File) = filtered.getName.endsWith(protoExtension)
	}

	val protoDir = new File("src/test/resources/proto/")
	val parsedDir = "src/test/resources/parsed/"

	/*
	 * Iterate over all files with the protoExtension in the protoDir directory and
	 * make sure the Parser output equals the corresponding output file in the parsedDir directory.
	 */
	for (file <- protoDir.listFiles(protoFileFilter)) {
		val fileName = file.getName.stripSuffix(protoExtension)
		test(fileName) {
			val output = io.Source.fromFile(new File(parsedDir + fileName + parsedExtension)).mkString
			Parser(file).toString should equal (output)
		}
	}

}