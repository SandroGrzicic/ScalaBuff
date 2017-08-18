publishMavenStyle := true

licenses := Seq("Apache" -> url("http://www.apache.org/licenses/LICENSE-2.0"))
homepage := Some(url("https://github.com/SandroGrzicic/ScalaBuff/"))
scmInfo := Some(
	ScmInfo(
		url("https://github.com/SandroGrzicic/ScalaBuff"),
		"scm:git@github.com:SandroGrzicic/ScalaBuff.git"
	)
)
developers := List(
  Developer(id = "sandrogrzicic", name = "Sandro Grzicic", email = "scalabuff@sandro.me.uk", url = url("https://github.com/SandroGrzicic/"))
)

publishTo := {
  val nexus = "https://oss.sonatype.org/"
  if (isSnapshot.value)
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases"  at nexus + "service/local/staging/deploy/maven2")
}

publishArtifact in Test := false
