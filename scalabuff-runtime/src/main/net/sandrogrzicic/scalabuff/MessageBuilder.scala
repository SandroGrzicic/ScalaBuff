package net.sandrogrzicic.scalabuff

import com.google.protobuf.{ExtensionRegistryLite, CodedInputStream, ByteString}
import java.io.{FilterInputStream, InputStream}

/**
 * Message trait for messages generated with ScalaBuff.
 * Ordinarily Messages would have GeneratedMessageLite.Builder mixed in, but since it's a Java class, we can't do that.
 * Contains methods implementing the MessageLite.Builder Java interface, similar to ones in GeneratedMessageLite.Builder.
 *
 * @author Sandro Gržičić
 */

trait MessageBuilder[MessageType]{
  implicit def _anyToOption[T](any: T): Option[T] = Option[T](any)

  implicit def _stringToByteString(string: String): ByteString = ByteString.copyFromUtf8(string)

  def mergeFrom(message: MessageType): MessageType

  def isInitialized: Boolean

  def mergeFrom(input: CodedInputStream, extensionRegistry: ExtensionRegistryLite): MessageType

  def mergeFrom(input: CodedInputStream): MessageType = mergeFrom(input, ExtensionRegistryLite.getEmptyRegistry)

  def mergeFrom(data: ByteString): MessageType = {
    val input = data.newCodedInput
    val merged = mergeFrom(input)
    input.checkLastTagWas(0)
    merged
  }

  def mergeFrom(data: ByteString, extensionRegistry: ExtensionRegistryLite): MessageType = {
    val input = data.newCodedInput
    val merged = mergeFrom(input, extensionRegistry)
    input.checkLastTagWas(0)
    merged
  }

  def mergeFrom(data: Array[Byte]): MessageType = mergeFrom(data, 0, data.length)

  def mergeFrom(data: Array[Byte], offset: Int, length: Int): MessageType = {
    val input = CodedInputStream.newInstance(data, offset, length)
    val merged = mergeFrom(input)
    input.checkLastTagWas(0)
    merged
  }

  def mergeFrom(data: Array[Byte], extensionRegistry: ExtensionRegistryLite): MessageType = mergeFrom(data, 0, data.length, extensionRegistry)

  def mergeFrom(data: Array[Byte], off: Int, len: Int, extensionRegistry: ExtensionRegistryLite): MessageType = {
    val input = CodedInputStream.newInstance(data, off, len)
    val merged = mergeFrom(input, extensionRegistry)
    input.checkLastTagWas(0)
    merged
  }

  def mergeFrom(input: InputStream): MessageType = {
    val codedInput = CodedInputStream.newInstance(input)
    val merged = mergeFrom(codedInput)
    codedInput.checkLastTagWas(0)
    merged
  }

  def mergeFrom(input: InputStream, extensionRegistry: ExtensionRegistryLite): MessageType = {
    val codedInput = CodedInputStream.newInstance(input)
    val merged = mergeFrom(codedInput, extensionRegistry)
    codedInput.checkLastTagWas(0)
    merged
  }

  /**
   * NOTE: Due to Java Protocol Buffers library compatibility, this method is useless in most cases.
   * @see Message#mergeDelimitedFromStream()
   */
  def mergeDelimitedFrom(input: InputStream, extensionRegistry: ExtensionRegistryLite) = {
    val firstByte = input.read
    if (firstByte != -1) {
      val size = CodedInputStream.readRawVarint32(firstByte, input)
      val limitedInput = new LimitedInputStream(input, size)
      mergeFrom(limitedInput, extensionRegistry)
      true
    } else {
      false
    }
  }

  /**
   * NOTE: Due to Java Protocol Buffers library compatibility, this method is useless in most cases.
   * @see Message#mergeDelimitedFromStream()
   */
  def mergeDelimitedFrom(input: InputStream): Boolean = {
    mergeDelimitedFrom(input, ExtensionRegistryLite.getEmptyRegistry)
  }

  def mergeDelimitedFromStream(input: InputStream, extensionRegistry: ExtensionRegistryLite): Option[MessageType] = {
    val firstByte = input.read
    if (firstByte != -1) {
      val size = CodedInputStream.readRawVarint32(firstByte, input)
      val limitedInput = new LimitedInputStream(input, size)
      Some(mergeFrom(limitedInput, extensionRegistry))
    } else {
      None
    }
  }

  def mergeDelimitedFromStream(input: InputStream): Option[MessageType] = {
    mergeDelimitedFromStream(input, ExtensionRegistryLite.getEmptyRegistry)
  }


  /**
   * See {@link com.google.protobuf.AbstractMessageLite.Builder#LimitedInputStream}.
   */
  private final class LimitedInputStream(
                                          val inputStream: InputStream, private var limit: Int
                                          ) extends FilterInputStream(inputStream) {

    override def available = scala.math.min(super.available, limit)

    override def read = {
      if (limit > 0) {
        val result = super.read
        if (result >= 0) {
          limit -= 1
        }
        result
      } else {
        -1
      }
    }

    override def read(bytes: Array[Byte], offset: Int, length: Int) = {
      if (limit > 0) {
        val limitedLength = scala.math.min(length, limit)
        val result = super.read(bytes, offset, limitedLength)
        if (result >= 0) {
          limit -= result
        }
        result
      } else {
        -1
      }
    }

    override def skip(n: Long) = {
      val result = super.skip(scala.math.min(n, limit))
      if (result >= 0) {
        limit = (limit - result).toInt
      }
      result
    }
  }
  /**
   * See {@link com.google.protobuf.CodedInputStream#readMessage}.
   *
   * CodedInputStream#readMessage attempts to mutate the passed Builder and discards the returned value,
   * which we need, since our "Builders" (Messages) return a new instance whenever a mutation is performed.
   */
  def readMessage[ReadMessageType <: MessageBuilder[ReadMessageType]](in: CodedInputStream, message: ReadMessageType, extensionRegistry: ExtensionRegistryLite) = {
    val length = in.readRawVarint32()
    val oldLimit = in.pushLimit(length)

    val newMessage = message.mergeFrom(in, extensionRegistry).asInstanceOf[ReadMessageType]

    in.checkLastTagWas(0)
    in.popLimit(oldLimit)

    newMessage
  }

}
