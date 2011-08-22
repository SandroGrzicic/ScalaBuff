package tests

import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers
import resources.generated._
import com.google.protobuf._

/**
 * Tests whether generated classes function correctly.
 * @author Sandro Gržičić
 */

class MessageTest extends FunSuite with ShouldMatchers {
	test("Complex Message") {
		val nestedOuterNested = "Nested Outer Nested String"
		val nestedOuterEnum = ComplexMessage.SimpleEnum.KEY_NAME

		val first = ByteString.copyFromUtf8("Sandro Gržičić")
		val second = "Sandro Grzicic"
		val nestedOuter = ComplexMessage.Nested.apply(nestedOuterNested, Some(nestedOuterEnum))
		val simpleEnum = Vector(ComplexMessage.SimpleEnum.KEY_NAME)

		val sent = ComplexMessage.apply(first, Some(second), Some(nestedOuter), simpleEnum)

		sent.firstField should equal(first)
		sent.getSecondField should equal(second)
		sent.getNestedOuterField should equal(nestedOuter)
		sent.simpleEnumField should equal (simpleEnum)

		val received = ComplexMessage.defaultInstance.mergeFrom(sent.toByteArray)

		received.firstField should equal(sent.firstField)
		received.getSecondField should equal(sent.getSecondField)
		received.getNestedOuterField should equal(sent.getNestedOuterField)
		received.simpleEnumField should equal (sent.simpleEnumField)
	}
}