package hr.sandrogrzicic.scalabuff.compiler

import java.io._
import java.nio.charset.Charset

/**
 * ScalaBuff runtime.
 * @author Sandro Gržičić
 */
object ScalaBuff {

	implicit def buffString(string: String): BuffedString = new BuffedString(string)

	val defaultCharset: Charset = if (Charset.isSupported("utf-8")) Charset.forName("utf-8") else Charset.defaultCharset()

	case class Settings (
		outputDirectory: File = new File("." + File.separatorChar),
		importDirectories: Seq[File] = Nil,
		stdout: Boolean = false,
		inputEncoding: Charset = defaultCharset,
		outputEncoding: Charset = defaultCharset
	)

	/**
	 * Runs ScalaBuff on the specified resource path (file path or URL) and returns the resulting Scala class.
	 * If the encoding is not specified, it defaults to either UTF-8 (if available) or the platform default charset.
	 */
	def apply(resourcePath: String, encoding: Charset = defaultCharset) = fromResourcePath(resourcePath, encoding)

	/**
	 * Runs ScalaBuff on the specified resource path (file path or URL) and returns the resulting Scala class.
	 * If the encoding is not specified, it defaults to either UTF-8 (if available) or the platform default charset.
	 */
	def fromResourcePath(resourcePath: String, encoding: Charset = defaultCharset): ScalaClass = {
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
	def main(args: Array[String]) {
		args match {
			case noArguments if noArguments.isEmpty => println(Strings.HELP)
			case arguments =>

				val (rawSettings, paths) = arguments.partition(_.startsWith("-"))

				val parsedSettings = rawSettings.foldLeft(Settings()) {
					case (settings, setting) => parseSetting(setting, settings) match {
						case Left(message) => println(message); settings //If parseSetting returned a message, print it and return the old settings
						case Right(newSettings) => newSettings
					}
				}

				for (path <- paths) {
					try {
						val scalaClass = apply(path, parsedSettings.inputEncoding)
						try {
							write(scalaClass, parsedSettings)
						} catch {
							// just print the error and continue processing
							case io: IOException => println(Strings.CANNOT_WRITE_FILE + scalaClass.path + scalaClass.file + ".scala")
						}
					} catch {
						// just print the error and continue processing
						case pf: ParsingFailureException => println(pf.getMessage)
						case io: IOException => println(Strings.CANNOT_ACCESS_RESOURCE + path)
					}
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
				val dir = s.substring("-I".length)
				val importDir = new File(dir)
				if (!importDir.isDirectory) Strings.INVALID_IMPORT_DIRECTORY + dir
				settings.copy(importDirectories = settings.importDirectories :+ importDir)

			case s if s startsWith "--proto_path=" =>
				val dir = s.substring("--proto_path=".length)
				val importDir = new File(dir)
				if (!importDir.isDirectory) Strings.INVALID_IMPORT_DIRECTORY + dir
				settings.copy(importDirectories = settings.importDirectories :+ importDir)

			case s if s startsWith "--scala_out=" =>
				val dir = s.substring("--scala_out=".length)
				val outputDir = new File(dir)
				if (!outputDir.isDirectory) Strings.INVALID_OUTPUT_DIRECTORY + dir
				else settings.copy(outputDirectory = outputDir)

			case "--stdout" =>
				settings.copy(stdout = true)

			case s if s startsWith "--proto_encoding=" =>
				val inputEncoding = s.substring("--proto_encoding=".length)
				try {
					settings.copy(inputEncoding = Charset.forName(inputEncoding))
				} catch {
					case ue: UnsupportedEncodingException => Strings.UNSUPPORTED_INPUT_ENCODING + inputEncoding
				}

			case s if s startsWith "--out_encoding=" =>
				val outputEncoding = s.substring("--out_encoding=".length)
				try {
					settings.copy(outputEncoding = Charset.forName(outputEncoding))
				} catch {
					case ue: UnsupportedEncodingException => Strings.UNSUPPORTED_OUTPUT_ENCODING + outputEncoding
				}

			case unknown =>
				Strings.UNKNOWN_ARGUMENT + unknown

		}) match {
			case s: String => Left(s)
			case s: Settings => Right(s)
		}

	/**
	 * Returns a new Reader based on the specified resource path, which is either a File or an URL.
	 */
	protected def read(resourcePath: String, encoding: Charset): Reader =
		new BufferedReader(new InputStreamReader((try {
			new FileInputStream(resourcePath)
		} catch {
			case fnf: FileNotFoundException => new java.net.URL(resourcePath).openStream
		}), encoding))

	/**
	 * Write the specified ScalaClass to a file, or to stdout, depending on the Settings.
	 */
	protected def write(generated: ScalaClass, settings: Settings) {
		if (settings.stdout) {
			println(generated)
		} else {
			val targetDir = new File(settings.outputDirectory + File.separator + generated.path)

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
