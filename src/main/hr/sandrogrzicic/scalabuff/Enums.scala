package hr.sandrogrzicic.scalabuff

import hr.sandrogrzicic.scalabuff.FieldLabels.InvalidFieldLabelException

/**
 * Viktor Klang's Enum
 * Source: https://gist.github.com/1057513/
 */
trait Enum {

	import java.util.concurrent.atomic.AtomicReference

	type EnumVal <: Value

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

	protected trait Value {
		self: EnumVal => // Enforce that no one mixes in Value in a non-EnumVal type
		final val ordinal = addEnumVal(this) // Adds the EnumVal and returns the ordinal

		def name: String

		override def toString = name
		override def equals(other: Any) = this eq other.asInstanceOf[AnyRef]
		override def hashCode = 31 * (this.getClass.## + name.## + ordinal)
	}

}

/**
 * Field labels.
 */
object FieldLabels extends Enum {
	sealed trait EnumVal extends Value

	val REQUIRED = new EnumVal { val name = "required" }
	val OPTIONAL = new EnumVal { val name = "optional" }
	val REPEATED = new EnumVal { val name = "repeated" }

	def apply(label: String) = values.find(label == _.name).getOrElse(
		throw new InvalidFieldLabelException(label)
	)

	class InvalidFieldLabelException(label: String) extends RuntimeException(
		"Invalid field label: " + label
	)
}

/**
 * Field types, along with a conversion method.
 */
object FieldTypes extends Enum {
	implicit def buffString(string: String): BuffedString = new BuffedString(string)

	case class EnumVal private[FieldTypes](name: String, scalaType: String, defaultValue: String) extends Value {
		override def toString = scalaType
	}

	val INT32 = EnumVal("int32", "Int", "0")
	val UINT32 = EnumVal("uint32", "Int", "0")
	val SINT32 = EnumVal("sint32", "Int", "0")
	val FIXED32 = EnumVal("fixed32", "Int", "0")
	val SFIXED32 = EnumVal("sfixed32", "Int", "0")
	val INT64 = EnumVal("int64", "Long", "0L")
	val UINT64 = EnumVal("uint64", "Long", "0L")
	val SINT64 = EnumVal("sint64", "Long", "0L")
	val FIXED64 = EnumVal("fixed64", "Long", "0L")
	val SFIXED64 = EnumVal("sfixed64", "Long", "0L")
	val BOOL = EnumVal("bool", "Boolean", "false")
	val FLOAT = EnumVal("float", "Float", "0.0f")
	val DOUBLE = EnumVal("double", "Double", "0.0")
	val BYTES = EnumVal("bytes", "com.google.protobuf.ByteString", "com.google.protobuf.ByteString.EMPTY")
	val STRING = EnumVal("string", "String", "\"\"")

	/**
	 * Returns a FieldType.EnumVal based on the specified proto field type,
	 * or a new EnumVal with a None default value if it's a user type.
	 */
	def apply(fieldType: String) = values.find(fieldType == _.name).getOrElse(EnumVal(fieldType, fieldType, "None"))
}
