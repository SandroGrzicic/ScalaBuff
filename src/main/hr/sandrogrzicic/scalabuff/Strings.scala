package hr.sandrogrzicic.scalabuff

/**
 * Language strings.
 * @author Sandro Gržičić
 */

object Strings {
	val CANNOT_ACCESS_RESOURCE = "Error: Cannot access specified resource: "
	val INVALID_OUTPUT_DIRECTORY = "Invalid output directory: "
	val UNKNOWN_ARGUMENT = "Unknown argument: "
	val HELP = """Scala protocol buffers compiler.
Usage: scalabuff [options] protoFiles
Parse protoFiles and generate output based on the options given:
  -IPATH, --proto_path=PATH   Specify the directory in which to search for
                              imports.  May be specified multiple times;
                              directories will be searched in order.  If not
                              given, the current working directory is used.
  -h, --help                  Show this text and exit.
  --scala_out=OUTPUT_DIR      Generate Scala source files in this directory
                              (if not specified, current directory is used).
  --scala_out                 Generate Scala source files, but output all
                              results to stdout, do not write any files.
"""


}