package tests

import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers
import hr.sandrogrzicic.scalabuff.{Strings, ScalaBuff}
import java.io.{PrintStream, ByteArrayOutputStream, File, OutputStream}

/**
 * ScalaBuff CLI runner test.
 * @author Sandro Gržičić
 */

class ScalaBuffTest extends FunSuite with ShouldMatchers {

	val NEWLINE = System.getProperty("line.separator")

	val parsedExtension = ".txt"
	val protoDir = "src/test/resources/proto/"
	val parsedDir = "src/test/resources/parsed/"

	val testProto = "simple"
	val testProtoOutput =
		io.Source.fromFile(new File(parsedDir + testProto + parsedExtension)).mkString

	/** The output stream used for testing program output. */
	val outputStream: ByteArrayOutputStream = new ByteArrayOutputStream()
	/** The print stream used for testing program output. */
	val printStream: PrintStream = new PrintStream(outputStream)

	test("camelCase") {
		ScalaBuff.camelCase("very_long_name_in_c_style_001") should equal ("VeryLongNameInCStyle001")
	}

	test("apply: simple .proto file") {
		ScalaBuff(protoDir + testProto + ".proto") should equal (testProtoOutput)
	}

	test("main: no arguments") {
		outputStream.reset()
		Console.withOut(printStream)({
			ScalaBuff.main(Array())
			outputStream.toString("utf-8") should equal (Strings.HELP + NEWLINE)
		})
	}

	test("main: simple .proto file without a specified output directory") {
		outputStream.reset()
		val simpleProto = protoDir + testProto + ".proto"
		Console.withOut(printStream)({
			ScalaBuff.main(Array(simpleProto))
			outputStream.toString("utf-8") should be ('empty)
		})
		val outputFile = new File(testProto + ".scala")
		outputFile should be ('exists)
		outputFile.deleteOnExit()
		val outputFileSource = io.Source.fromFile(outputFile)
		outputFileSource.mkString should equal (testProtoOutput)
		outputFileSource.close()
		outputFile.delete()
	}

	test("main: simple .proto file with a specified output directory") {
		val outputDirectory = "src/test"

		outputStream.reset()
		val simpleProto = protoDir + testProto + ".proto"
		Console.withOut(printStream)({
			ScalaBuff.main(Array("--scala_out=" + outputDirectory, simpleProto))
			outputStream.toString("utf-8") should be ('empty)
		})
		val outputFile = new File(outputDirectory + "/" + testProto + ".scala")
		outputFile should be ('exists)
		outputFile.deleteOnExit()
		val outputFileSource = io.Source.fromFile(outputFile)
		outputFileSource.mkString should equal (testProtoOutput)
		outputFileSource.close()
		outputFile.delete()
	}

	test("main: unknown option") {
		outputStream.reset()
		Console.withOut(printStream)({
			val unsupportedOption = "--unsupported-option"
			ScalaBuff.main(Array(unsupportedOption))
			outputStream.toString("utf-8") should be
				(Strings.UNKNOWN_ARGUMENT + unsupportedOption + NEWLINE)
		})
	}

	test("main: invalid output directory") {
		outputStream.reset()
		Console.withOut(printStream)({
			val invalidOutputDirectory = "()/%$#:;"
			ScalaBuff.main(Array("--scala_out=" + invalidOutputDirectory))
			outputStream.toString("utf-8") should be
				(Strings.INVALID_OUTPUT_DIRECTORY + invalidOutputDirectory + NEWLINE)
		})
	}


}