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

	test("takeFromLast simple") {
		"path/file.extension".takeFromLast('/') should equal ("file.extension")
	}

	test("takeFromLast complex") {
		"some/path/file.name.extension".takeFromLast('/') should equal ("file.name.extension")
	}

	test("dropUntilLast simple") {
		"path/file.extension".dropUntilLast('.') should equal ("path/file")
	}

	test("dropUntilLast complex") {
		"some/path/file.name.extension".dropUntilLast('.') should equal ("some/path/file.name")
	}

	test("betweenLast simple") {
		"path/file.extension".betweenLast('/', '.') should equal ("file")
	}

	test("betweenLast complex") {
		"some/path/file.name.extension".betweenLast('/', '.') should equal ("file.name")
	}

}