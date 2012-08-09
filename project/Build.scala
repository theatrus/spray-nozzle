import sbt._
import Keys._

object Build extends sbt.Build {
  import Dependencies._

	lazy val myProject = Project("spray-nozzle", file("."))
		.settings(
		organization  := "us.theatr",
		version       := "0.1-SNAPSHOT",
		scalaVersion  := "2.9.1",
		crossScalaVersions := Seq("2.9.1", "2.9.2"),
		scalacOptions := Seq("-deprecation", "-encoding", "utf8"),
		resolvers     ++= Dependencies.resolutionRepos,
		libraryDependencies ++=
			compile(akkaActor, sprayServer) ++
				test(specs2) ++
				runtime()
	)
}

