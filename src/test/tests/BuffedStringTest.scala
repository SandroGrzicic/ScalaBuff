package tests

import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers
import hr.sandrogrzicic.scalabuff.BuffedString

/**
 * Tests for the BuffedString string helper class.
 * @author Sandro Gržičić
 */

class BuffedStringTest extends FunSuite with ShouldMatchers {

	implicit def buffString(string: String): BuffedString = new BuffedString(string)

	test("camelCase") {
		"very_long_name_in_c_style_001".camelCase should equal ("VeryLongNameInCStyle001")
	}

	test("takeFromLast") {
		"path/file.extension".takeFromLast('/') should equal ("file.extension")
	}

	test("dropUntilLast") {
		"path/file.extension".dropUntilLast('.') should equal ("path/file")
	}
	test("betweenLast") {
		"path/file.extension".betweenLast('/', '.') should equal ("file")
	}
}