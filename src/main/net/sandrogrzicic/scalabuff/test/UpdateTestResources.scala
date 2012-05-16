package net.sandrogrzicic.scalabuff.test

/**
 * @author Sandro Gržičić
 */

import net.sandrogrzicic.scalabuff.compiler.{Generator, Parser, Node}

import java.io._

/**
 * A small program which updates the test resources (ScalaBuff outputs). Should be run sparingly.
 * @author Sandro Gržičić
 */

object UpdateTestResources {

	/**
	 * Update the output test resources.
	 */
	def update() {
		val protoExtension = ".proto"
		val parsedExtension = ".txt"

		val protoFileFilter = new FileFilter {
			def accept(filtered: File) = filtered.getName.endsWith(protoExtension)
		}

		val protoDir = new File("src/test/resources/proto/")

		val parsedDir = "src/test/resources/parsed/"
		val generatedDir = "src/test/resources/generated/"

		for (file <- protoDir.listFiles(protoFileFilter)) {
			val fileName = file.getName.dropRight(protoExtension.length)
			val oldFileParsed = new File(parsedDir + fileName + parsedExtension)
			oldFileParsed.delete()
			val oldFileGenerated = new File(generatedDir + fileName.capitalize + ".scala")
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
			if (parsed != null) output = parsed.toString + "\n"
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
