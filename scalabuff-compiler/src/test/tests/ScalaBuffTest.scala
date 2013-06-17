package tests

import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers
import net.sandrogrzicic.scalabuff.compiler._
import net.sandrogrzicic.scalabuff.compiler.{Strings, ScalaBuff, ScalaClass}
import java.io.{PrintStream, ByteArrayOutputStream, File}
import File.{separator => SEP}

/**
 * ScalaBuff CLI runner test.
 * @author Sandro Gržičić
 */

class ScalaBuffTest extends FunSuite with ShouldMatchers {

	val NEWLINE = System.getProperty("line.separator")

	val parsedExtension = ".txt"
	val protoDir = "scalabuff-compiler" + SEP + "src" + SEP + "test" + SEP + "resources" + SEP + "proto" + SEP
	val multiProtoDir = "scalabuff-compiler" + SEP + "src" + SEP + "test" + SEP + "resources" + SEP + "multipleprototests" + SEP
	val parsedDir = "scalabuff-compiler" + SEP + "src" + SEP + "test" + SEP + "resources" + SEP + "parsed" + SEP
	val generatedDir = "scalabuff-compiler" + SEP + "src" + SEP + "test" + SEP + "resources" + SEP + "generated" + SEP

	val testProto = "simple"
	val testProtoParsed =
		io.Source.fromFile(new File(parsedDir + testProto + parsedExtension)).mkString
	val testProtoGenerated =
		io.Source.fromFile(new File(generatedDir + testProto.capitalize + ".scala")).mkString

  val testProtoMulti = "multi_one"

	/** The output stream used for testing program output. */
	val outputStream: ByteArrayOutputStream = new ByteArrayOutputStream()
	/** The print stream used for testing program output. */
	val printStream: PrintStream = new PrintStream(outputStream)


	test("apply: simple .proto file") {
		val scalaClass: ScalaClass = ScalaBuff(new File(protoDir + testProto + ".proto"))
		scalaClass.body should equal (testProtoGenerated)
		scalaClass.file should equal ("Simple")
		scalaClass.path should equal ("resources" + SEP + "generated" + SEP)
	}

	test("main: no arguments") {
		outputStream.reset()
		Console.withOut(printStream)({
			ScalaBuff.main(Array())
			outputStream.toString("utf-8") should equal (Strings.HELP + NEWLINE)
		})
	}

	test("main: simple .proto file without a specified output directory") {
		val resourcesDirectory = new File("scalabuff-compiler" + SEP + "resources")
		val resourcesGeneratedDirectory = new File("scalabuff-compiler" + SEP + "resources" + SEP + "generated")
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
			val outputFile = new File("resources" + SEP + "generated" + SEP + testProto.camelCase + ".scala")
			outputFile should be ('exists)
			outputFile.deleteOnExit()
			val outputFileSource = io.Source.fromFile(outputFile)
			outputFileSource.mkString should equal (testProtoGenerated)
			outputFileSource.close()
			outputFile.delete()
			new File("resources/generated").delete()
			new File("resources").delete()
		}
	}

	test("main: simple .proto file with a specified output directory") {
		val outputDirectory = "scalabuff-compiler" + SEP + "src" + SEP + "test"

		outputStream.reset()
		val simpleProto = protoDir + testProto + ".proto"
		Console.withOut(printStream)({
			ScalaBuff.main(Array("--scala_out=" + outputDirectory, simpleProto))
			outputStream.toString("utf-8") should be ('empty)
		})
		val outputFile = new File(outputDirectory + "" + SEP + "resources" + SEP + "generated" + SEP + testProto.camelCase + ".scala")
		outputFile should be ('exists)
		val outputFileSource = io.Source.fromFile(outputFile)
		outputFileSource.mkString should equal (testProtoGenerated)
		outputFileSource.close()
	}

	test("main: input directory only") {
		val outputDirectory = "scalabuff-compiler" + SEP + "src" + SEP + "test"
		val protoFiles = Seq("multi_one", "multi_two")

		outputStream.reset()
		Console.withOut(printStream)({
			ScalaBuff.main(Array("--scala_out=" + outputDirectory, "--proto_path=" + multiProtoDir, "--verbose"))
			outputStream.toString("utf-8").split("\n").size should be (2)
		})

		for(proto <- protoFiles) {
			val outputFile = new File(outputDirectory + "" + SEP + "resources" + SEP + "generated" + SEP + proto.camelCase + ".scala")
			outputFile should be ('exists)
			val outputFileSource = io.Source.fromFile(outputFile)
			val exampleProtoGenerated =
				io.Source.fromFile(new File(generatedDir + proto.camelCase + ".scala")).mkString
			outputFileSource.mkString should equal (exampleProtoGenerated)
			outputFileSource.close()
		}
	}

	test("main: multiple input directories, with file") {
		val outputDirectory = "scalabuff-compiler" + SEP + "src" + SEP + "test"

		outputStream.reset()
		Console.withOut(printStream)({
			ScalaBuff.main(Array("--scala_out=" + outputDirectory, 
				"--proto_path=" + parsedDir, // There's no proto files here, but we want to make sure multi_one.proto is found
				"--proto_path=" + multiProtoDir, 
				"--verbose",
				testProtoMulti + ".proto"))
			outputStream.toString("utf-8").split("\n").size should be (1)
		})

		val outputFile = new File(outputDirectory + "" + SEP + "resources" + SEP + "generated" + SEP + testProtoMulti.camelCase + ".scala")
		outputFile should be ('exists)
		val outputFileSource = io.Source.fromFile(outputFile)
		val exampleProtoGenerated =
			io.Source.fromFile(new File(generatedDir + testProtoMulti.camelCase + ".scala")).mkString
		outputFileSource.mkString should equal (exampleProtoGenerated)
		outputFileSource.close()
	}

	test("main: import across packages") {
		val outputDirectory = "scalabuff-compiler" + SEP + "src" + SEP + "test"

		def compile(filename: String, subFolder: Option[String]) {
			val protoFile = filename + ".proto"
			val scalaFile = filename.camelCase + ".scala"
			outputStream.reset()
			Console.withOut(printStream)({
				ScalaBuff.main(Array("--scala_out=" + outputDirectory, 
					"--proto_path=" + protoDir, 
					"--verbose",
					protoFile))
				outputStream.toString("utf-8").split("\n").size should be (1)
			})

			val outputFile = new File(outputDirectory + "" + SEP + "resources" + SEP + "generated" + SEP + subFolder.map(_ + SEP).getOrElse("") + scalaFile)
			outputFile should be ('exists)
			val outputFileSource = io.Source.fromFile(outputFile)
			val exampleProtoGenerated =
				io.Source.fromFile(new File(generatedDir + subFolder.map(_ + SEP).getOrElse("") + scalaFile)).mkString
			outputFileSource.mkString should equal (exampleProtoGenerated)
			outputFileSource.close()
		}

		compile("package_name", Some("nested"))
		compile("import_packages", None)
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
