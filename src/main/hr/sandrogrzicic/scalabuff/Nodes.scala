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

case class Message(name: String, body: Any) extends Node