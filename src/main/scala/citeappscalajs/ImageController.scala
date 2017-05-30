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

	def imgThumb(fullUrn:Cite2Urn):String = {
			val urn:Cite2Urn = fullUrn.dropExtensions
			val path:String = s"""${ImageModel.imgArchivePath}${urn.dropSelector.toString.replaceAll(":","_")}/${urn.objectComponent}_files/8/0_0.jpeg"""
			path
	}

	def getFullImagePath(urn:Cite2Urn):String = {
			val path:String = s"""${ImageModel.imgArchivePath}${urn.dropSelector.toString.replaceAll(":","_")}/${urn.objectComponent}.jpeg"""
			path
	}

	def getBinaryImage(urn:Cite2Urn):String = {
		val canvas = document.createElement("canvas").asInstanceOf[HTMLCanvasElement]
	  val ctx = canvas.getContext("2d").asInstanceOf[CanvasRenderingContext2D]
	  canvas.width = (0.95 * window.innerWidth).toInt
	  canvas.height = (0.95 * window.innerHeight).toInt
		val s:String = "Test"
		s
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
				ImageController.loadJsArray
				ImageController.updateImageJS(collection.toString, s )
			}
			case _ => {
				ImageController.updateUserMessage(s"No image-object specified in ${tempUrn}",2)
			}
		}
	}

	def loadJsArray:Unit = {
		for (iroi <- ImageModel.imageROIs.get){
			val tempRoi:String = {
				iroi.roi match {
					case Some(r) => r
					case _ => ""
				}
			}
			val tempMappedData:String = {
				iroi.roiData match {
					case Some(u) => u.toString
					case _ => ""
				}
			}
			// We will have to do something clever here to make groups
			val tempGroup:String = "1"
			val tempIndex:Int = iroi.index
			ImageController.addToJsRoiArray(tempIndex, tempRoi,tempMappedData,tempGroup,true)
		}
	}

	/* ------- Versions of `changeUrn` -------
    For `changeUrn` we need three versions.
	1. ImageURN as String… convert to Cite2Urn and invoke…
	2. Image URN as Cite2Urn (only).
	   a. No-ROI substring: Invoke with an empty vector of roi-mappings
	   b. w/ROI substring: Invoke with a vector of [Some[String],None]
	3. Image URN with Vector of roi-mappings. Invoke with vector of [Some[String],Some[Urn]]
	------------------------------------------ */

	def changeUrn(urnString: String): Unit = {
		changeUrn(Cite2Urn(urnString),Vector((None, None)))
	}

	def changeUrn(urn: Cite2Urn): Unit = {
		try {
			val oe = urn.objectExtensionOption
			changeUrn(urn,Vector((oe,None)))
		} catch {
			case e: Exception => {
				validUrnInField := false
				updateUserMessage("Invalid URN. Current URN not changed.",2)
			}
		}
	}

	def changeUrn(urn:Cite2Urn,roiVec:Vector[(Option[String],Option[Urn])]):Unit = {
		try {
			ImageModel.displayUrn := urn
			validUrnInField := true
			ImageModel.urn := urn.dropExtensions
			val plainUrn:Cite2Urn = urn.dropExtensions
			ImageModel.updateRois(plainUrn,roiVec)
			ImageController.changeImage
		} catch {
			case e: Exception => {
				validUrnInField := false
				updateUserMessage("Invalid URN. Current URN not changed.",2)
			}
		}
	}

@JSName("clearJsRoiArray")
@js.native
object clearJsRoiArray extends js.Any {

}

@JSName("addToJsRoiArray")
@js.native
object addToJsRoiArray extends js.Any {
  def apply(index:Int, roiString:String, urnString:String, groupString:String, clearFirst:Boolean): js.Dynamic = js.native
}

@JSName("updateImageJS")
@js.native
object updateImageJS extends js.Any {
  def apply(collection: String, imageObject: String): js.Dynamic = js.native
}


}
