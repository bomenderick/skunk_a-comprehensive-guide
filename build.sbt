ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.3.0"

lazy val root = (project in file("."))
  .settings(
    name := "skunk_a-comprehensive-guide",
    libraryDependencies ++= Seq(
      "org.tpolecat" %% "skunk-core" % "0.6.3",
      "com.github.pureconfig" %% "pureconfig-core" % "0.17.5"
    )

  )
