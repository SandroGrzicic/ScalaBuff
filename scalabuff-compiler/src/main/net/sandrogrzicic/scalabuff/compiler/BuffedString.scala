package net.sandrogrzicic.scalabuff.compiler

/**
 * String extension with some useful methods.
 * @author Sandro Gržičić
 */

class BuffedString(str: String) {
	import BuffedString.camelCaseRegex

	/**
	 * CamelCases this string, with the first letter uppercased.
	 */
	def camelCase = lowerCamelCase.capitalize

	/**
	 * Generates a valid Scala identifier: 
	 * camelCases this string, leaving the first letter lowercased and wraps it into backticks.
	 */
	def toScalaIdent = "`" + lowerCamelCase + "`"
	
	/**
	 * camelCases this string, with the first letter lowercased.
	 */
	def lowerCamelCase = camelCaseRegex.replaceAllIn(str, _.matched.tail.toUpperCase) 

	/**
	 * Generates a valid temporary Scala identifier:
	 * camelCases this string and prefixes it with two underscores.
	 */
	def toTemporaryIdent = "__" + lowerCamelCase
	/**
	 * Returns the tail of this string, starting at the first character after the last occurence of the specified character.
	 */
	def dropUntilLast(c: Char) = str.drop(str.lastIndexOf(c)+1)

	/**
	 * Returns the tail of this string, starting at the first character after the first occurence of the specified character.
	 */
	def dropUntilFirst(c: Char) = str.drop(str.indexOf(c)+1)

	/**
	 * Returns the head of this string, until the first occurence of the specified character.
	 */
	def takeUntilFirst(c: Char) = str.take(str.indexOf(c))

	/**
	 * Returns the head of this string, until the last occurence of the specified character.
	 */
	def takeUntilLast(c: Char) = str.take(str.lastIndexOf(c))

	/**
	 * Returns the substring between the specified characters on the last original string positions.
	 * If any of the characters isn't found, the returned string is returned fully from the start and/or
	 * to the end of the original string.
	 * If the end position is lower than the start position, an empty string is returned.
	 */
	def betweenLast(from: Char, to: Char) = {
		var fromPos = str.lastIndexOf(from) + 1
		var toPos = str.lastIndexOf(to, from)
		if (fromPos < 0) fromPos = 0
		if (toPos < 0) toPos = str.length
		if (fromPos > toPos) ""
		str.substring(fromPos, toPos)
	}
	
	/**
	 * Removes leading and trailing double quotes from this string, if any.
	 */
	def stripQuotes = str.stripPrefix("\"").stripSuffix("\"")
}

object BuffedString {
	/**
	 * Generates as much tabs as there are indent levels.
	 */
	def indent(indentLevel: Int) = "\t" * indentLevel

	val camelCaseRegex = """_(\w)""".r
}
