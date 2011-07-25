package hr.sandrogrzicic.scalabuff

import util.parsing.combinator._
import util.parsing.input.{PagedSeqReader, CharSequenceReader}
import collection.immutable.PagedSeq

/**
 * Main Protobuf parser.
 * @author Sandro Gržičić
 */
object Parser extends RegexParsers with ImplicitConversions with PackratParsers {

	var fileName = "?"

	// skip C/C++ style comments and whitespace.
	override protected val whiteSpace = """((/\*(?:.|\r|\n)*?\*/)|//.*|\s+)+""".r


	lazy val protoParser: PackratParser[Any] = ((message | extendP | enumP | importP | packageP | option | ";") *)

	lazy val message: PackratParser[Any] = "message" ~> identifier ~ messageBody ^^ {
		case identifier ~ body => Message(identifier.toString, body)
	}

	lazy val extendP: PackratParser[Any] = "extend" ~> userType ~ ("{" ~> ((field | group | ";") *) <~ "}")

	lazy val enumP: PackratParser[Any] = "enum" ~> identifier ~ "{" ~ ((option | enumField | ";") *) <~ "}"
	lazy val enumField: PackratParser[Any] = (identifier <~ "=") ~ integerConstant <~ ";"

	lazy val importP: PackratParser[ImportStatement] = "import" ~> stringConstant <~ ";" ^^ {
		importedPackage => ImportStatement(importedPackage)
	}

	lazy val packageP: PackratParser[PackageStatement] = "package" ~> (identifier ~ (("." ~ identifier) *)) <~ ";" ^^ {
		protoPackage => PackageStatement(protoPackage._1 + protoPackage._2.mkString)
	}

	lazy val option: PackratParser[Any] = "option" ~> optionBody <~ ";"
	lazy val optionBody: PackratParser[Any] = identifier ~ (("." ~ identifier) *) ~ "=" ~ constant

	lazy val group: PackratParser[Any] = (label <~ "group") ~ (camelCaseIdentifier <~ "=") ~ integerConstant ~ messageBody

	lazy val messageBody: PackratParser[Any] = "{" ~> ((field | enumP | message | extendP | extensions | group | option | ":") *) <~ "}"

	lazy val field: PackratParser[Any] = label ~ fieldType ~ (identifier <~ "=") ~ integerConstant ~
		(("[" ~> fieldOption ~ (("," ~ fieldOption) *) <~ "]") ?) <~ ";"

	lazy val label: PackratParser[String] = "required" | "optional" | "repeated"

	lazy val extensions: PackratParser[Any] = "extensions" ~> extension ~ (("," ~ extension) *) <~ ";"
	lazy val extension: PackratParser[Any] = integerConstant ~ (("to" ~ (integerConstant | "max")) ?)

	lazy val fieldOption: PackratParser[Any] = optionBody | ("default" ~> "=" ~> constant)

	lazy val fieldType: PackratParser[Any] = "double" | "float" | "int32" | "int64" | "uint32" | "uint64" |
		"sint32" | "sint64" | "fixed32" | "fixed64" | "sfixed32" | "sfixed64" |
		"bool" | "string" | "bytes" | userType

	lazy val userType: PackratParser[Any] = (("." ?) ~ identifier ~ (("." ~ identifier) *))

	lazy val constant: PackratParser[Any] = identifier | integerConstant | floatConstant | stringConstant | booleanConstant
	lazy val identifier: PackratParser[String] = memo("""[A-Za-z_][\w_]*""".r)
	lazy val camelCaseIdentifier: PackratParser[String] = memo("""[A-Z][\w_]*""".r)

	lazy val integerConstant: PackratParser[Int] = decimalInteger | hexadecimalInteger | octalInteger
	lazy val decimalInteger: PackratParser[Int] = memo("""[1-9]\d*""".r) ^^ {
		int => int.toInt
	}
	lazy val hexadecimalInteger: PackratParser[Int] = memo("""0[xX]([A-Fa-f0-9])+""".r) ^^ {
		int => Integer.parseInt(int, 16)
	}
	lazy val octalInteger: PackratParser[Int] = memo("""0[0-7]+""".r) ^^ {
		int => Integer.parseInt(int, 8)
	}
	lazy val floatConstant: PackratParser[Double] = memo("""\d+(\.\d+)?([Ee][\+-]?\d+)?""".r) ^^ {
		float => float.toDouble
	}
	lazy val booleanConstant: PackratParser[Boolean] = ("true" | "false") ^^ {
		bool => bool.toBoolean
	}
	lazy val stringConstant: PackratParser[String] = quotationMarks ~> ((hexEscape | octEscape | charEscape | stringCharacter) *) <~ quotationMarks ^^ {
		string: List[String] => string.mkString
	}
	lazy val stringCharacter: PackratParser[String] = memo("""[^"\n]""".r)
	lazy val quotationMarks: PackratParser[String] = memo("""["']""".r)
	lazy val hexEscape: PackratParser[String] = memo("""\\[Xx][A-Fa-f0-9]{1,2}""".r)
	lazy val octEscape: PackratParser[String] = memo("""\\0?[0-7]{1,3}""".r)
	lazy val charEscape: PackratParser[String] = memo("""\\[abfnrtv\\\?'"]""".r)


	/**
	 * Parse the given input as a .proto file.
	 */
	def protoParse(input: Input) = {
		phrase(protoParser)(input) match {
			case Success(tree, _) => tree
			case NoSuccess(error, element) => parsingError(error, element)
		}
	}

	/**
	 * Parse the given Reader input as a .proto file.
	 */
	def apply(input: java.io.Reader) = protoParse(new PagedSeqReader(PagedSeq.fromReader(input)))

	/**
	 * Parse the given File input as a .proto file.
	 */
	def apply(input: java.io.File) = {
		fileName = input.getName
		protoParse(new PagedSeqReader(PagedSeq.fromFile(input)))
	}

	/**
	 * Parse the given String input as a .proto file.
	 */
	def apply(input: String) = protoParse(new CharSequenceReader(input))


	/**
	 * Returns a parsing error.
	 */
	def parsingError(error: String, element: Input) = {
		fileName + ":" + element.pos.line + ":" + element.pos.column + ": " + error + "\n" +
			element.pos.longString
	}

}