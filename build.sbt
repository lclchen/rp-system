name := "simpleReportingSystem"

version := "1.0"

scalaVersion := "2.11.2"

resolvers ++= Seq(
"Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots",
"rediscala" at "http://dl.bintray.com/etaty/maven"
)

libraryDependencies ++= Seq(
"com.typesafe.scala-logging" %% "scala-logging-slf4j" % "2.1.2",
"org.mongodb" %% "casbah-core" % "2.7.2",
"com.novus" %% "salat-core" % "1.9.8",
"joda-time" % "joda-time" % "2.2",
"org.scalatest" %% "scalatest" % "2.1.7" % "test",
//"junit" % "junit" % "4.11" % "test",
"com.novocode" % "junit-interface" % "0.10" % "test",
"ch.qos.logback" % "logback-classic" % "1.1.1",
"com.typesafe.akka" %% "akka-actor" % "2.3.3",
"log4j" % "log4j" % "1.2.14"
)
    