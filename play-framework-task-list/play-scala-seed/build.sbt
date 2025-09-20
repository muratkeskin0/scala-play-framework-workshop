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
  "com.microsoft.sqlserver" % "mssql-jdbc" % "12.4.2.jre11",  // JDK 17 ile uyumlu
  
  // Email sending
  "com.typesafe.play" %% "play-mailer" % "8.0.1",
  "com.typesafe.play" %% "play-mailer-guice" % "8.0.1",
)

libraryDependencies ++= Seq(
  // Play entegrasyonu (Scala artefact → %%)
  "org.pac4j" %% "play-pac4j" % "12.0.1-PLAY2.9",

  // pac4j çekirdek modülleri (Java artefact → %)
  "org.pac4j" %  "pac4j-core" % "6.1.2",
  "org.pac4j" %  "pac4j-http" % "6.1.2",
  "org.pac4j" %  "pac4j-jwt"  % "6.1.2",
)

// --- pac4j + play-pac4j (Play 2.9, Scala 2.13) ---
libraryDependencies ++= Seq(
  // Play entegrasyonu (Scala artefact → %%)
  "org.pac4j" %% "play-pac4j" % "12.0.1-PLAY2.9"
    exclude("com.fasterxml.jackson.core",   "jackson-databind")
    exclude("com.fasterxml.jackson.core",   "jackson-core")
    exclude("com.fasterxml.jackson.core",   "jackson-annotations")
    exclude("com.fasterxml.jackson.module", "jackson-module-scala")
    exclude("com.fasterxml.jackson.datatype","jackson-datatype-jsr310"),

  // pac4j modülleri (Java artefact → %)
  "org.pac4j" % "pac4j-core" % "6.1.2"
    exclude("com.fasterxml.jackson.core",   "jackson-databind")
    exclude("com.fasterxml.jackson.core",   "jackson-core")
    exclude("com.fasterxml.jackson.core",   "jackson-annotations")
    exclude("com.fasterxml.jackson.module", "jackson-module-scala")
    exclude("com.fasterxml.jackson.datatype","jackson-datatype-jsr310"),

  "org.pac4j" % "pac4j-http" % "6.1.2"
    exclude("com.fasterxml.jackson.core",   "jackson-databind")
    exclude("com.fasterxml.jackson.core",   "jackson-core")
    exclude("com.fasterxml.jackson.core",   "jackson-annotations")
    exclude("com.fasterxml.jackson.module", "jackson-module-scala")
    exclude("com.fasterxml.jackson.datatype","jackson-datatype-jsr310"),

  "org.pac4j" % "pac4j-jwt"  % "6.1.2"
    exclude("com.fasterxml.jackson.core",   "jackson-databind")
    exclude("com.fasterxml.jackson.core",   "jackson-core")
    exclude("com.fasterxml.jackson.core",   "jackson-annotations")
    exclude("com.fasterxml.jackson.module", "jackson-module-scala")
    exclude("com.fasterxml.jackson.datatype","jackson-datatype-jsr310"),

  // Play cache for Pac4j
  "com.typesafe.play" %% "play-cache" % "2.9.0"

  // SAML2Client kullanıyorsan şunu da ekle (yorumdan çıkar):
  // ,"org.pac4j" % "pac4j-saml" % "6.1.2"
  //    .exclude("com.fasterxml.jackson.core",   "jackson-databind")
  //    .exclude("com.fasterxml.jackson.core",   "jackson-core")
  //    .exclude("com.fasterxml.jackson.core",   "jackson-annotations")
  //    .exclude("com.fasterxml.jackson.module", "jackson-module-scala")
  //    .exclude("com.fasterxml.jackson.datatype","jackson-datatype-jsr310")
)

// Jackson’ı Akka/Play 2.9 ile uyumlu 2.14.3’e sabitle
dependencyOverrides ++= Seq(
  "com.fasterxml.jackson.core"     %  "jackson-databind"        % "2.14.3",
  "com.fasterxml.jackson.core"     %  "jackson-core"            % "2.14.3",
  "com.fasterxml.jackson.core"     %  "jackson-annotations"     % "2.14.3",
  "com.fasterxml.jackson.module"  %% "jackson-module-scala"     % "2.14.3",
  "com.fasterxml.jackson.datatype" %  "jackson-datatype-jsr310" % "2.14.3"
)
