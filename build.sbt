name := "Cyan"

version := "1.0"

lazy val `backend-core` = project in file("backend-core")

lazy val `cyan` = (project in file("."))
  .enablePlugins(PlayScala)
  .dependsOn(`backend-core`)

scalaVersion := "2.11.7"

libraryDependencies ++= Seq( cache , ws, evolutions, specs2 % Test, guice )

libraryDependencies ++= Seq(
  "com.typesafe.play" %% "play-slick" % "3.0.0",
  "com.typesafe.play" %% "play-slick-evolutions" % "3.0.0",
  "com.h2database" % "h2" % "1.4.191", // for development
  "org.postgresql" % "postgresql" % "9.4-1206-jdbc4", // for prod
  "com.google.inject.extensions" % "guice-multibindings" % "4.0",
  "com.chuusai" %% "shapeless" % "2.2.5", // THERE IS NO COMING BACK
  "org.scalatestplus" % "play_2.11" % "1.4.0" % "test"
)

libraryDependencies ++= Seq(
  "org.webjars" %% "webjars-play" % "2.6.0-M1",
  "org.webjars" % "jquery" % "2.2.0",
  "org.webjars" % "bootstrap" % "3.3.6",
  "org.webjars" % "momentjs" % "2.11.1",
  "org.webjars.bower" % "Chart.js" % "2.0.0-beta2",
  "org.webjars.bower" % "timeago" % "1.4.1"  exclude("org.webjars.bower", "jquery")
)

routesGenerator := InjectedRoutesGenerator

unmanagedResourceDirectories in Test <+=  baseDirectory ( _ /"target/web/public/test" )  

resolvers += "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases"  