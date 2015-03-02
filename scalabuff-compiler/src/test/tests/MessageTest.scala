package tests

import org.scalatest.{FunSuite, Matchers}
import resources.generated._
import com.google.protobuf._
import scala.collection._
import java.io.{ByteArrayInputStream, ByteArrayOutputStream}
import scala.util.{Try, Success, Failure}

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

    sent.firstField should equal(first)
    sent.secondField.get should equal(second)
    sent.nestedOuterField.get should equal(nestedOuter)
    sent.simpleEnumField should equal(simpleEnum)
    sent.repeatedStringField should equal(repeatedString)
    sent.repeatedBytesField should equal(repeatedBytes)

    val received = ComplexMessage.defaultInstance.mergeFrom(sent.toByteArray)

    received.firstField should equal (sent.firstField)
    received.secondField should equal (sent.secondField)
    received.nestedOuterField should equal (sent.nestedOuterField)
    received.simpleEnumField should equal (sent.simpleEnumField)
    received.repeatedStringField should equal (sent.repeatedStringField)
    received.repeatedBytesField should equal (sent.repeatedBytesField)
  }

  test("Missing required field") {
    val v1 = Required_v1(3,"test")
    val v2 = Required_v2(2,"test2","test3")


    val received1 = Required_v1.defaultInstance.mergeFrom(v2.toByteArray)
    val received2 = Try { Required_v2.defaultInstance.mergeFrom(v1.toByteArray) }

    val msg = received2 match {
      case Success(s) => s.toString
      case Failure(f) => f.getMessage
    }

    received1.`requiredField1` should equal(2)
    received1.`requiredField2` should equal("test2")
    received2.isFailure should equal(true)
    msg should equal("key not found: required_field_3")
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
