package parser

import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers
import hr.sandrogrzicic.protobuf.Parser
import java.io.{File, FileInputStream, InputStreamReader, BufferedReader}

/**
 * ScalaTest Parser test.
 * @author Sandro Gržičić
 */

class ParserTest extends FunSuite with ShouldMatchers {

	lazy val parserOutputSimple = "List(((message~SimpleRequest)~(({~List(((((((required~string)~query)~=)~1)~None)~;), ((((((optional~int32)~page_number)~=)~2)~None)~;), ((((((optional~int32)~results_per_page)~=)~3)~None)~;)))~})))"

	test("simple .proto file") {
		val file = new File("src/test/resources/proto/simple.proto")
		val input = new BufferedReader(new InputStreamReader(new FileInputStream(file), "utf-8"))

		Parser(input).toString should equal (parserOutputSimple)
	}
	test("simple .proto file with comments") {
		val file = new File("src/test/resources/proto/simpleWithComments.proto")
		val input = new BufferedReader(new InputStreamReader(new FileInputStream(file), "utf-8"))

		Parser(input).toString should equal (parserOutputSimple)
	}

}