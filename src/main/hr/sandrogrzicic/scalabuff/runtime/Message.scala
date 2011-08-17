package hr.sandrogrzicic.scalabuff.runtime

import java.io.InputStream
import com.google.protobuf._

/**
 * Message trait for messages generated with ScalaBuff.
 * Acts as a wrapper for GeneratedMessageLite.Builder; contains some helpful methods similar to those in those classes.
 * @author Sandro Gržičić
 */

trait Message[MessageType] // extends MessageLite.Builder
// can't just extend GeneratedMessageLite.Builder because it's a class (Java..)
/* extends com.google.protobuf.GeneratedMessageLite.Builder[MessageType, MessageType] */ {
	self =>

	/**
	 * Converts any type to an Option.
	 */
	implicit def anyToOption[T](any: T) = Option[T](any)

	// methods that are implemented by the generated message

	def build(): MessageType

	def buildPartial(): MessageType

	def mergeFrom(message: MessageType): MessageType

	def getDefaultInstanceForType: MessageType

	def isInitialized: Boolean

	def mergeFrom(input: CodedInputStream, extensionRegistry: ExtensionRegistryLite): MessageType


	// helper methods that utilize the above methods

	def mergeFrom(input: CodedInputStream): MessageType = mergeFrom(input, ExtensionRegistryLite.getEmptyRegistry)

	def mergeFrom(data: ByteString): MessageType = {
		val input: CodedInputStream = data.newCodedInput
		mergeFrom(input)
		input.checkLastTagWas(0)
		this.asInstanceOf[MessageType]
	}

	def mergeFrom(data: ByteString, extensionRegistry: ExtensionRegistryLite): MessageType = {
		val input: CodedInputStream = data.newCodedInput
		mergeFrom(input, extensionRegistry)
		input.checkLastTagWas(0)
		this.asInstanceOf[MessageType]
	}

	def mergeFrom(data: Array[Byte]): MessageType = mergeFrom(data, 0, data.length)

	def mergeFrom(data: Array[Byte], offset: Int, length: Int): MessageType = {
		val input: CodedInputStream = CodedInputStream.newInstance(data, offset, length)
		mergeFrom(input)
		input.checkLastTagWas(0)
		this.asInstanceOf[MessageType]
	}

	def mergeFrom(data: Array[Byte], extensionRegistry: ExtensionRegistryLite): MessageType = mergeFrom(data, 0, data.length, extensionRegistry)

	def mergeFrom(data: Array[Byte], off: Int, len: Int, extensionRegistry: ExtensionRegistryLite): MessageType = {
		val input: CodedInputStream = CodedInputStream.newInstance(data, off, len)
		mergeFrom(input, extensionRegistry)
		input.checkLastTagWas(0)
		this.asInstanceOf[MessageType]
	}

	def mergeFrom(input: InputStream): MessageType = {
		val codedInput: CodedInputStream = CodedInputStream.newInstance(input)
		mergeFrom(codedInput)
		codedInput.checkLastTagWas(0)
		this.asInstanceOf[MessageType]
	}

	def mergeFrom(input: InputStream, extensionRegistry: ExtensionRegistryLite): MessageType = {
		val codedInput: CodedInputStream = CodedInputStream.newInstance(input)
		mergeFrom(codedInput, extensionRegistry)
		codedInput.checkLastTagWas(0)
		this.asInstanceOf[MessageType]
	}


}