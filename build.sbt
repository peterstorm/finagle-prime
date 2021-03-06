lazy val CatsEffectVersion    = "2.3.1"
lazy val CatsMeowMtlVersion   = "0.4.0"
lazy val Fs2Version           = "2.4.6"
lazy val Http4sVersion        = "0.21.8"
lazy val Log4CatsVersion      = "1.1.1"
lazy val FinalgeVersion       = "20.12.0"
lazy val CirceVersion         = "0.13.0"
lazy val LogbackVersion       = "1.2.3" 
lazy val KindProjectorVersion = "0.11.0"
lazy val ScalaTestVersion     = "3.2.2"
lazy val ScalaCheckVersion    = "1.14.1"

lazy val root = (project in file("."))
  .settings(
    organization := "io.peterstorm",
    name := "finagle-prime",
    version := "0.0.1-SNAPSHOT",
    scalaVersion := "2.13.3",
    libraryDependencies ++= Seq(
      "org.typelevel"     %% "cats-effect"         % CatsEffectVersion,
      "com.olegpy"        %% "meow-mtl-core"       % CatsMeowMtlVersion,
      "com.olegpy"        %% "meow-mtl-effects"    % CatsMeowMtlVersion,
      "co.fs2"            %% "fs2-core"            % Fs2Version,
      "io.chrisdavenport" %% "log4cats-slf4j"      % Log4CatsVersion,
      "org.http4s"        %% "http4s-blaze-server" % Http4sVersion,
      "org.http4s"        %% "http4s-dsl"          % Http4sVersion,
      "org.http4s"        %% "http4s-circe"        % Http4sVersion,
      "io.circe"          %% "circe-generic"       % CirceVersion,
      "io.circe"          %% "circe-parser"        % CirceVersion,
      "com.twitter"       %% "scrooge-core"        % FinalgeVersion,
      "com.twitter"       %% "finagle-core"        % FinalgeVersion,
      "com.twitter"       %% "finagle-thrift"      % FinalgeVersion,
      "ch.qos.logback"    %  "logback-classic"     % LogbackVersion,
      "org.scalatest"     %% "scalatest"           % ScalaTestVersion  % "test",
      "org.scalacheck"    %% "scalacheck"          % ScalaCheckVersion % "test",
      "org.scalatestplus" %% "scalacheck-1-14"     % "3.2.2.0" % "test",
      "org.apache.thrift" % "libthrift"            % "0.10.0"
    )
  )

fork in run := true

crossScalaVersions := Seq("2.12.12", "2.13.3")

addCompilerPlugin(
  ("org.typelevel" %% "kind-projector" % KindProjectorVersion).cross(CrossVersion.full),
)
