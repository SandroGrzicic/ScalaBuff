package net.sandrogrzicic.scalabuff

import java.io.{FilterInputStream, InputStream}

/**
 * See {@link com.google.protobuf.AbstractMessageLite.Builder#LimitedInputStream}.
 */
final class LimitedInputStream(
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
