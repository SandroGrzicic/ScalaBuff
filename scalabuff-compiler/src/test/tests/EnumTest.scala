package tests

import com.google.protobuf._
import java.io.{ByteArrayInputStream, ByteArrayOutputStream}
import org.scalatest.{FunSuite, Matchers}
import resources.generated._
import scala.collection._

/**
 * Tests whether generated Scala classes function correctly.
 * @author Sandro Gržičić
 */

class EnumTest extends FunSuite with Matchers {

  // This is copied from:
  //   https://code.google.com/p/protobuf/source/browse/trunk/java/src/main/java/com/google/protobuf/WireFormat.java?r=425#69
  // because Google has seen fit NOT to make these accessible.  Grr!

  private val TAG_TYPE_BITS = 3

  private def makeTag(fieldNumber: Int, wireType: Int): Byte = {
    val tag = (fieldNumber << TAG_TYPE_BITS) | wireType
    tag.toByte
  }

  test("enum.parseFrom: valid enum ID") {
    val message = MyPeripherals(Some(ComputerPeripherals.MOUSE))

    MyPeripherals.parseFrom(message.toByteArray) should equal (message)
    MyPeripherals.parseFrom(message.toByteString) should equal (message)

    val os = new ByteArrayOutputStream
    message.writeTo(os)

    MyPeripherals.parseFrom(new ByteArrayInputStream(os.toByteArray)) should equal (message)

    // Write out the enum in wire format
    val tag = makeTag(1, WireFormat.WIRETYPE_VARINT)
    val rawMessage = ByteString.copyFrom(Array[Byte](tag, 2))

    MyPeripherals.parseFrom(rawMessage.toByteArray) should equal (message)
  }

  test("enum.parseFrom: unknown enum ID without default") {
    val tag = makeTag(2, WireFormat.WIRETYPE_VARINT)
    val message = ByteString.copyFrom(Array[Byte](tag, 7))

    val thrown = intercept[Exception] {
      MyPeripherals.parseFrom(new ByteArrayInputStream(message.toByteArray)) should equal (message)
    }

    thrown.getMessage.contains("Unknown enum ID") should be(true)
  }

  test("enum.parseFrom: unknown enum ID with default") {
    val tag = makeTag(1, WireFormat.WIRETYPE_VARINT)
    val message = ByteString.copyFrom(Array[Byte](tag, 7))
    val defaultMessage = MyPeripherals(Some(ComputerPeripherals.KEYBOARD))

    MyPeripherals.parseFrom(new ByteArrayInputStream(message.toByteArray)) should equal (defaultMessage)
  }
}
