package hr.sandrogrzicic.scalabuff

import annotation.tailrec
import java.io._
import collection.mutable.{ListBuffer, StringBuilder}
import com.google.protobuf._

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
	 * Whether to optimize the resultant class for speed (true) or for code size (false). True by default.
	 */
	var optimizeForSpeed = true

	/**
	 * Generates the Scala class code.
	 */
	protected def generate(tree: List[Node]) = {


		// *******************
		//   utility methods
		// *******************

		/**
		 * Enum generation
		 */
		def enum(enum: EnumStatement, indentLevel: Int = 0) = {
			val indentOuter = BuffedString.indent(indentLevel + 1)
			val indent = indentOuter + "\t"

			val out = StringBuilder.newBuilder
			out
				.append(indentOuter).append("object ").append(enum.name).append(" extends hr.sandrogrzicic.scalabuff.runtime.Enum {\n")
				.append(indent).append("sealed trait EnumVal extends Value\n")
				.append(indent).append("\t\n")

			for (enumOption <- enum.options) {
				// options?
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
			out.append("\n").append(indent).append("def valueOf(id: Int) = ")
			if (optimizeForSpeed) {	// O(1)
				out.append("(id: @annotation.switch) match {\n")
				for (const <- enum.constants) {
					out.append(indent).append("\t")
						.append("case ").append(const.id).append(" => ").append(const.name).append("\n")
				}
				out.append(indent).append("}\n")
			} else {	// O(n)
				out.append("values.find(_.id == id).getOrElse(null)\n")
			}

			// internalGetValueMap
			out.append(indent).append("val internalGetValueMap = new com.google.protobuf.Internal.EnumLiteMap[EnumVal] {\n")
				.append(indent).append("\tdef findValueByNumber(id: Int): EnumVal = valueOf(id)\n")
				.append(indent).append("}\n")

			out.append(indentOuter).append("}\n")

			out.mkString
		}

		/**
		 * Message generation, recurses for nested messages.
		 * Not tail-recursive, but shouldn't cause stack overflows on sane nesting levels.
		 */
		def message(name: String, body: MessageBody, indentLevel: Int = 0): String = {
			import FieldLabels.{REQUIRED, OPTIONAL, REPEATED}
			import FieldTypes.{INT32, UINT32, SINT32, FIXED32, SFIXED32, INT64, UINT64, SINT64, FIXED64, SFIXED64, BOOL, FLOAT, DOUBLE, BYTES, STRING}

			val indent0 = BuffedString.indent(indentLevel + 1)
			val (indent1, indent2, indent3) = (indent0 + "\t", indent0 + "\t\t", indent0 + "\t\t\t")

			val fields = body.fields

			body.options.foreach {
				case Option(key, value) => // ignored: no options supported yet
			}
			body.extensionRanges.foreach {
				case ExtensionRanges(extensionRanges) => // not supported yet
			}

			val out = StringBuilder.newBuilder

			// *** case class
			out.append(indent0).append("final case class ").append(name).append(" (\n")
			// constructor
			fields.foreach { field =>
				out.append(indent1).append(field.name.lowerCamelCase).append(": ")
				field.label match {
					case REQUIRED => out.append(field.fType.scalaType).append(" = ").append(field.fType.defaultValue).append(",\n")
					case OPTIONAL => out.append("Option[").append(field.fType.scalaType).append("] = None,\n")
					case REPEATED => out.append("Vector[").append(field.fType.scalaType).append("] = Vector.empty[").append(field.fType.scalaType).append("],\n")
					case _ => // weird warning - missing combination <local child> ?!
				}
			}
			out.length -= 2
			out.append("\n")
				.append(indent0).append(") extends com.google.protobuf.GeneratedMessageLite\n")
				.append(indent1).append("with hr.sandrogrzicic.scalabuff.runtime.Message[").append(name).append("] {\n\n")

			// getOptionalField
			fields.filter(_.label == OPTIONAL).foreach { field =>
				out.append(indent1)
					.append("def get").append(field.name.camelCase).append(" = ").append(field.name.lowerCamelCase)
					.append(".getOrElse(").append(field.fType.defaultValue).append(")\n")
			}

			// clearers
			fields.foreach { field =>
				out.append(indent1).append("def clear").append(field.name.camelCase).append(" = copy(").append(field.name.lowerCamelCase)
				field.label match {
					case REQUIRED => out.append(" = ").append(field.fType.defaultValue).append(")\n")
					case OPTIONAL => out.append(" = None\n")
					case REPEATED => out.append(" = Vector.empty[").append(field.fType.scalaType).append("]\n")
					case _ => // weird warning - missing combination <local child> ?!
				}
			}

			// writeTo(CodedOutputStream)
			out.append("\n").append(indent1)
				.append("def writeTo(output: com.google.protobuf.CodedOutputStream) {\n")
			fields.foreach { field =>
				field.label match {
					case REQUIRED => out.append(indent2)
						.append("output.write").append(field.fType.name.capitalize).append("(")
						.append(field.number).append(", ").append(field.name.lowerCamelCase).append(")\n")
					case OPTIONAL => out.append(indent2)
						.append(field.name.lowerCamelCase).append(".foreach(")
						.append("output.write").append(field.fType.name.capitalize).append("(")
						.append(field.number).append(", _))\n")
					case REPEATED => out.append(indent2)
						.append(field.name.lowerCamelCase).append(".foreach(")
						.append("output.write").append(field.fType.name.capitalize).append("(")
						.append(field.number).append(", _))\n")
					case _ => // weird warning - missing combination <local child> ?!
				}
			}
			out.append(indent1).append("}")

			// mergeFrom(CodedInputStream, ExtensionRegistryLite)
			out.append("\n").append(indent1)
				.append("def mergeFrom(in: com.google.protobuf.CodedInputStream, extensionRegistry: com.google.protobuf.ExtensionRegistryLite): ")
				.append(name).append(" = {\n")
			fields.foreach { field =>
				field.label match {
					case REQUIRED => out.append(indent2)
						.append("var _").append(field.name.lowerCamelCase)
						.append(" = ").append(field.fType.defaultValue).append("\n")
					case OPTIONAL => out.append(indent2)
						.append("var _").append(field.name.lowerCamelCase)
						.append(" = ").append(field.name.lowerCamelCase).append("\n")
					case REPEATED => out.append(indent2)
						.append("var _").append(field.name.lowerCamelCase)
						.append(" = ").append(field.name.lowerCamelCase).append(".toBuffer\n")
					case _ => // weird warning - missing combination <local child> ?!
				}
			}

			val mergedFields = StringBuilder.newBuilder
			fields.foreach { field =>
				mergedFields.append(indent3)
				if (field.label == REPEATED) mergedFields.append("Vector.concat(")
				mergedFields.append("_").append(field.name.lowerCamelCase)
				if (field.label == REPEATED) mergedFields.append(")")
				mergedFields.append(",\n")
			}
			if (!fields.isEmpty) mergedFields.length -= 2
			mergedFields.append("\n")

			out.append("\n")
				.append(indent2).append("while (true) (in.readTag: @annotation.switch) match {\n")
				.append(indent2).append("case 0 => return ").append(name).append("(\n").append(mergedFields).append(indent2).append(")\n")
			fields.foreach { field =>
				out.append(indent2).append("case ").append((field.number << 3) | field.fType.wireType).append(" => ")
					.append("_").append(field.name.lowerCamelCase).append(" ")
				if (field.label == REPEATED) out.append("+")
				out.append("= in.read")
				field.fType match {
					case STRING => out.append("Bytes().toStringUtf8")
					case _ => out.append(field.fType.name.capitalize).append("()")
				}
				out.append("\n")
			}
			out.append(indent2).append("case default => if (!in.skipField(default)) return ").append(name).append("(\n").append(mergedFields).append(indent2).append(")\n")
			out
				.append(indent2).append("}\n")
				.append(indent2).append("null // unreachable code\n")
				.append(indent1).append("}\n")

			// mergeFrom(Message)
			out.append("\n").append(indent1)
				.append("def mergeFrom(m: ").append(name).append(") = {\n")
				.append(indent2).append(name).append("(\n")
			fields.foreach { field =>
				field.label match {
					case REQUIRED => out.append(indent3)
						.append("m.").append(field.name.lowerCamelCase).append(",\n")
					case OPTIONAL => out.append(indent3)
						.append("m.").append(field.name.lowerCamelCase).append(".orElse(")
						.append(field.name.lowerCamelCase).append("),\n")
					case REPEATED => out.append(indent3)
						.append(field.name.lowerCamelCase).append(" ++ ")
						.append("m.").append(field.name.lowerCamelCase).append(",\n")
					case _ => // weird warning - missing combination <local child> ?!
				}
			}
			out.length -= 2
			out.append("\n").append(indent2).append(")\n")
			out.append(indent1).append("}\n")

			// getSerializedSize
			out.append("\n").append(indent1).append("lazy val getSerializedSize = {\n")
				.append(indent2).append("import com.google.protobuf.CodedOutputStream._\n")
				.append(indent2).append("import com.google.protobuf.ByteString.copyFromUtf8\n")
				.append(indent2).append("var size = 0\n")
			fields.foreach { field =>
				field.label match {
					case REQUIRED => out.append(indent2)
						.append("size += compute").append(field.fType.name.capitalize).append("Size(")
						.append(field.number).append(", ").append(field.name.lowerCamelCase).append(")\n")
					case OPTIONAL => out.append(indent2)
						.append(field.name.lowerCamelCase).append(".foreach(")
						.append("size += compute").append(field.fType.name.capitalize).append("Size(")
						.append(field.number).append(", _))\n")
					case REPEATED => out.append(indent2)
						.append(field.name.lowerCamelCase).append(".foreach(")
						.append("v => size += ")
						field.fType match {
							case STRING => out.append("1 + computeBytesSizeNoTag(copyFromUtf8(v))")
							case _ => out
								.append(field.fType.name.capitalize).append("Size(")
								.append(field.number).append(", v)")
						}
						out.append(")\n")
					case _ => // weird warning - missing combination <local child> ?!
				}
			}
			out.append("\n").append(indent2).append("size\n")
				.append(indent1).append("}\n")

			out.append("\n")
				.append(indent1).append("def getDefaultInstanceForType = ").append(name).append(".defaultInstance\n")
				.append(indent1).append("def clear = getDefaultInstanceForType\n")
				.append(indent1).append("def isInitialized = true\n")
				.append(indent1).append("def build = this\n")
				.append(indent1).append("def buildPartial = this\n")
				.append(indent1).append("def newBuilderForType = this\n")
				.append(indent1).append("def toBuilder = this\n")

			out.append(indent0).append("}\n\n")

			// *** companion object
			out.append(indent0).append("object ").append(name).append(" {\n")
				.append(indent1).append("@reflect.BeanProperty val defaultInstance = new ").append(name).append("()\n")

			out.append("\n")

			// field number integer constants
			fields.foreach { field =>
				out.append(indent1)
					.append("val ").append(field.name.toUpperCase)
					.append("_FIELD_NUMBER = ").append(field.number).append("\n")
			}

			out.append("\n")

			// append any nested enums
			body.enums.foreach {
				e => out.append(enum(e, indentLevel + 1)).append("\n")
			}

			// append any nested groups
			body.groups.foreach {
				case Group(label, nestedName, id, nestedBody) => // not supported yet (also, deprecated..)
			}
			// append any nested message extensions
			body.extensions.foreach {
				case Extension(nestedName, nestedBody) => // not supported yet
			}
			// append any nested messages
			body.messages.foreach {
				case Message(nestedName, nestedBody) => out.append(message(nestedName, nestedBody, indentLevel + 1))
			}

			// finalize object
			out.append(indent0).append("}\n")

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
						case "optimize_for" => value match {
							case "SPEED" => optimizeForSpeed = true
							case "CODE_SIZE" => optimizeForSpeed = false
							case "LITE_RUNTIME" => optimizeForSpeed = true
							case _ => throw new InvalidOptionValueException(key, value)
						}
						case _ => // ignore options which aren't recognized
					}
					case _ => throw new UnexpectedNodeException(node)
				}
				traverse(tail, output)
			}
		}

		// traverse the tree, so we can get class/package names, options, etc.
		val generated = traverse(tree)

		val output = StringBuilder.newBuilder

		// header
		output
			.append("// Generated by ScalaBuff, the Scala protocol buffer compiler. DO NOT EDIT!\n")
			.append("// source: ")
			.append(sourceName)
			.append("\n\n")
		// package
		if (!packageName.isEmpty)
			output.append("package ").append(packageName).append("\n\n")
		// begin outer object
		output.append("object ").append(className).append(" {\n")
		// inner classes, objects etc.
		output.append(generated).append("\n")
		// finalize outer object
		output
			.append("\tdef registerAllExtensions(registry: com.google.protobuf.ExtensionRegistryLite) {\n")
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

class InvalidOptionValueException(key: String, value: String) extends GenerationFailureException(
	"Invalid option value " + value + " for key " + key
)