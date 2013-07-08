package net.sandrogrzicic.scalabuff.compiler

import util.parsing.combinator._
import util.parsing.input.{ PagedSeqReader, CharSequenceReader }
import collection.immutable.PagedSeq
import collection.mutable.ListBuffer

/**
 * Main Protobuf parser.
 * @author Sandro Gržičić
 */
class Parser(val inputName: String) extends RegexParsers with PackratParsers {

  // skip C/C++ style comments and whitespace.
  override protected val whiteSpace = """((/\*(?:.|\r|\n)*?\*/)|//.*|\s+)+""".r

  lazy val protoParser: PackratParser[List[Node]] = ((message | extension | enumP | importP | packageP | option) *)

  lazy val message: PackratParser[Message] = "message" ~> identifier ~ messageBody ^^ {
		case name ~ body => Message(name, body)
  }

  lazy val messageBody: PackratParser[MessageBody] = ("{" ~> ((field | enumP | message | extension | extensionRanges | group | option) *) <~ "}") ^^ {
		body => parseBody(body)
  }

  lazy val extension: PackratParser[Extension] = ("extend" ~> userType ~ ("{" ~> ((field | group) *) <~ "}")) ^^ {
		case name ~ body => Extension(name, parseBody(body.filter(_.isInstanceOf[Node])))
  }

  lazy val enumP: PackratParser[EnumStatement] = ("enum" ~> identifier <~ "{") ~ ((option | enumField | ";") *) <~ "}" <~ (";" ?) ^^ {
		case name ~ values => {
			val constants = values.view.collect { case constant: EnumConstant => constant }
			val options = values.view.collect { case option: OptionValue => option }
      EnumStatement(name, constants.toList, options.toList)
    }
  }
	lazy val enumField: PackratParser[EnumConstant] = (identifier <~ "=") ~ integerConstant <~ ";" ^^ {
		case name ~ id => EnumConstant(name, id.toInt)
  }

  lazy val importP: PackratParser[ImportStatement] = "import" ~> quotedStringConstant <~ ";" ^^ {
		importedPackage => ImportStatement(importedPackage)
  }

  lazy val packageP: PackratParser[PackageStatement] = "package" ~> (identifier ~ (("." ~ identifier) *)) <~ ";" ^^ {
		case ident ~ idents => PackageStatement(ident + idents.map(i => i._1 + i._2).mkString.stripQuotes)
  }

  lazy val option: PackratParser[OptionValue] = "option" ~> optionBody <~ ";"
  lazy val optionBody: PackratParser[OptionValue] = (("(" ?) ~> identifier ~> (")" ?) ~ (("." ~ identifier) *)) ~ ("=" ~> constant) ^^ {
		case ident ~ idents ~ value => OptionValue(ident + idents.map(i => i._1 + i._2).mkString, value)
  }

	lazy val group: PackratParser[Group] = (label <~ "group") ~ (camelCaseIdentifier <~ "=") ~ integerConstant ~ messageBody ^^ {
		case gLabel ~ name ~ number ~ body => Group(gLabel, name, number.toInt, body)
  }

  lazy val field: PackratParser[Field] = label ~ fieldType ~ (identifier <~ "=") ~ fieldId ~
    (("[" ~> optionBody ~ (("," ~ optionBody) *) <~ "]") ?) <~ ";" ^^ {
			case fLabel ~ fType ~ name ~ number ~ options => {
        val optionList = options match {
					case Some(fOpt ~ fOpts) => List(fOpt) ++ fOpts.map(e => e._2)
					case None => List[OptionValue]()
        }

        Field(fLabel, fType, name, number.toInt, optionList)
      }
    }

  lazy val label: PackratParser[FieldLabels.EnumVal] = ("required" | "optional" | "repeated") ^^ {
		fLabel => FieldLabels(fLabel)
  }

  lazy val fieldType: PackratParser[FieldTypes.EnumVal] = userType ^^ {
		fType => FieldTypes(fType)
  }

  lazy val userType: PackratParser[String] = (("." ?) ~ identifier ~ (("." ~ identifier) *)) ^^ {
		case dot ~ ident ~ idents => dot.getOrElse("") + ident + idents.map(i => i._1 + i._2).mkString
  }

  lazy val extensionRanges: PackratParser[ExtensionRanges] = "extensions" ~> extensionRange ~ (("," ~ extensionRange) *) <~ ";" ^^ {
		case ext ~ exts => ExtensionRanges(List(ext) ++ exts.map(e => e._2))
  }
	lazy val extensionRange: PackratParser[ExtensionRange] = integerConstant ~ (("to" ~> (integerConstant | "max")) ?) ^^ {
		case from ~ to => to match {
			case Some(int) => int match {
				case "max" => ExtensionRange(from.toInt)
				case i => ExtensionRange(from.toInt, i.toInt)
      }
			case None => ExtensionRange(from.toInt, from.toInt)
    }
  }

  lazy val constant: PackratParser[String] = identifier | floatConstant | integerConstant | quotedStringConstant | stringConstant | booleanConstant

  lazy val identifier: PackratParser[String] = memo("""[A-Za-z_][\w_]*""".r)

  lazy val camelCaseIdentifier: PackratParser[String] = memo("""[A-Z][\w_]*""".r)

