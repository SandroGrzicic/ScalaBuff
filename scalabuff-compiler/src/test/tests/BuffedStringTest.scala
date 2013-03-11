package tests

import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers
import net.sandrogrzicic.scalabuff.compiler.BuffedString

/**
 * Tests for the BuffedString string helper class.
 * @author Sandro Gržičić
 */

class BuffedStringTest extends FunSuite with ShouldMatchers {

	implicit def buffString(string: String): BuffedString = new BuffedString(string)

  test("camelCase") {
		"very_long_name_in_c_style_001".camelCase should equal ("VeryLongNameInCStyle001")
	}

  val testPath = "root/dots.in.path/file.name.extension"

	test("dropUntilFirst") {
		testPath.dropUntilFirst('/') should equal ("dots.in.path/file.name.extension")
	}
	test("dropUntilLast") {
		testPath.dropUntilLast('/') should equal ("file.name.extension")
	}

	test("takeUntilFirst") {
		testPath.takeUntilFirst('.') should equal ("root/dots")
	}
	test("takeUntilLast") {
		testPath.takeUntilLast('.') should equal ("root/dots.in.path/file.name")
	}

	test("betweenLast") {
		testPath.betweenLast('/', '.') should equal ("file.name")
	}

}
