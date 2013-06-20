package net.sandrogrzicic.scalabuff

import com.google.protobuf._

/**
 * Message trait for messages generated with ScalaBuff.
 * Ordinarily Messages would have GeneratedMessageLite.Builder mixed in, but since it's a Java class, we can't do that.
 * Contains methods implementing the MessageLite.Builder Java interface, similar to ones in GeneratedMessageLite.Builder.
 *
 * @author Sandro Gržičić
 */
trait Message[MessageType <: MessageLite with MessageLite.Builder] 
  extends MessageLite.Builder with MessageBuilder[MessageType] {


}

