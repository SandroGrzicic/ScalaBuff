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

case class Message(name: String, body: List[Node]) extends Node

case class Extension(name: String, body: List[Node]) extends Node

case class Extensions(list: List[ExtensionRange]) extends Node

case class ExtensionRange(from: Int, to: Int = -1) extends Node

case class Group(label: String, name: String, number: Int, body: List[Node]) extends Node

case class Field(label: String, fieldType: String, name: String, number: Int, fOptions: List[Option]) extends Node

case class EnumStatement(name: String, constants: List[EnumConstant], options: List[Option]) extends Node

case class EnumConstant(name: String, id: Int) extends Node

