// Generated by ScalaBuff, the Scala Protocol Buffers compiler. DO NOT EDIT!
// source: dh_complex.proto

package resources.generated

final case class Response (
	`response`: collection.immutable.Seq[Response.VideoResult] = Vector.empty[Response.VideoResult]
) extends com.google.protobuf.GeneratedMessageLite
	with com.google.protobuf.MessageLite.Builder
	with net.sandrogrzicic.scalabuff.Message[Response] {

	def setResponse(_i: Int, _v: Response.VideoResult) = copy(`response` = `response`.updated(_i, _v))
	def addResponse(_f: Response.VideoResult) = copy(`response` = `response` :+ _f)
	def addAllResponse(_f: Response.VideoResult*) = copy(`response` = `response` ++ _f)
	def addAllResponse(_f: TraversableOnce[Response.VideoResult]) = copy(`response` = `response` ++ _f)

	def clearResponse = copy(`response` = Vector.empty[Response.VideoResult])

	def writeTo(output: com.google.protobuf.CodedOutputStream) {
		for (_v <- `response`) output.writeMessage(1, _v)
	}

	lazy val getSerializedSize = {
		import com.google.protobuf.CodedOutputStream._
		var size = 0
		for (_v <- `response`) size += computeMessageSize(1, _v)

		size
	}

	def mergeFrom(in: com.google.protobuf.CodedInputStream, extensionRegistry: com.google.protobuf.ExtensionRegistryLite): Response = {
		import com.google.protobuf.ExtensionRegistryLite.{getEmptyRegistry => _emptyRegistry}
		val __response: collection.mutable.Buffer[Response.VideoResult] = `response`.toBuffer

		def __newMerged = Response(
			Vector(__response: _*)
		)
		while (true) in.readTag match {
			case 0 => return __newMerged
			case 10 => __response += readMessage[Response.VideoResult](in, Response.VideoResult.defaultInstance, _emptyRegistry)
			case default => if (!in.skipField(default)) return __newMerged
		}
		null
	}

	def mergeFrom(m: Response) = {
		Response(
			`response` ++ m.`response`
		)
	}

	def getDefaultInstanceForType = Response.defaultInstance
	def clear = getDefaultInstanceForType
	def isInitialized = true
	def build = this
	def buildPartial = this
	def newBuilderForType = getDefaultInstanceForType
	def toBuilder = this
}

object Response {
	@reflect.BeanProperty val defaultInstance = new Response()

	val RESPONSE_FIELD_NUMBER = 1

	def apply(message: Array[Byte]): Response = defaultInstance.mergeFrom(message)
	def apply(message: com.google.protobuf.ByteString): Response = defaultInstance.mergeFrom(message)

	def newBuilder = defaultInstance.newBuilderForType
	def newBuilder(prototype: Response) = defaultInstance.mergeFrom(prototype)

	final case class Rendition (
		`profileKey`: Option[String] = None,
		`data`: Option[String] = None,
		`property`: collection.immutable.Seq[Rendition.Property] = Vector.empty[Rendition.Property]
	) extends com.google.protobuf.GeneratedMessageLite
		with com.google.protobuf.MessageLite.Builder
		with net.sandrogrzicic.scalabuff.Message[Rendition] {

		def setProfileKey(_f: String) = copy(`profileKey` = _f)
		def setData(_f: String) = copy(`data` = _f)
		def setProperty(_i: Int, _v: Rendition.Property) = copy(`property` = `property`.updated(_i, _v))
		def addProperty(_f: Rendition.Property) = copy(`property` = `property` :+ _f)
		def addAllProperty(_f: Rendition.Property*) = copy(`property` = `property` ++ _f)
		def addAllProperty(_f: TraversableOnce[Rendition.Property]) = copy(`property` = `property` ++ _f)

		def clearProfileKey = copy(`profileKey` = None)
		def clearData = copy(`data` = None)
		def clearProperty = copy(`property` = Vector.empty[Rendition.Property])

		def writeTo(output: com.google.protobuf.CodedOutputStream) {
			if (`profileKey`.isDefined) output.writeString(1, `profileKey`.get)
			if (`data`.isDefined) output.writeString(2, `data`.get)
			for (_v <- `property`) output.writeMessage(3, _v)
		}

		lazy val getSerializedSize = {
			import com.google.protobuf.CodedOutputStream._
			var size = 0
			if (`profileKey`.isDefined) size += computeStringSize(1, `profileKey`.get)
			if (`data`.isDefined) size += computeStringSize(2, `data`.get)
			for (_v <- `property`) size += computeMessageSize(3, _v)

			size
		}

		def mergeFrom(in: com.google.protobuf.CodedInputStream, extensionRegistry: com.google.protobuf.ExtensionRegistryLite): Rendition = {
			import com.google.protobuf.ExtensionRegistryLite.{getEmptyRegistry => _emptyRegistry}
			var __profileKey: Option[String] = `profileKey`
			var __data: Option[String] = `data`
			val __property: collection.mutable.Buffer[Rendition.Property] = `property`.toBuffer

			def __newMerged = Rendition(
				__profileKey,
				__data,
				Vector(__property: _*)
			)
			while (true) in.readTag match {
				case 0 => return __newMerged
				case 10 => __profileKey = in.readString()
				case 18 => __data = in.readString()
				case 26 => __property += readMessage[Rendition.Property](in, Rendition.Property.defaultInstance, _emptyRegistry)
				case default => if (!in.skipField(default)) return __newMerged
			}
			null
		}

		def mergeFrom(m: Rendition) = {
			Rendition(
				m.`profileKey`.orElse(`profileKey`),
				m.`data`.orElse(`data`),
				`property` ++ m.`property`
			)
		}

		def getDefaultInstanceForType = Rendition.defaultInstance
		def clear = getDefaultInstanceForType
		def isInitialized = true
		def build = this
		def buildPartial = this
		def newBuilderForType = getDefaultInstanceForType
		def toBuilder = this
	}

