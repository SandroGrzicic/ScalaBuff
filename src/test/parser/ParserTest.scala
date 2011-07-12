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

	implicit def file2reader(file: File) = new BufferedReader(new InputStreamReader(new FileInputStream(file), "utf-8"))

	test("simple .proto file") {
		val input = new File("src/test/resources/proto/simple.proto")
		Parser(input).toString should equal (parserOutputSimple)
	}
	test("simple .proto file with comments") {
		val input = new File("src/test/resources/proto/simpleWithComments.proto")
		Parser(input).toString should equal (parserOutputSimple)
	}

}