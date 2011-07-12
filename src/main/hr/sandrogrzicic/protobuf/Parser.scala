package hr.sandrogrzicic.protobuf

import util.parsing.combinator._

/**
 * Main Protobuf parser.
 * @author Sandro Gržičić
 */
object Parser extends RegexParsers with ImplicitConversions {

	// skip C/C++ style comments and whitespace.
	override protected val whiteSpace = """\s*(//.*\r*\n*\s*)+|\s*/\*(.|\r|\n)*\*/\s*|\s+""".r


	// root protobuf parser
	def proto = (message | extend_ | enum_ | import_ | package_ | option | ";")*


	def message: Parser[Any] = "message" ~ Identifier ~ messageBody

	def extend_ = "extend" ~ userType ~ "{" ~ ((field | group | ";")*) ~ "}"

	def enum_ = "enum" ~ Identifier ~ "{" ~ ((option | enumField | ";")*) ~ "}"
	def enumField = Identifier ~ "=" ~ Integer ~ ";"

	def import_ = "import" ~ StringConstant ~ ";"

	def package_ = "package" ~ Identifier ~ (("." ~ Identifier)*) ~ ";"

	def option = "option" ~ optionBody ~ ";"
	def optionBody = Identifier ~ (("." ~ Identifier)*) ~ "=" ~ Constant

	def group = label ~ "group" ~ CamelCaseIdentifier ~ "=" ~ Integer ~ messageBody

	def messageBody: Parser[Any] = "{" ~ ((field | enum_ | message | extend_ | extensions | group | option | ":")*) ~ "}"

	def field = label ~ fieldType ~ Identifier ~ "=" ~ Integer ~
		(("[" ~ fieldOption ~ ("," ~ (fieldOption)*) ~ "]")?) ~ ";"

	def label = "required" | "optional" | "repeated"

	def extensions = "extensions" ~ extension ~ (("," ~ extension)*) ~ ";"
	def extension = Integer ~ ("to" ~ (Integer | "max"))?

	def fieldOption = optionBody | ("default" ~ "=" ~ Constant)

	def fieldType = "double" | "float" | "int32" | "int64" | "uint32" | "uint64" |
		   "sint32" | "sint64" | "fixed32" | "fixed64" | "sfixed32" | "sfixed64" |
		   "bool" | "string" | "bytes" | userType

	def userType = ("."?) ~ Identifier ~ ("." ~ Identifier)*

	def Constant = Identifier | Integer | FloatingPoint | StringConstant | Bool
	def Identifier = """[A-Za-z_][\w_]*""".r
	def CamelCaseIdentifier = """[A-Z][\w_]*""".r
	def Integer = DecimalInteger | HexadecimalInteger | OctalInteger
	def DecimalInteger = """[1-9]\d*""".r
	def HexadecimalInteger = """0[xX]([A-Fa-f0-9])+""".r
	def OctalInteger = """0[0-7]+""".r
	def FloatingPoint = """\d+(\.\d+)?([Ee][\+-]?\d+)?""".r
	def Bool = "true" | "false"
	def StringConstant = QuotationMarks ~> ((HexEscape | OctEscape | CharEscape | StringConstantStr)*) <~ QuotationMarks
	def StringConstantStr = """[^\0\n]""".r
	def QuotationMarks = """["']""".r
	def HexEscape = """\\[Xx][A-Fa-f0-9]{1,2}""".r
	def OctEscape = """\\0?[0-7]{1,3}""".r
	def CharEscape = """\\[abfnrtv\\\?'"]""".r


	/**
	 * Parse the given Protobuf Reader.
	 */
	def apply(input: java.io.Reader) = {
		parseAll(proto, input) match {
			case Success(tree, _) => tree
			case NoSuccess(error, element) => parsingError(error, element)
		}
	}

	/**
	* Parse the given Protobuf input.
	 */
	def apply(input: String) = {
		parseAll(proto, input) match {
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