	object Rendition {
		@reflect.BeanProperty val defaultInstance = new Rendition()

		val PROFILE_KEY_FIELD_NUMBER = 1
		val DATA_FIELD_NUMBER = 2
		val PROPERTY_FIELD_NUMBER = 3

		def apply(message: Array[Byte]): Rendition = defaultInstance.mergeFrom(message)
		def apply(message: com.google.protobuf.ByteString): Rendition = defaultInstance.mergeFrom(message)

		def newBuilder = defaultInstance.newBuilderForType
		def newBuilder(prototype: Rendition) = defaultInstance.mergeFrom(prototype)

		final case class Property (
			`key`: Property.Key.EnumVal = Property.Key._UNINITIALIZED,
			`value`: String = ""
		) extends com.google.protobuf.GeneratedMessageLite
			with com.google.protobuf.MessageLite.Builder
			with net.sandrogrzicic.scalabuff.Message[Property] {



			def writeTo(output: com.google.protobuf.CodedOutputStream) {
				output.writeEnum(1, `key`)
				output.writeString(2, `value`)
			}

			lazy val getSerializedSize = {
				import com.google.protobuf.CodedOutputStream._
				var size = 0
				size += computeEnumSize(1, `key`)
				size += computeStringSize(2, `value`)

				size
			}

			def mergeFrom(in: com.google.protobuf.CodedInputStream, extensionRegistry: com.google.protobuf.ExtensionRegistryLite): Property = {
				import com.google.protobuf.ExtensionRegistryLite.{getEmptyRegistry => _emptyRegistry}
				var __key: Property.Key.EnumVal = Property.Key._UNINITIALIZED
				var __value: String = ""

				def __newMerged = Property(
					__key,
					__value
				)
				while (true) in.readTag match {
					case 0 => return __newMerged
					case 8 => __key = Property.Key.valueOf(in.readEnum())
					case 18 => __value = in.readString()
					case default => if (!in.skipField(default)) return __newMerged
				}
				null
			}

			def mergeFrom(m: Property) = {
				Property(
					m.`key`,
					m.`value`
				)
			}

			def getDefaultInstanceForType = Property.defaultInstance
			def clear = getDefaultInstanceForType
			def isInitialized = true
			def build = this
			def buildPartial = this
			def newBuilderForType = getDefaultInstanceForType
			def toBuilder = this
		}

		object Property {
			@reflect.BeanProperty val defaultInstance = new Property()

			val KEY_FIELD_NUMBER = 1
			val VALUE_FIELD_NUMBER = 2

			def apply(message: Array[Byte]): Property = defaultInstance.mergeFrom(message)
			def apply(message: com.google.protobuf.ByteString): Property = defaultInstance.mergeFrom(message)

			def newBuilder = defaultInstance.newBuilderForType
			def newBuilder(prototype: Property) = defaultInstance.mergeFrom(prototype)

			object Key extends net.sandrogrzicic.scalabuff.Enum {
				sealed trait EnumVal extends Value
				val _UNINITIALIZED = new EnumVal { val name = "UNINITIALIZED ENUM VALUE"; val id = -1 }

