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

import scala.scalajs.js.annotation.JSExport

@JSExport
object ObjectController {


	// Based on the UI toggle, sets showObject.
	//     true -> show each object and all its properties
	//     false -> show URN and label only
	@dom
	def switchDisplay(thisEvent: Event):Unit = {
		val before = ObjectModel.showObjects.get
		val showObjectsStr:String = js.Dynamic.global.document.getElementById("browse_onoffswitch").checked.toString
		ObjectModel.showObjects := (showObjectsStr == "true")
		ObjectController.setDisplay
	}

	def objectIsPresent(u:Cite2Urn):Boolean = {
		val tempU:Cite2Urn = u.dropExtensions
		if (ObjectModel.collectionRepository.citableObjects.filter(_.urn == tempU).size > 0){
			true
		} else { false }

	}

	def labelForCollection(u:Cite2Urn):String = {
		val collUrn = u.dropSelector
		ObjectModel.collectionRepository.collectionDefinition(collUrn) match {
			case Some(cd) => {
				cd.collectionLabel
			}
			case _ => {
				s"Collection ${collUrn} is not represented in this repository."
			}
		}
	}

	def updateUserMessage(msg: String, alert: Int): Unit = {
		ObjectModel.userMessageVisibility := "app_visible"
		ObjectModel.userMessage := msg
		alert match {
			case 0 => ObjectModel.userAlert := "default"
			case 1 => ObjectModel.userAlert := "wait"
			case 2 => ObjectModel.userAlert := "warn"
		}
		js.timers.clearTimeout(ObjectModel.msgTimer)
		ObjectModel.msgTimer = js.timers.setTimeout(6000){ ObjectModel.userMessageVisibility := "app_hidden" }
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
										ObjectModel.objectOrCollection := "range"
									}
									case _ => {
										ObjectModel.objectOrCollection := "none"
									}
								}
						}
						case _ => {
							ObjectModel.objectOrCollection := "object"
						}
					}
				}
				case _ => {
					ObjectModel.objectOrCollection := "collection"
				}
			}
		} catch {
			case e: Exception => {
				ObjectModel.objectOrCollection := "none"
			}
		}
	}

  // The sequence goes like this:
	//    - [getPrev or getNext]
	//    - changeUrn
	//    - changeObject
	def changeObject:Unit = {
		val tempUrn:Cite2Urn = ObjectModel.urn.get.get
		ObjectModel.clearObject
		QueryObjectModel.clearAll
		ObjectModel.urn := Some(tempUrn)
		val collUrn = ObjectModel.urn.get.get.dropSelector

		// Based on the new URN, set image, ordered, browsable flags
		ObjectModel.isOrdered := ObjectModel.collectionRepository.isOrdered(collUrn)

		if (
				(ObjectModel.objectOrCollection.get == true) ||
				(ObjectModel.urn.get.get.isRange == true) ||
				(ObjectModel.isOrdered.get == true) ||
				(ObjectModel.urn.get.get.objectOption == None)
			){
			  	ObjectModel.browsable := true
			} else { ObjectModel.browsable := false }

			ObjectModel.objectOrCollection.get match {
					case "object" => {
						//g.console.log(s"Doing get objects… ${ObjectModel.urn.get}")
						ObjectModel.getObjects(tempUrn)
					}
					case "collection" =>{
						ObjectModel.getObjects(tempUrn)
					}
					case "range" =>{
						ObjectModel.getObjects(tempUrn)
					}
					case _ => {
						g.console.log("Nothing to change.")
					}
			}
		ObjectController.setDisplay
	}

	def changeUrn(urnString: String): Unit = {
		changeUrn(Cite2Urn(urnString))
	}

	def changeUrn(urn: Cite2Urn): Unit = {
		try {
			ObjectModel.urn := Some(urn)
			val collUrn = urn.dropSelector
			ObjectModel.displayUrn := Some(urn)
			ObjectModel.urn.get.get.objectComponentOption match {
				case Some(o) => {
					// test for range
					ObjectModel.urn.get.get.rangeBeginOption match {
						case Some(rb) => {
							ObjectModel.urn.get.get.rangeEndOption match {
								case Some(re) => {
									ObjectModel.objectOrCollection := "range"
									ObjectModel.isOrdered := ObjectModel.collectionRepository.isOrdered(collUrn)
									ObjectController.updateUserMessage("Retrieving range…",1)
								}
								case _ => {
									ObjectModel.objectOrCollection := "none"
									ObjectModel.isOrdered := false
								}
							}
						}
						// if not a range, it is an object
						case _ =>{
							ObjectModel.objectOrCollection := "object"
							ObjectModel.isOrdered := ObjectModel.collectionRepository.isOrdered(collUrn)
							ObjectController.updateUserMessage("Retrieving object…",1)
						}
					}
				}
				// otherwise, this is a collection
				case _ => {
					ObjectModel.objectOrCollection := "collection"
					ObjectModel.isOrdered := ObjectModel.collectionRepository.isOrdered(collUrn)
					ObjectController.updateUserMessage("Retrieving collection…",1)
				}
			}
			if (ObjectModel.objectOrCollection.get != "none") {
				js.timers.setTimeout(500){ ObjectController.changeObject }
			}
		} catch {
			case e: Exception => {
				ObjectModel.objectOrCollection := "none"
				ObjectModel.isOrdered := false
				updateUserMessage("Invalid URN. Current URN not changed.",2)
			}
		}
	}

	def preloadUrn:Unit = {
			// get first collection in catalog
			if (ObjectModel.collections.get.size > 0){
					val urn = ObjectModel.collections.get(0).urn
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
		ObjectModel.clearObject
		QueryObjectModel.clearAll
		val firstUrn:Cite2Urn = ObjectModel.collectionRepository.citableObjects(urn)(0).urn

		//js.Dynamic.global.document.getElementById("object_urnInput").value = firstUrn.toString
		ObjectModel.urn := Some(firstUrn)
		ObjectModel.objectOrCollection := "object"
	}

	@dom
	def getNext:Unit = {
		ObjectModel.currentNext.get match {
			case Some(u) => {
				val no:Int = u._2
				val nl:Int = u._3
				ObjectModel.objectOrCollection.get match {
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
						ObjectModel.limit := nl
						ObjectModel.offset := no
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
		ObjectModel.currentPrev.get match {
			case Some(u) => {
				val no:Int = u._2
				val nl:Int = u._3
				ObjectModel.objectOrCollection.get match {
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
						ObjectModel.limit := nl
						ObjectModel.offset := no
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
		val numObj:Int = ObjectModel.boundObjects.get.size
		val tLim:Int = ObjectModel.limit.get
		val tOff:Int = ObjectModel.offset.get
		val startIndex:Int = tOff - 1
		val endIndex:Int = {
			if ( (tOff + tLim - 1)  >= numObj ) {
				(numObj - 1)
			} else {
				((tOff - 1) + (tLim - 1))
			}
		}
		ObjectModel.objectOrCollection.get match {
			case "object" => {
				ObjectModel.boundDisplayObjects.get.clear
				// Here we need to send off to construct displayObjects that are bound
				ObjectModel.boundDisplayObjects.get += ObjectModel.constructBoundDisplayObject(ObjectModel.boundObjects.get(0))

				ObjectModel.updatePrevNext
				ObjectController.updateReport
			}
			case "search" => {
							ObjectModel.boundDisplayObjects.get.clear
							for (i <- startIndex to endIndex){
									ObjectModel.boundDisplayObjects.get += ObjectModel.constructBoundDisplayObject(ObjectModel.boundObjects.get(i))
								}
								ObjectModel.updatePrevNext
			}
			case _ => {
				try {
							val collUrn:Cite2Urn = ObjectModel.urn.get.get.dropSelector
							if (tOff > numObj){
								ObjectController.updateUserMessage(s"There are ${numObj} objects in the requested ${ObjectModel.objectOrCollection.get}, so an offset of ${tOff} is invalid.",2)
							} else {
								ObjectModel.boundDisplayObjects.get.clear
								for (i <- startIndex to endIndex){
									ObjectModel.boundDisplayObjects.get += ObjectModel.constructBoundDisplayObject(ObjectModel.collectionRepository.citableObjects(collUrn)(i))
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
		val collUrn:Cite2Urn = ObjectModel.urn.get.get.dropSelector
		val collLabel:String = ObjectModel.collectionRepository.collectionDefinition(collUrn).get.collectionLabel
		val n:Int = ObjectModel.boundDisplayObjects.get.size
		val total:Int = ObjectModel.collectionRepository.collectionData(collUrn).objects.size
		val report = s"Showing ${n} out of ${total} objects in collection: ${collLabel} [${collUrn}]."
		ObjectModel.objectReport := report
	}


	/* I don't know how to do anything equivalent to "this" when passing events
	in ScalaJS. So this will cover all numeric fields. */
	def validateNumericEntry(thisEvent: Event):Unit = {
		val oldOffset:Int = ObjectModel.offset.get
		val oldLimit:Int = ObjectModel.limit.get
		val thisTarget = thisEvent.target.asInstanceOf[org.scalajs.dom.raw.HTMLInputElement]
		val targetId = thisTarget.id
		val testText = thisTarget.value.toString
		try{
			val mo: Int = testText.toInt
			targetId match {
				case "object_browseOffset" => ObjectModel.offset := mo
				case "object_browseLimit" => ObjectModel.limit := mo
			}
		} catch {
			case e: Exception => {
				val badMo: String = testText
				ObjectModel.offset := oldOffset
				ObjectModel.limit := oldLimit
				targetId match {
						case "object_browseOffset" => {
							ObjectController.updateUserMessage(s"Offset value must be an integer. '${badMo}' is not an integer.", 2)
							thisTarget.value =  ObjectModel.offset.get.toString
						}
						case "object_browseLimit" => {
							ObjectController.updateUserMessage(s"Limit value must be an integer. '${badMo}' is not an integer.", 2)
							thisTarget.value =  ObjectModel.limit.get.toString
						}
				}

			}
		}
	}

 @JSExport
	def propertyUrnClick(urnSt:String) = {
		  // below is how you invoke a cofirmation dialog
			// val cc = window.confirm("Hi")
			//CiteSwitcher.switch(urnSt)
	}


}
