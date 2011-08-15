package hr.sandrogrzicic.scalabuff

import collection.mutable.{ListBuffer, StringBuilder}
import annotation.tailrec
import java.io._

/**
 * Scala class generator.
 * @author Sandro Gržičić
 */

class Generator protected(sourceName: String, reader: Reader) {

	implicit def buffString(string: String): BuffedString = new BuffedString(string)

	val imports = ListBuffer[String]()

	var packageName: String = ""
	var className: String = sourceName.takeUntilFirst('.').camelCase

	/**
	 * Converts a .proto field type to a valid Java type.
	 */
	def fieldType(fType: String) = fType match {
		case "int32" | "uint32" | "sint32" | "fixed32" | "sfixed32" => "Int"
		case "int64" | "uint64" | "sint64" | "fixed64" | "sfixed64" => "Long"
		case "bool" => "Boolean"
		case "float" => "Float"
		case "double" => "Double"
		case "bytes" => "Array[Byte]"
		case "string" => "String"
		case other => other
	}

	/**
	 * Generates the Scala class code.
	 */
	protected def generate(tree: List[Node]) = {
		/**
		 * EnumStatement -> Enumeration
		 */
		def enum(enum: EnumStatement, indentLevel: Int = 0) = {
			val indentOuter = "\t" * (indentLevel + 1)
			val indent = indentOuter + "\t"

			val out = StringBuilder.newBuilder
			out
				.append(indentOuter).append("object ").append(enum.name).append(" extends hr.sandrogrzicic.scalabuff.runtime.Enum {\n")
				.append(indent).append("sealed trait EnumVal extends Value\n")
				.append(indent).append("\t\n")

			for (enumOption <- enum.options) {
				// options support?
			}
			// declaration of constants
			for (const <- enum.constants) {
				out.append(indent)
					.append("val ").append(const.name).append(" = new EnumVal { ")
					.append("val name = \"").append(const.name).append("\"; ")
					.append("val id = ").append(const.id)
					.append(" }\n")
			}
			out.append("\n")
			// constants, as statics
			for (const <- enum.constants) {
				out.append(indent).append("val ").append(const.name).append("_VALUE = ").append(const.id).append("\n")
			}
			// valueOf
			out.append("\n").append(indent).append("def valueOf(id: Int) = id match {\n")
			for (const <- enum.constants) {
				out.append(indent).append("\t")
					.append("case ").append(const.id).append(" => ").append(const.name).append("\n")
			}
			out.append(indent).append("}\n\n")
			// internalGetValueMap
			out.append(indent).append("val internalGetValueMap = new com.google.protobuf.Internal.EnumLiteMap[")
				.append(enum.name).append("] {\n")
				.append(indent).append("\tdef findValueByNumber(id: Int) = valueOf(id)\n")
				.append(indent).append("}\n")

			out.append(indentOuter).append("}\n")

			out.mkString
		}


		/**
		 * Traverse the message sub-tree, using recursion on nested messages.
		 * Not tail-recursive, but shouldn't cause stack overflows on sane nesting levels.
		 */
		def message(name: String, body: MessageBody, indentLevel: Int = 0): String = {
			val indentOuter = "\t" * (indentLevel + 1)
			val indent = indentOuter + "\t"

			body.options.foreach {
				case Option(key, value) => // ignored: no options supported yet
			}

			body.extensionRanges.foreach {
				case ExtensionRanges(extensionRanges) => // not supported yet
			}

			val out = StringBuilder.newBuilder

			// builder
			out
				.append(indentOuter).append("trait ").append(name)
				.append("OrBuilder extends com.google.protobuf.MessageLiteOrBuilder {\n\n")
			for (field <- body.fields) {
				out
					.append(indent).append("def has").append(field.name.camelCase).append(": Boolean\n")
					.append(indent).append("def get").append(field.name.camelCase)
					.append(": ").append(fieldType(field.fType)).append("\n")
					.append("\n")
			}
			out.append(indentOuter).append("}\n\n")

			// case class
			out
				.append(indentOuter).append("case class ").append(name).append("(")
				.append(") extends com.google.protobuf.GeneratedMessageLite with ")
				.append(name).append("OrBuilder {\n")

				// to do: case class body

			out.append(indentOuter).append("}\n\n")

			body.enums.foreach {
				e => out.append(enum(e, indentLevel)).append("\n")
			}
			body.groups.foreach {
				case Group(label, nestedName, id, nestedBody) => // not supported yet (also, deprecated..)
			}
			body.extensions.foreach {
				case Extension(nestedName, nestedBody) => // not supported yet
			}
			body.messages.foreach {
				case Message(nestedName, nestedBody) => out.append(message(nestedName, nestedBody, indentLevel + 1))
			}

			out.mkString
		}

		/**
		 * Recursively traverse the tree.
		 */
		@tailrec
		def traverse(tree: List[Node], output: StringBuilder = StringBuilder.newBuilder): String = {
			tree match {
				// if the tree is empty, return the generated output
				case Nil => output.mkString
				// else, build upon the output and call traverse() again with the tree tail
				case node :: tail => node match {
					case Message(name, body) => output.append(message(name, body))
					case Extension(name, body) => // very similar to Message
					case e: EnumStatement => output.append(enum(e))
					case ImportStatement(name) => imports += name
					case PackageStatement(name) => packageName = name
					case Option(key, value) => key match {
						case "java_package" => packageName = value
						case "scala_package" => packageName = value
						case "java_outer_classname" => className = value
						case "scala_outer_classname" => className = value
						case _ => // ignore options which aren't recognized
					}
					case _ => throw new UnexpectedNodeException(node)
				}
				traverse(tail, output)
			}
		}


		// traverse the tree now, so we can get class/package names, options, etc.
		val generated = traverse(tree)

		val output = StringBuilder.newBuilder

		output
			.append("// Generated by ScalaBuff, the Scala protocol buffer compiler. DO NOT EDIT!\n")
			.append("// source: ")
			.append(sourceName)
			.append("\n\n")

		if (!packageName.isEmpty)
			output.append("package ").append(packageName).append("\n\n")

		output.append("object ").append(className).append(" {\n")

		output
			.append(generated)
			.append("\n\tdef registerAllExtensions(registry: com.google.protobuf.ExtensionRegistryLite) {\n")
			.append("\t}\n\n")
			.append("}")

		ScalaClass(output.mkString, packageName.replace('.', '/') + '/', className)
	}


}