				val UNDEFINED = new EnumVal { val name = "UNDEFINED"; val id = 0 }
				val BANDWIDTH = new EnumVal { val name = "BANDWIDTH"; val id = 1 }
				val RESOLUTION = new EnumVal { val name = "RESOLUTION"; val id = 2 }
				val CODECS = new EnumVal { val name = "CODECS"; val id = 3 }

				val UNDEFINED_VALUE = 0
				val BANDWIDTH_VALUE = 1
				val RESOLUTION_VALUE = 2
				val CODECS_VALUE = 3

				def valueOf(id: Int) = id match {
					case 0 => UNDEFINED
					case 1 => BANDWIDTH
					case 2 => RESOLUTION
					case 3 => CODECS
					case _default => throw new net.sandrogrzicic.scalabuff.UnknownEnumException(_default)
				}
				val internalGetValueMap = new com.google.protobuf.Internal.EnumLiteMap[EnumVal] {
					def findValueByNumber(id: Int): EnumVal = valueOf(id)
				}
			}

		}
	}
	final case class Video (
		`identifier`: Option[String] = None,
		`assetKey`: Option[String] = None,
		`duration`: Option[Float] = None,
		`renditions`: collection.immutable.Seq[Rendition] = Vector.empty[Rendition]
	) extends com.google.protobuf.GeneratedMessageLite
		with com.google.protobuf.MessageLite.Builder
		with net.sandrogrzicic.scalabuff.Message[Video] {

		def setIdentifier(_f: String) = copy(`identifier` = _f)
		def setAssetKey(_f: String) = copy(`assetKey` = _f)
		def setDuration(_f: Float) = copy(`duration` = _f)
		def setRenditions(_i: Int, _v: Rendition) = copy(`renditions` = `renditions`.updated(_i, _v))
		def addRenditions(_f: Rendition) = copy(`renditions` = `renditions` :+ _f)
		def addAllRenditions(_f: Rendition*) = copy(`renditions` = `renditions` ++ _f)
		def addAllRenditions(_f: TraversableOnce[Rendition]) = copy(`renditions` = `renditions` ++ _f)

		def clearIdentifier = copy(`identifier` = None)
		def clearAssetKey = copy(`assetKey` = None)
		def clearDuration = copy(`duration` = None)
		def clearRenditions = copy(`renditions` = Vector.empty[Rendition])

		def writeTo(output: com.google.protobuf.CodedOutputStream) {
			if (`identifier`.isDefined) output.writeString(1, `identifier`.get)
			if (`assetKey`.isDefined) output.writeString(2, `assetKey`.get)
			if (`duration`.isDefined) output.writeFloat(3, `duration`.get)
			for (_v <- `renditions`) output.writeMessage(4, _v)
		}

		lazy val getSerializedSize = {
			import com.google.protobuf.CodedOutputStream._
			var size = 0
			if (`identifier`.isDefined) size += computeStringSize(1, `identifier`.get)
			if (`assetKey`.isDefined) size += computeStringSize(2, `assetKey`.get)
			if (`duration`.isDefined) size += computeFloatSize(3, `duration`.get)
			for (_v <- `renditions`) size += computeMessageSize(4, _v)

			size
		}

		def mergeFrom(in: com.google.protobuf.CodedInputStream, extensionRegistry: com.google.protobuf.ExtensionRegistryLite): Video = {
			import com.google.protobuf.ExtensionRegistryLite.{getEmptyRegistry => _emptyRegistry}
			var __identifier: Option[String] = `identifier`
			var __assetKey: Option[String] = `assetKey`
			var __duration: Option[Float] = `duration`
			val __renditions: collection.mutable.Buffer[Rendition] = `renditions`.toBuffer

			def __newMerged = Video(
				__identifier,
				__assetKey,
				__duration,
				Vector(__renditions: _*)
			)
			while (true) in.readTag match {
				case 0 => return __newMerged
				case 10 => __identifier = in.readString()
				case 18 => __assetKey = in.readString()
				case 29 => __duration = in.readFloat()
				case 34 => __renditions += readMessage[Rendition](in, Rendition.defaultInstance, _emptyRegistry)
				case default => if (!in.skipField(default)) return __newMerged
			}
			null
		}

		def mergeFrom(m: Video) = {
			Video(
				m.`identifier`.orElse(`identifier`),
				m.`assetKey`.orElse(`assetKey`),
				m.`duration`.orElse(`duration`),
				`renditions` ++ m.`renditions`
			)
		}

		def getDefaultInstanceForType = Video.defaultInstance
		def clear = getDefaultInstanceForType
		def isInitialized = true
		def build = this
		def buildPartial = this
		def newBuilderForType = getDefaultInstanceForType
		def toBuilder = this
	}

