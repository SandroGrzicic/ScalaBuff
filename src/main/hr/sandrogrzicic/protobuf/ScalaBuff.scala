package hr.sandrogrzicic.protobuf

import java.io._

/**
 * Protobuf Parser helper.
 * @author Sandro Gržičić
 */

object ScalaBuff {

	implicit def stream2reader(stream: InputStream) = new BufferedReader(new InputStreamReader(stream, "utf-8"))

	/**
	 * Runner: Runs the Protobuf Parser on the specified resource path and prints the output.
	 */
	def main(args: Array[String]) {
		if (args.length < 1)
			exit("Required parameter: input protobuf file name or URL.")

		val resourceName = args(0)
		try {
			println(parse(resourceName))
		} catch {
			case e => {
				exit(
					"Error: Cannot access specified resource [" + resourceName + "]:\n[" +
					e.getMessage + "]"
				)
			}
		}
	}

	/**
	 * Runs the Protobuf Parser on the specified resource path and returns the output.
	 */
	def parse(resourcePath: String) {
		var reader: Reader = null
		try {
			reader = new FileInputStream(resourcePath)
		} catch {
			case fnf: FileNotFoundException =>
				reader = new java.net.URL(resourcePath).openStream
			case e => throw e
		}
		Parser(reader)
	}

	/**
 	* Print out the specified message and exit.
 	*/
	protected def exit(message: String) {
		println(message)
		System.exit(1)
	}


}