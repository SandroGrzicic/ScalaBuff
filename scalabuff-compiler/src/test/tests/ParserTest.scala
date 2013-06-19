package tests

import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers
import net.sandrogrzicic.scalabuff.compiler.{Parser, buffString}
import java.io._
import File.{separator => SEP}

/**
 * ScalaTest Parser test.
 * @author Sandro Gržičić
 */

class ParserTest extends FunSuite with ShouldMatchers {

	val protoFileFilter = new FileFilter {
		def accept(filtered: File) = filtered.getName.endsWith(".proto")
	}

	val parsedExtension = ".txt"
	val protoDir = new File("scalabuff-compiler" + SEP + "src" + SEP + "test" + SEP + "resources" + SEP + "proto" + SEP)
	val parsedDir = "scalabuff-compiler" + SEP + "src" + SEP + "test" + SEP + "resources" + SEP + "parsed" + SEP

	/*
	 * Iterate over all files with the protoExtension in the protoDir directory and
	 * make sure the Parser output equals the corresponding output file in the parsedDir directory.
	 */
	for (file <- protoDir.listFiles(protoFileFilter)) {
		val fileName = file.getName.stripSuffix(".proto").camelCase
		test(fileName) {
			val output = io.Source.fromFile(new File(parsedDir + fileName + parsedExtension))
			var parsed: String = null
			try {
				parsed = Parser(file).toString + "\n"
			} catch {
				case e: Throwable => parsed = e.getMessage
			}
			parsed should equal (output.mkString)
			output.close()
		}
	}

}
