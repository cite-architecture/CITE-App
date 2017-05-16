package citeapp

import com.thoughtworks.binding.{Binding, dom}
import com.thoughtworks.binding.Binding.{BindingSeq, Var, Vars}
import scala.scalajs.js
import scala.scalajs.js._
import js.annotation._
import collection.mutable
import collection.mutable._
import scala.scalajs.js.Dynamic.{ global => g }
import org.scalajs.dom._
import org.scalajs.dom.ext._
import org.scalajs.dom.raw._
import edu.holycross.shot.cite._
import edu.holycross.shot.ohco2._
import edu.holycross.shot.citeobj._

import scala.scalajs.js.annotation.JSExport

@JSExport
object ObjectController {


	val validObjectUrnInField = Var(false)
	val validCollectionUrnInField = Var(false)

	def updateUserMessage(msg: String, alert: Int): Unit = {
		ObjectModel.userMessageVisibility := "app_visible"
		ObjectModel.userMessage := msg
		alert match {
			case 0 => ObjectModel.userAlert := "default"
			case 1 => ObjectModel.userAlert := "wait"
			case 2 => ObjectModel.userAlert := "warn"
		}
		js.timers.clearTimeout(ObjectModel.msgTimer)
		ObjectModel.msgTimer = js.timers.setTimeout(6000){ ObjectModel.userMessageVisibility := "app_hidden" }
	}

	// This version also insists on an object-identifier
	def validateUrn(urnString: String): Unit = {
		try{
			val newUrn: Cite2Urn = Cite2Urn(urnString)
			newUrn.objectComponentOption match {
				case Some(o) => {
					validObjectUrnInField := true
					validCollectionUrnInField := false
				}
				case _ => {
					validObjectUrnInField := false
					validCollectionUrnInField := true
				}
			}
		} catch {
			case e: Exception => {
				validObjectUrnInField := false
				validCollectionUrnInField := false
			}
		}
	}

	def changeObject:Unit = {
		val tempUrn:Cite2Urn = ObjectModel.urn.get
		ObjectModel.clearObject
	  val filteredData = ObjectModel.collectionRepository.data ~~ tempUrn
	  filteredData.objects.foreach( fc => {
				ObjectModel.objects.get += fc
		})
		/*
		val collection:Cite2Urn = tempUrn.dropSelector
		val ioo:Option[String] = tempUrn.objectComponentOption
		ioo match {
				case Some(s) => {
					// ImageController.updateImageJS(collection.toString, s )
					g.console.log(s"Will load ${tempUrn}")
					ObjectModel.get += tempUrn
				}
				case _ => {
					ObjectController.updateUserMessage(s"No object specified in ${tempUrn}",2)
				}
		}
		*/
	}

	def changeUrn(urnString: String): Unit = {
		changeUrn(Cite2Urn(urnString))
	}

	def changeUrn(urn: Cite2Urn): Unit = {
		try {
			ObjectModel.urn := urn
			ObjectModel.displayUrn := urn
			ObjectModel.urn.get.objectComponentOption match {
				case Some(o) => {
					validObjectUrnInField := true
					validCollectionUrnInField := false
					ObjectController.updateUserMessage("Retrieving object…",1)
				}
				case _ => {
					validObjectUrnInField := false
					validCollectionUrnInField := true
					ObjectController.updateUserMessage("Retrieving collection…",1)
				}
			}
			js.timers.setTimeout(500){ ObjectController.changeObject }

		} catch {
			case e: Exception => {
				validObjectUrnInField := false
				validCollectionUrnInField := false
				updateUserMessage("Invalid URN. Current URN not changed.",2)
			}
		}
	}

	def preloadUrn:Unit = {
			// get first collection in catalog
			if (ObjectModel.collections.get.size > 0){
					val urn = ObjectModel.collections.get(0).urn
					insertFirstObjectUrn(urn)
			} else {

			}
			g.console.log("preloading URN")
	}

	@dom
	def clearResults = {
			g.console.log("Clearing results")
	}

	@dom
	def clearHistory = {
			g.console.log("Clearing history")
	}

	def insertFirstObjectUrn(urn: Cite2Urn): Unit = {
		g.console.log(s"Will get first urn for: ${urn}")
		val firstUrn:Cite2Urn = ( ObjectModel.collectionRepository.data ~~ urn ).objects.head
		js.Dynamic.global.document.getElementById("object_urnInput").value = firstUrn.toString
		validObjectUrnInField := true
	//js.Dynamic.global.document.getElementById("o2_urnInput").value = firstUrn.toString
	//validUrnInField := true
	}



}