object Generator {
	/**
	 * Returns a valid Scala class.
	 */
	def apply(tree: List[Node], sourceName: String, sourceReader: Reader): ScalaClass = {
		new Generator(sourceName, sourceReader).generate(tree)
	}

	/**
	 * Returns a valid Scala class.
	 */
	def apply(tree: List[Node], sourceName: String, sourceFile: File): ScalaClass = {
		apply(tree, sourceName, new BufferedReader(new InputStreamReader(new FileInputStream(sourceFile), "utf-8")))
	}

}

/**
 * A generated Scala class. The path is relative.
 * @author Sandro Gržičić
 */
case class ScalaClass(body: String, path: String, file: String) {
	assert(path.endsWith("/"), "path must end with a /")
	assert(!file.isEmpty, "file name must not be empty")
	assert(!file.contains("/"), "file name must not contain a /")
}

/**
 * Thrown when a valid Scala class cannot be generated using the the tree returned from the Parser.
 */
class GenerationFailureException(message: String) extends RuntimeException(message)

/**
 * Thrown when a Node occurs in an unexpected location in the tree.
 */
class UnexpectedNodeException(node: Node, parentNode: Node = null) extends GenerationFailureException(
	"Unexpected child node " + node.toString + parentNode match {
		case null => ""
		case _ => "found in " + parentNode.toString
	}
)