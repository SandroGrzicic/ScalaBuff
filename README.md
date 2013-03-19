ScalaBuff is a Scala Protocol Buffers (protobuf) compiler. It takes .proto files and outputs valid Scala classes that can be used by your code to receive or send protobuf messages.

Both the ScalaBuff generator and the generated Scala classes depend on Google's Java runtime for Protocol Buffers, which is provided with ScalaBuff.

The [ScalaBuff Wiki](https://github.com/SandroGrzicic/ScalaBuff/wiki) contains more information. For API documentation, see the project [Scaladoc](http://sandrogrzicic.github.com/ScalaBuff/doc/).

If you'd like to use SBT with ScalaBuff to auto-generate Scala protobuf classes from .proto sources, try the [sbt-scalabuff project](https://github.com/sbt/sbt-scalabuff).
