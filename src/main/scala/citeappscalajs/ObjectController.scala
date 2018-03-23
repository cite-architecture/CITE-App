package citeapp

import com.thoughtworks.binding.{Binding, dom}
import com.thoughtworks.binding.Binding.{BindingSeq, Var, Vars}
import scala.scalajs.js
import scala.scalajs.js._
import js.annotation._
import collection.mutable
import collection.mutable._
import scala.scalajs.js.Dynamic.{ global => g }
import org.scalajs.dom._
import org.scalajs.dom.ext._
import org.scalajs.dom.raw._
import edu.holycross.shot.cite._
import edu.holycross.shot.ohco2._
import edu.holycross.shot.citeobj._
import scala.concurrent._
//import ExecutionContext.Implicits.global
import monix.execution.Scheduler.Implicits.global
import monix.eval._

import scala.scalajs.js.annotation.JSExport

@JSExportTopLevel("citeapp.ObjectController")
object ObjectController {


	// Based on the UI toggle, sets showObject.
	//     true -> show each object and all its properties
	//     false -> show URN and label only
	@dom
	def switchDisplay(thisEvent: Event):Unit = {
		val before = ObjectModel.showObjects.value
		val showObjectsStr:String = js.Dynamic.global.document.getElementById("object_browseOrListSwitch").checked.toString
		ObjectModel.showObjects.value = (showObjectsStr == "true")
		ObjectController.setDisplay
	}


	def objectIsPresent(u:Cite2Urn):Boolean = {
		val tempU:Cite2Urn = u.dropExtensions
		if (ObjectModel.collRep.value.get.citableObjects.filter(_.urn == tempU).size > 0){
			true
		} else { false }

	}

	def labelForCollection(u:Cite2Urn):String = {
		val collUrn = u.dropSelector
		ObjectModel.collRep.value.get.collectionDefinition(collUrn) match {
			case Some(cd) => {
				cd.collectionLabel
			}
			case _ => {
				s"Collection ${collUrn} is not represented in this repository."
			}
		}
	}

	def updateUserMessage(msg: String, alert: Int): Unit = {
		ObjectModel.userMessageVisibility.value = "app_visible"
		ObjectModel.userMessage.value = msg
		alert match {
			case 0 => ObjectModel.userAlert.value = "default"
			case 1 => ObjectModel.userAlert.value = "wait"
			case 2 => ObjectModel.userAlert.value = "warn"
		}
		js.timers.clearTimeout(ObjectModel.msgTimer)
		ObjectModel.msgTimer = js.timers.setTimeout(6000){ ObjectModel.userMessageVisibility.value = "app_hidden" }
	}

	// On key-up, and on submit, checks for valid Cite2Urn, and decides
	// whether the request is for an object, or a range, or nothing
	def validateUrn(urnString: String): Unit = {
		try{
			val newUrn: Cite2Urn = Cite2Urn(urnString)
			newUrn.objectComponentOption match {
				case Some(o) => {
					newUrn.rangeBeginOption match {
						case Some(rb) => {
								newUrn.rangeEndOption match {
									case Some (re) => {
										ObjectModel.objectOrCollection.value = "range"
									}
									case _ => {
										ObjectModel.objectOrCollection.value = "none"
									}
								}
						}
						case _ => {
							ObjectModel.objectOrCollection.value = "object"
						}
					}
				}
				case _ => {
					ObjectModel.objectOrCollection.value = "collection"
				}
			}
		} catch {
			case e: Exception => {
				ObjectModel.objectOrCollection.value = "none"
			}
		}
	}

  // The sequence goes like this:
	//    - [getPrev or getNext]
	//    - changeUrn
	//    - changeObject
	def changeObject:Unit = {
		val tempUrn:Cite2Urn = ObjectModel.urn.value.get
		//g.console.log(s"changeObject got: ${tempUrn}")
		ObjectModel.clearObject
		QueryObjectModel.clearAll
		ObjectModel.urn.value = Some(tempUrn)
		val collUrn = ObjectModel.urn.value.get.dropSelector

		// Based on the new URN, set image, ordered, browsable flags
		ObjectModel.isOrdered.value = ObjectModel.collRep.value.get.isOrdered(collUrn)

		if (
				(ObjectModel.objectOrCollection.value == "collection") ||
				(ObjectModel.urn.value.get.isRange == true) ||
				(ObjectModel.isOrdered.value == true) ||
				(ObjectModel.urn.value.get.objectOption == None)
			){
			  	ObjectModel.browsable.value = true
			} else { ObjectModel.browsable.value = false }

			ObjectModel.objectOrCollection.value match {
					case "object" => {
						//g.console.log(s"Doing get [object]… ${ObjectModel.urn.value} =? ${tempUrn}")
						ObjectModel.getObjects(tempUrn)
					}
					case "collection" =>{
						//g.console.log(s"Doing get [collection]… ${ObjectModel.urn.value} =? ${tempUrn}")
						//g.console.log(s"current urn: ${ObjectModel.urn.value}")
						//g.console.log(s"current display-urn: ${ObjectModel.displayUrn.value}")
						ObjectModel.getObjects(tempUrn)
					}
					case "range" =>{
						//g.console.log(s"Doing get [range]… ${ObjectModel.urn.value} =? ${tempUrn}")
						ObjectModel.getObjects(tempUrn)
					}
					case _ => {
						//g.console.log("Nothing to change.")
					}
			}
		ObjectController.setDisplay
	}

