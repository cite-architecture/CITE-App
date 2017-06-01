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

	def clearAll:Unit = {
		val previewImg = document.getElementById("image_previewImg").asInstanceOf[HTMLImageElement]
		previewImg.setAttribute("src","")
	}

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

	// *** Apropos Microservice ***
	def imgThumb(fullUrn:Cite2Urn):String = {
			val urn:Cite2Urn = fullUrn.dropExtensions
			val path:String = s"""${ImageModel.imgArchivePath}${urn.dropSelector.toString.replaceAll(":","_")}/${urn.objectComponent}_files/8/0_0.jpeg"""
			path
	}

	// *** Apropos Microservice ***
	def getFullImagePath(urn:Cite2Urn):String = {
			val path:String = s"""${ImageModel.imgArchivePath}${urn.dropSelector.toString.replaceAll(":","_")}/${urn.objectComponent}.jpg"""
			path
	}

// *** Apropos Microservice ***
  def previewImage(u:Cite2Urn) = {
			try {
				val justUrn = u.dropExtensions
				val justROI = u.objectExtensionOption
				var rT:Float = 0; var rL:Float = 0; var rW:Float = 1; var rH:Float = 1;
				justROI match {
					case Some(r) => {
						rL = r.split(',')(0).toFloat
						rT = r.split(',')(1).toFloat
						rW = r.split(',')(2).toFloat
						rH = r.split(',')(3).toFloat
					}
					case _ => {
						rT = 0; rL = 0; rW = 1; rH = 1;
					}
				}

				// Let's make some decisions about how big this ROI is!
				var path:String = ""
				(rW * rH) match {
					case x if x < 0.15 => path = ImageController.getFullImagePath(justUrn)
					case _ => path = ImageController.imgThumb(justUrn)
				}

				val canvas = document.createElement("canvas").asInstanceOf[HTMLCanvasElement]
				val ctx = canvas.getContext("2d").asInstanceOf[CanvasRenderingContext2D]
				val offScreenImg = document.createElement("img").asInstanceOf[HTMLImageElement]
				offScreenImg.setAttribute("src",path)
				//Wait for that to load, then proceed
				offScreenImg.onload = (e: Event) => {
						canvas.width = (offScreenImg.width * rW).round
						canvas.height = (offScreenImg.height * rH).round
						// draw it once
						ctx.drawImage(offScreenImg,(0-(offScreenImg.width * rL)).round,(0-(offScreenImg.height*rT)).round)

						val s:String = canvas.toDataURL("image/png")
						val prevImg = document.getElementById("image_previewImg").asInstanceOf[HTMLImageElement]
						prevImg.setAttribute("src",s)
				}

			} catch {
				case e: Exception => {
					ImageController.updateUserMessage(s"Could not download image for ${u}. ${e}",2)
				}
			}
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
				ImageController.previewImage(tempUrn)
				ImageController.updateImageJS(collection.toString, s )
			}
			case _ => {
				ImageController.updateUserMessage(s"No image-object specified in ${tempUrn}",2)
			}
		}
	}

	def loadJsArray:Unit = {
		ImageController.clearJsRoiArray(true)
		//g.console.log(s"Scala loadJsArray: ${ImageModel.imageROIs.get.size}")
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
			val tempGroup:String = iroi.roiGroup.toString
			val tempIndex:Int = iroi.index
			//g.console.log(s"Adding: ${tempIndex}, ${tempRoi}, ${tempMappedData}, ${tempGroup}")
			ImageController.addToJsRoiArray(tempIndex, tempRoi,tempMappedData,tempGroup)
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

	// *** Apropos Microservice ***
	def changeUrn(urnString: String): Unit = {
		changeUrn(Cite2Urn(urnString),Vector((None, None)))
	}

	// *** Apropos Microservice ***
	def changeUrn(urn: Cite2Urn): Unit = {
		try {
			val oe = urn.objectExtensionOption
			changeUrn(urn,Vector((oe,None)))
		} catch {
			case e: Exception => {
				validUrnInField := false
				updateUserMessage(s"Invalid URN. Current URN not changed. ${e}",2)
			}
		}
	}

	// *** Apropos Microservice ***
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
				updateUserMessage(s"Invalid URN [2]. Current URN not changed. ${e}",2)
			}
		}
	}

@JSName("clearJsRoiArray")
@js.native
object clearJsRoiArray extends js.Any {
	def apply(really:Boolean): js.Dynamic = js.native
}

@JSName("addToJsRoiArray")
@js.native
object addToJsRoiArray extends js.Any {
  def apply(index:Int, roiString:String, urnString:String, groupString:String): js.Dynamic = js.native
}

@JSName("updateImageJS")
@js.native
object updateImageJS extends js.Any {
  def apply(collection: String, imageObject: String): js.Dynamic = js.native
}

/*
@JSName("imageInNewWindow")
@js.native
object imageInNewWindow extends js.Any {
  def apply(canvas: HTMLCanvasElement): js.Dynamic = js.native
}
*/



}
