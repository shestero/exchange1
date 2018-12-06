name := "exchange1"

version := "0.1"

scalaVersion := "2.12.7"

// https://mvnrepository.com/artifact/org.apache.logging.log4j/log4j-core
libraryDependencies += "org.apache.logging.log4j" % "log4j-core" % "2.11.+" // current: 2.11.1
libraryDependencies += "org.apache.logging.log4j" % "log4j-api" % "2.11.+"  // current: 2.11.1
//libraryDependencies += "org.apache.logging.log4j" % "log4j-slf4j-impl" % "2.11.+"
libraryDependencies += "org.apache.logging.log4j" %% "log4j-api-scala" % "11.0"

// https://mvnrepository.com/artifact/org.scala-lang.modules/scala-xml
libraryDependencies += "org.scala-lang.modules" %% "scala-xml" % "1.1.1" // current: 1.1.1

libraryDependencies += "com.typesafe.akka" %% "akka-http"   % "10.1.+"   // current: 10.1.5
libraryDependencies += "com.typesafe.akka" %% "akka-http-spray-json" % "10.1.+"

libraryDependencies += "com.typesafe.akka" %% "akka-actor"   % "2.5.+" // current: 2.5.18
libraryDependencies += "com.typesafe.akka" %% "akka-testkit" % "2.5.+" % "test" // current: 2.5.18
libraryDependencies += "com.typesafe.akka" %% "akka-stream" % "2.5.+" // current: 10.5.18

// libraryDependencies += "io.spray" %%  "spray-json" % "1.3.5"

// scalatest
libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.+" % Test // current: 3.0.5
libraryDependencies += "com.typesafe.akka" %% "akka-http-testkit" % "10.1.+" % Test // route test kit // current: 10.1.5

// junit
// https://mvnrepository.com/artifact/junit/junit
//libraryDependencies += "junit" % "junit" % "4.12" % Test
//libraryDependencies += "com.novocode" %% "junit-interface" % "0.11" % "test" // sbt's test interface for JUnit 4.

// scalaz
libraryDependencies += "org.scalaz" %% "scalaz-core" % "7.2.+" // current: 7.2.27

// https://stackoverflow.com/questions/19584686/java-lang-noclassdeffounderror-while-running-jar-from-scala
retrieveManaged := true
