package hr.sandrogrzicic.protobuf

import java.io._

/**
 * Protobuf Parser runner.
 * @author Sandro Gržičić
 */

object ScalaBuff {

	implicit def stream2reader(stream: InputStream) = new BufferedReader(new InputStreamReader(stream, "utf-8"))

	/**
	 * Runs the Protobuf Parser on the specified resource.
	 */
	def main(args: Array[String]) {
		if (args.length < 1)
			exit("Required parameter: input protobuf file name or URL.")

		var reader: Reader = null
		val name = args(0)
		try {
			reader = new FileInputStream(name)
		} catch {
			case fnf: FileNotFoundException =>
				try {
					reader = new java.net.URL(name).openStream
				} catch {
					case e => exit(
						"Error: Cannot access specified resource [" + name + "]:\n[" +
						e.getMessage + "]"
					)
				}
			case e => exit(
				"Error: Cannot access specified resource [" + name + "]:\n[" +
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