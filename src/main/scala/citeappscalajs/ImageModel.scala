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
import edu.holycross.shot.citeobj._


import scala.scalajs.js.annotation.JSExport

@JSExport
object ImageModel {

	var msgTimer:scala.scalajs.js.timers.SetTimeoutHandle = null



  // Image Collections here
	val imgArchivePath:String = "../../../image_archive/"
	var imageExtensions:ImageExtensions = null
	val imageCollections = Vars.empty[Cite2Urn]

	// urn is what the user requested
	val urn = Var(Cite2Urn("urn:cite2:ns:coll.v1:obj"))
	// displayUrn is what will be shown
	val displayUrn = Var(Cite2Urn("urn:cite2:ns:coll.v1:obj"))
	val versionsForCurrentUrn = Var(1)

	//We might have zero+ ROIs on this image
	val imageROIs = Vars.empty[ImageROI]

	val userMessage = Var("")
	val userAlert = Var("default")
	val userMessageVisibility = Var("app_hidden")

	val imageSearchResults = Vars.empty[ImageObject]

   	case class ImageObject(val urn:Cite2Urn, val label:String, val rights:String) {
			override def toString = s"${urn} - ${label}. ${rights}"
	}

	case class ImageROI(val urn:Cite2Urn, val roi:String, val roiData:Urn = null, val roiGroup:Integer = 1){
			override def toString = s"${urn}@${roi}. ${roiData} [Group ${roiGroup}]"
	}

	def updateImageCollections = {
		imageCollections.get.clear
		imageExtensions.protocolMap.foreach(k => imageCollections.get += k._1)
	}

	def updateRois(u:Cite2Urn, ve:Vector[String]):Unit = {
			imageROIs.get.clear
			for (r <- ve){
				imageROIs.get += ImageROI(u, r)
			}

	}


	/* This is how to pass data to the global JS scope */
	/*
	js.Dynamic.global.currentImageUrn = "urn:cts"
	js.Dynamic.global.roiArray = Array("one","two","three")
	*/

	val urn1 = Cite2Urn("urn:cite2:hmt:vaimg.v1:VA012RN_0013")
	val label1 = "Venetus A: Marcianus Graecus Z. 454 (= 822), folio 12, recto."
	val urn2 = Cite2Urn("urn:cite2:hmt:vaimg.v1:VA012VN_0514")
	val label2 = "Venetus A: Marcianus Graecus Z. 454 (= 822), folio 12, verso."
	val urn3 = Cite2Urn("urn:cite2:hmt:vaimg.v1:VA013RN_0014")
	val label3 = "Venetus A: Marcianus Graecus Z. 454 (= 822), folio 13, recto."
	val urn4 = Cite2Urn("urn:cite2:hmt:vaimg.v1:VA012RN_0013@0.165,0.2755,0.335,0.0225")
	val label4 = "Venetus A: Marcianus Graecus Z. 454 (= 822), folio 12, recto, highlighting Iliad 1.3."
	val rights = "This image was derived from an original ©2007, Biblioteca Nazionale Marciana, Venezie, Italia. The derivative image is ©2010, Center for Hellenic Studies. Original and derivative are licensed under the Creative Commons Attribution-Noncommercial-Share Alike 3.0 License. The CHS/Marciana Imaging Project was directed by David Jacobs of the British Library."

	val imageRepository = Vector[ImageObject](new ImageObject(urn1,label1,rights),new ImageObject(urn2,label2,rights),new ImageObject(urn3,label3,rights),new ImageObject(urn4,label4,rights))

	imageSearchResults.get.clear
	for (i <- imageRepository){ imageSearchResults.get += i}


	ImageController.changeUrn(ImageModel.imageRepository(0).urn)

}
