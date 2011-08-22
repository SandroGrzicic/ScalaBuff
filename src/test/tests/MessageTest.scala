package tests

import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers
import resources.generated._

/**
 * Tests whether generated classes function correctly.
 * @author Sandro Gržičić
 */

class MessageTest extends FunSuite with ShouldMatchers {
	 test("Complex Message") {
		 val input = "Sandro Grzicic"

		 val sent = ComplexMessage.Nested(input)

		 sent.nestedField should equal (input)

		 val received = ComplexMessage.Nested.defaultInstance.mergeFrom(sent.toByteArray)

		 received.nestedField should equal (sent.nestedField)
	 }
}