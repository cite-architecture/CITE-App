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
import edu.holycross.shot.scm._
import edu.holycross.shot.citebinaryimage._

import scala.scalajs.js.annotation.JSExport
import js.annotation._

/* 
	Defines a model for dealing with objects that implement the
	CiteBinaryImage datamodel. 
*/

@JSExportTopLevel("citeapp.CiteBinaryImageModel")
object CiteBinaryImageModel {

	// URNs for implemented Image models
	val binaryImageModelUrn:Cite2Urn = Cite2Urn("urn:cite2:cite:datamodels.v1:binaryimg")
	val protocolPropertyName:String = "protocol"
	val iiifApiProtocolString:String = "iiifApi"
	val localDZProtocolString:String = "localDeepZoom"
	val iipDZProtocolString:String = "iipDeepZoom"
	val jpgProtocolString:String = "JPG"


	// this is changed by the user using the local/remote switch	
	val imgUseLocal = Var[Boolean](true)

	// this is set at app init
	val imgArchivePath = Var[String]("")


	// To save everyone time, is *any* collection in the current CEX
	// supported for local viewing?
	val hasLocalOption = Var[Boolean](false)
	// To save everyone time, is *any* collection in the current CEX
	// supported for remote viewing?
	val hasRemoteOption = Var[Boolean](false)


	// any binary image implemented?
	val hasBinaryImages = Var[Boolean](false)	
	val binaryImageCollections = Vars.empty[Cite2Urn]

	// which protocols are implemented in this CEX?
	/*
	val hasIiifApi = Var[Boolean](false)
	val hasLocalDeepZoom = Var[Boolean](false)
	val hasJPG = Var[Boolean](false)
	val hasIipDZ = Var[Boolean](false)
	*/

	// urn is what the user requested
	val urn = Var[Option[Cite2Urn]](None)

	/* If a user requests a single URN with an ROI, preview that. But if
		we're doing some fancy data model stuff, we might want to show
		the whole image in the preview. So we separate the current URN from
		the current Preview Urn */
	val previewUrn = Var[Option[Cite2Urn]](None)

	// An ImageROI object associates an roi with a urn; 
	// our image may have none, one, or many
	val imageRoiTuple = Vars.empty[(Int,ImageRoiModel.Roi)]
	// This splits imageRoiTuple up into groups
	val imageRoiGroups = Var[Option[Map[String,Int]]](None)
	// And for convenience, and making Binding happen, another view of the roiGroups…
	val imageRoiGroupSeq = Vars.empty[(String,Int)]

	val currentContextUrn = Var[Option[Urn]](None)


	// User Interface stuff
	val userMessage = Var("")
	val userAlert = Var("default")
	val userMessageVisibility = Var("app_hidden")
	// for displaying and hiding user messages
	var msgTimer:scala.scalajs.js.timers.SetTimeoutHandle = null
	val thumbnailMaxWidth:Int = 400

	// Current info on image displayed	
	val displayUrn = Var[Option[Cite2Urn]](None)
	// Do we use this?
	val versionsForCurrentUrn = Var(1)

	def clearROIs:Unit = {
		imageRoiTuple.value.clear
		imageRoiGroups.value = None
		imageRoiGroupSeq.value.clear
	}

	def loadROIs(rois:Vector[ImageRoiModel.Roi]):Unit = {
		clearROIs
		for ( (roi,i) <- rois.zipWithIndex){
			val t:(Int,ImageRoiModel.Roi) = (i, roi)
			CiteBinaryImageModel.imageRoiTuple.value += t
		}
		imageRoiGroups.value = CiteBinaryImageController.groupsForROIs(rois)
		imageRoiGroups.value match {
			case Some(irg) => {
				irg.toVector.foreach( g => {
					imageRoiGroupSeq.value += g
				})
			}	
			case None => imageRoiGroupSeq.value.clear
		}

	}

	
	def addToROIs(roi:ImageRoiModel.Roi):Unit = {
			val i = imageRoiTuple.value.size
			val t:(Int,ImageRoiModel.Roi) = (i, roi)
			CiteBinaryImageModel.imageRoiTuple.value += t
		   val roiPreVec =  CiteBinaryImageModel.imageRoiTuple.value.map(_._2).toVector
		   imageRoiGroups.value = None // Clear it out
		   imageRoiGroups.value = CiteBinaryImageController.groupsForROIs(roiPreVec)
	}

	def imageRoisToOptionVector:Option[Vector[ImageRoiModel.Roi]] = {
		imageRoiTuple.value.size match {
			case s if (s == 1) => None 
			case _ => {
				val roiVec:Vector[ImageRoiModel.Roi] = imageRoiTuple.value.map( r => r._2).toVector
				Some(roiVec)
			}
		}	
	}

	/* This is how to pass data to the global JS scope */
	/*
	js.Dynamic.global.currentImageUrn = "urn:cts"
	js.Dynamic.global.roiArray = Array("one","two","three")
	*/

}
