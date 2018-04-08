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
import edu.holycross.shot.citerelation._
import edu.holycross.shot.scm._

import monix.execution.Scheduler.Implicits.global
import monix.eval._

import scala.scalajs.js.annotation.JSExport

@JSExportTopLevel("citeapp.RelationsController")
object RelationsController {


	val validUrnInField = Var(false)

	def updateUserMessage(msg: String, alert: Int): Unit = {
		NGModel.userMessageVisibility.value = "app_visible"
		NGModel.userMessage.value = msg
		alert match {
			case 0 => RelationsModel.userAlert.value = "default"
			case 1 => RelationsModel.userAlert.value = "wait"
			case 2 => RelationsModel.userAlert.value = "warn"
		}
		js.timers.clearTimeout(RelationsModel.msgTimer)
		RelationsModel.msgTimer = js.timers.setTimeout(6000){ RelationsModel.userMessageVisibility.value = "app_hidden" }
	}

	def validateUrn(urnString: String): Unit = {
		try{
			urnString.take(8) match {
				case "urn:cts:" => {
					val newUrn:CtsUrn = CtsUrn(urnString)
					validUrnInField.value = true
				}
				case _ => {
					urnString.take(10) match {
						case "urn:cite2:" => {
							val newUrn:Cite2Urn = Cite2Urn(urnString)
							validUrnInField.value = true
						}
					}
				}
			}
		} catch {
			case e: Exception => {
				validUrnInField.value = false
			}
		}
	}

	def changeUrn(urnString:String):Unit = {
		val u:Option[Urn] = {
			urnString.take(8) match {
				case "urn:cts:" => {
					val newUrn:CtsUrn = CtsUrn(urnString)
					Some(newUrn)
				}
				case _ => {
					urnString.take(10) match {
						case "urn:cite2:" => {
							val newUrn:Cite2Urn = Cite2Urn(urnString)
							Some(newUrn)
						}
						case _ => { None }
					}
				}
			}
		}
		RelationsModel.urn.value = u
		findRelations
	}

	def changeUrn(urn:Urn):Unit = {
		RelationsModel.urn.value = Some(urn)
		findRelations
	}

	def findRelations(hi:RelationsModel.HistoryItem):Unit = {
		RelationsModel.urn.value = Some(hi.search)
		RelationsModel.filterVerb.value = hi.filter
		RelationsController.findRelations
	}

	def findRelations:Unit = {
		if ((RelationsModel.urn.value != None) && (RelationsModel.citeRelations.value != None)) {

			val foundRelations:CiteRelationSet = {
				RelationsModel.filterVerb.value match {
					case Some(fv) => {
						val historyItem:RelationsModel.HistoryItem = RelationsModel.HistoryItem(RelationsModel.urn.value.get, Some(fv))
						RelationsModel.updateHistory(historyItem)	
						RelationsModel.citeRelations.value.get.verb(fv) ~~ RelationsModel.urn.value.get
					}
					case _ => {
						val historyItem:RelationsModel.HistoryItem = RelationsModel.HistoryItem(RelationsModel.urn.value.get, None )
						RelationsModel.updateHistory(historyItem)	
						RelationsModel.citeRelations.value.get ~~ RelationsModel.urn.value.get
					}
				}

			}
			//g.console.log(s"found: ${foundRelations.size} out of ${RelationsModel.citeRelations.value.get.size} for ${RelationsModel.urn.value}")	
			// Let's sort by subject URN
			val sortedRelations:Vector[CiteTriple] = foundRelations.relations.toVector.sortBy(_.urn1.toString)
			RelationsModel.foundRelations.value.clear
			for (r <- sortedRelations) {
				RelationsModel.foundRelations.value += r
			}	

		}
	}

	def setFilterVerb(s:String):Unit = {
		try {
			val u:Option[Cite2Urn] = {
				s.take(10) match {
					case "urn:cite2:" => Some(Cite2Urn(s))
					case _ => None
				}
			}
			RelationsModel.filterVerb.value = u
			findRelations
		} catch {
			case e:Exception => {
				RelationsController.updateUserMessage(s"Relations-tab: Unaccountably, something went wrong with the value of the Filter by Relation popup. Could not turn ${s} into a URN: ${e}",2)
			}
		}	
	}

}
