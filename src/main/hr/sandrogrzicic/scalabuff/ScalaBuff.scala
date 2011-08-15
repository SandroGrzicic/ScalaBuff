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

	implicit def stream2reader(stream: InputStream) = new BufferedReader(new InputStreamReader(stream, "utf-8"))
	implicit def buffString(string: String): BuffedString = new BuffedString(string)

	/**
	 * Runs ScalaBuff on the specified resource path and returns the output Scala class.
	 */
	def apply(resourcePath: String) = {
		var reader: Reader = null
		try {
			reader = new FileInputStream(resourcePath)
		} catch {
			case fnf: FileNotFoundException =>
				reader = new java.net.URL(resourcePath).openStream
			case e => throw e
		}

		Generator(Parser(reader), resourcePath.dropUntilLast('/'), reader)
	}

	/**
	 * Runner: Runs ScalaBuff on the specified resource path(s).
	 */
	def main(args: Array[String]) {
		if (args.length < 1) {
			println(Strings.HELP)
			return
		}

		for (arg <- args) {
			// check if the argument is a potential option
			if (arg.startsWith("-")) {
				if (option(arg))
					return
			} else {
				// argument is a resource path
				try {
					if (stdout) {
						println(apply(arg))
					} else {
						write(apply(arg))
					}
				} catch {
					// just print the error and continue processing other files
					case pf: ParsingFailureException => println(pf.getMessage)
					case io: IOException => println(Strings.CANNOT_ACCESS_RESOURCE + arg)

					// serious error - stop execution
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
		} else {
			println(Strings.UNKNOWN_ARGUMENT + option)
			true
		}
		false
	}

	/**
	 * Write the specified string to a file as a Scala class.
	 */
	protected def write(generated: ScalaClass) {
		val className = new File(outputDirectory + generated.path +
			generated.file.camelCase + ".scala")
		if (className.exists()) className.delete()

		val file = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(className), "utf-8"))
		file.write(generated.body)
		file.close()
	}

}

/**
 * The root ScalaBuff RuntimeException.
 */
class ScalaBuffException(message: String) extends RuntimeException(message)