package net.sandrogrzicic.scalabuff.compiler

import java.io._
import java.nio.charset.Charset
import java.util.concurrent.atomic.AtomicBoolean

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
                         verbose: Boolean = false,
                         extraVerbose: Boolean = false,
                         generateJsonMethod: Boolean = false)

  val defaultSettings = Settings()

  /**
   * Runs ScalaBuff on the specified file and returns the resulting Scala class.
   * If the encoding is not specified, it defaults to either UTF-8 (if available) or the platform default charset.
   */
  def apply(file: File)(implicit settings: Settings = defaultSettings) = {
    val tree = parse(file)
    val symbols = processImportSymbols(tree)
    Generator(tree, file.getName, symbols, settings.generateJsonMethod)
  }
 
  /**
   * Runs ScalaBuff on the specified input String and returns the output Scala class.
   */
  def fromString(input: String, generateJsonMethod: Boolean = false) = {
    Generator(Parser(input), "", Map(), generateJsonMethod)
  }

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

  val protoFileFilter = new FileFilter {
    def accept(filtered: File) = filtered.getName.endsWith(".proto")
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
    val file = new File(filename)

    if (file.isAbsolute) {
      Option(file).filter(_.exists)
    } else {
      settings.importDirectories.map { folder =>
        new File(folder, filename)
      }.find(_.exists)
    }
  }

  def verbosePrintln(msg: String)(implicit settings: Settings) { if (settings.verbose) println(msg) }


  /**
   * ScalaBuff entry point. If any errors occur, calls System.exit(1).
   */
  def main(args: Array[String]) {
    val success = run(args)

    if (!success) {
      System.exit(1)
    }
  }

  /**
   * Runner: Runs ScalaBuff on the specified resource path(s).
   *
   * @return success: if true, no errors were encountered.
   */
  def run(args: Array[String]): Boolean = {
    args match {
      case noArguments if noArguments.isEmpty => println(Strings.HELP); true

      case arguments: Array[String] =>
        val (rawSettings: Array[String], paths: Array[String]) = arguments.partition(_.startsWith("-"))

        implicit val parsedSettings = rawSettings.foldLeft(Settings()) {
          case (settings, setting) => parseSetting(setting, settings) match {
            case Left(message)      => println(message); settings
            case Right(newSettings) => newSettings
          }
        }

        if (parsedSettings.extraVerbose) {
          println("Parameters: " + rawSettings.mkString(" "))
          println("Paths: " + paths.mkString(" "))
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

        val success = new AtomicBoolean(true)

        for (file <- protoFiles.par) {
          verbosePrintln("Processing: " + file.getAbsolutePath)
          try {
            val scalaClass = apply(file)
            try {
              write(scalaClass)
            } catch {
              // just print the error and continue processing
              case io: IOException =>
                success.set(false)
                println(Strings.CANNOT_WRITE_FILE + scalaClass.path + scalaClass.file + ".scala")
            }
          } catch {
            // just print the error and continue processing
            case pf: ParsingFailureException =>
              success.set(false)
              println(pf.getMessage)
            case io: IOException =>
              success.set(false)
              println(Strings.CANNOT_ACCESS_RESOURCE + file.getAbsolutePath)
          }
        }
        success.get()
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

      case "-v" | "--verbose" if !settings.verbose =>
        settings.copy(verbose = true)

      case "-v" | "--verbose" if settings.verbose =>
        settings.copy(extraVerbose = true)

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

      case "--generate_json_method" =>
        settings.copy(generateJsonMethod = true)

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
