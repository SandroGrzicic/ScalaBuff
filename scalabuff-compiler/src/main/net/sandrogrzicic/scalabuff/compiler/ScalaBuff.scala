package net.sandrogrzicic.scalabuff.compiler

import java.io._
import java.nio.charset.Charset

/**
 * ScalaBuff runtime.
 * @author Sandro Gržičić
 */
object ScalaBuff {

  val defaultCharset: Charset = if (Charset.isSupported("utf-8")) Charset.forName("utf-8") else Charset.defaultCharset()

  case class Settings(
                         outputDirectory: File = new File("." + File.separatorChar),
                         importDirectories: Seq[File] = Nil,
                         stdout: Boolean = false,
                         inputEncoding: Charset = defaultCharset,
                         outputEncoding: Charset = defaultCharset,
                         verbose: Boolean = false)

  val defaultSettings = Settings()

  /**
   * Runs ScalaBuff on the specified resource path (file path or URL) and returns the resulting Scala class.
   * If the encoding is not specified, it defaults to either UTF-8 (if available) or the platform default charset.
   */
  def apply(resourcePath: String)(implicit settings: Settings = defaultSettings) = fromResourcePath(resourcePath)

  /**
   * Runs ScalaBuff on the specified resource path (file path or URL) and returns the resulting Scala class.
   * If the encoding is not specified, it defaults to either UTF-8 (if available) or the platform default charset.
   */
  def fromResourcePath(resourcePath: String)(implicit settings: Settings = defaultSettings): ScalaClass = {
    val reader = read(resourcePath)
    try {
      Generator(Parser(reader), resourcePath.dropUntilLast(File.separatorChar))
    } finally {
      reader.close()
    }
  }

  /**
   * Runs ScalaBuff on the specified input String and returns the output Scala class.
   */
  def fromString(input: String) = Generator(Parser(input), "")

  val protoExtension = ".proto"

  val protoFileFilter = new FileFilter {
    def accept(filtered: File) = filtered.getName.endsWith(protoExtension)
  }

  def findFiles(startAt: File): Seq[File] = {
    def recurse(src: File, seq: Seq[File] = Seq[File]()): Seq[File] = {
      src match {
        case e if !e.exists() => println(Strings.INVALID_IMPORT_DIRECTORY + e); seq
        case f if f.isFile => seq :+ src
        case d => seq ++ src.listFiles(protoFileFilter).toSeq.map(recurse(_)).foldLeft(Seq[File]())(_ ++ _)
      }
    }

    recurse(startAt)
  }

  def verbosePrintln(msg: String)(implicit settings: Settings) { if (settings.verbose) println(msg) }

  /**
   * Runner: Runs ScalaBuff on the specified resource path(s).
   */
  def main(args: Array[String]) {
    args match {
      case noArguments if noArguments.isEmpty => println(Strings.HELP)

      case arguments =>
        val (rawSettings, paths) = arguments.partition(_.startsWith("-"))

        implicit val parsedSettings = rawSettings.foldLeft(Settings()) {
          case (settings, setting) => parseSetting(setting, settings) match {
            case Left(message) =>
              println(message); settings //If parseSetting returned a message, print it and return the old settings
            case Right(newSettings) => newSettings
          }
        }

        /*
         * Find all protobuf files under directory if none specified
         */
        val protoFiles: Seq[String] = paths match {
          case empty if empty.isEmpty =>
            parsedSettings.importDirectories.foldLeft(Seq[File]()) {
              case (seq, dir) => seq ++ findFiles(dir)
            } map(_.getAbsolutePath)
          case files =>
            files map {
              f: String =>
                def findProto(inputDir: File, remainder: Seq[File]): scala.Option[String] = {
                  new File(inputDir, f) match {
                    case good if good.exists => Some(good.getAbsolutePath)
                    case bad if !remainder.isEmpty => findProto(remainder.head, remainder.tail)
                    case ugly => None
                  }
                }
                parsedSettings.importDirectories match {
                  case empty if empty.isEmpty => Some(f)
                  case nonEmpty => findProto(parsedSettings.importDirectories.head, parsedSettings.importDirectories.tail)
                }
            } filter (_.isDefined) map (_.get)
        }

        for (path <- protoFiles) {
          verbosePrintln("Processing: " + path)
          try {
            val scalaClass = apply(path)
            try {
              write(scalaClass)
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
   * Parse the provided setting, return either an error message or a new Settings.
   */
  protected def parseSetting(setting: String, settings: Settings): Either[String, Settings] =
    (setting match {
      case "-h" | "--help" =>
        Strings.HELP

      case s if s startsWith "-I" =>
        val dir = s.substring("-I".length)
        val importDir = new File(dir)
        if (!importDir.isDirectory) Strings.INVALID_IMPORT_DIRECTORY + dir
        else settings.copy(importDirectories = settings.importDirectories :+ importDir)

      case s if s startsWith "--proto_path=" =>
        val dir = s.substring("--proto_path=".length)
        val importDir = new File(dir)
        if (!importDir.isDirectory) Strings.INVALID_IMPORT_DIRECTORY + dir
        else settings.copy(importDirectories = settings.importDirectories :+ importDir)

      case s if s startsWith "--scala_out=" =>
        val dir = s.substring("--scala_out=".length)
        val outputDir = new File(dir)
        if (!outputDir.isDirectory) Strings.INVALID_OUTPUT_DIRECTORY + dir
        else settings.copy(outputDirectory = outputDir)

      case "--stdout" =>
        settings.copy(stdout = true)

      case "-v" =>
        settings.copy(verbose = true)

      case "--verbose" =>
        settings.copy(verbose = true)

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
  protected def read(resourcePath: String)(implicit settings: Settings): Reader =
    new BufferedReader(new InputStreamReader((try {
      new FileInputStream(resourcePath)
    } catch {
      case fnf: FileNotFoundException => new java.net.URL(resourcePath).openStream
    }), settings.inputEncoding))

  /**
   * Write the specified ScalaClass to a file, or to stdout, depending on the Settings.
   */
  protected def write(generated: ScalaClass)(implicit settings: Settings) {
    if (settings.stdout) {
      println(generated)
    } else {

      val targetDir = new File(settings.outputDirectory + File.separator + generated.path)

      // generate all the directories between outputDirectory and generated.path
      // target directory exists because the passed option is checked in option()
      targetDir.mkdirs()

      val targetFile = new File(targetDir, generated.file.camelCase + ".scala")

      if (targetFile.exists()) targetFile.delete()

      val writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(targetFile), settings.outputEncoding))

      try {
        writer.write(generated.body)
      } finally {
        writer.close()
      }
    }
  }

}

/**
 * The root ScalaBuff RuntimeException.
 */
class ScalaBuffException(message: String) extends RuntimeException(message)
