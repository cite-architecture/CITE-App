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

import scala.scalajs.js.annotation.JSExport

@JSExport
object ImageController {


	val validUrnInField = Var(false)

	def updateUserMessage(msg: String, alert: Int): Unit = {
		ImageModel.userMessageVisibility := "app_visible"
		ImageModel.userMessage := msg
		alert match {
			case 0 => ImageModel.userAlert := "default"
			case 1 => ImageModel.userAlert := "wait"
			case 2 => ImageModel.userAlert := "warn"
		}
		js.timers.clearTimeout(ImageModel.msgTimer)
		ImageModel.msgTimer = js.timers.setTimeout(6000){ ImageModel.userMessageVisibility := "app_hidden" }
	}


	def validateUrn(urnString: String): Unit = {
		try{
			val newUrn: Cite2Urn = Cite2Urn(urnString)
			validUrnInField := true
		} catch {
			case e: Exception => {
				validUrnInField := false
			}
		}
	}

	def changeImage:Unit = {
		val tempUrn:Cite2Urn = ImageModel.urn.get
		val collection:Cite2Urn = tempUrn.dropSelector
		val ioo:Option[String] = tempUrn.objectComponentOption
		ioo match {
				case Some(s) => {
					ImageController.updateImageJS(collection.toString, s )
				}
				case _ => {
					ImageController.updateUserMessage(s"No image-object specified in ${tempUrn}",2)
				}
		}
	}

	def changeUrn(urnString: String): Unit = {
		changeUrn(Cite2Urn(urnString))
	}

	def changeUrn(urn: Cite2Urn): Unit = {
		try {
			ImageModel.displayUrn := urn
			validUrnInField := true
			g.console.log(s"Start with: ${urn}")
			val oe = urn.objectExtensionOption
			g.console.log(s"Extension: ${oe}")
			oe match {
				case Some(e) => {
						val ve = Vector[String](e)
						ImageModel.updateRois(urn.dropExtensions, ve)
						ImageModel.urn := urn.dropExtensions
						g.console.log(s"Urn without extension: ${ImageModel.urn.get.toString}")
				}
				case _ => {
						ImageModel.urn := urn
				}
			}
			ImageController.updateUserMessage("Retrieving image…",1)
			js.timers.setTimeout(500){
			ImageController.changeImage
			}

		} catch {
			case e: Exception => {
				validUrnInField := false
				updateUserMessage("Invalid URN. Current URN not changed.",2)
			}
		}
	}


@JSName("updateImageJS")
@js.native
object updateImageJS extends js.Any {
  def apply(collection: String, imageObject: String): js.Dynamic = js.native
}


}
