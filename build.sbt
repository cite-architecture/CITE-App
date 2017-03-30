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



lazy val spa = taskKey[Unit]("Assemble single-page app")

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


  val newFile = template1.replaceAll("TEMPLATE1",version.value)
  println("Output will be in " + newFile)
  //(spaText)


  new PrintWriter(newFile) { write(template1Text + js + template2Text); close }
}