	def changeUrn(urnString: String): Unit = {
		changeUrn(Cite2Urn(urnString))
	}

	def changeUrn(urn: Cite2Urn): Unit = {
		try {
			ObjectModel.urn.value = Some(urn)
			val collUrn = urn.dropSelector
			ObjectModel.displayUrn.value = Some(urn)
			ObjectModel.urn.value.get.objectComponentOption match {
				case Some(o) => {
					// test for range
					ObjectModel.urn.value.get.rangeBeginOption match {
						case Some(rb) => {
							ObjectModel.urn.value.get.rangeEndOption match {
								case Some(re) => {
									ObjectModel.objectOrCollection.value = "range"
									ObjectModel.isOrdered.value = ObjectModel.collRep.value.get.isOrdered(collUrn)
									ObjectController.updateUserMessage("Retrieving range…",1)
								}
								case _ => {
									ObjectModel.objectOrCollection.value = "none"
									ObjectModel.isOrdered.value = false
								}
							}
						}
						// if not a range, it is an object
						case _ =>{
							ObjectModel.objectOrCollection.value = "object"
							ObjectModel.isOrdered.value = ObjectModel.collRep.value.get.isOrdered(collUrn)
							ObjectController.updateUserMessage("Retrieving object…",1)
						}
					}
				}
				// otherwise, this is a collection
				case _ => {
					ObjectModel.objectOrCollection.value = "collection"
					ObjectModel.isOrdered.value = ObjectModel.collRep.value.get.isOrdered(collUrn)
					ObjectController.updateUserMessage("Retrieving collection…",1)
				}
			}
			if (ObjectModel.objectOrCollection.value != "none") {
				val task = Task{ ObjectController.changeObject }
				val future = task.runAsync
				/*
				js.timers.setTimeout(200){
					Future{ ObjectController.changeObject }
				}
				*/
			}
		} catch {
			case e: Exception => {
				ObjectModel.objectOrCollection.value = "none"
				ObjectModel.isOrdered.value = false
				updateUserMessage("Invalid URN. Current URN not changed.",2)
			}
		}
	}

	def preloadUrn:Unit = {
			// get first collection in catalog
			if (ObjectModel.collections.value.size > 0){
					val urn = ObjectModel.collections.value(0).urn
					insertFirstObjectUrn(urn)
			} else {

			}
			//g.console.log("preloading URN")
	}

	@dom
	def clearResults = {
			//g.console.log("Clearing results")
	}


	@dom
	def clearHistory = {
			//g.console.log("Clearing history")
	}

	def insertFirstObjectUrn(urn: Cite2Urn): Unit = {
		//ObjectModel.clearObject
		//QueryObjectModel.clearAll
		val firstUrn:Cite2Urn = ObjectModel.collRep.value.get.objectsForCollection(urn)(0).urn
		//js.Dynamic.global.document.getElementById("object_urnInput").value = firstUrn.toString
		ObjectModel.urn.value = Some(firstUrn)
		ObjectModel.objectOrCollection.value = "object"
		document.getElementById("object_urnInput").asInstanceOf[HTMLInputElement].value = firstUrn.toString
	}

	@dom
	def getNext:Unit = {
		//g.console.log(s"currentNext: ${ObjectModel.currentNext.value.toString}")
		ObjectModel.currentNext.value match {
			case Some(u) => {
				val no:Int = u._2
				val nl:Int = u._3
				ObjectModel.objectOrCollection.value match {
					case "object" => {
						u._1 match {
							case Some(cu) => ObjectController.changeUrn(cu)
							case _ => ObjectController.updateUserMessage("The URN for the next object is None. This is an error. Please file an issue on GitHub.",2)
						}
					}
					case "none" => {
						ObjectController.updateUserMessage("There is no object. getNext should not have been called. Please file an issue on GitHub.",2)
					}
					// range, search results, or paged collection
					case _ => {
						ObjectModel.limit.value = nl
						ObjectModel.offset.value = no
						ObjectController.setDisplay
					}
				}
			}
			case _ => {
					ObjectController.updateUserMessage("There is no next object. getNext should not have been called. Please file an issue on GitHub.",2)
			}
		}
	}

