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
import js.annotation._

/* 
	Defines a model for dealing with objects that implement the
	CiteBinaryImage datamodel. 
*/

@JSExportTopLevel("citeapp.CiteBinaryImageModel")
object CiteBinaryImageModel {

	// URNs for implemented Image models
	val binaryImgModelUrn:Cite2Urn = Cite2Urn("urn:cite2:cite:datamodels.v1:binaryimg")

	var msgTimer:scala.scalajs.js.timers.SetTimeoutHandle = null

	// any binary image implemented?
	val hasBinaryImages = Var[Boolean](false)	
	val binaryImageCollections = Var[Option[Vector[Cite2Urn]]](None)

	// which protocols are implemented in this CEX?
	val hasIiifApi = Var[Boolean](false)
	val hasLocalDeepZoom = Var[Boolean](false)

	// urn is what the user requested
	val urn = Var[Option[Cite2Urn]](None)

	// An ImageROI object associates an roi with a urn; 
	// our image may have none, one, or many
	val imageROIs = Var[Option[Vector[ImageROI]]](None)

	// User Interface stuff
	val userMessage = Var("")
	val userAlert = Var("default")
	val userMessageVisibility = Var("app_hidden")

	val imgUseLocal = Var[Boolean](true)

	/* 
	An ImageROI object must have an Image URN. It may have a defined ROI, a mapped URN, and a group.
	If it `roi` = None, then any mapping applies to the whole image.
	*/
	case class ImageROI(val urn:Cite2Urn, val roi:Option[String], val roiData:Option[Urn] = None, val roiGroup:Integer = 1){
			override def toString = s"${urn}@${roi}. ${roiData} [Group ${roiGroup}]"
	}

	/* This is how to pass data to the global JS scope */
	/*
	js.Dynamic.global.currentImageUrn = "urn:cts"
	js.Dynamic.global.roiArray = Array("one","two","three")
	*/

}
