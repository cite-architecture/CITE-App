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

	def setPreferredImageSource:Unit = {
		val imgSourceStr:String = js.Dynamic.global.document.getElementById("citeMain_localImageSwitch").checked.toString
		ImageModel.imgUseLocal := { imgSourceStr == "true" }
	}

	def protocolsForImage(u:Cite2Urn):Option[Vector[edu.holycross.shot.citeobj.BinaryImageSource[scala.Any]]] = {
		val collUrn:Cite2Urn = u.dropSelector
		ImageModel.imageExtensions match {
			case Some(e) => {
				val exts = e.extensions(collUrn)
				val v = Some(exts)
				v
			}
			case None => {
					val v:Option[Vector[edu.holycross.shot.citeobj.BinaryImageSource[scala.Any]]] = None
					v
			}
		}
	}

	def hasIipImageDzProtocol(u:Cite2Urn):Boolean = {
		val b:Boolean = true
		g.console.log("hasIipImageDzProtocol called. B.S. temp state… ")
		b
	}
	def hasIipImageJpegProtocol(u:Cite2Urn):Boolean = {
		val b:Boolean = true
		g.console.log("hasIipImageJpegProtocol called. B.S. temp state… ")
		b
	}
	def hasLocalDzProtocol(u:Cite2Urn):Boolean = {
		val b:Boolean = true
		g.console.log("hasLocalDzProtocol called. B.S. temp state… ")
		b
	}
	def hasLocalJpegProtocol(u:Cite2Urn):Boolean = {
		val b:Boolean = true
		g.console.log("hasLocalDzProtocol called. B.S. temp state… ")
		b
	}

	def updateUserMessage(msg: String, alert: Int): Unit = {
		ImageModel.userMessageVisibility := "app_visible"
		ImageModel.userMessage := msg
		alert match {
			case 0 => ImageModel.userAlert := "default"
			case 1 => ImageModel.userAlert := "wait"
			case 2 => ImageModel.userAlert := "warn"
		}
		js.timers.clearTimeout(ImageModel.msgTimer)
		ImageModel.msgTimer = js.timers.setTimeout(16000){ ImageModel.userMessageVisibility := "app_hidden" }
	}

	def urnToLocalPath(urn:Cite2Urn):String = {
		val s:String = s"${ImageModel.imgArchivePath}/${urn.namespace}/${urn.collection}/${urn.version}/"	
		s
	}

	def getLocalJpegPath(urn:Cite2Urn):String = {
		val p:String = s"${ImageController.urnToLocalPath(urn)}${urn.objectComponent}.jpg"
		p
	}
	def getIipImageJpegPath(urn:Cite2Urn):String = {
		val p:String = "NOT A PATH YET"
		p
	}
	def getIipImageDzPath(urn:Cite2Urn):String = {
		val p:String = "NOT A PATH YET"
		p
	}
	def getLocalDzPath(urn:Cite2Urn):String = {
		val p:String = s"${ImageController.urnToLocalPath(urn)}${urn.objectComponent}.dzi"
		p
	}

	def getThumbPath(urn:Cite2Urn):String = {
		var path:String = ""
		if (ImageController.hasLocalJpegProtocol(urn) & ImageController.hasIipImageJpegProtocol(urn)){
			if (ImageModel.imgUseLocal.get){
				path = ImageController.getLocalJpegPath(urn)
			} else {
					path = ImageController.getIipImageJpegPath(urn)
			}
		}
	  path
	}

	def getZoomSource(urn:Cite2Urn):String = {

		if (ImageModel.imgUseLocal.get){
			val src:String = getLocalDzPath(urn)
			src
		} else {
			g.console.log("ImageController > getZoomSource: using remote path")
			val src:String = getIipImageDzPath(urn)
			src
		}
	}

	def getFullImageSource(urn:Cite2Urn):String = {
		val src:String = ""
		g.console.log("getFullImageSource not implemented, returning empty string.")
		src
	}

	// *** Apropos Microservice ***
	/*
	def imgThumb(fullUrn:Cite2Urn):String = {
		val urn:Cite2Urn = fullUrn.dropExtensions
		val path:String = s"""${ImageModel.imgArchivePath}${urn.dropSelector.toString.replaceAll(":","_")}/${urn.objectComponent}_files/8/0_0.jpeg"""
		path
	}
	*/

	// *** Apropos Microservice ***
	/*
	def getFullImagePath(urn:Cite2Urn):String = {
		val path:String = s"""${ImageModel.imgArchivePath}${urn.dropSelector.toString.replaceAll(":","_")}/${urn.objectComponent}.jpg"""
		path
	}
	*/

	// *** Apropos Microservice ***
	def previewImage(u:Cite2Urn) = {
		if (ImageModel.imgUseLocal.get){
			val path:String = getLocalJpegPath(u)
			setPreviewImageFromLocal(u,path)
		} else {
			//ImageController.remoteIIPPreviewImage(u)
			g.console.log("Called deprecated ImageController.remoteIIPPreviewImage.")
		}

	}

	def remoteIIPPreviewImage(u:Cite2Urn) = {
		g.console.log("Remote Preview Image not implemented")

	}

	// *** Apropos Microservice ***
	def setPreviewImageFromLocal(u:Cite2Urn, path:String) = {
		try {
			val justUrn = u.dropExtensions
			var justROI:Option[ImageModel.ImageROI] = None
			ImageModel.imageROIs.get.size match {
				case 1 => {
					justROI = Some(ImageModel.imageROIs.get(0))
				}
				case _ => {
					justROI = None
				}
			}
			var rT:Float = 0; var rL:Float = 0; var rW:Float = 1; var rH:Float = 1;
			justROI match {
				case Some(r) => {
					rL = r.roi.get.split(',')(0).toFloat
					rT = r.roi.get.split(',')(1).toFloat
					rW = r.roi.get.split(',')(2).toFloat
					rH = r.roi.get.split(',')(3).toFloat
				}
				case _ => {
					rT = 0; rL = 0; rW = 1; rH = 1;
				}
			}

			// Let's make some decisions about how big this ROI is!
			/*
			var path:String = ""
			(rW * rH) match {
				case x if x < 0.15 => path = ImageController.getThumbPath(justUrn)
				case _ => path = ImageController.getThumbPath(justUrn)
			}
			*/

			val canvas = document.createElement("canvas").asInstanceOf[HTMLCanvasElement]
			canvas.setAttribute("crossOrigin","Anonymous")
			val ctx = canvas.getContext("2d").asInstanceOf[CanvasRenderingContext2D]
			val offScreenImg = document.createElement("img").asInstanceOf[HTMLImageElement]
			offScreenImg.setAttribute("crossOrigin","Anonymous")
			offScreenImg.setAttribute("src",path)
			//Wait for that to load, then proceed
			offScreenImg.onload = (e: Event) => {
	 			canvas.width = (offScreenImg.width * rW).round
				canvas.height = (offScreenImg.height * rH).round
				// draw it once
				val osw:Double = 0-(offScreenImg.width.toFloat * rL).round.toDouble
				val osh:Double = 0-(offScreenImg.height.toFloat * rT).round.toDouble
				//ctx.drawImage(osw,osh)
				ctx.drawImage(offScreenImg,(0-(offScreenImg.width.toFloat * rL)).round.toDouble,(0-(offScreenImg.height.toFloat*rT)).round.toDouble)

				val s:String = canvas.toDataURL("image/png")
				val prevImg = document.getElementById("image_previewImg").asInstanceOf[HTMLImageElement]
				prevImg.setAttribute("crossOrigin","Anonymous")
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
		ImageModel.urn.get match {
			case Some(u) => {
				val tempUrn:Cite2Urn = u
				val collection:Cite2Urn = tempUrn.dropSelector
				val ioo:Option[String] = tempUrn.objectComponentOption
				ioo match {
					case Some(s) => {
						ImageController.loadJsArray
						ImageController.previewImage(tempUrn)
						val path:String = ImageController.getZoomSource(u)
						ImageController.updateImageJS(collection.toString, s, path )
					}
					case _ => {
						ImageController.updateUserMessage(s"No image-object specified in ${tempUrn}",2)
					}
				}
			}
			case _ => {
				ImageController.updateUserMessage(s"No image-object specified.",2)
			}

		}
	}

	def loadJsArray:Unit = {
		ImageController.clearJsRoiArray(true)
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
			ImageModel.displayUrn := Some(urn)
			validUrnInField := true
			ImageModel.urn := Some(urn.dropExtensions)
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
		def apply(collection: String, imageObject: String, path:String): js.Dynamic = js.native
	}

	/*
	@JSName("imageInNewWindow")
	@js.native
	object imageInNewWindow extends js.Any {
	def apply(canvas: HTMLCanvasElement): js.Dynamic = js.native
}
*/



}
