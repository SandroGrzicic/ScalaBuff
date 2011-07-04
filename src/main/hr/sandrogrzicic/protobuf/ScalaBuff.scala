package hr.sandrogrzicic.protobuf

import java.io._

/**
 * Protobuf Parser runner.
 * @author Sandro Gržičić
 */

object ScalaBuff {

	/**
	 * Runs the Protobuf Parser on the input file.
	 */
	def main(args: Array[String]) {
		if (args.length < 1)
			exit("Required parameter: input protobuf file name.")

		var reader: Reader = null
		try {
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(args(0)), "utf-8"))
		} catch {
			case e => exit(
				"Error: Cannot access specified file " + args(0) + "!\n" +
				e.getMessage
			)
		}
		println(Parser(reader))
	}

	/**
	 * Print out the specified message and exit.
	 */
	protected def exit(message: String) {
		println(message)
		System.exit(1)
	}

}