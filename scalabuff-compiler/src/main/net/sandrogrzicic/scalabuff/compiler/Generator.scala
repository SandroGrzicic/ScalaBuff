package net.sandrogrzicic.scalabuff.compiler

import annotation.tailrec
import collection.mutable
import net.sandrogrzicic.scalabuff.compiler.FieldTypes._
import com.google.protobuf._
import java.io.File

/**
 * Scala class generator.
 * @author Sandro Gržičić
 */

class Generator protected (sourceName: String, importedSymbols: Map[String, ImportedSymbol], generateJsonMethod: Boolean,
                           targetScalaVersion: Option[String]) {
  import Generator._

  protected val imports = mutable.ListBuffer[String]()

  protected var packageName: String = ""
  protected var className: String = sourceName.takeUntilFirst('.').camelCase

  /**
   * Whether to optimize the resultant class for speed (true) or for code size (false). True by default.
   */
  protected var optimizeForSpeed = true

  protected var lazyGetSerializedSize = false

  /**
   * Generates the Scala class code.
   */
  protected def generate(tree: List[Node]): ScalaClass = {

    // *******************
    //   utility methods
    // *******************

    /**
     * Enum generation
     */
    def enum(enum: EnumStatement, indentLevel: Int = 0) = {
      val indentOuter = BuffedString.indent(indentLevel)
      val indent = indentOuter + "\t"

      val out = StringBuilder.newBuilder
      out
        .append(indentOuter).append("object ").append(enum.name).append(" extends net.sandrogrzicic.scalabuff.Enum {\n")
        .append(indent).append("sealed trait EnumVal extends Value\n")
        .append(indent).append("val _UNINITIALIZED = new EnumVal { val name = \"UNINITIALIZED ENUM VALUE\"; val id = -1 }\n\n")

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
      if (optimizeForSpeed) { // O(1)
        // todo: find out why @annotation.switch doesn't work properly
        out.append("id match {\n")
        for (const <- enum.constants) {
          out.append(indent).append("\t")
            .append("case ").append(const.id).append(" => ").append(const.name).append("\n")
        }
        out.append(indent).append("\t").append("case _default => throw new net.sandrogrzicic.scalabuff.UnknownEnumException(_default)\n")
        out.append(indent).append("}\n")
      } else { // O(n)
        out.append("values.find(_.id == id).orNull\n")
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
      import FieldLabels.{ REQUIRED, OPTIONAL, REPEATED }
      import FieldTypes.{ INT32, UINT32, SINT32, FIXED32, SFIXED32, INT64, UINT64, SINT64, FIXED64, SFIXED64, BOOL, FLOAT, DOUBLE, BYTES, STRING }
      import WireFormat.{ WIRETYPE_VARINT, WIRETYPE_FIXED32, WIRETYPE_FIXED64, WIRETYPE_LENGTH_DELIMITED, WIRETYPE_START_GROUP, WIRETYPE_END_GROUP }

      val indent0 = BuffedString.indent(indentLevel)
      val (indent1, indent2, indent3, indent4) = (indent0 + "\t", indent0 + "\t\t", indent0 + "\t\t\t", indent0 + "\t\t\t\t")

      val fields = body.fields

      /** Whether this message has any extension ranges defined. */
      var hasExtensionRanges = false

      body.options.foreach {
        case OptionValue(key, value) => // no options here
      }
      body.extensionRanges.foreach {
        case ExtensionRanges(extensionRanges) =>
          hasExtensionRanges = true
      }

      /** main StringBuilder for the whole message */
      val out = StringBuilder.newBuilder

      // *** case class
      out.append(indent0).append("final case class ").append(name).append(" (\n")
      // constructor
      for (field <- fields) {
        out.append(indent1).append(field.name.toScalaIdent).append(": ")
        field.label match {
          case REQUIRED =>
            out.append(field.fType.scalaType).append(" = ").append(field.fType.defaultValue).append(",\n")
          case OPTIONAL =>
            out.append("Option[").append(field.fType.scalaType).append("] = ").append(field.defaultValue).append(",\n")
          case REPEATED =>
            out.append("scala.collection.immutable.Seq[").append(field.fType.scalaType).append("] = Vector.empty[").append(field.fType.scalaType).append("],\n")
          case _ => // "missing combination <local child>"
        }
      }
      if (!fields.isEmpty) out.length -= 2

      out.append('\n').append(indent0).append(") extends com.google.protobuf.")
      if (!hasExtensionRanges) {
        // normal message
        out.append("GeneratedMessageLite")
        out.append('\n').append(indent1).append("with com.google.protobuf.MessageLite.Builder")
        out.append('\n').append(indent1).append("with net.sandrogrzicic.scalabuff.Message[").append(name).append("]")
      } else {
        // extendable message
        out.append("GeneratedMessageLite.ExtendableMessage[").append(name).append("]")
        out.append('\n').append(indent1).append("with net.sandrogrzicic.scalabuff.ExtendableMessage[").append(name).append("]")
      }
      out.append('\n').append(indent1).append("with net.sandrogrzicic.scalabuff.Parser[").append(name).append("]")

      out.append(" {\n\n")

      // setters
      for (field <- fields) {
        field.label match {
          case OPTIONAL => out.append(indent1)
            .append("def set").append(field.name.camelCase).append("(_f: ").append(field.fType.scalaType)
            .append(") = copy(").append(field.name.toScalaIdent).append(" = Some(_f))\n")
          case REPEATED => out
            .append(indent1).append("def set").append(field.name.camelCase).append("(_i: Int, _v: ").append(field.fType.scalaType)
            .append(") = copy(").append(field.name.toScalaIdent).append(" = ").append(field.name.toScalaIdent).append(".updated(_i, _v))\n")
            .append(indent1).append("def add").append(field.name.camelCase).append("(_f: ").append(field.fType.scalaType)
            .append(") = copy(").append(field.name.toScalaIdent).append(" = ").append(field.name.toScalaIdent).append(" :+ _f)\n")
            .append(indent1).append("def addAll").append(field.name.camelCase).append("(_f: ").append(field.fType.scalaType)
            .append("*) = copy(").append(field.name.toScalaIdent).append(" = ").append(field.name.toScalaIdent).append(" ++ _f)\n")
            .append(indent1).append("def addAll").append(field.name.camelCase).append("(_f: TraversableOnce[").append(field.fType.scalaType)
            .append("]) = copy(").append(field.name.toScalaIdent).append(" = ").append(field.name.toScalaIdent).append(" ++ _f)\n")
          case _ => // don't generate a setter for REQUIRED fields, as the copy method can be used
        }
      }
      out.append("\n")

      // clearers
      for (field <- fields if field.label != REQUIRED) {
        out.append(indent1).append("def clear").append(field.name.camelCase).append(" = copy(").append(field.name.toScalaIdent).append(" = ")
        field.label match {
          case OPTIONAL => out.append("None")
          case REPEATED => out.append("Vector.empty[").append(field.fType.scalaType).append("]")
          case _        => // don't generate a clearer for REQUIRED fields -> would result in an illegal message
        }
        out.append(")\n")
      }

      // writeTo(CodedOutputStream)
      out.append("\n").append(indent1)
        .append("def writeTo(output: com.google.protobuf.CodedOutputStream): Unit = {\n")

      fields.foreach { field =>
        field.label match {
          case REQUIRED => out.append(indent2)
            .append("output.write").append(field.fType.name).append("(")
            .append(field.number).append(", ").append(field.name.toScalaIdent).append(")\n")
          case OPTIONAL => out.append(indent2).append("if (")
            .append(field.name.toScalaIdent).append(".isDefined) ")
            .append("output.write").append(field.fType.name).append("(")
            .append(field.number).append(", ").append(field.name.toScalaIdent).append(".get)\n")
          case REPEATED =>
            (field.fType.packable, field.options.filter(value => value.key == "packed" && value.value == "true").headOption) match {
              case (true, Some(option)) =>
                out.append(indent2).append(s"// write field ${field.name} packed \n")
                out.append(indent2).append("if (!").append(field.name.toScalaIdent).append(".isEmpty) {\n")
                out.append(indent3).append("import com.google.protobuf.CodedOutputStream._\n")
                out.append(indent3).append("val dataSize = ").append(field.name.toScalaIdent)
                  .append(".map(compute").append(field.fType.name).append("SizeNoTag(_)).sum")
                  .append(" \n")
                out.append(indent3).append("output.writeRawVarint32(")
                  .append((field.number << 3) | WIRETYPE_LENGTH_DELIMITED).append(")").append("\n")
                out.append(indent3).append("output.writeRawVarint32(dataSize)").append("\n")
                out.append(indent3).append("for (_v <- ")
                  .append(field.name.toScalaIdent).append(") ")
                  .append("output.write").append(field.fType.name).append("NoTag")
                  .append("(_v)\n")
                out.append(indent2).append("}\n")
              case _ =>
                out.append(indent2).append("for (_v <- ")
                  .append(field.name.toScalaIdent).append(") ")
                  .append("output.write").append(field.fType.name)
                out.append("(").append(field.number).append(", _v)\n")
            }
          case _ => // "missing combination <local child>"
        }
      }
      out.append(indent1).append("}\n")

      // getSerializedSize
      val definition = if (lazyGetSerializedSize) "lazy val" else "def"

      out.append("\n").append(indent1).append(definition).append(" getSerializedSize = {\n")
      if(fields.nonEmpty) { // prevent Unused import compilation warning when there are no fields for size computing
        out.append(indent2).append("import com.google.protobuf.CodedOutputStream._\n")
      }
      out.append(indent2).append("var __size = 0\n")
      for (field <- fields) {
        field.label match {
          case REQUIRED => out.append(indent2)
            .append("__size += compute").append(field.fType.name).append("Size(")
            .append(field.number).append(", ").append(field.name.toScalaIdent).append(")\n")
          case OPTIONAL => out.append(indent2).append("if (")
            .append(field.name.toScalaIdent).append(".isDefined) ")
            .append("__size += compute").append(field.fType.name).append("Size(")
            .append(field.number).append(", ").append(field.name.toScalaIdent).append(".get)\n")
          case REPEATED =>
            // TODO make this nicer currently code is generated 2 times
            (field.fType.packable, field.options.find(value => value.key == "packed" && value.value == "true")) match {
              case (true, Some(option)) =>
                out.append(indent2).append("if (!").append(field.name.toScalaIdent).append(".isEmpty) {\n")
                out.append(indent3).append("val dataSize = ").append(field.name.toScalaIdent)
                  .append(".map(compute").append(field.fType.name).append("SizeNoTag(_)).sum")
                  .append(" \n")

                val tagSize = CodedOutputStream.computeTagSize(field.number)
                out.append(indent3).append(s"__size += $tagSize + computeInt32SizeNoTag(dataSize) + dataSize\n")
                out.append(indent2).append("}\n")
              case _            =>
                out.append(indent2).append("for (_v <- ")
                  .append(field.name.toScalaIdent).append(") ")
                  .append("__size += compute").append(field.fType.name).append("Size(")
                  .append(field.number).append(", _v)\n")
            }
          case _ => // "missing combination <local child>"
        }
      }
      out.append("\n").append(indent2).append("__size\n")
        .append(indent1).append("}\n")

      // mergeFrom(CodedInputStream, ExtensionRegistryLite)
      // need to split this into 2 versions: optimize for speed (current code) and for code size (use setters, generating new Messages each time)
      out.append("\n").append(indent1)
        .append("def mergeFrom(in: com.google.protobuf.CodedInputStream, extensionRegistry: com.google.protobuf.ExtensionRegistryLite): ")
        .append(name).append(" = {\n")
        .append(indent2).append("val _emptyRegistry = com.google.protobuf.ExtensionRegistryLite.getEmptyRegistry\n")

      for (field <- fields) {
        field.label match {
          case REQUIRED => out.append(indent2)
            .append("var ").append(field.name.toTemporaryIdent).append(": ").append(field.fType.scalaType)
            .append(" = ").append(field.fType.defaultValue).append("\n")
          case OPTIONAL => out.append(indent2)
            .append("var ").append(field.name.toTemporaryIdent).append(": Option[").append(field.fType.scalaType).append("]")
            .append(" = ").append(field.name.toScalaIdent).append("\n")
          case REPEATED => out.append(indent2)
            .append("val ").append(field.name.toTemporaryIdent).append(": scala.collection.mutable.Buffer[").append(field.fType.scalaType).append("]")
            .append(" = ").append(field.name.toScalaIdent).append(".toBuffer\n")
          case _ => // "missing combination <local child>"
        }
      }
      out.append("\n")
        .append(indent2).append("def __newMerged = ").append(name).append("(\n")
      fields.foreach { field =>
        out.append(indent3)
        if (field.label == REPEATED) out.append("Vector(")
        out.append(field.name.toTemporaryIdent)
        if (field.label == REPEATED) out.append(": _*)")
        out.append(",\n")
      }
      if (!fields.isEmpty) out.length -= 2
      out.append("\n")
      out.append(indent2).append(")\n")
        .append(indent2).append("while (true) in.readTag match {\n")
        .append(indent3).append("case 0 => return __newMerged\n")
      var isOptional = false
      for (field <- fields) {
        isOptional = field.label match {
          case OPTIONAL => true
          case _        => false
        }
        out.append(indent3).append("case ").append((field.number << 3) | field.fType.wireType).append(" => ")
        out.append(field.name.toTemporaryIdent).append(" ")

        if (field.label == REPEATED) out.append("+")
        out.append("= ")
        if (isOptional) out.append("Some(")
        if (field.fType == WIRETYPE_LENGTH_DELIMITED) out.append("in.readBytes()")
        else if (field.fType.isEnum) {
          // IFF this is an optional field, AND it has a default (i.e. is not
          // None), then fall back to using that default if an exception is
          // thrown.
          if (isOptional && field.defaultValue != "None") {
            out.append("try { ")
            out.append(field.fType.scalaType.takeUntilLast('.')).append(".valueOf(in.readEnum())")
            out.append(" } catch { case e: Exception => ").append(field.defaultValue).append(".get }")

          } else {
            out.append(field.fType.scalaType.takeUntilLast('.')).append(".valueOf(in.readEnum())")
          }
        } else if (field.fType.isMessage) {

          out.append("readMessage[").append(field.fType.scalaType).append("](in, ")
          field.label match {
            case REQUIRED => out.append(field.name.toTemporaryIdent)
            case OPTIONAL => out
              .append(field.name.toTemporaryIdent).append(".orElse({\n")
              .append(indent3).append("\t").append(field.name.toTemporaryIdent).append(" = ").append(field.fType.defaultValue).append("\n")
              .append(indent3).append("\t").append(field.name.toTemporaryIdent).append("\n")
              .append(indent3).append("}).get")
            case REPEATED => out
              .append(field.fType.defaultValue)
            case _ => // "missing combination <local child>"
          }
          out.append(", _emptyRegistry)")

        } else out.append("in.read").append(field.fType.name).append("()")
        if(isOptional) out.append(")")
        out.append("\n")
        
        if (field.fType.packable && field.label == REPEATED) {
          out.append(indent3).append("case ").append((field.number << 3) | WIRETYPE_LENGTH_DELIMITED).append(" => ")
          out.append("\n")
            .append(indent4).append("val length = in.readRawVarint32()\n")
            .append(indent4).append("val limit = in.pushLimit(length)\n")
            .append(indent4).append("while (in.getBytesUntilLimit() > 0) {\n")
            .append(indent1).append(indent4).append(field.name.toTemporaryIdent).append(" += ")
          if (field.fType.isEnum)
            out.append(field.fType.scalaType.takeUntilLast('.')).append(".valueOf(in.readEnum())")
          else
            out.append("in.read").append(field.fType.name).append("()")
          out.append("\n").append(indent4).append("}\n")
          out.append(indent4).append("in.popLimit(limit)\n")
        }
      }
      out.append(indent3).append("case default => if (!in.skipField(default)) return __newMerged\n")
      out
        .append(indent2).append("}\n")
        .append(indent2).append("null\n") // compiler needs a return value
        .append(indent1).append("}\n")

      // mergeFrom(Message)
      out.append("\n").append(indent1)
        .append("def mergeFrom(m: ").append(name).append(") = {\n")
        .append(indent2).append(name).append("(\n")
      for (field <- fields) {
        field.label match {
          case REQUIRED => out.append(indent3)
            .append("m.").append(field.name.toScalaIdent).append(",\n")
          case OPTIONAL => out.append(indent3)
            .append("m.").append(field.name.toScalaIdent).append(".orElse(")
            .append(field.name.toScalaIdent).append("),\n")
          case REPEATED => out.append(indent3)
            .append(field.name.toScalaIdent).append(" ++ ")
            .append("m.").append(field.name.toScalaIdent).append(",\n")
          case _ => // "missing combination <local child>"
        }
      }
      if (!fields.isEmpty) out.length -= 2
      out.append("\n").append(indent2).append(")\n")
      out.append(indent1).append("}\n")

      out.append("\n")
        .append(indent1).append("def getDefaultInstanceForType = ").append(name).append(".defaultInstance\n")
        .append(indent1).append("def clear = getDefaultInstanceForType\n")
        .append(indent1).append("def isInitialized = true\n")
        .append(indent1).append("def build = this\n")
        .append(indent1).append("def buildPartial = this\n")

      out.append(indent1)
        .append("def parsePartialFrom(cis: com.google.protobuf.CodedInputStream, er: com.google.protobuf.ExtensionRegistryLite) = ")
        .append("mergeFrom(cis, er)\n")

      out.append(indent1).append("override def getParserForType = this\n")

      if (!hasExtensionRanges) {
        out
          .append(indent1).append("def newBuilderForType = getDefaultInstanceForType\n")
          .append(indent1).append("def toBuilder = this\n")
      } else {
        out
          .append(indent1).append("def newBuilderForType = throw new RuntimeException(\"Method not available.\")\n")
          .append(indent1).append("def toBuilder = throw new RuntimeException(\"Method not available.\")\n")
      }

      // toJson
      if (generateJsonMethod) {
        out
        .append(indent1).append("def toJson(indent: Int = 0): String = {\n")
          .append(indent2).append("val indent0 = \"\\n\" + (\"\\t\" * indent)\n")
          .append(indent2).append("val (indent1, indent2) = (indent0 + \"\\t\", indent0 + \"\\t\\t\")\n")
          .append(indent2).append("val sb = StringBuilder.newBuilder\n")
          .append(indent2).append("sb\n")
          .append(indent3).append(".append(\"{\")\n")

          for (field <- fields) {
            val name = field.name.lowerCamelCase
            val (quotesStart, quotesEnd) = if (!field.fType.isMessage) (".append(\"\\\"\")", ".append(\"\\\"\")") else ("", "")
            val mapQuotes = if (!field.fType.isMessage) ".map(\"\\\"\" + _ + \"\\\"\")" else ""
            val toJson = if (field.fType.isMessage) ".toJson(indent + 1)" else ""
            val mapToJson = if (field.fType.isMessage) ".map(_.toJson(indent + 1))" else ""

            field.label match {
              case REQUIRED =>
                out.append(indent3).append("sb.append(indent1).append(\"\\\"").append(name)
                     .append("\\\": \")").append(quotesStart).append(".append(`").append(name)
                     .append("`").append(toJson).append(")").append(quotesEnd).append(".append(',')").append('\n')
              case OPTIONAL =>
                out.append(indent3)
                     .append("if (`").append(name).append("`.isDefined) { ").append("sb.append(indent1).append(\"\\\"").append(name)
                     .append("\\\": \")").append(quotesStart).append(".append(`").append(name)
                     .append("`.get").append(toJson).append(")").append(quotesEnd).append(".append(',') }").append('\n')
              case REPEATED =>
                out.append(indent3).append("sb.append(indent1).append(\"\\\"").append(name).append("\\\": [\")")
                     .append(".append(indent2).append(`").append(name).append("`").append(mapToJson).append(mapQuotes)
                     .append(".mkString(\", \" + indent2)).append(indent1).append(']').append(',')").append('\n')
              case _ =>
            }
          }
        out.append(indent2).append("if (sb.last.equals(',')) sb.length -= 1\n")
        out.append(indent2).append("sb.append(indent0).append(\"}\")\n")

        out.append(indent2).append("sb.toString()\n")
        out.append(indent1).append("}\n\n")

      } else {
        out.append(indent1).append("def toJson(indent: Int = 0): String = \"ScalaBuff JSON generation not enabled. Use --generate_json_method to enable.\"\n")
      }

      out.append(indent0).append("}\n\n")

      // *** companion object
      out.append(indent0).append("object ").append(name).append(" {\n")
        .append(indent1)

      out.append("@scala.beans.BeanProperty val defaultInstance = new ")

      out.append(name).append("()\n").append("\n")

      // parseFrom()
      out.append(indent1).append("def parseFrom(data: Array[Byte]): ").append(name)
        .append(" = defaultInstance.mergeFrom(data)\n")
      out.append(indent1).append("def parseFrom(data: Array[Byte], offset: Int, length: Int): ").append(name)
        .append(" = defaultInstance.mergeFrom(data, offset, length)\n")
      out.append(indent1).append("def parseFrom(byteString: com.google.protobuf.ByteString): ").append(name)
        .append(" = defaultInstance.mergeFrom(byteString)\n")
      out.append(indent1).append("def parseFrom(stream: java.io.InputStream): ").append(name)
        .append(" = defaultInstance.mergeFrom(stream)\n")
      out.append(indent1).append("def parseDelimitedFrom(stream: java.io.InputStream): Option[").append(name)
        .append("] = defaultInstance.mergeDelimitedFromStream(stream)\n")
      out.append("\n")

      // field number integer constants
      for (field <- fields) {
        out.append(indent1)
          .append("val ").append(field.name.toUpperCase)
          .append("_FIELD_NUMBER = ").append(field.number).append("\n")
      }

      out.append("\n")

      // newBuilder
      out
        .append(indent1).append("def newBuilder = defaultInstance.newBuilderForType\n")
        .append(indent1).append("def newBuilder(prototype: ").append(name).append(") = defaultInstance.mergeFrom(prototype)\n")

      out.append("\n")

      // append any nested enums
      for (e <- body.enums) {
        out.append(enum(e, indentLevel + 1)).append("\n")
      }

      // append any nested groups
      body.groups.foreach {
        case Group(label, nestedName, id, nestedBody) => // not supported yet (also, deprecated..)
      }
      // append any nested message extensions
      body.extensions.foreach {
        case Extension(nestedName, nestedBody) => // not supported yet
      }
      // append any nested messages (recursive)
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
        case node :: tail =>
          node match {
            case Message(name, body)    => output.append(message(name, body))
            case Extension(name, body)  => // very similar to Message
            case e: EnumStatement       => output.append(enum(e))
            case ImportStatement(name)  => imports += name
            case PackageStatement(name) => if (packageName.isEmpty) packageName = name
            case OptionValue(key, value) => key match {
              case "java_package"         => packageName = value.stripQuotes
              case "java_outer_classname" => className = value.stripQuotes
              case "lazy_compute_serialized_size" => value match {
                case "true"         => lazyGetSerializedSize = true
                case "false"        => lazyGetSerializedSize = false
                case _              => throw new InvalidOptionValueException(key, value)
              }
              case "optimize_for" => value match {
                case "SPEED"        => optimizeForSpeed = true
                case "CODE_SIZE"    => optimizeForSpeed = false
                case "LITE_RUNTIME" => optimizeForSpeed = true
                case _              => throw new InvalidOptionValueException(key, value)
              }
              case _ => // ignore options which aren't recognized
            }
            case _ => throw new UnexpectedNodeException(node)
          }
          traverse(tail, output)
      }
    }

    // **********************
    // additional tree passes
    // **********************

    recognizeCustomTypes(tree, importedSymbols)
    prependParentClassNames(tree, getAllNestedMessageTypes(tree))
    setDefaultsForOptionalFields(tree)
    fullySpecifyImportedSymbols(tree, importedSymbols)

    // final tree pass: traverse the tree, so we can get class/package names, options, etc.
    val generatedOutput = traverse(tree)

    // Now that we have processed all of the options, make sure that the class name doesn't
    // match one of the top level type names.
    val matchingTypeName = tree.exists {
      case msg: Message => msg.name == className
      case e: EnumStatement => e.name == className
      case _ => false
    }

    if (matchingTypeName) {
      throw new GenerationFailureException("Cannot generate valid Scala output because the class name, '%s' for the extension registry class matches the name of one of the messages or enums declared in the .proto.  Please either rename the type or use the java_outer_classname option to specify a different class name for the .proto file.".format(className))
      }

    val output = StringBuilder.newBuilder

    // header
    output
      .append("// Generated by ScalaBuff, the Scala Protocol Buffers compiler. DO NOT EDIT!\n")
      .append("// source: ")
      .append(sourceName)
      .append("\n\n")
    // package
    if (!packageName.isEmpty)
      output.append("package ").append(packageName).append("\n\n")

    // imports
    imports.foreach { i =>
      output.append("//import ").append(i).append("\n")
    }
    if (imports.size > 0) output.append("\n")
    imports.clear()

    // generated output
    output.append(generatedOutput).append("\n")

    val messageNames = tree.collect { case m:Message => m }.map(_.name)
    // outer object
    output
      .append("object ").append(className).append(" {\n")
      .append("\tdef registerAllExtensions(registry: com.google.protobuf.ExtensionRegistryLite): Unit = {\n")
      .append("\t}\n\n")

      .append("\tprivate val fromBinaryHintMap = collection.immutable.HashMap[String, Array[Byte] ⇒ com.google.protobuf.GeneratedMessageLite](").append("\n")
      .append(messageNames.map { m => "\t\t" + s""" "$m" -> (bytes ⇒ $m.parseFrom(bytes))""" } mkString ",\n")
      .append("\n\t)").append("\n")

      .append("\n")
      .append("\tdef deserializePayload(payload: Array[Byte], payloadType: String): com.google.protobuf.GeneratedMessageLite = {").append("\n")
      .append("\t\tfromBinaryHintMap.get(payloadType) match {").append("\n")
      .append("\t\t\tcase Some(f) ⇒ f(payload)").append("\n")
      .append("\t\t\tcase None    ⇒ throw new IllegalArgumentException(s\"unimplemented deserialization of message payload of type [${payloadType}]\")").append("\n")
      .append("\t\t}").append("\n")
      .append("\t}").append("\n")

      .append("}\n")

    ScalaClass(output.mkString, packageName.replace('.', File.separatorChar) + File.separatorChar, className)
  }

}

