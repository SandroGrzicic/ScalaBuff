// Generated by ScalaBuff, the Scala Protocol Buffers compiler. DO NOT EDIT!
// source: enum_to_import.proto

package resources.generated.nested

object AnEnumToImport extends net.sandrogrzicic.scalabuff.Enum {
	sealed trait EnumVal extends Value
	val _UNINITIALIZED = new EnumVal { val name = "UNINITIALIZED ENUM VALUE"; val id = -1 }

	val KEYBOARD = new EnumVal { val name = "KEYBOARD"; val id = 1 }
	val MOUSE = new EnumVal { val name = "MOUSE"; val id = 2 }

	val KEYBOARD_VALUE = 1
	val MOUSE_VALUE = 2

	def valueOf(id: Int) = id match {
		case 1 => KEYBOARD
		case 2 => MOUSE
		case _default => throw new net.sandrogrzicic.scalabuff.UnknownEnumException(_default)
	}
	val internalGetValueMap = new com.google.protobuf.Internal.EnumLiteMap[EnumVal] {
		def findValueByNumber(id: Int): EnumVal = valueOf(id)
	}
}

object EnumToImport {
	def registerAllExtensions(registry: com.google.protobuf.ExtensionRegistryLite) {
	}

	private val fromBinaryHintMap = collection.immutable.HashMap[String, Array[Byte] ⇒ com.google.protobuf.GeneratedMessageLite](

	)

	def deserializePayload(payload: Array[Byte], payloadType: String): com.google.protobuf.GeneratedMessageLite = {
		fromBinaryHintMap.get(payloadType) match {
			case Some(f) ⇒ f(payload)
			case None    ⇒ throw new IllegalArgumentException(s"unimplemented deserialization of message payload of type [${payloadType}]")
		}
	}
}
