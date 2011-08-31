package tests

import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers
import resources.generated._
import com.google.protobuf._
import java.util.Arrays

/**
 * Tests whether generated Scala classes are interoperable with corresponding Java classes.
 * @author Sandro Gržičić
 */

class JavaInteroperabilityTest extends FunSuite with ShouldMatchers {

	test("ComplexMessage") {
		import resources.java.{Complex => JComplex}

		val first = ByteString.copyFromUtf8("Sandro Gržičić")
		val second = "Sandro Grzicic"
		val nestedNested = "Nested String"

		val repeatedStringArray = Array("net", "sandrogrzicic", "scalabuff")
		val repeatedBytesArray = Array(ByteString.copyFrom(Array[Byte](1, 2, 3)), ByteString.copyFrom(Array[Byte](4, 5, 6)))

		val scala = ComplexMessage(
			first,
			Some(second),
			Some(ComplexMessage.Nested(
				nestedNested,
				Some(ComplexMessage.SimpleEnum.KEY_NAME)
			)),
			Vector(ComplexMessage.SimpleEnum.KEY_NAME),
			Vector(repeatedStringArray: _*),
			Vector(repeatedBytesArray: _*)
		)

		val javaNested = JComplex.ComplexMessage.Nested
			.newBuilder()
			.setNestedField(nestedNested)
			.setNestedEnum(JComplex.ComplexMessage.SimpleEnum.KEY_NAME)
			.build()

		val java = JComplex.ComplexMessage
			.newBuilder()
			.setFirstField(first)
			.setSecondField(second)
			.setNestedOuterField(javaNested)
			.addAllSimpleEnumField(Arrays.asList(JComplex.ComplexMessage.SimpleEnum.KEY_NAME))
			.addAllRepeatedStringField(Arrays.asList(repeatedStringArray: _*))
			.addAllRepeatedBytesField(Arrays.asList(repeatedBytesArray: _*))
			.build()

		Arrays.hashCode(scala.toByteArray) should equal (Arrays.hashCode(java.toByteArray))

	}

}
