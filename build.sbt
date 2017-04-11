enablePlugins(ScalaJSPlugin, BuildInfoPlugin)

name := "citeapp"

version := "1.2.0"

scalaVersion := "2.11.8"

resolvers += Resolver.jcenterRepo
resolvers += Resolver.bintrayRepo("neelsmith", "maven")
resolvers += "beta" at "http://beta.hpcc.uh.edu/nexus/content/repositories/releases"
resolvers += sbt.Resolver.bintrayRepo("denigma", "denigma-releases")

libraryDependencies ++= Seq(
  "org.scala-js" %% "scalajs-stubs" % scalaJSVersion % "provided",
  "org.scala-js" %%% "scalajs-dom" % "0.9.1",
  "edu.holycross.shot.cite" %%% "xcite" % "2.2.3",
  "edu.holycross.shot" %%% "ohco2" % "7.0.1",
  "edu.holycross.shot" %%% "scm" % "1.3.3",
  "edu.holycross.shot" %% "citeenv" % "1.1.2",
  "com.thoughtworks.binding" %%% "dom" % "latest.version"
)

addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full)

lazy val spa = taskKey[Unit]("Assemble single-page app from html templates and generated CSS and JS output")

import scala.io.Source
import java.io.PrintWriter
spa := {
  val compileFirst = (fullOptJS in Compile).value

  val junk = "//# sourceMappingURL=citeapp-opt.js.map"
  val js = Source.fromFile("target/scala-2.11/citeapp-opt.js").getLines.mkString("\n").replaceAll(junk,"")

  val css = Source.fromFile("target/scala-2.11/classes/application.css").getLines.mkString("\n")

  val template1 = "src/main/resources/cite-TEMPLATE1.html"
  val template1Text = Source.fromFile(template1).getLines.mkString("\n").replaceAll("ACTUALVERSION", version.value).replaceAll("ACTUALCSS",css)

  val template2Text = Source.fromFile("src/main/resources/cite-TEMPLATE2.html").getLines.mkString("\n")

  val newFile = "downloads/cite-" + version.value + ".html"
  new PrintWriter(newFile) { write(template1Text + js + template2Text); close }
  println("Runnable single-page app is in " + newFile)
}

buildInfoKeys := Seq[BuildInfoKey](name, version, scalaVersion, sbtVersion)
buildInfoPackage := "citeapp"
