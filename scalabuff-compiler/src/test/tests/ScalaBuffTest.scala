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

  val outputDir = "scalabuff-compiler" + SEP + "src" + SEP + "test" + SEP

  val protoDir = outputDir + "resources" + SEP + "proto" + SEP
  val multiProtoDir = outputDir + "resources" + SEP + "multipleprototests" + SEP
  val parsedDir = outputDir + "resources" + SEP + "parsed" + SEP
  val resourcesGeneratedDir = "resources" + SEP + "generated" + SEP
  val generatedDir = outputDir + resourcesGeneratedDir

  val testProto = "simple"
  val testProtoParsed = io.Source.fromFile(new File(parsedDir + testProto + parsedExtension)).mkString
  val testProtoGenerated = io.Source.fromFile(new File(generatedDir + testProto.capitalize + ".scala")).mkString

  val testProtoMulti = "multi_one"

  test("apply: simple .proto file") {
    val settings = ScalaBuff.Settings(generateJsonMethod = true)
    val scalaClass: ScalaClass = ScalaBuff(new File(protoDir + testProto + ".proto"))(settings)
    scalaClass.body should equal(testProtoGenerated)
    scalaClass.file should equal("Simple")
    scalaClass.path should equal(resourcesGeneratedDir)
  }

  test("run: no arguments") {
    val outputStream = new ByteArrayOutputStream()
    Console.withOut(new PrintStream(outputStream)) {
      ScalaBuff.run(Array())
      outputStream.toString("utf-8") should equal(Strings.HELP + NEWLINE)
    }
  }

  test("run: simple .proto file without a specified output directory") {
    val resourcesDirectory = new File("scalabuff-compiler" + SEP + "resources")
    val resourcesGeneratedDirectory = new File("scalabuff-compiler" + SEP + resourcesGeneratedDir)
    // don't attempt to modify an existing root folder
    if (!(resourcesDirectory.exists() && resourcesDirectory.isDirectory ||
      resourcesGeneratedDirectory.exists() && resourcesGeneratedDirectory.isDirectory)
    ) {
      val outputStream = new ByteArrayOutputStream()
      val simpleProto = protoDir + testProto + ".proto"
      Console.withOut(new PrintStream(outputStream)) {
        ScalaBuff.run(Array("--generate_json_method", simpleProto))
        outputStream.toString("utf-8") should be('empty)
      }
      val outputFile = new File(resourcesGeneratedDir + testProto.camelCase + ".scala")
      outputFile should be('exists)
      outputFile.deleteOnExit()
      val outputFileSource = io.Source.fromFile(outputFile)
      outputFileSource.mkString should equal(testProtoGenerated)
      outputFileSource.close()
      outputFile.delete()
      new File("resources" + SEP + "generated").delete()
      new File("resources").delete()
    }
  }

  test("run: simple .proto file with a specified output directory") {
    val outputStream = new ByteArrayOutputStream()
    val simpleProto = protoDir + testProto + ".proto"
    Console.withOut(new PrintStream(outputStream)) {
      ScalaBuff.run(Array("-v", "-v", "--generate_json_method", "--scala_out=" + outputDir, simpleProto))
      val output = outputStream.toString.split("\r\n")
      output.length should be (3)
      output(1) should startWith("Processing: ")
    }
    val outputFile = new File(outputDir + SEP + resourcesGeneratedDir + testProto.camelCase + ".scala")
    outputFile should be('exists)
    val outputFileSource = io.Source.fromFile(outputFile)
    outputFileSource.mkString should equal(testProtoGenerated)
    outputFileSource.close()
  }

  test("run: input directory only") {
    val protoFiles = Seq("multi_one", "multi_two")

    val outputStream = new ByteArrayOutputStream()
    Console.withOut(new PrintStream(outputStream)) {
      ScalaBuff.run(Array("-v", "-v", "--scala_out=" + outputDir, "--proto_path=" + multiProtoDir))
      outputStream.toString("utf-8").split("\n").size should be(2)
    }

    for (proto <- protoFiles) {
      val outputFile = new File(outputDir + SEP + resourcesGeneratedDir + proto.camelCase + ".scala")
      outputFile should be('exists)
      val outputFileSource = io.Source.fromFile(outputFile)
      val exampleProtoGenerated = io.Source.fromFile(new File(generatedDir + proto.camelCase + ".scala")).mkString
      outputFileSource.mkString should equal(exampleProtoGenerated)
      outputFileSource.close()
    }
  }

  test("run: multiple input directories, with file") {
    val outputStream = new ByteArrayOutputStream()
    Console.withOut(new PrintStream(outputStream)) {
      ScalaBuff.run(Array("--scala_out=" + outputDir,
        "--proto_path=" + parsedDir, // no proto files here, but we want to make sure multi_one.proto is found
        "--proto_path=" + multiProtoDir,
        "--verbose",
        testProtoMulti + ".proto"))
      outputStream.toString("utf-8").split("\n").size should be(1)
    }

    val outputFile = new File(outputDir + SEP + resourcesGeneratedDir + testProtoMulti.camelCase + ".scala")
    outputFile should be('exists)
    val outputFileSource = io.Source.fromFile(outputFile)
    val exampleProtoGenerated = io.Source.fromFile(new File(generatedDir + testProtoMulti.camelCase + ".scala")).mkString
    outputFileSource.mkString should equal(exampleProtoGenerated)
    outputFileSource.close()
  }

  test("run: import across packages") {
    def compile(filename: String, subFolder: Option[String]) {
      val protoFile = filename + ".proto"
      val scalaFile = filename.camelCase + ".scala"
      val outputStream = new ByteArrayOutputStream()
      Console.withOut(new PrintStream(outputStream)) {
        ScalaBuff.run(Array("--scala_out=" + outputDir,
          "--proto_path=" + protoDir,
          "--verbose",
          protoFile))
        outputStream.toString("utf-8").split("\n").size should be(1)
      }

      val outputFile =
        new File(outputDir + SEP + resourcesGeneratedDir +
          subFolder.map(_ + SEP).getOrElse("") + scalaFile)
      outputFile should be('exists)
      val outputFileSource = io.Source.fromFile(outputFile)
      val exampleProtoGenerated =
        io.Source.fromFile(new File(generatedDir + subFolder.map(_ + SEP).getOrElse("") + scalaFile)).mkString
      outputFileSource.mkString should equal(exampleProtoGenerated)
      outputFileSource.close()
    }

    compile("package_name", Some("nested"))
    compile("import_packages", None)
  }

  test("run: unknown option") {
    val outputStream = new ByteArrayOutputStream()
    Console.withOut(new PrintStream(outputStream)) {
      val unsupportedOption = "--unsupported-option"
      ScalaBuff.run(Array(unsupportedOption))
      outputStream.toString("utf-8") should be(Strings.UNKNOWN_ARGUMENT + unsupportedOption + NEWLINE)
    }
  }

  test("run: invalid output directory") {
    val outputStream = new ByteArrayOutputStream()
    Console.withOut(new PrintStream(outputStream)) {
      val invalidOutputDirectory = "()/%$#:;"
      ScalaBuff.run(Array("--scala_out=" + invalidOutputDirectory))
      outputStream.toString("utf-8") should be(Strings.INVALID_OUTPUT_DIRECTORY + invalidOutputDirectory + NEWLINE)
    }
  }


}
