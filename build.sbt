name := "ArticleBodyParserAkka"

version := "0.1"

scalaVersion := "2.12.3"

organization := "com.akk"

val akkaVersion ="2.5.6"


//unmanagedBase := baseDirectory.value /"libs"

libraryDependencies ++=Seq(

  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "com.Akka" %% "akka-inmemory-db" % "1.0",


)
