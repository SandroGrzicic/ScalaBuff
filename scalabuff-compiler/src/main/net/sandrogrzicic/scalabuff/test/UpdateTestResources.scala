package net.sandrogrzicic.scalabuff.test

import net.sandrogrzicic.scalabuff.compiler._
import java.io.File.{separator => /}
import java.io._
import scala.Some

/**
 * A small program which updates the test resources (ScalaBuff outputs).
 *
 * @author Sandro Gržičić
 */

object UpdateTestResources extends App {
  // Set this to true for debugging resource generation
  val verbose = false

  update()

	/**
	 * Update the output test resources.
	 */
	def update() {
		val protoExtension = ".proto"
		val parsedExtension = ".txt"

		val protoFileFilter = new FileFilter {
			def accept(filtered: File) = filtered.getName.endsWith(protoExtension)
		}

    val testDir = "scalabuff-compiler" + / +  "src" + / + "test" + /

    val parsedDir = testDir + "resources" + / + "parsed" + /

    val protoDirFile = new File(testDir + "resources" + / + "proto" + /)

    println(s"Processing files in directory ($protoDirFile)...\n")

    val protoFiles = protoDirFile.listFiles(protoFileFilter)
		for (file <- protoFiles) {
			val fileName = file.getName.dropRight(protoExtension.length).camelCase
			val generatedParsedFile = new File(parsedDir + fileName.camelCase + parsedExtension)
			generatedParsedFile.delete()

			val generatedParsed = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(generatedParsedFile), "utf-8"))

			var parsedOption: Option[List[Node]] = None
			var output: String = null

			try {
				parsedOption = Some(Parser(file))
			} catch {
				// in case of a parsing error, write it to the output file
				case e: Throwable =>
					if (verbose) println(s"Error parsing ${file}: ${e}")
					output = e.getMessage
			}
      try {
        generatedParsed.write(parsedOption.map(_.toString + "\n").getOrElse(output))
      } catch {
        case e: Throwable =>
          if (verbose) println(s"Error parsing ${file}: ${e}")
          e.printStackTrace

      } finally {
        generatedParsed.close()
      }

	parsedOption.foreach { parsed =>
	// if we have a valid parsing tree, generate a Scala proto class.

        // for now, this is hard-coded.
        val importedSymbols = Map("PackageTest" -> ImportedSymbol("nested", isEnum = false))

        val generated = Generator(parsed, file.getName, importedSymbols, generateJsonMethod = true, None)
	val generatedPath = testDir + generated.path + generated.file + ".scala"

        new File(testDir + generated.path).mkdirs()

        if (verbose) println(s"# Deleting ${generatedPath}")
        new File(generatedPath).delete()

        val generatedClass = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(generatedPath), "utf-8"))

	try {
  	  generatedClass.write(generated.body)

	} catch {
	  case e: Throwable =>
  	    if (verbose) println(s"Error parsing ${file}: ${e}")
	    e.printStackTrace

	} finally {
	  generatedClass.close()
	}

	if (verbose) println(s"# Wrote ${generatedPath}")
    }
			println(fileName)
		}

    println(s"\nFinished processing (${protoFiles.length}) files.")
	}

}
