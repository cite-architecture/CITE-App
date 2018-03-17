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
	val iiifProtocolString:String = "iiifApi"
	val dzProtocolString:String = "localDeepZoom"

	var msgTimer:scala.scalajs.js.timers.SetTimeoutHandle = null

	// any binary image implemented?
	val hasBinaryImages = Var[Boolean](false)	
	val binaryImageCollections = Vars.empty[Cite2Urn]

	// which protocols are implemented in this CEX?
	val hasIiifApi = Var[Boolean](false)
	val hasLocalDeepZoom = Var[Boolean](false)

	// urn is what the user requested
	val urn = Var[Option[Cite2Urn]](None)

	// An ImageROI object associates an roi with a urn; 
	// our image may have none, one, or many
	val imageROIs = Var[Option[ImageRoiModel.ImageRoi]](None)

	// User Interface stuff
	val userMessage = Var("")
	val userAlert = Var("default")
	val userMessageVisibility = Var("app_hidden")

	val imgUseLocal = Var[Boolean](true)

	val imgArchivePath = Var[String]("")

	val thumbnailMaxWidth:Int = 400

	// Current info on image displayed	
	val displayUrn = Var[Option[Cite2Urn]](None)
	val versionsForCurrentUrn = Var(1)

	def updateRois(u:Cite2Urn, roiObject:Option[ImageRoiModel.ImageRoi] = None):Unit = {
			imageROIs.value = roiObject
	}


	/* This is how to pass data to the global JS scope */
	/*
	js.Dynamic.global.currentImageUrn = "urn:cts"
	js.Dynamic.global.roiArray = Array("one","two","three")
	*/

}
