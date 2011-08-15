package hr.sandrogrzicic.scalabuff.runtime

/**
 * Viktor Klang's Enum, modified for ScalaBuff for protobuf usage
 */
trait Enum {

	import java.util.concurrent.atomic.AtomicReference

	type EnumVal <: Value //This is a type that needs to be found in the implementing class

	private val _values = new AtomicReference(Vector[EnumVal]()) //Stores our enum values

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


	//This is the trait that we need to extend our EnumVal type with, it does the book-keeping for us
	protected trait Value extends com.google.protobuf.Internal.EnumLite {
		self: EnumVal => // Enforce that no one mixes in Value in a non-EnumVal type
		final val ordinal = addEnumVal(this) // Adds the EnumVal and returns the ordinal

		// proto enum name
		def name: String
		// proto enum value
		def id: Int
		val getNumber = id

		override def toString = name
		override def equals(other: Any) = this eq other.asInstanceOf[AnyRef]

		override def hashCode = 31 * (this.getClass.## + name.## + ordinal)
	}

}