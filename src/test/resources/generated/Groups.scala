// Generated by ScalaBuff, the Scala protocol buffer compiler. DO NOT EDIT!
// source: groups.proto

package resources.generated

final case class Groups (

) extends com.google.protobuf.GeneratedMessageLite
	with net.sandrogrzicic.scalabuff.Message[Groups] {




	def writeTo(output: com.google.protobuf.CodedOutputStream) {
	}

	lazy val getSerializedSize = {
		import com.google.protobuf.CodedOutputStream._
		var size = 0

		size
	}

	def mergeFrom(in: com.google.protobuf.CodedInputStream, extensionRegistry: com.google.protobuf.ExtensionRegistryLite): Groups = {
		import com.google.protobuf.ExtensionRegistryLite.{getEmptyRegistry => _emptyRegistry}

		def _newMerged = Groups(

		)
		while (true) in.readTag match {
			case 0 => return _newMerged
			case default => if (!in.skipField(default)) return _newMerged
		}
		null // compiler needs a return value
	}

	def mergeFrom(m: Groups) = {
		Groups(

		)
	}

	def getDefaultInstanceForType = Groups.defaultInstance
	def clear = getDefaultInstanceForType
	def isInitialized = true
	def build = this
	def buildPartial = this
	def newBuilderForType = this
	def toBuilder = this
}

object Groups {
	@reflect.BeanProperty val defaultInstance = new Groups()


}

object GroupsTest {
	def registerAllExtensions(registry: com.google.protobuf.ExtensionRegistryLite) {
	}

}