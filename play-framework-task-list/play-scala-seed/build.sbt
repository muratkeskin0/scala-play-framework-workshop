name := """play-scala-seed"""
organization := "firstPorject"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.13.16"

libraryDependencies += guice
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "7.0.2" % Test

// Adds additional packages into Twirl
//TwirlKeys.templateImports += "firstPorject.controllers._"

// Adds additional packages into conf/routes
// play.sbt.routes.RoutesKeys.routesImport += "firstPorject.binders._"
libraryDependencies ++= Seq(
  "com.typesafe.slick" %% "slick" % "3.5.0",
  "com.typesafe.slick" %% "slick-hikaricp" % "3.5.0",  // Connection pool
  "com.microsoft.sqlserver" % "mssql-jdbc" % "12.4.2.jre11"  // JDK 17 ile uyumlu
)