name := "Cyan"

version := "1.0"

lazy val `backend-core` = project in file("backend-core")

lazy val `cyan` = (project in file("."))
  .enablePlugins(PlayScala)
  .dependsOn(`backend-core`)

scalaVersion := "2.11.7"

libraryDependencies ++= Seq( cache , ws, evolutions, specs2 % Test )

libraryDependencies ++= Seq(
  "com.typesafe.play" %% "play-slick" % "1.1.1",
  "com.typesafe.play" %% "play-slick-evolutions" % "1.1.1",
  "com.h2database" % "h2" % "1.4.191", // for development
  "org.postgresql" % "postgresql" % "9.4-1206-jdbc4", // for prod
  "com.google.inject.extensions" % "guice-multibindings" % "4.0",
  "org.scalatestplus" % "play_2.11" % "1.4.0" % "test"
)

libraryDependencies ++= Seq(
  "org.webjars" %% "webjars-play" % "2.4.0-1",
  "org.webjars" % "jquery" % "2.2.0",
  "org.webjars" % "bootstrap" % "3.3.6",
  "org.webjars" % "jasny-bootstrap" % "3.1.3-2"
)

routesGenerator := InjectedRoutesGenerator

unmanagedResourceDirectories in Test <+=  baseDirectory ( _ /"target/web/public/test" )  

resolvers += "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases"  