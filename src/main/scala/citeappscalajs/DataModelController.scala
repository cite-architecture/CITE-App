package citeapp

import com.thoughtworks.binding.{Binding, dom}
import com.thoughtworks.binding.Binding.{BindingSeq, Var, Vars}
import scala.scalajs.js
import scala.scalajs.js._
import js.annotation._
import scala.concurrent._
//import ExecutionContext.Implicits.global
import collection.mutable
import collection.mutable._
import scala.scalajs.js.Dynamic.{ global => g }
import org.scalajs.dom._
import org.scalajs.dom.ext._
import org.scalajs.dom.raw._
import edu.holycross.shot.cite._
import edu.holycross.shot.ohco2._
import edu.holycross.shot.citeobj._
import edu.holycross.shot.scm._

import monix.execution.Scheduler.Implicits.global
import monix.eval._

import scala.scalajs.js.annotation.JSExport

@JSExportTopLevel("citeapp.DataModelController")
object DataModelController {

	// Checks to see if a text is present in the currently loaded library
	// Will match with ~~ similarity
	// *** ALERT *** for testing, I'm turning all of these into work-level URNs.
	def hasText(u:CtsUrn):Boolean = {
		O2Model.textRepo.value match {
			case Some(tr) => {
				tr.catalog.texts.filter(_.urn ~~ u.dropPassage.dropVersion).size match {
					case 0 => false
					case _ => true
				}
			}
			case None => {
				false		
			}
		}
	}

	def hasObject(u:Cite2Urn):Boolean = {
		val urn:Cite2Urn = {
			val tempU:Cite2Urn = u.dropExtensions	
			tempU.isRange match {
				case true => tempU.rangeBeginUrn
				case _ => tempU
			}
		}
		ObjectModel.collRep.value match {
			case Some(cr) => {
				cr.citableObjects.filter(_.urn == urn).size match {
					case 1 => true
					case _ => false
				}	
			}
			case None => {
				false		
			}
		}	
	}

	def retrieveTextPassage(urn:CtsUrn):Unit = {
			O2Controller.changeUrn(urn)
			js.Dynamic.global.document.getElementById("tab-1").checked = true
	}


	def retrieveObject(contextUrn:Option[Cite2Urn] = None, urn:Cite2Urn):Unit = {
			val tempUrn:Cite2Urn = urn.dropExtensions
			ObjectModel.urn.value = Some(tempUrn)
			ObjectModel.displayUrn.value = Some(urn)
			ObjectController.changeObject
			js.Dynamic.global.document.getElementById("tab-3").checked = true
	}


}
