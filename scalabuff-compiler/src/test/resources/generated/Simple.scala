// Generated by ScalaBuff, the Scala Protocol Buffers compiler. DO NOT EDIT!
// source: simple.proto

package resources.generated

final case class SimpleTest (
	`requiredField`: Int = 0,
	`optionalField`: Option[Float] = None,
	`repeatedField`: scala.collection.immutable.Seq[String] = Vector.empty[String],
	`type`: Option[Int] = Some(100),
	`int32Default`: Option[Int] = Some(100),
	`int32Negative`: Option[Int] = Some(-1),
	`stringDefault`: Option[String] = Some("somestring"),
	`floatDefault`: Option[Float] = Some(1.0f),
	`floatNegative`: Option[Float] = Some(-1.0f)
) extends com.google.protobuf.GeneratedMessageLite
	with com.google.protobuf.MessageLite.Builder
	with net.sandrogrzicic.scalabuff.Message[SimpleTest]
	with net.sandrogrzicic.scalabuff.Parser[SimpleTest] {

	def setOptionalField(_f: Float) = copy(`optionalField` = Some(_f))
	def setRepeatedField(_i: Int, _v: String) = copy(`repeatedField` = `repeatedField`.updated(_i, _v))
	def addRepeatedField(_f: String) = copy(`repeatedField` = `repeatedField` :+ _f)
	def addAllRepeatedField(_f: String*) = copy(`repeatedField` = `repeatedField` ++ _f)
	def addAllRepeatedField(_f: TraversableOnce[String]) = copy(`repeatedField` = `repeatedField` ++ _f)
	def setType(_f: Int) = copy(`type` = Some(_f))
	def setInt32Default(_f: Int) = copy(`int32Default` = Some(_f))
	def setInt32Negative(_f: Int) = copy(`int32Negative` = Some(_f))
	def setStringDefault(_f: String) = copy(`stringDefault` = Some(_f))
	def setFloatDefault(_f: Float) = copy(`floatDefault` = Some(_f))
	def setFloatNegative(_f: Float) = copy(`floatNegative` = Some(_f))

	def clearOptionalField = copy(`optionalField` = None)
	def clearRepeatedField = copy(`repeatedField` = Vector.empty[String])
	def clearType = copy(`type` = None)
	def clearInt32Default = copy(`int32Default` = None)
	def clearInt32Negative = copy(`int32Negative` = None)
	def clearStringDefault = copy(`stringDefault` = None)
	def clearFloatDefault = copy(`floatDefault` = None)
	def clearFloatNegative = copy(`floatNegative` = None)

	def writeTo(output: com.google.protobuf.CodedOutputStream) {
		output.writeInt32(1, `requiredField`)
		if (`optionalField`.isDefined) output.writeFloat(2, `optionalField`.get)
		for (_v <- `repeatedField`) output.writeString(3, _v)
		if (`type`.isDefined) output.writeInt32(4, `type`.get)
		if (`int32Default`.isDefined) output.writeInt32(5, `int32Default`.get)
		if (`int32Negative`.isDefined) output.writeInt32(6, `int32Negative`.get)
		if (`stringDefault`.isDefined) output.writeString(7, `stringDefault`.get)
		if (`floatDefault`.isDefined) output.writeFloat(8, `floatDefault`.get)
		if (`floatNegative`.isDefined) output.writeFloat(9, `floatNegative`.get)
	}

	def getSerializedSize = {
		import com.google.protobuf.CodedOutputStream._
		var __size = 0
		__size += computeInt32Size(1, `requiredField`)
		if (`optionalField`.isDefined) __size += computeFloatSize(2, `optionalField`.get)
		for (_v <- `repeatedField`) __size += computeStringSize(3, _v)
		if (`type`.isDefined) __size += computeInt32Size(4, `type`.get)
		if (`int32Default`.isDefined) __size += computeInt32Size(5, `int32Default`.get)
		if (`int32Negative`.isDefined) __size += computeInt32Size(6, `int32Negative`.get)
		if (`stringDefault`.isDefined) __size += computeStringSize(7, `stringDefault`.get)
		if (`floatDefault`.isDefined) __size += computeFloatSize(8, `floatDefault`.get)
		if (`floatNegative`.isDefined) __size += computeFloatSize(9, `floatNegative`.get)

		__size
	}

	def mergeFrom(in: com.google.protobuf.CodedInputStream, extensionRegistry: com.google.protobuf.ExtensionRegistryLite): SimpleTest = {
		import com.google.protobuf.ExtensionRegistryLite.{getEmptyRegistry => _emptyRegistry}
		var __requiredField: Int = 0
		var __optionalField: Option[Float] = `optionalField`
		val __repeatedField: scala.collection.mutable.Buffer[String] = `repeatedField`.toBuffer
		var __type: Option[Int] = `type`
		var __int32Default: Option[Int] = `int32Default`
		var __int32Negative: Option[Int] = `int32Negative`
		var __stringDefault: Option[String] = `stringDefault`
		var __floatDefault: Option[Float] = `floatDefault`
		var __floatNegative: Option[Float] = `floatNegative`

		def __newMerged = SimpleTest(
			__requiredField,
			__optionalField,
			Vector(__repeatedField: _*),
			__type,
			__int32Default,
			__int32Negative,
			__stringDefault,
			__floatDefault,
			__floatNegative
		)
		while (true) in.readTag match {
			case 0 => return __newMerged
			case 8 => __requiredField = in.readInt32()
			case 21 => __optionalField = Some(in.readFloat())
			case 26 => __repeatedField += in.readString()
			case 32 => __type = Some(in.readInt32())
			case 40 => __int32Default = Some(in.readInt32())
			case 48 => __int32Negative = Some(in.readInt32())
			case 58 => __stringDefault = Some(in.readString())
			case 69 => __floatDefault = Some(in.readFloat())
			case 77 => __floatNegative = Some(in.readFloat())
			case default => if (!in.skipField(default)) return __newMerged
		}
		null
	}

	def mergeFrom(m: SimpleTest) = {
		SimpleTest(
			m.`requiredField`,
			m.`optionalField`.orElse(`optionalField`),
			`repeatedField` ++ m.`repeatedField`,
			m.`type`.orElse(`type`),
			m.`int32Default`.orElse(`int32Default`),
			m.`int32Negative`.orElse(`int32Negative`),
			m.`stringDefault`.orElse(`stringDefault`),
			m.`floatDefault`.orElse(`floatDefault`),
			m.`floatNegative`.orElse(`floatNegative`)
		)
	}

	def getDefaultInstanceForType = SimpleTest.defaultInstance
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
			sb.append(indent1).append("\"requiredField\": ").append("\"").append(`requiredField`).append("\"").append(',')
			if (`optionalField`.isDefined) { sb.append(indent1).append("\"optionalField\": ").append("\"").append(`optionalField`.get).append("\"").append(',') }
			sb.append(indent1).append("\"repeatedField\": [").append(indent2).append(`repeatedField`.map("\"" + _ + "\"").mkString(", " + indent2)).append(indent1).append(']').append(',')
			if (`type`.isDefined) { sb.append(indent1).append("\"type\": ").append("\"").append(`type`.get).append("\"").append(',') }
			if (`int32Default`.isDefined) { sb.append(indent1).append("\"int32Default\": ").append("\"").append(`int32Default`.get).append("\"").append(',') }
			if (`int32Negative`.isDefined) { sb.append(indent1).append("\"int32Negative\": ").append("\"").append(`int32Negative`.get).append("\"").append(',') }
			if (`stringDefault`.isDefined) { sb.append(indent1).append("\"stringDefault\": ").append("\"").append(`stringDefault`.get).append("\"").append(',') }
			if (`floatDefault`.isDefined) { sb.append(indent1).append("\"floatDefault\": ").append("\"").append(`floatDefault`.get).append("\"").append(',') }
			if (`floatNegative`.isDefined) { sb.append(indent1).append("\"floatNegative\": ").append("\"").append(`floatNegative`.get).append("\"").append(',') }
		if (sb.last.equals(',')) sb.length -= 1
		sb.append(indent0).append("}")
		sb.toString()
	}

}