object Generator {
  /**
   * Returns a valid Scala class.
   */
  def apply(tree: List[Node], sourceName: String, importedSymbols: Map[String, ImportedSymbol], generateJsonMethod: Boolean,
            targetScalaVersion: Option[String]): ScalaClass = {
    new Generator(sourceName, importedSymbols, generateJsonMethod, targetScalaVersion).generate(tree)
  }

  /**
   * Modifies some fields of Message and Enum types so that they can be used properly.
   * Discovers whether each field type is a Message or an Enum.
   */
  protected def recognizeCustomTypes(tree: List[Node], importedSymbols: Map[String, ImportedSymbol]) {
    val (enumNames, customFieldTypes) = getEnumNames(tree)
    fixCustomTypes(tree, enumNames, customFieldTypes, importedSymbols)
  }

  /** Return all enum names and custom field types found in the specified tree. */
  protected def getEnumNames(
                              tree: List[Node],
                              enumNames: mutable.HashSet[String] = mutable.HashSet.empty[String],
                              customFieldTypes: mutable.ArrayBuffer[FieldTypes.EnumVal] = mutable.ArrayBuffer.empty[FieldTypes.EnumVal]
                              ): (mutable.HashSet[String], mutable.ArrayBuffer[FieldTypes.EnumVal]) = {

    for (node <- tree) {
      node match {
        case Message(name, body) =>
          enumNames ++= body.enums.map(_.name)
          customFieldTypes ++= body.fields.map(_.fType) collect { case t: CustomEnumVal => t }
          getEnumNames(body.messages, enumNames, customFieldTypes)
        case EnumStatement(name, constants, options) => enumNames += name
        case _ =>
      }
    }
    (enumNames, customFieldTypes)
  }

