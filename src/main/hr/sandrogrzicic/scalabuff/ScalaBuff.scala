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
	def fromResourcePath(resourcePath: String) = {
		val reader = read(resourcePath)
		val scalaClass = Generator(Parser(reader), resourcePath.dropUntilLast('/'))
		reader.close()

		scalaClass
	}
	/**
	 * Runs ScalaBuff on the specified input String and returns the output Scala class.
	 */
	def fromString(input: String) = {
		Generator(Parser(input), "")
	}


	/**
	 * Runner: Runs ScalaBuff on the specified resource path(s).
	 */
	def main(args: Array[String]) {
		if (args.isEmpty) {
			println(Strings.HELP)
		}
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
						if (stdout) {
							println(scalaClass)
						} else {
							write(scalaClass)
						}
					} catch {
						case ue: UnsupportedEncodingException => println(Strings.UNSUPPORTED_OUTPUT_ENCODING + outputEncoding); return
						case io: IOException => println(Strings.CANNOT_WRITE_FILE + scalaClass.path + scalaClass.file + ".scala")
						case e => throw e
					}
				} catch {
					// on parsing failure or resource access error name, just print the error
					case pf: ParsingFailureException => println(pf.getMessage)
					case ue: UnsupportedEncodingException => println(Strings.UNSUPPORTED_INPUT_ENCODING + inputEncoding); return
					case io: IOException => println(Strings.CANNOT_ACCESS_RESOURCE + arg)
					case e => throw e
				}
			}
		}
	}

	/**
	 * Handle the specified option. Returns true if the option needs to stop program execution.
	 */
	protected def option(option: String) = {
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
	protected def read(resourcePath: String) = {
		implicit def stream2reader(stream: InputStream) = new BufferedReader(new InputStreamReader(stream, inputEncoding))

		var reader: Reader = null
		try {
			reader = new FileInputStream(resourcePath)
		} catch {
			case fnf: FileNotFoundException =>
				reader = new java.net.URL(resourcePath).openStream
			case e => throw e
		}
		reader
	}
	/**
	 * Write the specified string to a file as a Scala class.
	 */
	protected def write(generated: ScalaClass) {
		val className = new File(outputDirectory + generated.path +
			generated.file.camelCase + ".scala")

		// todo: add recursive directory creation, if generated path inside outputDirectory doesn't exist

		val file = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(className), outputEncoding))

		if (className.exists()) className.delete()
		file.write(generated.body)
		file.close()
	}

}

/**
 * The root ScalaBuff RuntimeException.
 */
class ScalaBuffException(message: String) extends RuntimeException(message)