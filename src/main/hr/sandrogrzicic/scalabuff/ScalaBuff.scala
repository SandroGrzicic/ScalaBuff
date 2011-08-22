package hr.sandrogrzicic.scalabuff

import java.io._

/**
 * ScalaBuff runtime.
 * @author Sandro Gržičić
 */
object ScalaBuff {
	protected var outputDirectory: String = "./"
	protected var importDirectories: Array[String] = Array[String]()
	/** Whether to write output to stdout (true) or follow standard protoc behavior (false). */
	protected var stdout = false

	// If UTF-8 isn't supported by the JVM, the user can override the encodings;
	// no checks are performed until after the user has set his encoding preferences.
	protected var inputEncoding = "utf-8"
	protected var outputEncoding = "utf-8"

	implicit def buffString(string: String): BuffedString = new BuffedString(string)

	/**
	 * Runs ScalaBuff on the specified resource path (file path or URL) and returns the resulting Scala class.
	 */
	def apply(resourcePath: String) = fromResourcePath(resourcePath)

	/**
	 * Runs ScalaBuff on the specified resource path (file path or URL) and returns the resulting Scala class.
	 */
	def fromResourcePath(resourcePath: String): ScalaClass = {
		val reader = read(resourcePath)
		try {
		  Generator(Parser(reader), resourcePath.dropUntilLast('/'))
	    } finally {
		  reader.close()
		}
	}
	/**
	 * Runs ScalaBuff on the specified input String and returns the output Scala class.
	 */
	def fromString(input: String) = Generator(Parser(input), "")

	/**
	 * Runner: Runs ScalaBuff on the specified resource path(s).
	 */
	def main(args: Array[String]): Unit = args match {
		case a if a.isEmpty => println(Strings.HELP)
		case args =>
		for (arg <- args) {
			// check if the argument is a potential option
			if (arg.startsWith("-")) {
				if (option(arg))
					return
			} else {
				// argument is a resource path
				var scalaClass: ScalaClass = null
				try {
					scalaClass = apply(arg)
					try {
						if (stdout) println(scalaClass) else write(scalaClass)
					} catch {
						case ue: UnsupportedEncodingException => println(Strings.UNSUPPORTED_OUTPUT_ENCODING + outputEncoding); return
						case io: IOException => println(Strings.CANNOT_WRITE_FILE + scalaClass.path + scalaClass.file + ".scala")
					}
				} catch {
					// on parsing failure or resource access error name, just print the error
					case pf: ParsingFailureException => println(pf.getMessage)
					case ue: UnsupportedEncodingException => println(Strings.UNSUPPORTED_INPUT_ENCODING + inputEncoding); return
					case io: IOException => println(Strings.CANNOT_ACCESS_RESOURCE + arg)
				}
			}
		}
	}

	/**
	 * Handle the specified option. Returns true if the option needs to stop program execution.
	 */
	protected def option(option: String): Boolean = {
		if (option == "-h" || option == "--help") {
			println(Strings.HELP)
			true
		} else if (option.startsWith("-I")) {
			importDirectories :+= option.substring("-I".length)
		} else if (option.startsWith("--proto_path=")) {
			importDirectories :+= option.substring("--proto_path=".length)
		} else if (option.startsWith("--scala_out=")) {
			outputDirectory = option.substring("--scala_out=".length)
			if (!outputDirectory.endsWith("/")) {
				outputDirectory += "/"
			}
			if (!(new File(outputDirectory).isDirectory)) {
				println(Strings.INVALID_OUTPUT_DIRECTORY + outputDirectory)
				true
			}
		} else if (option == "--stdout") {
			stdout = true
		} else if (option.startsWith("--proto_encoding=")) {
			inputEncoding = option.substring("--proto_encoding=".length)
		} else if (option.startsWith("--out_encoding=")) {
			outputEncoding = option.substring("--out_encoding=".length)
		} else {
			println(Strings.UNKNOWN_ARGUMENT + option)
			true
		}
		false
	}

	/**
	 * Returns a new Reader based on the specified resource path, which is either a File or an URL.
	 */
	protected def read(resourcePath: String): Reader = 
		new BufferedReader(new InputStreamReader((try {
			new FileInputStream(resourcePath)
		} catch {
			case fnf: FileNotFoundException => new java.net.URL(resourcePath).openStream
	    }), inputEncoding))
	    
	/**
	 * Write the specified string to a file as a Scala class.
	 */
	protected def write(generated: ScalaClass) {
		val targetDir = new File(outputDirectory + generated.path)

		// generate all the directories between outputDirectory and generated.path
		// outputDirectory exists because the passed option is checked in option()
		targetDir.mkdirs()
		
		val targetFile = new File(targetDir, generated.file.camelCase + ".scala")
		
		if (targetFile.exists()) targetFile.delete()

		val writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(targetFile), outputEncoding))

		try { writer.write(generated.body) } finally { writer.close() }
	}

}

/**
 * The root ScalaBuff RuntimeException.
 */
class ScalaBuffException(message: String) extends RuntimeException(message)