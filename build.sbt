enablePlugins(ScalaJSPlugin, BuildInfoPlugin)

name := "citeapp"

version := "1.9.4"

scalaVersion := "2.12.3"

resolvers += Resolver.jcenterRepo
resolvers += Resolver.bintrayRepo("neelsmith", "maven")
resolvers += sbt.Resolver.bintrayRepo("denigma", "denigma-releases")

libraryDependencies ++= Seq(
  "org.scala-js" %% "scalajs-stubs" % scalaJSVersion % "provided",
  "org.scala-js" %%% "scalajs-dom" % "0.9.5",
  "io.monix" %%% "monix" % "2.3.0",
  "edu.holycross.shot.cite" %%% "xcite" % "3.3.0",
  "edu.holycross.shot" %%% "ohco2" % "10.7.0",
  "edu.holycross.shot" %%% "scm" % "6.0.0",
  "edu.holycross.shot" %%% "citeobj" % "7.0.0",
  "edu.holycross.shot" %%% "citerelations" % "2.0.4",
  "edu.holycross.shot" %%% "citebinaryimage" % "1.1.2",
  "com.thoughtworks.binding" %%% "dom" % "latest.version"
)

addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full)

//scalacOptions += "-P:scalajs:suppressExportDeprecations"
//scalacOptions += "-P:scalajs:suppressMissingJSGlobalDeprecations"
scalacOptions += "-unchecked"
scalacOptions += "-deprecation"

lazy val spa = taskKey[Unit]("Assemble single-page app from html templates and generated CSS and JS output")

import scala.io.Source
import java.io.PrintWriter
spa := {

//	val defaultLibraryUrl = "https://raw.githubusercontent.com/cite-architecture/citedx/master/libraries/millionplus.cex"
  val defaultLibraryUrl = "https://raw.githubusercontent.com/Eumaeus/fuCiteDX/master/hmt-cwb4.cex"
  //val defaultLibraryUrl = "https://raw.githubusercontent.com/Eumaeus/fuCiteDX/master/fu-spring2018.cex"
  val serverMode = true
	val defaultLibraryDelimiter = "#"
  val defaultImagePath = "" // Path prefixed to the named directories for local images

  val compileFirst = (fullOptJS in Compile).value

  val junk = "//# sourceMappingURL=citeapp-opt.js.map"
  val js = Source.fromFile("target/scala-2.12/citeapp-opt.js").getLines.mkString("\n").replaceAll(junk,"")

  val css = Source.fromFile("target/scala-2.12/classes/application.css").getLines.mkString("\n")

  val template1 = "src/main/resources/cite-TEMPLATE1.html"
  val template1Text = Source.fromFile(template1).getLines.mkString("\n").replaceAll("ACTUALVERSION", version.value).replaceAll("ACTUALCSS",css)


	val urlPlaceholder = "DEFAULTLIBRARYURL"
	val delimiterPlaceholder = "DEFAULTLIBRARYDELIMITER"
  val imagePathPlaceholder = "DEFAULTIMAGEPATH"
  val serverModePlaceholder = "SERVERMODE"

  val template2Text = Source.fromFile("src/main/resources/cite-TEMPLATE2.html").getLines.mkString("\n").replaceAll(urlPlaceholder,defaultLibraryUrl).replaceAll(delimiterPlaceholder,defaultLibraryDelimiter).replaceAll(imagePathPlaceholder,defaultImagePath).replaceAll(serverModePlaceholder,serverMode.toString)
  val newFile = "downloads/cite-" + version.value + ".html"
  new PrintWriter(newFile) { write(template1Text + js + template2Text); close }
  println("Runnable single-page app is in " + newFile)
}

buildInfoKeys := Seq[BuildInfoKey](name, version, scalaVersion, sbtVersion)
buildInfoPackage := "citeapp"
