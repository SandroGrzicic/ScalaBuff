package net.sandrogrzicic.scalabuff.compiler
import scala.util.matching.Regex

/**
 * String extension with some useful methods.
 * @author Sandro Gržičić
 */

class BuffedString(str: String) {
	import BuffedString.{camelCaseRegex, scalaReserved}

	/**
	 * CamelCases this string, with the first letter uppercased.
	 */
	def camelCase: String = lowerCamelCase.capitalize

	/**
	 * Adds backticks for reserved keywords or names with characters like spaces, symbols etc.
	 */
	def quotedIdent(name: String): String = {
		if (scalaReserved(name)) '`' + name + '`'
		else if (name.matches("[a-zA-Z_][\\w\\d_]*")) name
		else '`' + name + '`'
	}

	/**
	 * Generates a valid Scala identifier: 
	 * camelCases this string, leaving the first letter lowercased and wraps it into backticks.
	 */
	def toScalaIdent: String = quotedIdent(lowerCamelCase)
	
	/**
	 * camelCases this string, with the first letter lowercased.
	 */
	def lowerCamelCase: String = camelCaseRegex.replaceAllIn(str.replace('-', '_'), _.matched.tail.toUpperCase)

	/**
	 * Generates a valid temporary Scala identifier:
	 * camelCases this string and prefixes it with two underscores.
	 */
	def toTemporaryIdent: String = "__" + lowerCamelCase
	/**
	 * Returns the tail of this string, starting at the first character after the last occurence of the specified character.
	 */
	def dropUntilLast(c: Char): String = str.drop(str.lastIndexOf(c)+1)

	/**
	 * Returns the tail of this string, starting at the first character after the first occurence of the specified character.
	 */
	def dropUntilFirst(c: Char): String = str.drop(str.indexOf(c)+1)

	/**
	 * Returns the head of this string, until the first occurence of the specified character.
	 */
	def takeUntilFirst(c: Char): String = str.take(str.indexOf(c))

	/**
	 * Returns the head of this string, until the last occurence of the specified character.
	 */
	def takeUntilLast(c: Char): String = str.take(str.lastIndexOf(c))

	/**
	 * Returns the substring between the specified characters on the last original string positions.
	 * If any of the characters isn't found, the returned string is returned fully from the start and/or
	 * to the end of the original string.
	 * If the end position is lower than the start position, an empty string is returned.
	 */
	def betweenLast(from: Char, to: Char): String = {
		var fromPos = str.lastIndexOf(from) + 1
		var toPos = str.lastIndexOf(to, from)
		if (fromPos < 0) fromPos = 0
		if (toPos < 0) toPos = str.length
		if (fromPos > toPos) ""
		else str.substring(fromPos, toPos)
	}

  /**
   * Returns the substring between the specified characters.
   * If any of the characters isn't found, the returned string is returned fully from the start and/or
   * to the end of the original string.
   * If the end position is lower than the start position, an empty string is returned.
   */
  def between(from: Char, to: Char): String = {
    var fromPos = str.indexOf(from) + 1
    var toPos = str.lastIndexOf(to, from)
    if (fromPos < 0) fromPos = 0
    if (toPos < 0) toPos = str.length
    if (fromPos > toPos) ""
    else str.substring(fromPos, toPos)
  }

	/**
	 * Removes leading and trailing double quotes from this string, if any.
	 */
	def stripQuotes: String = str.stripPrefix("\"").stripSuffix("\"")
}

object BuffedString {
	/**
	 * Generates as much tabs as there are indent levels.
	 */
	def indent(indentLevel: Int): String = "\t" * indentLevel

	val camelCaseRegex: Regex = """_(\w)""".r

	/**
	 * Reserved scala keywords that require backticks
	 */
	val scalaReserved =
		Set("abstract", "case", "catch", "class", "def", "do", "else",
			"extends", "false", "final", "finally", "for", "forSome",
			"if", "implicit", "import", "lazy", "macro", "match",
			"new", "null", "object", "override", "package", "private",
			"protected", "return", "sealed", "super", "then", "this",
			"throw", "trait", "true", "try", "type", "val", "var",
			"while", "with", "yield")
}
