package tests

import org.scalatest.{FunSuite, Matchers}
import net.sandrogrzicic.scalabuff.compiler._
import net.sandrogrzicic.scalabuff.compiler.{Strings, ScalaBuff, ScalaClass}
import java.io.{PrintStream, ByteArrayOutputStream, File}
import File.{separator => /}

/**
 * ScalaBuff CLI runner test.
 * @author Sandro Gržičić
 */
class ScalaBuffTest extends FunSuite with Matchers {

  val NEWLINE = System.getProperty("line.separator")

  val parsedExtension = ".txt"

  val outputDir = "scalabuff-compiler" + / + "src" + / + "test" + /

  val protoDir = outputDir + "resources" + / + "proto" + /
  val multiProtoDir = outputDir + "resources" + / + "multipleprototests" + /
  val parsedDir = outputDir + "resources" + / + "parsed" + /
  val resourcesGeneratedDir = "resources" + / + "generated" + /
  val generatedDir = outputDir + resourcesGeneratedDir

  val testProto = "Simple"
  val testProtoParsed = io.Source.fromFile(new File(parsedDir + testProto + parsedExtension), "UTF-8").mkString
  val testProtoGenerated = io.Source.fromFile(new File(generatedDir + testProto.capitalize + ".scala"), "UTF-8").mkString

  val testProtoMulti = "MultiOne"

  val testProtoPacked = "Packed"

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
    val resourcesDirectory = new File("scalabuff-compiler" + / + "resources")
    val resourcesGeneratedDirectory = new File("scalabuff-compiler" + / + resourcesGeneratedDir)
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
      val outputFile = new File(resourcesGeneratedDir + testProto + ".scala")
      outputFile should be('exists)
      outputFile.deleteOnExit()
      val outputFileSource = io.Source.fromFile(outputFile, "UTF-8")
      outputFileSource.mkString should equal(testProtoGenerated)
      outputFileSource.close()
      outputFile.delete()
      new File("resources" + / + "generated").delete()
      new File("resources").delete()
    }
  }

  test("run: simple .proto file with a specified output directory") {
    val outputStream = new ByteArrayOutputStream()
    val simpleProto = protoDir + testProto + ".proto"
    Console.withOut(new PrintStream(outputStream)) {
      ScalaBuff.run(Array("-v", "-v", "--generate_json_method", "--scala_out=" + outputDir, simpleProto))
      val output = outputStream.toString.split("\r?\n")
      output.length should be (2)
      output(0) should startWith("Parameters: ")
      output(1) should startWith("Paths: ")
    }
    val outputFile = new File(outputDir + / + resourcesGeneratedDir + testProto + ".scala")
    outputFile should be('exists)
    val outputFileSource = io.Source.fromFile(outputFile, "UTF-8")
    outputFileSource.mkString should equal(testProtoGenerated)
    outputFileSource.close()
  }

  test("run: input directory only") {
    val protoFiles = Seq("MultiOne", "MultiTwo")

    val outputStream = new ByteArrayOutputStream()
    Console.withOut(new PrintStream(outputStream)) {
      ScalaBuff.run(Array("-v", "-v", "--scala_out=" + outputDir, "--proto_path=" + multiProtoDir))
      outputStream.toString("utf-8").split("\n").size should be(2)
    }

    for (proto <- protoFiles) {
      val outputFile = new File(outputDir + / + resourcesGeneratedDir + proto + ".scala")
      outputFile should be('exists)
      val outputFileSource = io.Source.fromFile(outputFile, "UTF-8")
      val exampleProtoGenerated = io.Source.fromFile(new File(generatedDir + proto + ".scala"), "UTF-8").mkString
      outputFileSource.mkString should equal(exampleProtoGenerated)
      outputFileSource.close()
    }
  }

  test("run: multiple input directories, with file") {
    val outputStream = new ByteArrayOutputStream()
    Console.withOut(new PrintStream(outputStream)) {
      ScalaBuff.run(Array("--scala_out=" + outputDir,
        "--proto_path=" + parsedDir, // no proto files here, but we want to make sure MultiOne.proto is found
        "--proto_path=" + multiProtoDir,
        "--verbose",
        testProtoMulti + ".proto"))
      outputStream.toString("utf-8").split("\n").size should be(1)
    }

    val outputFile = new File(outputDir + / + resourcesGeneratedDir + testProtoMulti + ".scala")
    outputFile should be('exists)
    val outputFileSource = io.Source.fromFile(outputFile, "UTF-8")
    val exampleProtoGenerated = io.Source.fromFile(new File(generatedDir + testProtoMulti + ".scala"), "UTF-8").mkString
    outputFileSource.mkString should equal(exampleProtoGenerated)
    outputFileSource.close()
  }

  test("run: import across packages") {
    def compile(filename: String, subFolder: Option[String]) {
      val protoFile = filename + ".proto"
      val scalaFile = filename + ".scala"
      val outputStream = new ByteArrayOutputStream()
      Console.withOut(new PrintStream(outputStream)) {
        ScalaBuff.run(Array("--scala_out=" + outputDir,
          "--proto_path=" + protoDir,
          "--verbose",
          protoFile))
        outputStream.toString("utf-8").split("\n").size should be(1)
      }

      val outputFile =
        new File(outputDir + / + resourcesGeneratedDir +
          subFolder.map(_ + /).getOrElse("") + scalaFile)
      outputFile should be('exists)
      val outputFileSource = io.Source.fromFile(outputFile, "UTF-8")
      val exampleProtoGenerated =
        io.Source.fromFile(new File(generatedDir + subFolder.map(_ + /).getOrElse("") + scalaFile), "UTF-8").mkString
      outputFileSource.mkString should equal(exampleProtoGenerated)
      outputFileSource.close()
    }

    compile("PackageName", Some("nested"))
    compile("ImportPackages", None)
    compile("ImportUseFullname", None)
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

  test("apply: packed .proto file") {

    val settings = ScalaBuff.Settings(generateJsonMethod = true)
    val scalaClass: ScalaClass = ScalaBuff(new File(protoDir + testProtoPacked + ".proto"))(settings)
    // TODO matches
   //  println(scalaClass)
  }
}