	object Video {
		@reflect.BeanProperty val defaultInstance = new Video()

		val IDENTIFIER_FIELD_NUMBER = 1
		val ASSET_KEY_FIELD_NUMBER = 2
		val DURATION_FIELD_NUMBER = 3
		val RENDITIONS_FIELD_NUMBER = 4

		def apply(message: Array[Byte]): Video = defaultInstance.mergeFrom(message)
		def apply(message: com.google.protobuf.ByteString): Video = defaultInstance.mergeFrom(message)

		def newBuilder = defaultInstance.newBuilderForType
		def newBuilder(prototype: Video) = defaultInstance.mergeFrom(prototype)

	}
	final case class VideoFailure (
		`assetKey`: Option[String] = None,
		`reason`: collection.immutable.Seq[String] = Vector.empty[String],
		`cause`: Option[VideoFailure.Cause.EnumVal] = None
	) extends com.google.protobuf.GeneratedMessageLite
		with com.google.protobuf.MessageLite.Builder
		with net.sandrogrzicic.scalabuff.Message[VideoFailure] {

		def setAssetKey(_f: String) = copy(`assetKey` = _f)
		def setReason(_i: Int, _v: String) = copy(`reason` = `reason`.updated(_i, _v))
		def addReason(_f: String) = copy(`reason` = `reason` :+ _f)
		def addAllReason(_f: String*) = copy(`reason` = `reason` ++ _f)
		def addAllReason(_f: TraversableOnce[String]) = copy(`reason` = `reason` ++ _f)
		def setCause(_f: VideoFailure.Cause.EnumVal) = copy(`cause` = _f)

		def clearAssetKey = copy(`assetKey` = None)
		def clearReason = copy(`reason` = Vector.empty[String])
		def clearCause = copy(`cause` = None)

		def writeTo(output: com.google.protobuf.CodedOutputStream) {
			if (`assetKey`.isDefined) output.writeString(1, `assetKey`.get)
			for (_v <- `reason`) output.writeString(2, _v)
			if (`cause`.isDefined) output.writeEnum(3, `cause`.get)
		}

		lazy val getSerializedSize = {
			import com.google.protobuf.CodedOutputStream._
			var size = 0
			if (`assetKey`.isDefined) size += computeStringSize(1, `assetKey`.get)
			for (_v <- `reason`) size += computeStringSize(2, _v)
			if (`cause`.isDefined) size += computeEnumSize(3, `cause`.get)

			size
		}

		def mergeFrom(in: com.google.protobuf.CodedInputStream, extensionRegistry: com.google.protobuf.ExtensionRegistryLite): VideoFailure = {
			import com.google.protobuf.ExtensionRegistryLite.{getEmptyRegistry => _emptyRegistry}
			var __assetKey: Option[String] = `assetKey`
			val __reason: collection.mutable.Buffer[String] = `reason`.toBuffer
			var __cause: Option[VideoFailure.Cause.EnumVal] = `cause`

			def __newMerged = VideoFailure(
				__assetKey,
				Vector(__reason: _*),
				__cause
			)
			while (true) in.readTag match {
				case 0 => return __newMerged
				case 10 => __assetKey = in.readString()
				case 18 => __reason += in.readString()
				case 24 => __cause = VideoFailure.Cause.valueOf(in.readEnum())
				case default => if (!in.skipField(default)) return __newMerged
			}
			null
		}

		def mergeFrom(m: VideoFailure) = {
			VideoFailure(
				m.`assetKey`.orElse(`assetKey`),
				`reason` ++ m.`reason`,
				m.`cause`.orElse(`cause`)
			)
		}

		def getDefaultInstanceForType = VideoFailure.defaultInstance
		def clear = getDefaultInstanceForType
		def isInitialized = true
		def build = this
		def buildPartial = this
		def newBuilderForType = getDefaultInstanceForType
		def toBuilder = this
	}

	object VideoFailure {
		@reflect.BeanProperty val defaultInstance = new VideoFailure()

		val ASSET_KEY_FIELD_NUMBER = 1
		val REASON_FIELD_NUMBER = 2
		val CAUSE_FIELD_NUMBER = 3

		def apply(message: Array[Byte]): VideoFailure = defaultInstance.mergeFrom(message)
		def apply(message: com.google.protobuf.ByteString): VideoFailure = defaultInstance.mergeFrom(message)

		def newBuilder = defaultInstance.newBuilderForType
		def newBuilder(prototype: VideoFailure) = defaultInstance.mergeFrom(prototype)

		object Cause extends net.sandrogrzicic.scalabuff.Enum {
			sealed trait EnumVal extends Value
			val _UNINITIALIZED = new EnumVal { val name = "UNINITIALIZED ENUM VALUE"; val id = -1 }

