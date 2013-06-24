package net.sandrogrzicic.scalabuff

/**
 * Viktor Klang's Enum, modified for ScalaBuff for protobuf usage
 * Source: https://gist.github.com/1057513/
 */
trait Enum {

	import java.util.concurrent.atomic.AtomicReference

	type EnumVal <: Value

	implicit def _enumToInt(_e: EnumVal) = _e.id

	private val _values = new AtomicReference(Vector[EnumVal]())

	/**
	 * Add an EnumVal to our storage, using CCAS to make sure it's thread safe, returns the ordinal.
	 */
	private final def addEnumVal(newVal: EnumVal): Int = {
		import _values.{get, compareAndSet => CAS}
		val oldVec = get
		val newVec = oldVec :+ newVal
		if ((get eq oldVec) && CAS(oldVec, newVec)) newVec.indexWhere(_ eq newVal) else addEnumVal(newVal)
	}

	/**
	 * Get all the enums that exist for this type.
	 */
	def values: Vector[EnumVal] = _values.get

	protected trait Value extends com.google.protobuf.Internal.EnumLite {
		self: EnumVal => // Enforce that no one mixes in Value in a non-EnumVal type
		final val ordinal = addEnumVal(this) // Adds the EnumVal and returns the ordinal

		// proto enum value
		val id: Int
		// proto enum name
		val name: String

		def getNumber = id

		override def toString = name
		/**
		 * Enum Values with identical values are equal.
		 */
		override def equals(other: Any) = other.isInstanceOf[Value] && this.id == other.asInstanceOf[Value].id
		/**
		 * Enum Values with identical values return the same hashCode.
		 */
		override def hashCode = 31 * (this.getClass.## + name.## + id)
	}

}


/**
 * Thrown when an unknown enum number is passed to the valueOf method of an Enum.
 */
class UnknownEnumException(enumID: Int) extends RuntimeException("Unknown enum ID: " + enumID)

/**
 * Thrown when a required field with enum type is uninitialized on access attempt.
 */
class UninitializedEnumException[T](name: String) extends RuntimeException("Enum not initialized: " + name)
