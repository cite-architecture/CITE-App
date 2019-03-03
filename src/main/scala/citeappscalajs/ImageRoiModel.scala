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

@JSExportTopLevel("ImageRoiModel")
object ImageRoiModel {

	case class ImageRoi(imageUrn:Cite2Urn, rois:Vars[Roi]) {
		override def toString = s"Image: ${imageUrn}. ${rois}"
	}

 	case class Roi(val l:Float, t:Float, w:Float, h:Float, dataUrn:Option[Urn] = None, contextUrn:Option[Cite2Urn] = None ) {
			override def toString = s"left:${l}, top:${t}, width:${w}, height:${h} => ${dataUrn}"
			def toSubrefString:String = {
				s"${l},${t},${w},${h}"
			}
	}

	def roiFromUrn(u:Cite2Urn, data:Option[Urn] = None, context:Option[Cite2Urn] = None):Option[Roi] = {
		// in this case, there will be only one Roi object in the ImageRoi
		try {
			u.objectExtensionOption match {
				case Some(oe) => {
					val parts = oe.split(",")
					if (parts.size != 4) throw new Exception(s"${oe} did not contain four, comma-separated values.")
					val l:Float = parts(0).toFloat
					val t:Float = parts(1).toFloat
					val w:Float = parts(2).toFloat
					val h:Float = parts(3).toFloat
					if ( (l <0) | ( l > 1) ) throw new Exception(s"${l} must be > 0 and < 1.")
					if ( (t <0) | ( t > 1) ) throw new Exception(s"${t} must be > 0 and < 1.")
					if ( (w <0) | ( w > 1) ) throw new Exception(s"${w} must be > 0 and < 1.")
					if ( (h <0) | ( h > 1) ) throw new Exception(s"${h} must be > 0 and < 1.")
					// Got here and we're good
					val roi:Roi = Roi(l,t,w,h,data, context)
					Some(roi)
				}
				case None => None
			}
		} catch {
			case e:Exception => throw new Exception(s"Unable to make ROI from ${u}. ${e}")
		}
	}	
}