			val UNEXPECTED = new EnumVal { val name = "UNEXPECTED"; val id = 1 }
			val ASSET_NOT_FOUND = new EnumVal { val name = "ASSET_NOT_FOUND"; val id = 2 }
			val UNKNOWN_ACCOUNT = new EnumVal { val name = "UNKNOWN_ACCOUNT"; val id = 3 }
			val TIMEOUT = new EnumVal { val name = "TIMEOUT"; val id = 4 }
			val DATABASE_ERROR = new EnumVal { val name = "DATABASE_ERROR"; val id = 5 }

			val UNEXPECTED_VALUE = 1
			val ASSET_NOT_FOUND_VALUE = 2
			val UNKNOWN_ACCOUNT_VALUE = 3
			val TIMEOUT_VALUE = 4
			val DATABASE_ERROR_VALUE = 5

			def valueOf(id: Int) = id match {
				case 1 => UNEXPECTED
				case 2 => ASSET_NOT_FOUND
				case 3 => UNKNOWN_ACCOUNT
				case 4 => TIMEOUT
				case 5 => DATABASE_ERROR
				case _default => throw new net.sandrogrzicic.scalabuff.UnknownEnumException(_default)
			}
			val internalGetValueMap = new com.google.protobuf.Internal.EnumLiteMap[EnumVal] {
				def findValueByNumber(id: Int): EnumVal = valueOf(id)
			}
		}

	}
	final case class VideoResult (
		`success`: Option[Video] = None,
		`failure`: Option[VideoFailure] = None
	) extends com.google.protobuf.GeneratedMessageLite
		with com.google.protobuf.MessageLite.Builder
		with net.sandrogrzicic.scalabuff.Message[VideoResult] {

		def setSuccess(_f: Video) = copy(`success` = _f)
		def setFailure(_f: VideoFailure) = copy(`failure` = _f)

		def clearSuccess = copy(`success` = None)
		def clearFailure = copy(`failure` = None)

		def writeTo(output: com.google.protobuf.CodedOutputStream) {
			if (`success`.isDefined) output.writeMessage(1, `success`.get)
			if (`failure`.isDefined) output.writeMessage(2, `failure`.get)
		}

		lazy val getSerializedSize = {
			import com.google.protobuf.CodedOutputStream._
			var size = 0
			if (`success`.isDefined) size += computeMessageSize(1, `success`.get)
			if (`failure`.isDefined) size += computeMessageSize(2, `failure`.get)

			size
		}

		def mergeFrom(in: com.google.protobuf.CodedInputStream, extensionRegistry: com.google.protobuf.ExtensionRegistryLite): VideoResult = {
			import com.google.protobuf.ExtensionRegistryLite.{getEmptyRegistry => _emptyRegistry}
			var __success: Option[Video] = `success`
			var __failure: Option[VideoFailure] = `failure`

			def __newMerged = VideoResult(
				__success,
				__failure
			)
			while (true) in.readTag match {
				case 0 => return __newMerged
				case 10 => __success = readMessage[Video](in, __success.orElse({
					__success = Video.defaultInstance
					__success
				}).get, _emptyRegistry)
				case 18 => __failure = readMessage[VideoFailure](in, __failure.orElse({
					__failure = VideoFailure.defaultInstance
					__failure
				}).get, _emptyRegistry)
				case default => if (!in.skipField(default)) return __newMerged
			}
			null
		}

		def mergeFrom(m: VideoResult) = {
			VideoResult(
				m.`success`.orElse(`success`),
				m.`failure`.orElse(`failure`)
			)
		}

		def getDefaultInstanceForType = VideoResult.defaultInstance
		def clear = getDefaultInstanceForType
		def isInitialized = true
		def build = this
		def buildPartial = this
		def newBuilderForType = getDefaultInstanceForType
		def toBuilder = this
	}

	object VideoResult {
		@reflect.BeanProperty val defaultInstance = new VideoResult()

		val SUCCESS_FIELD_NUMBER = 1
		val FAILURE_FIELD_NUMBER = 2

		def apply(message: Array[Byte]): VideoResult = defaultInstance.mergeFrom(message)
		def apply(message: com.google.protobuf.ByteString): VideoResult = defaultInstance.mergeFrom(message)

		def newBuilder = defaultInstance.newBuilderForType
		def newBuilder(prototype: VideoResult) = defaultInstance.mergeFrom(prototype)

	}
}

object DhComplex {
	def registerAllExtensions(registry: com.google.protobuf.ExtensionRegistryLite) {
	}

}
