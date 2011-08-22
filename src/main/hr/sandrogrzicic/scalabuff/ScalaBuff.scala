package hr.sandrogrzicic.scalabuff

import java.io._

/**
 * ScalaBuff runtime.
 * @author Sandro Gržičić
 */
object ScalaBuff {
	case class Settings(
		outputDirectory: String = "./",
		importDirectories: Seq[String] = Nil,
		stdout: Boolean = false,
		inputEncoding: String = "utf-8",
		outputEncoding: String = "utf-8")

	implicit def buffString(string: String): BuffedString = new BuffedString(string)

	/**
	 * Runs ScalaBuff on the specified resource path (file path or URL) and returns the resulting Scala class.
	 */
	def apply(resourcePath: String, encoding: String = "utf-8") = fromResourcePath(resourcePath, encoding)

	/**
	 * Runs ScalaBuff on the specified resource path (file path or URL) and returns the resulting Scala class.
	 */
	def fromResourcePath(resourcePath: String, encoding: String): ScalaClass = {
		val reader = read(resourcePath, encoding)
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

		  val (rawSettings, paths) = args.partition(_.startsWith("-"))

          val parsedSettings = rawSettings.foldLeft(Settings()) {
		  	case (settings, setting) => parseSetting(setting, settings) match {
		  		case Left(message) => println(message); settings //If parseSetting returned a message, print it and return the old settings
		  		case Right(newSettings) => newSettings
		  	}
		  }

		  for(path <- paths) {
		  	try {
				val scalaClass = apply(path, parsedSettings.inputEncoding)
				try {
					write(scalaClass, parsedSettings)
				} catch {
					case ue: UnsupportedEncodingException => println(Strings.UNSUPPORTED_OUTPUT_ENCODING + parsedSettings.outputEncoding)
					case io: IOException => println(Strings.CANNOT_WRITE_FILE + scalaClass.path + scalaClass.file + ".scala")
				}
			} catch {
				// on parsing failure or resource access error name, just print the error
				case pf: ParsingFailureException => println(pf.getMessage)
				case ue: UnsupportedEncodingException => println(Strings.UNSUPPORTED_INPUT_ENCODING + parsedSettings.inputEncoding)
				case io: IOException => println(Strings.CANNOT_ACCESS_RESOURCE + path)
			}
		  }
	}

	/**
	 * Parse the provided setting, return either an error message or a new Settings
	 */
	protected def parseSetting(setting: String, settings: Settings): Either[String, Settings] =
	  (setting match {
		case "-h" | "--help" =>
		  Strings.HELP

		case s if s startsWith "-I" =>
		  settings.copy(importDirectories = settings.importDirectories :+ s.substring("-I".length))

		case s if s startsWith "--proto_path=" =>
		  settings.copy(importDirectories = settings.importDirectories :+ s.substring("--proto_path=".length))

		case s if s startsWith "--scala_out=" =>
		  val dir = s.substring("--scala_out=".length) + (if (s endsWith File.separator) "" else File.separator)
		  val fileDir = new File(dir)
		  if (fileDir.exists && !fileDir.isDirectory) Strings.INVALID_OUTPUT_DIRECTORY + dir
		  else settings.copy(outputDirectory = dir)

		case "--stdout" =>
		  settings.copy(stdout = true)

		case s if s startsWith "--proto_encoding=" =>
		  settings.copy(inputEncoding = s.substring("--proto_encoding=".length))

        case s if s startsWith "--out_encoding=" =>
          settings.copy(outputEncoding = s.substring("--out_encoding=".length))

		case unknown =>
		  Strings.UNKNOWN_ARGUMENT + unknown

	  }) match {
		case s: String => Left(s)
		case s: Settings => Right(s)
	  }

	/**
	 * Returns a new Reader based on the specified resource path, which is either a File or an URL.
	 */
	protected def read(resourcePath: String, encoding: String): Reader = 
		new BufferedReader(new InputStreamReader((try {
			new FileInputStream(resourcePath)
		} catch {
			case fnf: FileNotFoundException => new java.net.URL(resourcePath).openStream
	    }), encoding))
	    
	/**
	 * Write the specified string to a file as a Scala class.
	 */
	protected def write(generated: ScalaClass, settings: Settings) {
		if (settings.stdout) {
			println(generated)
		} else {
			val targetDir = new File(settings.outputDirectory + generated.path)

			// generate all the directories between outputDirectory and generated.path
			// outputDirectory exists because the passed option is checked in option()
			targetDir.mkdirs()
		
			val targetFile = new File(targetDir, generated.file.camelCase + ".scala")
		
			if (targetFile.exists()) targetFile.delete()

			val writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(targetFile), settings.outputEncoding))

			try { writer.write(generated.body) } finally { writer.close() }
		}
	}

}

/**
 * The root ScalaBuff RuntimeException.
 */
class ScalaBuffException(message: String) extends RuntimeException(message)