  /** Update fields which have custom types. */
  protected def fixCustomTypes(tree: List[Node], enumNames: mutable.Set[String], customFieldTypes: mutable.Buffer[EnumVal], importedSymbols: Map[String, ImportedSymbol]) {
    for (fType <- customFieldTypes if !fType.isMessage && !fType.isEnum) {
      if (enumNames.contains(fType.name.dropUntilLast('.')) || importedSymbols.get(fType.scalaType).exists(_.isEnum)) {
        fType.isEnum = true
        fType.name = "Enum"
        fType.defaultValue = fType.scalaType + "._UNINITIALIZED"
        fType.scalaType += ".EnumVal"
        fType.wireType = WireFormat.WIRETYPE_VARINT
      } else {
        fType.isMessage = true
        fType.name = "Message"
        fType.defaultValue = fType.scalaType + ".defaultInstance"
      }
    }
  }

  /** Returns all message types of nested messages. */
  protected def getAllNestedMessageTypes(tree: List[Node]): Map[Message, List[String]] = {
    val nestedMessages = new mutable.HashMap[Message, List[String]]
    for (node <- tree) node match {
      case m @ Message(name, body) =>
        for (innerMessage <- body.messages) {
          nestedMessages.put(m, innerMessage.name :: nestedMessages.getOrElse(m, Nil))
        }
        nestedMessages ++= getAllNestedMessageTypes(body.messages)
      case _ => Nil
    }
    nestedMessages.toMap
  }