  lazy val fieldId: PackratParser[String] = hexadecimalInteger | octalInteger | positiveInteger

  lazy val integerConstant: PackratParser[String] = hexadecimalInteger | octalInteger | decimalInteger
  lazy val decimalInteger: PackratParser[String] = memo("""-?\d+""".r)
  lazy val positiveInteger: PackratParser[String] = memo("""\d+""".r)
  lazy val hexadecimalInteger: PackratParser[String] = memo("""(0[xX])[A-Fa-f0-9]+""".r) ^^ {
    hexStr => Integer.parseInt(hexStr.drop(2), 16).toString
  }
  lazy val octalInteger: PackratParser[String] = memo("""0[0-7]+""".r) ^^ {
    octStr => Integer.parseInt(octStr, 8).toString
  }
  lazy val floatConstant: PackratParser[String] = memo("""-?\d+\.\d+([Ee][+-]\d+)?""".r)
  lazy val booleanConstant: PackratParser[String] = "true" | "false"
  lazy val stringConstant: PackratParser[String] = ((hexEscape | octEscape | charEscape | stringCharacter) *) ^^ {
    string: List[String] => string.mkString
  }
  lazy val quotedStringConstant: PackratParser[String] = quotationMarks ~> ((hexEscape | octEscape | charEscape | stringCharacter) *) <~ quotationMarks ^^ {
    string: List[String] => "\"" + string.mkString + "\""
  }
  lazy val stringCharacter: PackratParser[String] = memo("""[^"\n]""".r)
  lazy val quotationMarks: PackratParser[String] = memo("""["']""".r)
  lazy val hexEscape: PackratParser[String] = memo("""\\0[Xx][A-Fa-f0-9]{1,2}""".r)
  lazy val octEscape: PackratParser[String] = memo("""\\0?[0-7]{1,3}""".r)
  lazy val charEscape: PackratParser[String] = memo("""\\[abfnrtv\\\?'"]""".r)


  /**
   * Parsing helper, parses the body of a Message or Extension.
   */
  def parseBody(body: List[Node]): MessageBody = {

    val fields = ListBuffer[Field]()
    val enums = ListBuffer[EnumStatement]()
    val messages = ListBuffer[Message]()
    val groups = ListBuffer[Group]()
    val extensionRanges = ListBuffer[ExtensionRanges]()
    val options = ListBuffer[OptionValue]()
    val extensions = ListBuffer[Extension]()

		for (node <- body) node match {
			case n: Field => fields += n
			case n: EnumStatement => enums += n
			case n: Message => messages += n
			case n: ExtensionRanges => extensionRanges += n
			case n: Extension => extensions += n
			case n: Group => groups += n
			case n: OptionValue => options += n
			case _ => require(false, "Impossible node type found.")
    }

    MessageBody(
      fields.toList, enums.toList, messages.toList,
			extensionRanges.toList, extensions.toList, groups.toList, options.toList
		)
  }


  /**
   * Parse the given input as a .proto file.
   */
  def protoParse(input: Input): List[Node] = {
    phrase(protoParser)(input) match {
			case Success(tree, _) => tree
			case NoSuccess(error, element) => throw new ParsingFailureException(parsingError(error, element))
    }
  }

  /**
   * Returns the parsing failure details.
   */
  def parsingError(error: String, element: Input) = {
    inputName + ":" + element.pos.line + ":" + element.pos.column + ": " + error + "\n" +
      element.pos.longString
  }
}

object Parser {
  /**
   * Parse the given Reader input as a .proto file and return the resulting parse tree.
   */
  def apply(input: java.io.Reader, name: String): List[Node] = {
    new Parser(name).protoParse(new PagedSeqReader(PagedSeq.fromReader(input)))
  }
  /**
   * Parse the given Reader input as a .proto file and return the resulting parse tree.
   */
  def apply(input: java.io.Reader): List[Node] = apply(input, Strings.UNKNOWN_INPUT)

  /**
   * Parse the given InputStream input as a .proto file and return the resulting parse tree.
   */
  def apply(input: java.io.InputStream, name: String, encoding: String): List[Node] = {
    apply(new java.io.BufferedReader(new java.io.InputStreamReader(input, encoding)), name)
  }
  /**
   * Parse the given InputStream input as a .proto file and return the resulting parse tree.
   */
  def apply(input: java.io.InputStream, encoding: String = "utf-8"): List[Node] = apply(input, Strings.UNKNOWN_INPUT, "utf-8")

  /**
   * Parse the given File input as a .proto file and return the resulting parse tree.
   */
  def apply(input: java.io.File): List[Node] = {
    val fis = new java.io.FileInputStream(input)
    try { apply(fis, input.getName) } finally { fis.close() }
  }

  /**
   * Parse the given String input as a .proto file and return the resulting parse tree.
   */
  def apply(input: String, name: String): List[Node] =
    new Parser(name).protoParse(new CharSequenceReader(input))

  /**
   * Parse the given String input as a .proto file and return the resulting parse tree.
   */
  def apply(input: String): List[Node] = apply(input, Strings.UNKNOWN_INPUT)

}

/**
 * Thrown when an input .proto file cannot be parsed successfully by the Parser.
 */
class ParsingFailureException(message: String) extends RuntimeException(message)
