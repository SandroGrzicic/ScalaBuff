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

case class OptionStatement(option: OptionBody) extends Node

case class OptionBody(key: String, value: String) extends Node

case class Message(name: String, body: Any) extends Node

case class UserType(userType: String) extends Node

case class Extension(from: Int, to: Int = -1) extends Node