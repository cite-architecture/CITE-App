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


import scala.scalajs.js.annotation.JSExport

@JSExport
object ImageModel {

	var msgTimer:scala.scalajs.js.timers.SetTimeoutHandle = null



  // Image Collections here
	var imgUseLocal = Var(true) 
	var imgArchivePath:String = ""
	var imageExtensions:Option[ImageExtensions] = None
	val imageCollections = Vars.empty[Cite2Urn]

	// urn is what the user requested
	val urn = Var[Option[Cite2Urn]](None)
	// displayUrn is what will be shown
	val displayUrn = Var[Option[Cite2Urn]](None)
	val versionsForCurrentUrn = Var(1)

	//We might have zero+ ROIs on this image
	val imageROIs = Vars.empty[ImageROI]

	val userMessage = Var("")
	val userAlert = Var("default")
	val userMessageVisibility = Var("app_hidden")

 	case class ImageObject(val urn:Cite2Urn, val label:String, val rights:String) {
			override def toString = s"${urn} - ${label}. ${rights}"
	}

	// An ImageROI object must have an Image URN. It may have a defined ROI, a mapped URN, and a group.
	// If it `roi` = None, then any mapping applies to the whole image.
	case class ImageROI(val index:Int, val urn:Cite2Urn, val roi:Option[String], val roiData:Option[Urn] = None, val roiGroup:Integer = 1){
			override def toString = s"${urn}@${roi}. ${roiData} [Group ${roiGroup}]"
	}

	def idForMappedUrn(i:Int):String = {
		s"image_mappedUrn_${i}"
	}
	def idForMappedROI(i:Int):String = {
		s"image_mappedROI_${i}"
	}

	def updateImageCollections = {
		imageCollections.get.clear
		imageExtensions match {
				case Some(ie) => ie.protocolMap.foreach(k => imageCollections.get += k._1)
				case _ => ;
		}
	}

  // Eventually, do something clever to assign groups to ROIs based on their mapped URNs
	def updateRois(u:Cite2Urn, roiVector:Vector[(Option[String],Option[Urn])]):Unit = {
			imageROIs.get.clear
			for ((r, i) <- roiVector.zipWithIndex){
				// For now we're putting each ROI in its own group
				r._1 match {
					case Some(s) => {
						imageROIs.get += ImageROI(i+1, u, r._1, r._2, (i+1))
					}
					case _ => {
						g.console.log(s"no roi for ${roiVector}")
					}
				}
			}
			// FOR TESTING ONLY! REMOVE BEFORE FLIGHT!!
			/*
		imageROIs.get += ImageROI(roiVector.size+1,Cite2Urn("urn:cite2:hmt:vaimg.v1:VA012RN_0013"),Some("0,0,0.25,0.25"),Some(CtsUrn("urn:cts:greekLit:tlg0012.tlg001.allen:2.1")),roiVector.size+1)
		imageROIs.get += ImageROI(roiVector.size+2,Cite2Urn("urn:cite2:hmt:vaimg.v1:VA012RN_0013"),Some("0.75,0.75,0.25,0.25"),Some(CtsUrn("urn:cts:greekLit:tlg0012.tlg001.allen:2.2")),roiVector.size+2)
			*/
	}


	/* This is how to pass data to the global JS scope */
	/*
	js.Dynamic.global.currentImageUrn = "urn:cts"
	js.Dynamic.global.roiArray = Array("one","two","three")
	*/

    /* Stuff for initial testing, remove when this all works */
	/*
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

	ImageController.changeUrn(ImageModel.imageRepository(0).urn)
	*/

}
