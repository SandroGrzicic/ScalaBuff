package net.sandrogrzicic.scalabuff

import com.google.protobuf._

/**
 * Message trait for extendable messages generated with ScalaBuff.
 * Ordinarily Messages would have GeneratedMessageLite.Builder mixed in, but since it's a Java class, we can't do that.
 * Contains methods implementing the MessageLite.Builder Java interface, similar to ones in GeneratedMessageLite.Builder.
 *
 * @todo WORK IN PROGRESS
 * @author Sandro Gržičić
 */
trait ExtendableMessage[
  MessageType <: GeneratedMessageLite.ExtendableMessage[MessageType]] extends MessageBuilder[MessageType]
//    extends GeneratedMessageLite.ExtendableBuilder[MessageType, MessageType] {
{



}

