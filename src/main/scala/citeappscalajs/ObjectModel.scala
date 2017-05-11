package citeapp

import com.thoughtworks.binding.{Binding, dom}
import com.thoughtworks.binding.Binding.{BindingSeq, Var, Vars}
import scala.scalajs.js
import scala.scalajs.js._
import js.annotation._
import collection.mutable
import collection.mutable._
import org.scalajs.dom._
import org.scalajs.dom.ext._
import org.scalajs.dom.raw._
import edu.holycross.shot.cite._
import edu.holycross.shot.ohco2._
import edu.holycross.shot.citeenv._

import scala.scalajs.js.annotation.JSExport

@JSExport
object ObjectModel {

	var msgTimer:scala.scalajs.js.timers.SetTimeoutHandle = null


	// urn is what the user requested
	val urn = Var(Cite2Urn("urn:cite2:hmt:vaimg.v1:VA012RN_0013"))
	// displayUrn is what will be shown
	val displayUrn = Var(Cite2Urn("urn:cite2:hmt:vaimg.v1:VA012RN_0013"))
	val versionsForCurrentUrn = Var(1)

	val userMessage = Var("")
	val userAlert = Var("default")
	val userMessageVisibility = Var("app_hidden")

	/* This is how to pass data to the global JS scope */
	/*
	js.Dynamic.global.currentObjectUrn = "urn:cts"
	js.Dynamic.global.roiArray = Array("one","two","three")
	*/

	val urn1 = Cite2Urn("urn:cite2:hmt:vaimg.v1:VA012RN_0013")
	val urn2 = Cite2Urn("urn:cite2:hmt:vaimg.v1:VA012VN_0514")
	val urn3 = Cite2Urn("urn:cite2:hmt:vaimg.v1:VA013RN_0014")



}
