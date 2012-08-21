import sbt._

object Dependencies {
  val resolutionRepos = Seq(
    ScalaToolsSnapshots,
    "Typesafe repo" at "http://repo.typesafe.com/typesafe/releases/",
    "spray repo" at "http://repo.spray.cc/"
  )

  def compile   (deps: ModuleID*): Seq[ModuleID] = deps map (_ % "compile")
  def provided  (deps: ModuleID*): Seq[ModuleID] = deps map (_ % "provided")
  def test      (deps: ModuleID*): Seq[ModuleID] = deps map (_ % "test")
  def runtime   (deps: ModuleID*): Seq[ModuleID] = deps map (_ % "runtime")
  def container (deps: ModuleID*): Seq[ModuleID] = deps map (_ % "container")

  object V {
    val akka     = "2.0.1"
    val spray    = "1.0-M2"
  }

  val sprayServer = "cc.spray"          %  "spray-server"    % V.spray
  val specs2      = "org.specs2"        %% "specs2"          % "1.11"
	val akkaActor   = "com.typesafe.akka" % "akka-actor"       % V.akka
	val bcprov      = "org.bouncycastle"  % "bcprov-jdk16"     % "1.46"


}
