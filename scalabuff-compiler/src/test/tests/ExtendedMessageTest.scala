package tests

import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers
import resources.generated._

/**
 * Tests whether generated Scala classes function correctly.
 * @author Sandro Gržičić
 */

class ExtendedMessageTest extends FunSuite with ShouldMatchers {

	test("ExtendableMessage") {
    val foo = 0

		val sent = ExtensionsTest(foo)

		sent.foo should equal (foo)

		val received = ExtensionsTest.defaultInstance.mergeFrom(sent.toByteArray)

    received.foo should equal (sent.foo)

	}

}
