enablePlugins(ScalaJSPlugin, BuildInfoPlugin)

name := "citeapp"

version := "1.3.6"

scalaVersion := "2.12.3"

resolvers += Resolver.jcenterRepo
resolvers += Resolver.bintrayRepo("neelsmith", "maven")
resolvers += sbt.Resolver.bintrayRepo("denigma", "denigma-releases")

libraryDependencies ++= Seq(
  "org.scala-js" %% "scalajs-stubs" % scalaJSVersion % "provided",
  "org.scala-js" %%% "scalajs-dom" % "0.9.2",
  "io.monix" %%% "monix" % "2.3.0",
  "edu.holycross.shot.cite" %%% "xcite" % "3.2.2" from "file:///cite/scala/unmanaged_jars/xcite_sjs0.6_2.12-3.2.2.jar",
  "edu.holycross.shot" %%% "ohco2" % "10.4.1" from "file:///cite/scala/unmanaged_jars/ohco2_sjs0.6_2.12-10.4.1.jar",
  "edu.holycross.shot" %%% "scm" % "5.1.10" from "file:///cite/scala/unmanaged_jars/scm_sjs0.6_2.12-5.1.10.jar",
  "edu.holycross.shot" %%% "citeobj" % "5.0.1" from "file:///cite/scala/unmanaged_jars/citeobj_sjs0.6_2.12-5.2.0.jar",
  "edu.holycross.shot" %%% "citerelations" % "2.0.1" from "file:///cite/scala/unmanaged_jars/citerelations_sjs0.6_2.12-2.0.1.jar",
  "edu.holycross.shot" %%% "citebinaryimage" % "1.0.0" from "file:///cite/scala/unmanaged_jars/citebinaryimage_sjs0.6_2.12-1.0.0.jar",
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

	val defaultLibraryUrl = "https://raw.githubusercontent.com/cite-architecture/citedx/master/libraries/millionplus.cex"
	val defaultLibraryDelimiter = "#"

  val compileFirst = (fullOptJS in Compile).value

  val junk = "//# sourceMappingURL=citeapp-opt.js.map"
  val js = Source.fromFile("target/scala-2.12/citeapp-opt.js").getLines.mkString("\n").replaceAll(junk,"")

  val css = Source.fromFile("target/scala-2.12/classes/application.css").getLines.mkString("\n")

  val template1 = "src/main/resources/cite-TEMPLATE1.html"
  val template1Text = Source.fromFile(template1).getLines.mkString("\n").replaceAll("ACTUALVERSION", version.value).replaceAll("ACTUALCSS",css)


	val urlPlaceholder = "DEFAULTLIBRARYURL"
	val delimiterPlaceholder = "DEFAULTLIBRARYDELIMITER"
  val template2Text = Source.fromFile("src/main/resources/cite-TEMPLATE2.html").getLines.mkString("\n").replaceAll(urlPlaceholder,defaultLibraryUrl).replaceAll(delimiterPlaceholder,defaultLibraryDelimiter)
  val newFile = "downloads/cite-" + version.value + ".html"
  new PrintWriter(newFile) { write(template1Text + js + template2Text); close }
  println("Runnable single-page app is in " + newFile)
}

buildInfoKeys := Seq[BuildInfoKey](name, version, scalaVersion, sbtVersion)
buildInfoPackage := "citeapp"