	@dom
	def getPrev:Unit = {
		//g.console.log(s"currentPrev: ${ObjectModel.currentPrev.value.toString}")
		ObjectModel.currentPrev.value match {
			case Some(u) => {
				val no:Int = u._2
				val nl:Int = u._3
				ObjectModel.objectOrCollection.value match {
					case "object" => {
						u._1 match {
							case Some(cu) => ObjectController.changeUrn(cu)
							case _ => ObjectController.updateUserMessage("The URN for the previous object is None. This is an error. Please file an issue on GitHub.",2)
						}
					}
					case "none" => {
						ObjectController.updateUserMessage("There is no object. getPrev should not have been called",2)
					}
					case _ => {
						ObjectModel.limit.value = nl
						ObjectModel.offset.value = no
						ObjectController.setDisplay
					}
				}
			}
			case _ => {
					ObjectController.updateUserMessage("There is no previous object. getPrev should not have been called",2)
			}
		}
	}

	// Sets the display to a [possible] subset of the current objects
	@dom
	def setDisplay:Unit = {
		val numObj:Int = ObjectModel.boundObjects.value.size
		val tLim:Int = ObjectModel.limit.value
		val tOff:Int = ObjectModel.offset.value
		val startIndex:Int = tOff - 1
		val endIndex:Int = {
			if ( (tOff + tLim - 1)  >= numObj ) {
				(numObj - 1)
			} else {
				((tOff - 1) + (tLim - 1))
			}
		}
		ObjectModel.objectOrCollection.value match {
			case "object" => {
				ObjectModel.boundDisplayObjects.value.clear
				// Here we need to send off to construct displayObjects that are bound
				ObjectModel.boundDisplayObjects.value += ObjectModel.constructBoundDisplayObject(ObjectModel.boundObjects.value(0))

				ObjectModel.updatePrevNext
				ObjectController.updateReport
			}
			case "search" => {
							ObjectModel.boundDisplayObjects.value.clear
							for (i <- startIndex to endIndex){
									ObjectModel.boundDisplayObjects.value += ObjectModel.constructBoundDisplayObject(ObjectModel.boundObjects.value(i))
								}
								ObjectModel.updatePrevNext
			}
			case _ => {
				try {
							val collUrn:Cite2Urn = ObjectModel.urn.value.get.dropSelector
							if (tOff > numObj){
								ObjectController.updateUserMessage(s"There are ${numObj} objects in the requested ${ObjectModel.objectOrCollection.value}, so an offset of ${tOff} is invalid.",2)
							} else {
								ObjectModel.boundDisplayObjects.value.clear
								for (i <- startIndex to endIndex){
									ObjectModel.boundDisplayObjects.value += ObjectModel.constructBoundDisplayObject(ObjectModel.collRep.value.get.objectsForCollection(collUrn)(i))
								}
								ObjectModel.updatePrevNext
							}
				} catch {
					case e: Exception => {
						ObjectController.updateUserMessage(s"Failed on setDisplay. ${e}",2 )
					}

				}
			}
		}
	}

	def updateReport:Unit = {
		val collUrn:Cite2Urn = ObjectModel.urn.value.get.dropSelector
		val collLabel:String = ObjectModel.collRep.value.get.collectionDefinition(collUrn).get.collectionLabel
		val n:Int = ObjectModel.boundDisplayObjects.value.size
		val total:Int = ObjectModel.collRep.value.get.collectionData(collUrn).objects.size
		val report = s"Showing ${n} out of ${total} objects in collection: ${collLabel} [${collUrn}]."
		ObjectModel.objectReport.value = report
	}


	/* I don't know how to do anything equivalent to "this" when passing events
	in ScalaJS. So this will cover all numeric fields. */
	def validateNumericEntry(thisEvent: Event):Unit = {
		val oldOffset:Int = ObjectModel.offset.value
		val oldLimit:Int = ObjectModel.limit.value
		val thisTarget = thisEvent.target.asInstanceOf[org.scalajs.dom.raw.HTMLInputElement]
		val targetId = thisTarget.id
		val testText = thisTarget.value.toString
		try{
			val mo: Int = testText.toInt
			targetId match {
				case "object_browseOffset" => ObjectModel.offset.value = mo
				case "object_browseLimit" => ObjectModel.limit.value = mo
			}
		} catch {
			case e: Exception => {
				val badMo: String = testText
				ObjectModel.offset.value = oldOffset
				ObjectModel.limit.value = oldLimit
				targetId match {
						case "object_browseOffset" => {
							ObjectController.updateUserMessage(s"Offset value must be an integer. '${badMo}' is not an integer.", 2)
							thisTarget.value =  ObjectModel.offset.value.toString
						}
						case "object_browseLimit" => {
							ObjectController.updateUserMessage(s"Limit value must be an integer. '${badMo}' is not an integer.", 2)
							thisTarget.value =  ObjectModel.limit.value.toString
						}
				}

			}
		}
	}

 @JSExport
	def propertyUrnClick(urnSt:String) = {
		  // below is how you invoke a cofirmation dialog
			// val cc = window.confirm("Hi")
			//CiteLinks.switch(urnSt)
	}


}
