package hr.sandrogrzicic.protobuf

import util.parsing.combinator._
import util.parsing.input.{PagedSeqReader, CharSequenceReader}
import collection.immutable.PagedSeq

/**
 * Main Protobuf parser.
 * @author Sandro Gržičić
 */
object Parser extends RegexParsers with ImplicitConversions with PackratParsers {

	// skip C/C++ style comments and whitespace.
	override protected val whiteSpace = """\s*(//.*\r*\n*\s*)+|\s*/\*(.|\r|\n)*\*/\s*|\s+""".r


	// root protobuf parser
	lazy val proto: PackratParser[Any] = (message | extendP | enumP | importP | packageP | option | ";")*


	lazy val message: PackratParser[Any] = "message" ~ Identifier ~ messageBody

	lazy val extendP: PackratParser[Any] = "extend" ~ userType ~ "{" ~ ((field | group | ";")*) ~ "}"

	lazy val enumP: PackratParser[Any] = "enum" ~ Identifier ~ "{" ~ ((option | enumField | ";")*) ~ "}"
	lazy val enumField: PackratParser[Any] = Identifier ~ "=" ~ Integer ~ ";"

	lazy val importP: PackratParser[Any] = "import" ~ StringConstant ~ ";"

	lazy val packageP: PackratParser[Any] = "package" ~ Identifier ~ (("." ~ Identifier)*) ~ ";"

	lazy val option: PackratParser[Any] = "option" ~ optionBody ~ ";"
	lazy val optionBody: PackratParser[Any] = Identifier ~ (("." ~ Identifier)*) ~ "=" ~ Constant

	lazy val group: PackratParser[Any] = label ~ "group" ~ CamelCaseIdentifier ~ "=" ~ Integer ~ messageBody

	lazy val messageBody: PackratParser[Any] = "{" ~ ((field | enumP | message | extendP | extensions | group | option | ":")*) ~ "}"

	lazy val field: PackratParser[Any] = label ~ fieldType ~ Identifier ~ "=" ~ Integer ~
		(("[" ~ fieldOption ~ ("," ~ (fieldOption)*) ~ "]")?) ~ ";"

	lazy val label: PackratParser[Any] = "required" | "optional" | "repeated"

	lazy val extensions: PackratParser[Any] = "extensions" ~ extension ~ (("," ~ extension)*) ~ ";"
	lazy val extension: PackratParser[Any] = Integer ~ ("to" ~ (Integer | "max"))?

	lazy val fieldOption: PackratParser[Any] = optionBody | ("default" ~ "=" ~ Constant)

	lazy val fieldType: PackratParser[Any] = "double" | "float" | "int32" | "int64" | "uint32" | "uint64" |
		   "sint32" | "sint64" | "fixed32" | "fixed64" | "sfixed32" | "sfixed64" |
		   "bool" | "string" | "bytes" | userType

	lazy val userType: PackratParser[Any] = ("."?) ~ Identifier ~ (("." ~ Identifier)*)

	lazy val Constant: PackratParser[Any] = Identifier | Integer | FloatingPoint | StringConstant | Bool
	lazy val Identifier: PackratParser[Any] = memo("""[A-Za-z_][\w_]*""".r)
	lazy val CamelCaseIdentifier: PackratParser[Any] = memo("""[A-Z][\w_]*""".r)
	lazy val Integer: PackratParser[Any] = DecimalInteger | HexadecimalInteger | OctalInteger
	lazy val DecimalInteger: PackratParser[Any] = memo("""[1-9]\d*""".r)
	lazy val HexadecimalInteger: PackratParser[Any] = memo("""0[xX]([A-Fa-f0-9])+""".r)
	lazy val OctalInteger: PackratParser[Any] = memo("""0[0-7]+""".r)
	lazy val FloatingPoint: PackratParser[Any] = memo("""\d+(\.\d+)?([Ee][\+-]?\d+)?""".r)
	lazy val Bool: PackratParser[Any] = "true" | "false"
	lazy val StringConstant: PackratParser[Any] = QuotationMarks ~> ((HexEscape | OctEscape | CharEscape | StringCharacter)*) <~ QuotationMarks
	lazy val StringCharacter: PackratParser[Any] = memo("""[^"\n]""".r)
	lazy val QuotationMarks: PackratParser[Any] = memo("""["']""".r)
	lazy val HexEscape: PackratParser[Any] = memo("""\\[Xx][A-Fa-f0-9]{1,2}""".r)
	lazy val OctEscape: PackratParser[Any] = memo("""\\0?[0-7]{1,3}""".r)
	lazy val CharEscape: PackratParser[Any] = memo("""\\[abfnrtv\\\?'"]""".r)


	/**
	 * Parse the given Reader input as a protobuf file.
	 */
	def apply(input: java.io.Reader) = protoParse(new PagedSeqReader(PagedSeq.fromReader(input)))

	/**
	 * Parse the given String input as a protobuf file.
	 */
	def apply(input: String) = protoParse(new CharSequenceReader(input))

	/**
	 * Parse the given input as a protobuf file.
	 */
	def protoParse(input: Input) = {
		phrase(proto)(input) match {
			case Success(tree, _) => tree
			case NoSuccess(error, element) => parsingError(error, element)
		}
	}

	/**
	 * Returns a parsing error.
	 */
	def parsingError(error: String, element: Input) = {
		"Error while attempting to parse input at " +
		"line [" + element.pos.line + "], column [" + element.pos.column + "]:\n\t[" +
          	error + "]\n" +
		element.pos.longString
	}

}