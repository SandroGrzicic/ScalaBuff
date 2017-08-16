package tests

import org.scalatest.{FunSuite, Matchers}
import net.sandrogrzicic.scalabuff.compiler.{Parser, buffString}
import java.io._
import File.{separator => /}

/**
 * ScalaTest Parser test.
 * @author Sandro Gržičić
 */

class ParserTest extends FunSuite with Matchers {

	val protoFileFilter = new FileFilter {
		def accept(filtered: File) = filtered.getName.endsWith(".proto")
	}

	val parsedExtension = ".txt"
	val protoDir = new File("scalabuff-compiler" + / + "src" + / + "test" + / + "resources" + / + "proto" + /)
	val parsedDir = "scalabuff-compiler" + / + "src" + / + "test" + / + "resources" + / + "parsed" + /

	/*
	 * Iterate over all files with the protoExtension in the protoDir directory and
	 * make sure the Parser output equals the corresponding output file in the parsedDir directory.
	 */
	for (file <- protoDir.listFiles(protoFileFilter)) {
		val fileName = file.getName.stripSuffix(".proto")
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
