name := "SimpleEncryptorScala"

version := "1.0"

// Version of Scala used by the project
scalaVersion := "3.3.1"

// Add dependency on ScalaFX library
libraryDependencies += "org.scalafx" %% "scalafx" % "21.0.0-R32"


ThisBuild / Compile / run / mainClass := Some("SimpleEncryptorScala")

// Fork a new JVM for 'run' and 'test:run', to avoid JavaFX double initialization problems
import sbtassembly.AssemblyPlugin.autoImport._

assemblyMergeStrategy in assembly := {
  case PathList("META-INF", xs @ _*) => MergeStrategy.discard
  case x => MergeStrategy.first
}

ThisBuild / assembly / mainClass := Some("SimpleEncryptorScala")
