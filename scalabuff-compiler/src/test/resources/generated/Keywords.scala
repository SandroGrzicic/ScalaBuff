// Generated by ScalaBuff, the Scala Protocol Buffers compiler. DO NOT EDIT!
// source: keywords.proto

package resources.generated

final case class KeywordsTest (
	`size`: Long = 0L,
	`case`: Int = 0,
	`val`: Int = 0,
	`var`: Int = 0,
	`def`: Int = 0,
	`object`: Int = 0,
	`class`: Int = 0,
	`lazy`: Int = 0,
	`type`: Int = 0
) extends com.google.protobuf.GeneratedMessageLite
	with com.google.protobuf.MessageLite.Builder
	with net.sandrogrzicic.scalabuff.Message[KeywordsTest]
	with net.sandrogrzicic.scalabuff.Parser[KeywordsTest] {



	def writeTo(output: com.google.protobuf.CodedOutputStream) {
		output.writeInt64(1, `size`)
		output.writeInt32(2, `case`)
		output.writeInt32(3, `val`)
		output.writeInt32(4, `var`)
		output.writeInt32(5, `def`)
		output.writeInt32(6, `object`)
		output.writeInt32(7, `class`)
		output.writeInt32(8, `lazy`)
		output.writeInt32(9, `type`)
	}

	def getSerializedSize = {
		import com.google.protobuf.CodedOutputStream._
		var __size = 0
		__size += computeInt64Size(1, `size`)
		__size += computeInt32Size(2, `case`)
		__size += computeInt32Size(3, `val`)
		__size += computeInt32Size(4, `var`)
		__size += computeInt32Size(5, `def`)
		__size += computeInt32Size(6, `object`)
		__size += computeInt32Size(7, `class`)
		__size += computeInt32Size(8, `lazy`)
		__size += computeInt32Size(9, `type`)

		__size
	}

	def mergeFrom(in: com.google.protobuf.CodedInputStream, extensionRegistry: com.google.protobuf.ExtensionRegistryLite): KeywordsTest = {
		import com.google.protobuf.ExtensionRegistryLite.{getEmptyRegistry => _emptyRegistry}
		var __size: Long = 0L
		var __case: Int = 0
		var __val: Int = 0
		var __var: Int = 0
		var __def: Int = 0
		var __object: Int = 0
		var __class: Int = 0
		var __lazy: Int = 0
		var __type: Int = 0

		def __newMerged = KeywordsTest(
			__size,
			__case,
			__val,
			__var,
			__def,
			__object,
			__class,
			__lazy,
			__type
		)
		while (true) in.readTag match {
			case 0 => return __newMerged
			case 8 => __size = in.readInt64()
			case 16 => __case = in.readInt32()
			case 24 => __val = in.readInt32()
			case 32 => __var = in.readInt32()
			case 40 => __def = in.readInt32()
			case 48 => __object = in.readInt32()
			case 56 => __class = in.readInt32()
			case 64 => __lazy = in.readInt32()
			case 72 => __type = in.readInt32()
			case default => if (!in.skipField(default)) return __newMerged
		}
		null
	}

	def mergeFrom(m: KeywordsTest) = {
		KeywordsTest(
			m.`size`,
			m.`case`,
			m.`val`,
			m.`var`,
			m.`def`,
			m.`object`,
			m.`class`,
			m.`lazy`,
			m.`type`
		)
	}

	def getDefaultInstanceForType = KeywordsTest.defaultInstance
	def clear = getDefaultInstanceForType
	def isInitialized = true
	def build = this
	def buildPartial = this
	def parsePartialFrom(cis: com.google.protobuf.CodedInputStream, er: com.google.protobuf.ExtensionRegistryLite) = mergeFrom(cis, er)
	override def getParserForType = this
	def newBuilderForType = getDefaultInstanceForType
	def toBuilder = this
	def toJson(indent: Int = 0): String = {
		val indent0 = "\n" + ("\t" * indent)
		val (indent1, indent2) = (indent0 + "\t", indent0 + "\t\t")
		val sb = StringBuilder.newBuilder
		sb
			.append("{")
			sb.append(indent1).append("\"size\": ").append("\"").append(`size`).append("\"").append(',')
			sb.append(indent1).append("\"case\": ").append("\"").append(`case`).append("\"").append(',')
			sb.append(indent1).append("\"val\": ").append("\"").append(`val`).append("\"").append(',')
			sb.append(indent1).append("\"var\": ").append("\"").append(`var`).append("\"").append(',')
			sb.append(indent1).append("\"def\": ").append("\"").append(`def`).append("\"").append(',')
			sb.append(indent1).append("\"object\": ").append("\"").append(`object`).append("\"").append(',')
			sb.append(indent1).append("\"class\": ").append("\"").append(`class`).append("\"").append(',')
			sb.append(indent1).append("\"lazy\": ").append("\"").append(`lazy`).append("\"").append(',')
			sb.append(indent1).append("\"type\": ").append("\"").append(`type`).append("\"").append(',')
		sb.length -= 1
		sb.append(indent0).append("}")
		sb.toString()
	}

}

object KeywordsTest {
	@reflect.BeanProperty val defaultInstance = new KeywordsTest()

	def parseFrom(data: Array[Byte]): KeywordsTest = defaultInstance.mergeFrom(data)
	def parseFrom(data: Array[Byte], offset: Int, length: Int): KeywordsTest = defaultInstance.mergeFrom(data, offset, length)
	def parseFrom(byteString: com.google.protobuf.ByteString): KeywordsTest = defaultInstance.mergeFrom(byteString)
	def parseFrom(stream: java.io.InputStream): KeywordsTest = defaultInstance.mergeFrom(stream)
	def parseDelimitedFrom(stream: java.io.InputStream): Option[KeywordsTest] = defaultInstance.mergeDelimitedFromStream(stream)

	val SIZE_FIELD_NUMBER = 1
	val CASE_FIELD_NUMBER = 2
	val VAL_FIELD_NUMBER = 3
	val VAR_FIELD_NUMBER = 4
	val DEF_FIELD_NUMBER = 5
	val OBJECT_FIELD_NUMBER = 6
	val CLASS_FIELD_NUMBER = 7
	val LAZY_FIELD_NUMBER = 8
	val TYPE_FIELD_NUMBER = 9

	def newBuilder = defaultInstance.newBuilderForType
	def newBuilder(prototype: KeywordsTest) = defaultInstance.mergeFrom(prototype)

}

object Keywords {
	def registerAllExtensions(registry: com.google.protobuf.ExtensionRegistryLite) {
	}

	private val fromBinaryHintMap = collection.immutable.HashMap[String, Array[Byte] ⇒ com.google.protobuf.GeneratedMessageLite](
		 "KeywordsTest" -> (bytes ⇒ KeywordsTest.parseFrom(bytes))
	)

	def deserializePayload(payload: Array[Byte], payloadType: String): com.google.protobuf.GeneratedMessageLite = {
		fromBinaryHintMap.get(payloadType) match {
			case Some(f) ⇒ f(payload)
			case None    ⇒ throw new IllegalArgumentException(s"unimplemented deserialization of message payload of type [${payloadType}]")
		}
	}
}
