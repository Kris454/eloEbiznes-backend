name := """ebiznes-backend"""
organization := "com.example"

version := "1.0"

lazy val `ebiznes-backend` = (project in file(".")).enablePlugins(PlayScala)
      
resolvers += "Akka Snapshot Repository" at "https://repo.akka.io/snapshots/"

resolvers += "Atlassian's Maven Public Repository" at "https://packages.atlassian.com/maven-public/"
resolvers += Resolver.jcenterRepo
resolvers += Resolver.sonatypeRepo("snapshots")

scalaVersion := "2.12.3"


libraryDependencies ++= Seq(ehcache , ws , specs2 % Test , guice )
libraryDependencies ++= Seq(
  "com.typesafe.play" %% "play-slick" % "4.0.2",
  "com.typesafe.play" %% "play-slick-evolutions" % "4.0.2",
  "org.xerial"        %  "sqlite-jdbc" % "3.31.1",
  "com.iheart" %% "ficus" % "1.4.7",
  "com.mohiva" %% "play-silhouette" % "6.1.1",
  "com.mohiva" %% "play-silhouette-password-bcrypt" % "6.1.1",
  "com.mohiva" %% "play-silhouette-persistence" % "6.1.1",
  "com.mohiva" %% "play-silhouette-crypto-jca" % "6.1.1",
  "com.mohiva" %% "play-silhouette-totp" % "6.1.1",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.9.3",
  "net.codingwell" %% "scala-guice" % "4.2.6"

)
// Adds additional packages into Twirl
//TwirlKeys.templateImports += "com.example.controllers._"

// Adds additional packages into conf/routes
// play.sbt.routes.RoutesKeys.routesImport += "com.example.binders._"
