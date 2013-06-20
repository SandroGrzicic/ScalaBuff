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
                         importDirectories: Seq[File] = List(new File(".")),
                         stdout: Boolean = false,
                         inputEncoding: Charset = defaultCharset,
                         outputEncoding: Charset = defaultCharset,
                         verbose: Boolean = false)

  val defaultSettings = Settings()

  /**
   * Runs ScalaBuff on the specified file and returns the resulting Scala class.
   * If the encoding is not specified, it defaults to either UTF-8 (if available) or the platform default charset.
   */
  def apply(file: File)(implicit settings: Settings = defaultSettings) = {
    val tree = parse(file)
    val symbols = processImportSymbols(tree)
    Generator(tree, file.getName, symbols)
  }
 
  /**
   * Runs ScalaBuff on the specified input String and returns the output Scala class.
   */
  def fromString(input: String) = Generator(Parser(input), "", Map())

  /**
   * Parse a protobuf file into Nodes.
   */
  def parse(file: File)(implicit settings: Settings = defaultSettings): List[Node] = {
    val reader = read(file)
    try {
      Parser(reader)
    } finally {
      reader.close()
    }
  }

  /**
   * Process "import" statements in a protobuf AST by scanning the imported
   * files and building a map of their exported symbols.
   */
  def processImportSymbols(tree: List[Node])(implicit settings: Settings = defaultSettings): Map[String, ImportedSymbol] = {
    def dig(name: String) = {
      val tree = parse(searchPath(name).getOrElse { throw new IOException("Unable to import: " + name) })
      val packageName = tree.collectFirst {
        case OptionValue(key, value) if key == "java_package" => value.stripQuotes
      }.getOrElse("")
      tree.collect {
        case Message(name, _) => (name, ImportedSymbol(packageName, false))
        case EnumStatement(name, _, _) => (name, ImportedSymbol(packageName, true))
      }
    }
    tree.collect {
      case ImportStatement(name) => dig(name.stripQuotes)
    }.flatten.toMap
  }

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

  def searchPath(filename: String)(implicit settings: Settings = defaultSettings): Option[File] = {
    if (filename startsWith "/") {
      Option(new File(filename)).filter(_.exists)
    } else {
      settings.importDirectories.map { folder =>
        new File(folder, filename)
      }.find(_.exists)
    }
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
        val protoFiles: Seq[File] = paths match {
          case empty if empty.isEmpty =>
            parsedSettings.importDirectories.foldLeft(Seq[File]()) {
              case (seq, dir) => seq ++ findFiles(dir)
            }
          case files =>
            files.flatMap(searchPath)
        }

        var errors = 0
        for (file <- protoFiles) {
          verbosePrintln("Processing: " + file.getAbsolutePath)
          try {
            val scalaClass = apply(file)
            try {
              write(scalaClass)
            } catch {
              // just print the error and continue processing
              case io: IOException =>
                errors += 1
                println(Strings.CANNOT_WRITE_FILE + scalaClass.path + scalaClass.file + ".scala")
            }
          } catch {
            // just print the error and continue processing
            case pf: ParsingFailureException =>
              errors += 1
              println(pf.getMessage)
            case io: IOException =>
              errors += 1
              println(Strings.CANNOT_ACCESS_RESOURCE + file.getAbsolutePath)
          }
        }
        if (errors > 0) System.exit(1)
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
   * Returns a new Reader based on the specified File and Charset.
   */
  protected def read(file: File)(implicit settings: Settings = defaultSettings): Reader =
    new BufferedReader(new InputStreamReader(new FileInputStream(file), defaultSettings.inputEncoding))

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
