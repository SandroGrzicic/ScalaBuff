package net.sandrogrzicic.scalabuff.compiler

/**
 * Text strings.
 * @author Sandro Gržičić
 */

object Strings {
	val CANNOT_ACCESS_RESOURCE = "* Error: Cannot access specified resource: "
	val CANNOT_WRITE_FILE = "* Error: Unable to write output file: "
	val INVALID_IMPORT_DIRECTORY = "* Error: Invalid import directory: "
	val INVALID_OUTPUT_DIRECTORY = "* Error: Invalid output directory: "
	val UNSUPPORTED_INPUT_ENCODING = "* Error: Unsupported proto encoding: "
	val UNSUPPORTED_OUTPUT_ENCODING = "* Error: Unsupported output encoding: "
	val UNKNOWN_ARGUMENT = "* Error: Unknown argument: "
	val HELP = """Scala protocol buffers compiler.
Usage: scalabuff [options] protoFiles
Parse protocol buffer files and generate output based on the options given:
  -IPATH, --proto_path=PATH   Specify the directory in which to search for
                              imports.  May be specified multiple times;
                              directories will be searched in order.  If not
                              given, the current working directory is used.
  --scala_out=OUTPUT_DIR      Generate Scala source files in this directory
                              (if not specified, current directory is used).
  --stdout                    Output all results to stdout, do not write any
                              files.
  --proto_encoding=ENC        Use ENC as the encoding of the input files.
  --out_encoding=ENC          Use ENC as the encoding of the output files.
  -h, --help                  Show this help text and exit.
"""

	val UNKNOWN_INPUT = "<unknown>"


}
