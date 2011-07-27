package hr.sandrogrzicic.scalabuff

/**
 * Exceptions generated during .proto processing.
 * @author Sandro Gržičić
 */

/**
 * Thrown when an input .proto file cannot be parsed successfully.
 */
class ParsingFailureException(message: String) extends RuntimeException(message)