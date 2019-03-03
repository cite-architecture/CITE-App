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

@JSExportTopLevel("CitableImageModel")
object CitableImageModel {

 	case class ImageObject(val urn:Cite2Urn, val caption:String, val rights:String) {
			override def toString = s"${urn} - ${caption}. ${rights}"
	}
	
}
