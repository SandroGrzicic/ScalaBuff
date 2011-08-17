package hr.sandrogrzicic.scalabuff

/**
 * Nodes produced by the Parser.
 * @author Sandro Gržičić
 */

/**
 * AST node.
 */
sealed abstract class Node

case class ImportStatement(packageName: String) extends Node

case class PackageStatement(packageName: String) extends Node

case class Option(key: String, value: String) extends Node

case class MessageBody(
		fields: List[Field], enums: List[EnumStatement], messages: List[Message],
		extensionRanges: List[ExtensionRanges], extensions: List[Extension],
 		groups: List[Group], options: List[Option]
)

case class Message(name: String, body: MessageBody) extends Node

case class Extension(name: String, body: MessageBody) extends Node

case class ExtensionRanges(list: List[ExtensionRange]) extends Node

case class ExtensionRange(from: Int, to: Int = -1) extends Node

case class Group(label: FieldLabels.EnumVal, name: String, number: Int, body: MessageBody) extends Node

case class Field(label: FieldLabels.EnumVal, fType: FieldTypes.EnumVal, name: String, number: Int, options: List[Option]) extends Node

case class EnumStatement(name: String, constants: List[EnumConstant], options: List[Option]) extends Node

case class EnumConstant(name: String, id: Int) extends Node

