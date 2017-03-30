enablePlugins(ScalaJSPlugin)

name := "citeapp"

version := "1.1.0"

//scalacOptions := Seq("-unchecked", "-deprecation")

scalaVersion := "2.11.8"

resolvers += Resolver.jcenterRepo
resolvers += Resolver.bintrayRepo("neelsmith", "maven")
resolvers += sbt.Resolver.bintrayRepo("denigma", "denigma-releases") //add resolver

libraryDependencies ++= Seq(
  "org.scala-js" %% "scalajs-stubs" % scalaJSVersion % "provided",
  "org.scala-js" %%% "scalajs-dom" % "0.9.1",
  "edu.holycross.shot.cite" %%% "xcite" % "2.2.3",
  "edu.holycross.shot" %%% "ohco2" % "7.0.1",
  "edu.holycross.shot" %% "citeenv" % "1.1.2",
  "com.thoughtworks.binding" %%% "dom" % "latest.version"
)

addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full)