  /** Prepend parent class names to all nested custom field types. */
  val processedFieldTypes = new mutable.HashSet[FieldTypes.EnumVal]()
  val processedEnums = new mutable.HashSet[FieldTypes.EnumVal]()
  protected def prependParentClassNames(tree: List[Node], nestedMessageTypes: Map[Message, List[String]]) {
    for (node <- tree) node match {
      case parent @ Message(parentName, parentBody) =>
        // prepend parent class names to messages
        parentBody.messages.foreach {
          case child @ Message(_, nestedMessage) =>
            val filteredFields = parentBody.fields.withFilter(f => f.fType.isMessage && !processedFieldTypes(f.fType))
            for (field <- filteredFields) {
              val fType = field.fType
              // prepend only if the mesage type is a child of the parent message
              if (nestedMessageTypes(parent).contains(fType.scalaType)) {
                fType.scalaType = parentName + "." + fType.scalaType
                fType.defaultValue = parentName + "." + fType.defaultValue
                processedFieldTypes += fType
              }
            }
            // recurse for any nested messages
            prependParentClassNames(child :: nestedMessage.messages, nestedMessageTypes)
        }
        // prepend parent class names to all nested enums
        parentBody.enums.foreach {
          case EnumStatement(eName, eConstants, eOptions) =>
            for (field <- parentBody.fields.withFilter { f => f.fType.isEnum && !processedEnums(f.fType) }) {
              val fType = field.fType
              processedEnums += fType
              fType.scalaType = parentName + "." + fType.scalaType
              fType.defaultValue = fType.scalaType.replace(".EnumVal", "") + "._UNINITIALIZED"
            }
        }
      case _ =>
    }
  }