object SimpleTest {
	@scala.beans.BeanProperty val defaultInstance = new SimpleTest()

	def parseFrom(data: Array[Byte]): SimpleTest = defaultInstance.mergeFrom(data)
	def parseFrom(data: Array[Byte], offset: Int, length: Int): SimpleTest = defaultInstance.mergeFrom(data, offset, length)
	def parseFrom(byteString: com.google.protobuf.ByteString): SimpleTest = defaultInstance.mergeFrom(byteString)
	def parseFrom(stream: java.io.InputStream): SimpleTest = defaultInstance.mergeFrom(stream)
	def parseDelimitedFrom(stream: java.io.InputStream): Option[SimpleTest] = defaultInstance.mergeDelimitedFromStream(stream)

	val REQUIRED_FIELD_FIELD_NUMBER = 1
	val OPTIONAL_FIELD_FIELD_NUMBER = 2
	val REPEATED_FIELD_FIELD_NUMBER = 3
	val TYPE_FIELD_NUMBER = 4
	val INT32DEFAULT_FIELD_NUMBER = 5
	val INT32NEGATIVE_FIELD_NUMBER = 6
	val STRINGDEFAULT_FIELD_NUMBER = 7
	val FLOATDEFAULT_FIELD_NUMBER = 8
	val FLOATNEGATIVE_FIELD_NUMBER = 9

	def newBuilder = defaultInstance.newBuilderForType
	def newBuilder(prototype: SimpleTest) = defaultInstance.mergeFrom(prototype)

}

object Simple {
	def registerAllExtensions(registry: com.google.protobuf.ExtensionRegistryLite) {
	}

	private val fromBinaryHintMap = collection.immutable.HashMap[String, Array[Byte] ⇒ com.google.protobuf.GeneratedMessageLite](
		 "SimpleTest" -> (bytes ⇒ SimpleTest.parseFrom(bytes))
	)

	def deserializePayload(payload: Array[Byte], payloadType: String): com.google.protobuf.GeneratedMessageLite = {
		fromBinaryHintMap.get(payloadType) match {
			case Some(f) ⇒ f(payload)
			case None    ⇒ throw new IllegalArgumentException(s"unimplemented deserialization of message payload of type [${payloadType}]")
		}
	}
}
