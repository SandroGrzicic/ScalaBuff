// Generated by ScalaBuff, the Scala Protocol Buffers compiler. DO NOT EDIT!
// source: keywords.proto

package resources.generated

final case class KeywordsTest (
	`size`: Long = 0L
) extends com.google.protobuf.GeneratedMessageLite
	with com.google.protobuf.MessageLite.Builder
	with net.sandrogrzicic.scalabuff.Message[KeywordsTest] {



	def writeTo(output: com.google.protobuf.CodedOutputStream) {
		output.writeInt64(1, `size`)
	}

	lazy val getSerializedSize = {
		import com.google.protobuf.CodedOutputStream._
		var __size = 0
		__size += computeInt64Size(1, `size`)

		__size
	}

	def mergeFrom(in: com.google.protobuf.CodedInputStream, extensionRegistry: com.google.protobuf.ExtensionRegistryLite): KeywordsTest = {
		import com.google.protobuf.ExtensionRegistryLite.{getEmptyRegistry => _emptyRegistry}
		var __size: Long = 0L

		def __newMerged = KeywordsTest(
			__size
		)
		while (true) in.readTag match {
			case 0 => return __newMerged
			case 8 => __size = in.readInt64()
			case default => if (!in.skipField(default)) return __newMerged
		}
		null
	}

	def mergeFrom(m: KeywordsTest) = {
		KeywordsTest(
			m.`size`
		)
	}

	def getDefaultInstanceForType = KeywordsTest.defaultInstance
	def clear = getDefaultInstanceForType
	def isInitialized = true
	def build = this
	def buildPartial = this
	def newBuilderForType = getDefaultInstanceForType
	def toBuilder = this
}

object KeywordsTest {
	@reflect.BeanProperty val defaultInstance = new KeywordsTest()

	def parseFrom(data: Array[Byte]): KeywordsTest = defaultInstance.mergeFrom(data)
	def parseFrom(data: Array[Byte], offset: Int, length: Int): KeywordsTest = defaultInstance.mergeFrom(data, offset, length)
	def parseFrom(byteString: com.google.protobuf.ByteString): KeywordsTest = defaultInstance.mergeFrom(byteString)
	def parseFrom(stream: java.io.InputStream): KeywordsTest = defaultInstance.mergeFrom(stream)
	def parseDelimitedFrom(stream: java.io.InputStream): Option[KeywordsTest] = defaultInstance.mergeDelimitedFromStream(stream)

	val SIZE_FIELD_NUMBER = 1

	def newBuilder = defaultInstance.newBuilderForType
	def newBuilder(prototype: KeywordsTest) = defaultInstance.mergeFrom(prototype)

}

object Keywords {
	def registerAllExtensions(registry: com.google.protobuf.ExtensionRegistryLite) {
	}

}
