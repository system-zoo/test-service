import AssemblyKeys._
import sbt.Keys._
import sbtdocker.Plugin.DockerKeys._
import sbtdocker.mutable.Dockerfile
import sbtdocker.ImageName

name := "test-service"

organization := "systemzoo"

version := "1"

startYear := Some(2015)

/* scala versions and options */
scalaVersion := "2.11.2"

val akka = "2.3.3"
val spray = "1.3.2"

/* dependencies */
libraryDependencies ++= Seq (
  // -- Logging --
  "ch.qos.logback"               % "logback-classic"     % "1.1.2"
  ,"com.typesafe.scala-logging" %% "scala-logging-slf4j" % "2.1.2"
  // -- Akka --
  ,"com.typesafe.akka"          %% "akka-actor"          % akka
  ,"com.typesafe.akka"          %% "akka-slf4j"          % akka
  // -- Spray --
  ,"io.spray"                   %% "spray-routing"       % spray
  ,"io.spray"                   %% "spray-client"        % spray
  ,"io.spray"                   %% "spray-testkit"       % spray    % "test"
  // -- json --
  ,"io.spray"                   %%  "spray-json"         % "1.3.1"
  // -- config --
  ,"com.typesafe"                % "config"              % "1.2.1"
  ,"org.scalatest"              %% "scalatest"           % "2.2.3"  % "test"
)

resolvers ++= Seq(
  "spray repo" at "http://repo.spray.io",
  Resolver.sonatypeRepo("releases"),
  Resolver.sonatypeRepo("snapshots")
)

val testSettings = Seq(
  fork in Test := true,
  javaOptions in Test := Seq("-Denv=local")
)

testSettings

dockerSettings

assemblySettings

test in assembly := {}

mainClass in assembly := Some("com.systemzoo.TestServiceApp")

docker <<= (docker dependsOn assembly)

dockerfile in docker := {
  val artifact = (outputPath in assembly).value
  val artifactTargetPath = s"/app/${artifact.name}"
  new Dockerfile {
    from("develar/java")
    add(artifact, artifactTargetPath)
    entryPoint("java", "-Denv=prod", "-jar", artifactTargetPath)
    expose(80)
  }
}

imageName in docker := {
  ImageName(
    namespace = Some(organization.value),
    repository = name.value,
    tag = Some("latest")
  )
}

Seq(Revolver.settings: _*)
