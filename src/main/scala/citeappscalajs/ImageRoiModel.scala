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
	Defines a model for Image ROIs, defined rectangles on citable images, associated with 
	Cite and CTS Urns.
*/

@JSExportTopLevel("citeapp.ImageRoiModel")
object ImageRoiModel {

	case class ImageRoi(imageUrn:Cite2Urn, rois:Vector[Roi]) {
		override def toString = s"Image: ${imageUrn}. ${rois}"
	}

 	case class Roi(val l:Float, t:Float, w:Float, h:Float, dataUrn:Option[Urn] = None ) {
			override def toString = s"left:${l}, top:${t}, width:${w}, height:${h} => ${dataUrn}"
	}
	
}
