package hr.sandrogrzicic.protobuf

import java.io.{BufferedReader, InputStreamReader, FileInputStream}

/**
 * Protobuf Parser runner.
 * @author Sandro Gržičić
 */

object ScalaBuff {

	/**
	 * Runs the Protobuf Parser on the input file.
	 */
	def main(args: Array[String]) {
		val reader = new BufferedReader(new InputStreamReader(new FileInputStream(args(0)), "utf-8"))
		println(Parser(reader))
	}

}