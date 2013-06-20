ScalaBuff is a Scala [Protocol Buffers](https://developers.google.com/protocol-buffers/docs/overview) (protobuf) compiler. It takes .proto files and outputs valid Scala classes that can be used by your code to receive or send protobuf messages.

Both the ScalaBuff generator and the generated Scala classes depend on Google's Java runtime for Protocol Buffers, which is provided with ScalaBuff.

If you want to utilize ScalaBuff to generate your Scala classes from .proto sources, you'll need to either [download the source](https://github.com/SandroGrzicic/ScalaBuff/archive/master.zip) or download the packaged JAR for your Scala version from the Sonatype OSS repository. If you download the sources, you can easily run it from SBT.

If you just want to use ScalaBuff-generated classes in your SBT-managed project, here's the dependency to add (located on the Sonatype OSS repository): `"net.sandrogrzicic" %% "scalabuff-runtime" % "[desired_version]"`
The latest release is **1.3.0** with support for Scala 2.9.3, 2.10 and 2.11.0-M3.

If you'd like to use SBT with ScalaBuff to auto-generate Scala protobuf classes from .proto sources, try the [sbt-scalabuff project](https://github.com/sbt/sbt-scalabuff).

The [ScalaBuff Wiki](https://github.com/SandroGrzicic/ScalaBuff/wiki) contains more information. For API documentation, see the project [Scaladoc](http://sandrogrzicic.github.com/ScalaBuff/doc/).

For any questions or general discussion, you can use the [ScalaBuff Google Group](https://groups.google.com/forum/#!forum/scalabuff) but please feel free to [create new issues](https://github.com/SandroGrzicic/ScalaBuff/issues/new) for bug reports or feature requests. Thanks!


