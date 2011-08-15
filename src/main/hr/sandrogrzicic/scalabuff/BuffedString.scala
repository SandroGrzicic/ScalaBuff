package hr.sandrogrzicic.scalabuff

/**
 * String extension with some useful methods.
 * @author Sandro Gržičić
 */

class BuffedString(str: String) {
	protected val camelCaseRegex = """_(\w)""".r
	/**
	 * CamelCases the string.
	 */
	def camelCase = {
		camelCaseRegex.replaceAllIn(str, m => m.matched.tail.toUpperCase).capitalize
	}

	/**
	 * Returns the tail of the string, starting at the first character after the last occurence of the specified character.
	 */
	def dropUntilLast(c: Char) = str.drop(str.lastIndexOf(c)+1)

	/**
	 * Returns the tail of the string, starting at the first character after the first occurence of the specified character.
	 */
	def dropUntilFirst(c: Char) = str.drop(str.indexOf(c)+1)

	/**
	 * Returns the head of the string, until the first occurence of the specified character.
	 */
	def takeUntilFirst(c: Char) = str.take(str.indexOf(c))

	/**
	 * Returns the head of the string, until the last occurence of the specified character.
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
		if (fromPos < 1) fromPos = 0
		if (toPos < 0) toPos = str.length
		if (fromPos > toPos) ""
		str.substring(fromPos, toPos)
	}
}
