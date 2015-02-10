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
			val generatedParsedFile = new File(parsedDir + fileName + parsedExtension)
			generatedParsedFile.delete()

			val generatedParsed = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(generatedParsedFile), "utf-8"))

			var parsedOption: Option[List[Node]] = None
			var output: String = null

			try {
				parsedOption = Some(Parser(file))
			} catch {
				// in case of a parsing error, write it to the output file
				case e: Throwable => output = e.getMessage
			}
			try {
        generatedParsed.write(parsedOption.map(_.toString + "\n").getOrElse(output))
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

        val generatedClass = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(generatedPath), "utf-8"))
				try {
          new File(generatedPath).delete()
          generatedClass.write(generated.body)
				} finally {
					generatedClass.close()
				}
			}
			println(fileName)
		}

    println(s"\nFinished processing (${protoFiles.length}) files.")
	}

}
