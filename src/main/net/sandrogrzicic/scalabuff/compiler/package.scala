package net.sandrogrzicic.scalabuff

/**
 * Useful things for this package.
 */
package object compiler {

	implicit def buffString(string: String): BuffedString = new BuffedString(string)

}


