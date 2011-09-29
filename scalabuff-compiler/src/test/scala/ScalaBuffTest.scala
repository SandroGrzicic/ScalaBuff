package tests

import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers
import net.sandrogrzicic.scalabuff.compiler.{Strings, ScalaBuff}
import _root_.java.io.{PrintStream, ByteArrayOutputStream, File}

/**
 * ScalaBuff CLI runner tests.
 * @author Sandro Gržičić
 */

class ScalaBuffTest extends FunSuite with ShouldMatchers {

	val NEWLINE = System.getProperty("line.separator")

	val parsedExtension = ".txt"
	val protoDir = "scalabuff-compiler/src/test/resources/tests/proto/"
	val parsedDir = "scalabuff-compiler/src/test/resources/tests/parsed/"
	val generatedDir = "scalabuff-compiler/src/test/scala/tests/generated/"

	val testProto = "simple"
	val testProtoParsed =
		io.Source.fromFile(new File(parsedDir + testProto + parsedExtension)).mkString
	val testProtoGenerated =
		io.Source.fromFile(new File(generatedDir + testProto.capitalize + ".scala")).mkString

	/** The output stream used for testing program output. */
	val outputStream: ByteArrayOutputStream = new ByteArrayOutputStream()
	/** The print stream used for testing program output. */
	val printStream: PrintStream = new PrintStream(outputStream)


	test("apply: simple .proto file") {
		val scalaClass = ScalaBuff(protoDir + testProto + ".proto")
		scalaClass.body should equal (testProtoGenerated)
		scalaClass.file should equal ("Simple")
		scalaClass.path should equal ("tests/generated/")
	}

	test("main: no arguments") {
		outputStream.reset()
		Console.withOut(printStream)({
			ScalaBuff.main(Array())
			outputStream.toString("utf-8") should equal (Strings.HELP + NEWLINE)
		})
	}

	test("main: simple .proto file without a specified output directory") {
		val resourcesDirectory = new File("tests")
		val resourcesGeneratedDirectory = new File("tests/generated")
		// don't attempt to modify an existing root folder
		if (!(resourcesDirectory.exists() && resourcesDirectory.isDirectory ||
			resourcesGeneratedDirectory.exists() && resourcesGeneratedDirectory.isDirectory)
		) {
			outputStream.reset()
			val simpleProto = protoDir + testProto + ".proto"
			Console.withOut(printStream)({
				ScalaBuff.main(Array(simpleProto))
				outputStream.toString("utf-8") should be ('empty)
			})
			val outputFile = new File("tests/generated/" + testProto.capitalize + ".scala")
			outputFile should be ('exists)
			outputFile.deleteOnExit()
			val outputFileSource = io.Source.fromFile(outputFile)
			outputFileSource.mkString should equal (testProtoGenerated)
			outputFileSource.close()
			outputFile.delete()
			new File("tests/generated").delete()
			new File("tests").delete()
		}
	}

	test("main: simple .proto file with a specified output directory") {
		val outputDirectory = "scalabuff-compiler/src/test"

		outputStream.reset()
		val simpleProto = protoDir + testProto + ".proto"
		Console.withOut(printStream)({
			ScalaBuff.main(Array("--scala_out=" + outputDirectory, simpleProto))
			outputStream.toString("utf-8") should be ('empty)
		})
		val outputFile = new File(outputDirectory + "/resources/tests/generated/" + testProto.capitalize + ".scala")
		outputFile should be ('exists)
		val outputFileSource = io.Source.fromFile(outputFile)
		outputFileSource.mkString should equal (testProtoGenerated)
		outputFileSource.close()
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
