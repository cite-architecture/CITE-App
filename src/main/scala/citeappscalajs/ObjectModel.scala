package citeapp

import com.thoughtworks.binding.{Binding, dom}
import com.thoughtworks.binding.Binding.{BindingSeq, Var, Vars}
import scala.scalajs.js
import scala.scalajs.js._
import scala.scalajs.js.Dynamic.{ global => g }
import js.annotation._
import collection.mutable
import collection.mutable._
import org.scalajs.dom._
import org.scalajs.dom.ext._
import org.scalajs.dom.raw._
import edu.holycross.shot.cite._
import edu.holycross.shot.ohco2._
import edu.holycross.shot.citeobj._
import edu.holycross.shot.citeenv._

import scala.scalajs.js.annotation.JSExport

@JSExport
object ObjectModel {

	// Messages
	var msgTimer:scala.scalajs.js.timers.SetTimeoutHandle = null
	val userMessage = Var("")
	val userAlert = Var("default")
	val userMessageVisibility = Var("app_hidden")


	// urn is what the user requested
	val urn = Var(Cite2Urn("urn:cite2:hmt:vaimg.v1:VA012RN_0013"))
	// displayUrn is what will be shown
	val displayUrn = Var(Cite2Urn("urn:cite2:hmt:vaimg.v1:VA012RN_0013"))


	// Keeping track of current data
	val collections = Vars.empty[CiteCollectionDef]
	val objects = Vars.empty[Cite2Urn]

	// For Displat
	val offset = Var(Int)
	val limit = Var(Int)
	val showObjects = Var(false)

	// Object-or-collection?
	//    Choices: "none","object","collection","range"
	val objectOrCollection = Var("none")


	var collectionRepository: CiteCollectionRepository = null


	@dom
	def clearObject = {
			g.console.log("Clearing object")
			objects.get.clear
	}

	@dom
	def updateCollections = {
		ObjectModel.collections.get.clear
		for ( cc <- ObjectModel.collectionRepository.catalog.collections){
			ObjectModel.collections.get += cc
		}
	}

	def countObjects(urn:Cite2Urn):Int = {
		val howMany:Int = ( ObjectModel.collectionRepository.data ~~ urn ).objects.size
		howMany
	}

	/* This is how to pass data to the global JS scope */
	/*
	js.Dynamic.global.currentObjectUrn = "urn:cts"
	js.Dynamic.global.roiArray = Array("one","two","three")
	*/

	val urn1 = Cite2Urn("urn:cite2:hmt:vaimg.v1:VA012RN_0013")
	val urn2 = Cite2Urn("urn:cite2:hmt:vaimg.v1:VA012VN_0514")
	val urn3 = Cite2Urn("urn:cite2:hmt:vaimg.v1:VA013RN_0014")



}
