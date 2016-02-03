name := "Cyan"

version := "1.0"

lazy val `cyan` = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.7"

libraryDependencies ++= Seq( jdbc , cache , ws, evolutions, specs2 % Test )

libraryDependencies ++= Seq(
  "com.typesafe.play" %% "anorm" % "2.4.0"
)

libraryDependencies ++= Seq(
  "org.webjars" %% "webjars-play" % "2.4.0-1",
  "org.webjars" % "jquery" % "2.2.0",
  "org.webjars" % "bootstrap" % "3.3.6",
  "org.webjars" % "jasny-bootstrap" % "3.1.3-2"
)

unmanagedResourceDirectories in Test <+=  baseDirectory ( _ /"target/web/public/test" )  

resolvers += "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases"  