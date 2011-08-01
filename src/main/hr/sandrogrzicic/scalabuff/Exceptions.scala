package hr.sandrogrzicic.scalabuff

/**
 * Exceptions generated during .proto processing.
 * @author Sandro Gržičić
 */

/**
 * Thrown when an input .proto file cannot be tree successfully.
 */
class ParsingFailureException(message: String) extends RuntimeException(message)