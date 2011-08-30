name := "ScalaBuff"

unmanagedBase <<= baseDirectory { base => base / "lib" }

mainClass in (Compile, run) := Some("hr.sandrogrzicic.scalabuff.compiler.ScalaBuff")

