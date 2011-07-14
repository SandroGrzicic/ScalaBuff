package hr.sandrogrzicic.scalabuff

/**
 * Language strings.
 * @author Sandro Gržičić
 */

object Strings {
	val NEED_INPUT_FILE = "Required parameter: input protobuf file name(s) or URL(s)."
	val HELP = """Scala protocol buffers compiler.
Usage: scalabuff [options] protoFiles
Parse protoFiles and generate output based on the options given:
  -IPATH, --proto_path=PATH   Specify the directory in which to search for
                              imports.  May be specified multiple times;
                              directories will be searched in order.  If not
                              given, the current working directory is used.
  -h, --help                  Show this text and exit.
  --scala_out=OUT_DIR         Generate Scala source files in this directory
                              (if not specified, current directory is used).
"""


}