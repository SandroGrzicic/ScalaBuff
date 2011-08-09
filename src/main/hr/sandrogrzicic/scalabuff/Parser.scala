package hr.sandrogrzicic.scalabuff

import util.parsing.combinator._
import util.parsing.input.{PagedSeqReader, CharSequenceReader}
import collection.immutable.PagedSeq

/**
 * Main Protobuf parser.
 * @author Sandro Gržičić
 */
class Parser(filename: String) extends RegexParsers with ImplicitConversions with PackratParsers {

	// skip C/C++ style comments and whitespace.
	override protected val whiteSpace = """((/\*(?:.|\r|\n)*?\*/)|//.*|\s+)+""".r

	lazy val protoParser: PackratParser[List[Node]] = ((message | extendP | enumP | importP | packageP | option) *)

	lazy val message: PackratParser[Message] = "message" ~> identifier ~ messageBody ^^ {
		case name ~ body => Message(name, body)
	}

	lazy val extendP: PackratParser[Extension] = ("extend" ~> userType ~ ("{" ~> (((field <~ ";") | group) *) <~ "}")) ^^ {
		case name ~ body => Extension(name, body.filter(_.isInstanceOf[Node]))
	}

	lazy val enumP: PackratParser[EnumStatement] = ("enum" ~> identifier <~ "{" ) ~ ((option | enumField | ";") *) <~ "}" <~ (";" ?) ^^ {
		case name ~ values => {
			val constants = values.view.collect { case constant: EnumConstant => constant }
			val options = values.view.collect { case option: Option => option }
			EnumStatement(name, constants.toList, options.toList)
		}
	}
	lazy val enumField: PackratParser[EnumConstant] = (identifier <~ "=") ~ integerConstant <~ ";" ^^ {
		case name ~ id => EnumConstant(name, id.toInt)
	}

	lazy val importP: PackratParser[ImportStatement] = "import" ~> stringConstant <~ ";" ^^ {
		importedPackage => ImportStatement(importedPackage)
	}

	lazy val packageP: PackratParser[PackageStatement] = "package" ~> (identifier ~ (("." ~ identifier) *)) <~ ";" ^^ {
		protoPackage => PackageStatement(protoPackage._1 + protoPackage._2.mkString)
	}

	lazy val option: PackratParser[Option] = "option" ~> optionBody <~ ";"
	lazy val optionBody: PackratParser[Option] = (("(" ?) ~> identifier ~ (("." ~ identifier) *) <~ (")" ?)) ~ ("=" ~> constant) ^^ {
		case ident ~ idents ~ value => Option(ident + idents.mkString, value)
	}

	lazy val group: PackratParser[Node] = (label <~ "group") ~ (camelCaseIdentifier <~ "=") ~ integerConstant ~ messageBody ^^ {
		case gLabel ~ name ~ number ~ body => Group(gLabel, name, number.toInt, body)
	}

	lazy val messageBody: PackratParser[List[Node]] = "{" ~> ((field | enumP | message | extendP | extensions | group | option) *) <~ "}"

	lazy val field: PackratParser[Field] = label ~ fieldType ~ (identifier <~ "=") ~ integerConstant ~
		(("[" ~> fieldOption ~ (("," ~ fieldOption) *) <~ "]") ?) <~ ";" ^^ {
		case fLabel ~ fType ~ name ~ number ~ options => Field(fLabel, fType, name, number.toInt, options match {
			case Some(fOpt ~ fOpts) => List(fOpt) ++ fOpts.map(e => e._2)
			case None => List[Option]()
		})
	}

	lazy val label: PackratParser[String] = "required" | "optional" | "repeated"

	lazy val fieldOption: PackratParser[Option] = optionBody |
		("default" ~> "=" ~> constant) ^^ {
			value => Option("default", value)
		}

	lazy val fieldType: PackratParser[String] = "double" | "float" | "int32" | "int64" | "uint32" | "uint64" |
		"sint32" | "sint64" | "fixed32" | "fixed64" | "sfixed32" | "sfixed64" |
		"bool" | "string" | "bytes" | userType


	lazy val userType: PackratParser[String] = (("." ?) ~ identifier ~ (("." ~ identifier) *)) ^^ {
		case dot ~ ident ~ idents => dot.getOrElse("") + ident + idents.mkString
	}

	lazy val extensions: PackratParser[Extensions] = "extensions" ~> extension ~ (("," ~ extension) *) <~ ";" ^^ {
		case ext ~ exts => Extensions(List(ext) ++ exts.map(e => e._2))
	}
	lazy val extension: PackratParser[ExtensionRange] = integerConstant ~ (("to" ~> (integerConstant | "max")) ?) ^^ {
		case from ~ to => to match {
			case Some(int) => int match {
				case "max" => ExtensionRange(from.toInt)
				case i => ExtensionRange(from.toInt, i.toInt)
			}
			case None => ExtensionRange(from.toInt, from.toInt)
		}
	}

	lazy val constant: PackratParser[String] = identifier | integerConstant | floatConstant | stringConstant | booleanConstant

	lazy val identifier: PackratParser[String] = memo("""[A-Za-z_][\w_]*""".r)
	lazy val camelCaseIdentifier: PackratParser[String] = memo("""[A-Z][\w_]*""".r)

	lazy val integerConstant: PackratParser[String] = hexadecimalInteger | octalInteger | decimalInteger
	lazy val decimalInteger: PackratParser[String] = memo("""[0-9]\d*""".r)
	lazy val hexadecimalInteger: PackratParser[String] = memo("""0[xX]([A-Fa-f0-9])+""".r) ^^ {
		int => Integer.parseInt(int, 16).toString
	}
	lazy val octalInteger: PackratParser[String] = memo("""0[0-7]+""".r) ^^ {
		int => Integer.parseInt(int, 8).toString
	}
	lazy val floatConstant: PackratParser[String] = memo("""\d+(\.\d+)?([Ee][\+-]?\d+)?""".r)
	lazy val booleanConstant: PackratParser[String] = "true" | "false"
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
			case NoSuccess(error, element) => throw new ParsingFailureException(parsingError(error, element))
		}
	}

	/**
	 * Returns the parsing failure details.
	 */
	def parsingError(error: String, element: Input) = {
		filename + ":" + element.pos.line + ":" + element.pos.column + ": " + error + "\n" +
			element.pos.longString
	}
}

object Parser {
	/**
	 * Parse the given Reader input as a .proto file.
	 */
	def apply(input: java.io.Reader) = new Parser("unknown").protoParse(new PagedSeqReader(PagedSeq.fromReader(input)))

	/**
	 * Parse the given File input as a .proto file.
	 */
	def apply(input: java.io.File) = new Parser(input.getName)
		.protoParse(new PagedSeqReader(PagedSeq.fromFile(input)))

	/**
	 * Parse the given String input as a .proto file.
	 */
	def apply(input: String) = new Parser("unknown").protoParse(new CharSequenceReader(input))
}


/**
 * Thrown when an input .proto file cannot be parsed successfully by the Parser.
 */
class ParsingFailureException(message: String) extends RuntimeException(message)

