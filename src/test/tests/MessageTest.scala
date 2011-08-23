package tests

import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers
import resources.generated._
import com.google.protobuf._

/**
 * Tests whether generated Scala classes function correctly.
 * @author Sandro Gržičić
 */

class MessageTest extends FunSuite with ShouldMatchers {

	test("ComplexMessage") {
		val nestedNested = "Nested String"
		val nestedEnum = ComplexMessage.SimpleEnum.KEY_NAME

		val first = ByteString.copyFromUtf8("Sandro Gržičić")
		val second = "Sandro Grzicic"
		val nestedOuter = ComplexMessage.Nested(nestedNested, Some(nestedEnum))
		val simpleEnum = Vector(ComplexMessage.SimpleEnum.KEY_NAME)
		val repeatedString = Vector("hr", "sandrogrzicic", "scalabuff")
		val repeatedBytes = Vector(ByteString.copyFrom(Array[Byte](1, 2, 3)), ByteString.copyFrom(Array[Byte](4, 5, 6)))

		val sent = ComplexMessage(first, Some(second), Some(nestedOuter), simpleEnum, repeatedString, repeatedBytes)

		sent.firstField should equal(first)
		sent.getSecondField should equal(second)
		sent.getNestedOuterField should equal(nestedOuter)
		sent.simpleEnumField should equal (simpleEnum)
		sent.repeatedStringField should equal (repeatedString)
		sent.repeatedBytesField should equal (repeatedBytes)

		val received = ComplexMessage.defaultInstance.mergeFrom(sent.toByteArray)

		received.firstField should equal(sent.firstField)
		received.getSecondField should equal(sent.getSecondField)
		received.getNestedOuterField should equal(sent.getNestedOuterField)
		received.simpleEnumField should equal (sent.simpleEnumField)
		received.repeatedStringField should equal (sent.repeatedStringField)
		received.repeatedBytesField should equal (sent.repeatedBytesField)

	}
}
