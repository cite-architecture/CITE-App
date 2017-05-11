package citeapp

import com.thoughtworks.binding.{Binding, dom}
import com.thoughtworks.binding.Binding.{BindingSeq, Var, Vars}
import scala.scalajs.js
import scala.scalajs.js._
import js.annotation._
import collection.mutable
import collection.mutable._
import scala.scalajs.js.Dynamic.global
import org.scalajs.dom._
import org.scalajs.dom.ext._
import org.scalajs.dom.raw._
import edu.holycross.shot.cite._
import edu.holycross.shot.ohco2._

import scala.scalajs.js.annotation.JSExport

@JSExport
object ObjectController {


	val validUrnInField = Var(false)

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
				case Some(o) => validUrnInField := true
				case _ => validUrnInField := false
			}
		} catch {
			case e: Exception => {
				validUrnInField := false
			}
		}
	}

	def changeObject:Unit = {
		val tempUrn:Cite2Urn = ImageModel.urn.get
		val collection:Cite2Urn = tempUrn.dropSelector
		val ioo:Option[String] = tempUrn.objectComponentOption
		ioo match {
				case Some(s) => {
					// ImageController.updateImageJS(collection.toString, s )
				}
				case _ => {
					ObjectController.updateUserMessage(s"No object specified in ${tempUrn}",2)
				}
		}
	}

	def changeUrn(urnString: String): Unit = {
		changeUrn(Cite2Urn(urnString))
	}

	def changeUrn(urn: Cite2Urn): Unit = {
		try {
			ObjectModel.urn := urn
			ObjectModel.displayUrn := urn
			validUrnInField := true
			ObjectController.updateUserMessage("Retrieving object…",1)
			js.timers.setTimeout(500){
			ObjectController.changeObject
			}

		} catch {
			case e: Exception => {
				validUrnInField := false
				updateUserMessage("Invalid URN. Current URN not changed.",2)
			}
		}
	}



}
