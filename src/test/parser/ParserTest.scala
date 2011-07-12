package parser

import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers
import hr.sandrogrzicic.protobuf.Parser
import java.io.{File, FileInputStream, InputStreamReader, BufferedReader}

/**
 * @author Sandro Gržičić
 */

class ParserTest extends FunSuite with ShouldMatchers {

	test("simple .proto file") {
		val file = new File("src/test/resources/proto/simple001.proto")
		val input = new BufferedReader(new InputStreamReader(new FileInputStream(file), "utf-8"))

		Parser(input).toString should equal ("List(((message~SimpleRequest)~(({~List(((((((required~string)~query)~=)~1)~None)~;), ((((((optional~int32)~page_number)~=)~2)~None)~;), ((((((optional~int32)~result_per_page)~=)~3)~None)~;)))~})))")
	}
}