package tests

import org.scalatest.{FunSuite, Matchers}
import resources.generated._
import com.google.protobuf._
import scala.collection._
import java.io.{ByteArrayInputStream, ByteArrayOutputStream}

/**
 * Tests whether generated Scala classes function correctly.
 * @author Sandro Gržičić
 */

class MessageTest extends FunSuite with Matchers {

  test("ComplexMessage") {
    val nestedNested = "Nested String"
    val nestedEnum = ComplexMessage.SimpleEnum.KEY_NAME

    val first = ByteString.copyFromUtf8("Sandro Gržičić")
    val second = "Sandro Grzicic"
    val nestedOuter = ComplexMessage.Nested(nestedNested, Some(nestedEnum))
    val simpleEnum = immutable.Seq(ComplexMessage.SimpleEnum.KEY_NAME)
    val repeatedString = List("net", "sandrogrzicic", "scalabuff")
    val repeatedBytes = Vector(ByteString.copyFrom(Array[Byte](1, 2, 3)), ByteString.copyFrom(Array[Byte](4, 5, 6)))

    val sent = ComplexMessage(first, Some(second), Some(nestedOuter), simpleEnum, repeatedString, repeatedBytes)

    sent.first_Field should equal(first)
    sent.second_Field.get should equal(second)
    sent.nested_Outer_Field.get should equal(nestedOuter)
    sent.simple_Enum_Field should equal(simpleEnum)
    sent.repeated_String_Field should equal(repeatedString)
    sent.repeated_Bytes_Field should equal(repeatedBytes)

    val received = ComplexMessage.defaultInstance.mergeFrom(sent.toByteArray)

    received.first_Field should equal (sent.first_Field)
    received.second_Field should equal (sent.second_Field)
    received.nested_Outer_Field should equal (sent.nested_Outer_Field)
    received.simple_Enum_Field should equal (sent.simple_Enum_Field)
    received.repeated_String_Field should equal (sent.repeated_String_Field)
    received.repeated_Bytes_Field should equal (sent.repeated_Bytes_Field)
  }

  test("object.parseFrom") {
    val message = ComplexMessage(ByteString.copyFromUtf8("Sandro Gržičić"))

    ComplexMessage.parseFrom(message.toByteArray) should equal (message)
    ComplexMessage.parseFrom(message.toByteString) should equal (message)

    val os = new ByteArrayOutputStream()
    message.writeTo(os)

    ComplexMessage.parseFrom(new ByteArrayInputStream(os.toByteArray)) should equal (message)
  }

  test("Keywords") {
    val message = KeywordsTest(123456789L)
    message.toByteArray.size should equal (21)
  }
}
