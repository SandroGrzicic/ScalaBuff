package hr.sandrogrzicic.protobuf

import java.net.URL
import java.io._

/**
 * Protobuf Parser runner.
 * @author Sandro Gržičić
 */

object ScalaBuff {

	/**
	 * Runs the Protobuf Parser on the input file or URL.
	 */
	def main(args: Array[String]) {
		if (args.length < 1)
			exit("Required parameter: input protobuf file name or URL.")

		var reader: Reader = null
		val name = args(0)
		try {
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(name), "utf-8"))
		} catch {
			case fnf: FileNotFoundException =>
				try {
					reader = new BufferedReader(new InputStreamReader(new URL(name).openStream))
				} catch {
					case e => exit(
						"Error: Cannot access specified file/URL " + name + ":\n[" +
						e.getMessage + "]"
					)
				}
			case e => exit(
				"Error: Cannot access specified file/URL " + name + ":\n[" +
				e.getMessage + "]"
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