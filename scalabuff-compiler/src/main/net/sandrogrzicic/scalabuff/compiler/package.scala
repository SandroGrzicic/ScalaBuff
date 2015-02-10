package net.sandrogrzicic.scalabuff

import scala.language.implicitConversions

/**
 * Useful things for this package.
 */
package object compiler {

	implicit def buffString(string: String): BuffedString = new BuffedString(string)

}


