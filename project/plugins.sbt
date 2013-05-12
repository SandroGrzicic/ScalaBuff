resolvers ++= Seq(
  "mDialog Snapshots" at "http://artifactory.mdialog.com/artifactory/snapshots",
  "mDialog Releases" at "http://artifactory.mdialog.com/artifactory/releases"
)

credentials += Credentials(Path.userHome / ".mdialog.credentials")

addSbtPlugin("com.mdialog" % "sbt_bundle_plugin" % "3.6.0-SNAPSHOT")

addSbtPlugin("com.eed3si9n" % "sbt-buildinfo" % "0.2.2")
