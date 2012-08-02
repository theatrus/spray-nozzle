import sbt._
import Keys._

object Build extends sbt.Build {
  import Dependencies._

  lazy val myProject = Project("sf-spray-addons", file("."))
    .settings(
      organization  := "com.stackfoundry",
      version       := "0.1",
      scalaVersion  := "2.9.1",
      scalacOptions := Seq("-deprecation", "-encoding", "utf8"),
      resolvers     ++= Dependencies.resolutionRepos,
      libraryDependencies ++=
        compile(akkaActor, sprayServer) ++
        test(specs2) ++
        runtime(akkaSlf4j, slf4j, logback)
    )
}

