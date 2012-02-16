package net.sandrogrzicic.scalabuff.compiler.tests

/**
 * @author Sandro Gržičić
 */

import net.sandrogrzicic.scalabuff.compiler.{Generator, Parser, Node}

import java.io._

/**
 * A small program which updates the tests resources (ScalaBuff outputs). Should be run sparingly.
 * @author Sandro Gržičić
 */

object UpdateTestResources {

	/**
	 * Update the output tests resources.
	 */
	def update() {
		val protoExtension = ".proto"
		val parsedExtension = ".txt"

		val protoFileFilter = new FileFilter {
			def accept(filtered: File) = filtered.getName.endsWith(protoExtension)
		}

    val baseDir = new File("scalabuff-compiler/src/test/resources/tests")

		val protoDir = new File(baseDir, "proto")

		val parsedDir = new File(baseDir, "parsed")
		val generatedDir = new File("scalabuff-compiler/src/test/scala/tests/generated")

    protoDir.mkdirs
    parsedDir.mkdirs
    generatedDir.mkdirs

		for (file <- protoDir.listFiles(protoFileFilter)) {
			val fileName = file.getName.dropRight(protoExtension.length)
			val oldFileParsed = new File(parsedDir,  fileName + parsedExtension)
			oldFileParsed.delete()
			val oldFileGenerated = new File(generatedDir, fileName.capitalize + ".scala")
			oldFileGenerated.delete()

			val outParsed = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(oldFileParsed), "utf-8"))

			var parsed: List[Node] = null
			var output: String = null
			try {
				parsed = Parser(file)
			} catch {
				// in case of a parsing error, write it to the output file
				case e => output = e.getMessage
			}
			if (parsed != null) output = parsed.toString
			outParsed.write(output)
			outParsed.close()

			if (parsed != null) {
				// if we have a valid parsing tree, generate a scala class too
				val outGenerated = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(oldFileGenerated), "utf-8"))
				try {
					// ignore package file path
					outGenerated.write(Generator(parsed, file.getName).body)
				} finally {
					outGenerated.close()
				}
			}
			println(fileName)
		}
	}

	def main(args: Array[String]) {
		update()
	}

}
