package tests

import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers
import net.sandrogrzicic.scalabuff.compiler.Parser
import java.io._

/**
 * ScalaTest Parser test.
 * @author Sandro Gržičić
 */

class ParserTest extends FunSuite with ShouldMatchers {

	val protoFileFilter = new FileFilter {
		def accept(filtered: File) = filtered.getName.endsWith(".proto")
	}

	val parsedExtension = ".txt"
	val protoDir = new File("src/test/resources/proto/")
	val parsedDir = "src/test/resources/parsed/"

	/*
	 * Iterate over all files with the protoExtension in the protoDir directory and
	 * make sure the Parser output equals the corresponding output file in the parsedDir directory.
	 */
	for (file <- protoDir.listFiles(protoFileFilter)) {
		val fileName = file.getName.stripSuffix(".proto")
		test(fileName) {
			val output = io.Source.fromFile(new File(parsedDir + fileName + parsedExtension)).mkString
			var parsed: String = null
			try {
				parsed = Parser(file).toString
			} catch {
				case e => parsed = e.getMessage
			}
			parsed should equal (output)
		}
	}

}
