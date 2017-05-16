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


	@dom
	def switchDisplay(thisEvent: Event):Unit = {
		val showObjectsStr:String = js.Dynamic.global.document.getElementById("browse_onoffswitch").checked.toString
		ObjectModel.showObjects := (showObjectsStr == "true")
		g.console.log("Show Objects = ${ObjectModel.showObject.get}")
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

	// This version also insists on an object-identifier
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

	def changeObject:Unit = {
		val tempUrn:Cite2Urn = ObjectModel.urn.get
		ObjectModel.clearObject

		ObjectModel.objectOrCollection.get match {
				case "object" =>{
				  val filteredData = ObjectModel.collectionRepository.data ~~ tempUrn
				  filteredData.objects.foreach( fc => {
						ObjectModel.objects.get += fc
					})
				}
				case "collection" =>{
				  val filteredData = ObjectModel.collectionRepository.data ~~ tempUrn
				  filteredData.objects.foreach( fc => {
						ObjectModel.objects.get += fc
					})
				}
				case "range" =>{
					ObjectController.updateUserMessage(s"Will deal with range ${tempUrn}.",1)
				}
		}
	}

	def changeUrn(urnString: String): Unit = {
		changeUrn(Cite2Urn(urnString))
	}

	def changeUrn(urn: Cite2Urn): Unit = {
		try {
			ObjectModel.urn := urn
			ObjectModel.displayUrn := urn
			ObjectModel.urn.get.objectComponentOption match {
				case Some(o) => {
					ObjectModel.urn.get.rangeBeginOption match {
						case Some(rb) => {
							ObjectModel.urn.get.rangeEndOption match {
								case Some(re) => {
									ObjectModel.objectOrCollection := "range"
									ObjectController.updateUserMessage("Retrieving range…",1)
								}
								case _ => {
									ObjectModel.objectOrCollection := "none"
								}
							}
						}
						case _ =>{
							ObjectModel.objectOrCollection := "object"
							ObjectController.updateUserMessage("Retrieving object…",1)
						}
					}
				}
				case _ => {
					ObjectModel.objectOrCollection := "collection"
					ObjectController.updateUserMessage("Retrieving collection…",1)
				}
			}
			if (ObjectModel.objectOrCollection.get != "none") {
				js.timers.setTimeout(500){ ObjectController.changeObject }
				updateUserMessage("Invalid URN. Current URN not changed.",2)
			}
		} catch {
			case e: Exception => {
				ObjectModel.objectOrCollection := "none"
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
			g.console.log("preloading URN")
	}

	@dom
	def clearResults = {
			g.console.log("Clearing results")
	}

	@dom
	def clearHistory = {
			g.console.log("Clearing history")
	}

	def insertFirstObjectUrn(urn: Cite2Urn): Unit = {
		g.console.log(s"Will get first urn for: ${urn}")
		val firstUrn:Cite2Urn = ( ObjectModel.collectionRepository.data ~~ urn ).objects.head
		js.Dynamic.global.document.getElementById("object_urnInput").value = firstUrn.toString
		ObjectModel.objectOrCollection := "object"
	//js.Dynamic.global.document.getElementById("o2_urnInput").value = firstUrn.toString
	//validUrnInField := true
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

}
