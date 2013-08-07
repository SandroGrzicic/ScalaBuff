package net.sandrogrzicic.scalabuff

import com.google.protobuf._
import java.io.IOException
import java.io.InputStream

/**
 * Trait which implements most of the com.google.protobuf.Parser interface methods.
 *
 * @author Sandro Gržičić
 */
trait Parser[MessageType <: MessageLite] extends com.google.protobuf.Parser[MessageType] {
  final val EMPTY_REGISTRY = ExtensionRegistryLite.getEmptyRegistry

  private def checkMessageInitialized(message: MessageType): MessageType = {
    if (message != null && !message.isInitialized) {
      throw new UninitializedMessageException(message)
          .asInvalidProtocolBufferException
          .setUnfinishedMessage(message)
    }
    message
  }

  def parsePartialFrom(input: CodedInputStream): MessageType = {
     parsePartialFrom(input, EMPTY_REGISTRY)
  }

  def parseFrom(input: CodedInputStream, extensionRegistry: ExtensionRegistryLite): MessageType = {
    checkMessageInitialized(parsePartialFrom(input, extensionRegistry))
  }

  def parseFrom(input: CodedInputStream): MessageType = {
    parseFrom(input, EMPTY_REGISTRY)
  }

  def parsePartialFrom(data: ByteString, extensionRegistry: ExtensionRegistryLite): MessageType = {
    val input = data.newCodedInput
    val message = parsePartialFrom(input, extensionRegistry)
    try {
      input.checkLastTagWas(0)
    }
    catch {
      case e: InvalidProtocolBufferException => {
        throw e.setUnfinishedMessage(message)
      }
    }
    message
  }

  def parsePartialFrom(data: ByteString): MessageType = {
    parsePartialFrom(data, EMPTY_REGISTRY)
  }

  def parseFrom(data: ByteString, extensionRegistry: ExtensionRegistryLite): MessageType = {
    checkMessageInitialized(parsePartialFrom(data, extensionRegistry))
  }

  def parseFrom(data: ByteString): MessageType = {
    parseFrom(data, EMPTY_REGISTRY)
  }

  def parsePartialFrom(data: Array[Byte], off: Int, len: Int, extensionRegistry: ExtensionRegistryLite): MessageType = {
    val input = CodedInputStream.newInstance(data, off, len)
    val message = parsePartialFrom(input, extensionRegistry)
    try {
      input.checkLastTagWas(0)
    } catch {
      case e: InvalidProtocolBufferException => {
        throw e.setUnfinishedMessage(message)
      }
    }
    message
  }

  def parsePartialFrom(data: Array[Byte], off: Int, len: Int): MessageType = {
    parsePartialFrom(data, off, len, EMPTY_REGISTRY)
  }

  def parsePartialFrom(data: Array[Byte], extensionRegistry: ExtensionRegistryLite): MessageType = {
    parsePartialFrom(data, 0, data.length, extensionRegistry)
  }

  def parsePartialFrom(data: Array[Byte]): MessageType = {
    parsePartialFrom(data, 0, data.length, EMPTY_REGISTRY)
  }

  def parseFrom(data: Array[Byte], off: Int, len: Int, extensionRegistry: ExtensionRegistryLite): MessageType = {
    checkMessageInitialized(parsePartialFrom(data, off, len, extensionRegistry))
  }

  def parseFrom(data: Array[Byte], off: Int, len: Int): MessageType = {
    parseFrom(data, off, len, EMPTY_REGISTRY)
  }

  def parseFrom(data: Array[Byte], extensionRegistry: ExtensionRegistryLite): MessageType = {
    parseFrom(data, 0, data.length, extensionRegistry)
  }

  def parseFrom(data: Array[Byte]): MessageType = {
    parseFrom(data, EMPTY_REGISTRY)
  }

  def parsePartialFrom(input: InputStream, extensionRegistry: ExtensionRegistryLite): MessageType = {
    val codedInput = CodedInputStream.newInstance(input)
    val message = parsePartialFrom(codedInput, extensionRegistry)
    try {
      codedInput.checkLastTagWas(0)
    } catch {
      case e: InvalidProtocolBufferException => {
        throw e.setUnfinishedMessage(message)
      }
    }

    message
  }

  def parsePartialFrom(input: InputStream): MessageType = {
    parsePartialFrom(input, EMPTY_REGISTRY)
  }

  def parseFrom(input: InputStream, extensionRegistry: ExtensionRegistryLite): MessageType = {
    checkMessageInitialized(parsePartialFrom(input, extensionRegistry))
  }

  def parseFrom(input: InputStream): MessageType = {
    parseFrom(input, EMPTY_REGISTRY)
  }

  def parsePartialDelimitedFrom(input: InputStream, extensionRegistry: ExtensionRegistryLite): MessageType = {
    var size = 0
    try {
      val firstByte = input.read
      if (firstByte == -1) {
        null
      }
      size = CodedInputStream.readRawVarint32(firstByte, input)
    }
    catch {
      case e: IOException => {
        throw new InvalidProtocolBufferException(e.getMessage)
      }
    }
    val limitedInput = new LimitedInputStream(input, size)
    parsePartialFrom(limitedInput, extensionRegistry)
  }

  def parsePartialDelimitedFrom(input: InputStream): MessageType = {
    parsePartialDelimitedFrom(input, EMPTY_REGISTRY)
  }

  def parseDelimitedFrom(input: InputStream, extensionRegistry: ExtensionRegistryLite): MessageType = {
    checkMessageInitialized(parsePartialDelimitedFrom(input, extensionRegistry))
  }

  def parseDelimitedFrom(input: InputStream): MessageType = {
    parseDelimitedFrom(input, EMPTY_REGISTRY)
  }
}