  protected def setDefaultsForOptionalFields(tree: List[Node]) {
    for (node <- tree) node match {
      case Message(_, body) =>
        for (field <- body.fields) {
          field.defaultValue = {
            if (field.label == FieldLabels.OPTIONAL) {
              field.options.find(_.key == "default") match {
                case Some(option) => "Some(" + {
                  val qualifiedType = field.fType.scalaType.takeUntilLast('.')
                  if (qualifiedType.isEmpty) option.value
                  else qualifiedType + "." + option.value
                } + {
                  if (field.fType.scalaType == "Float") "f" else ""
                } +
                  ")"

                case None => "None"
              }
            } else {
              field.fType.defaultValue
            }
          }
        }
        // recurse for any nested messages
        setDefaultsForOptionalFields(body.messages)
      case _ =>
    }
  }

  protected def fullySpecifyImportedSymbols(tree: List[Node], importedSymbols: Map[String, ImportedSymbol]) {
    def apply(node: Node) {
      node match {
        case Message(_, body) =>
          body.messages.foreach(apply)
          body.fields.foreach { field =>
            val scalaType = if (field.fType.scalaType endsWith ".EnumVal")
                field.fType.scalaType.split("\\.")(0)
              else
                field.fType.scalaType
            importedSymbols.filter {
              case (name, symbol) =>
                val shortName = scalaType.stripPrefix(symbol.protoPackage + ".")
                name == shortName || shortName.startsWith(name + ".")
            }.foreach {
              case (name, symbol) =>
	              // namespaces might be empty for imported message types
	              val namespacePrefix = if (symbol.packageName.isEmpty) "" else symbol.packageName + "."
                val protoPkgPrefix = if (symbol.protoPackage.isEmpty) "" else symbol.protoPackage + "."
                field.fType.scalaType = namespacePrefix + field.fType.scalaType.stripPrefix(protoPkgPrefix)
                field.fType.defaultValue = namespacePrefix + field.fType.defaultValue.stripPrefix(protoPkgPrefix)
            }
          }
        case _ =>
      }
    }
    tree.foreach(apply)
  }
}

case class ImportedSymbol(packageName: String, isEnum: Boolean, protoPackage: String = "")

/**
 * A generated Scala class. The path is relative.
 */
case class ScalaClass(body: String, path: String, file: String) {
  assert(path.endsWith(File.separator), "path must end with a " + File.separator)
  assert(!file.contains(File.separator), "file name must not contain a " + File.separator)
}

/**
 * Thrown when a valid Scala class cannot be generated using the the tree returned from the Parser.
 */
class GenerationFailureException(message: String) extends RuntimeException(message)

/**
 * Thrown when a Node occurs in an unexpected location in the tree.
 */
class UnexpectedNodeException(node: Node, parentNode: Node = null) extends GenerationFailureException(
  "Unexpected child node " + node + (if (parentNode ne null) "found in " + parentNode else "")
)

class InvalidOptionValueException(key: String, value: String) extends GenerationFailureException(
  "Invalid option value " + value + " for key " + key
)
