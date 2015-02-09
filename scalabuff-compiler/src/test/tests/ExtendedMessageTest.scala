package tests

import org.scalatest.{FunSuite, Matchers}
import resources.generated._

/**
 * Tests whether generated Scala classes function correctly.
 * @author Sandro Gržičić
 */

class ExtendedMessageTest extends FunSuite with Matchers {

	test("ExtendableMessage") {
    val foo = 0

		val sent = ExtensionsTest(foo)

		sent.foo should equal (foo)

		val received = ExtensionsTest.defaultInstance.mergeFrom(sent.toByteArray)

    received.foo should equal (sent.foo)

	}

}
