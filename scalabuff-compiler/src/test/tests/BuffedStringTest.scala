package tests

import org.scalatest.{FunSuite, Matchers}
import net.sandrogrzicic.scalabuff.compiler.BuffedString
import scala.language.implicitConversions

/**
 * Tests for the BuffedString string helper class.
 * @author Sandro Gržičić
 */

class BuffedStringTest extends FunSuite with Matchers {

	implicit def buffString(string: String): BuffedString = new BuffedString(string)

  test("camelCase") {
		"very_long_name_in_c_style_001".camelCase should equal ("Very_Long_Name_In_C_Style_001")
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

	test("camelCase Unique") {
		val first = "ASDF1_1".camelCase
		val second = "ASDF11".camelCase
		first should not equal second
	}

}
