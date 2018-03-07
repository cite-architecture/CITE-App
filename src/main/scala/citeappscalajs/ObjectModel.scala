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

@JSExportTopLevel("citeapp.ObjectModel")
object ObjectModel {

	// Messages
	var msgTimer:scala.scalajs.js.timers.SetTimeoutHandle = null
	val userMessage = Var("")
	val userAlert = Var("default")
	val userMessageVisibility = Var("app_hidden")


	// urn is what the user requested
	val urn = Var[Option[Cite2Urn]](None)
	// displayUrn is what will be shown
	val displayUrn = Var[Option[Cite2Urn]](None)

	// Keeping track of current collection data
	val collections = Vars.empty[CiteCollectionDef]

	// Binding up objects, their properties, and extensions
	// Is this used?
	//val citeObject:Var[CiteObject] = null

	case class BoundCiteProperty(urn:Var[Cite2Urn],propertyType:Var[CitePropertyType],propertyValue:Var[String])

	case class BoundCiteProtocol(prot:Var[String])

   case class BoundDisplayObject(urn:Var[Cite2Urn], label:Var[String],props:Vars[BoundCiteProperty], prot:Vars[BoundCiteProtocol])


	val boundObjects = Vars.empty[CiteObject]
	val boundDisplayObjects = Vars.empty[BoundDisplayObject]

	// for collection-browsing
	val isOrdered = Var(false)


	// For Display
	val offset = Var(1)
	val limit = Var(4)
	val showObjects = Var(false) // if true, show a whole object; false, URN+label
	val browsable = Var(false)
	val objectReport = Var("")

	// for navigation
	val prevOption:Option[Tuple3[Option[Cite2Urn],Int,Int]] = None
	val nextOption:Option[Tuple3[Option[Cite2Urn],Int,Int]] = None
	val currentPrev = Var(prevOption)
	val currentNext = Var(nextOption)

	// Object-or-collection? (based on current request)
	//    Choices: "none","object","collection","range","search"
	val objectOrCollection = Var("none")



	// The big data repo from the .cex file
	val collRep = Var[Option[CiteCollectionRepository]](None)
	//var collRep:CiteCollectionRepository = null


	def constructBoundDisplayObject(obj:CiteObject):BoundDisplayObject = {
		  val collUrn:Cite2Urn = obj.urn.dropSelector
			val urn = Var(obj.urn)
			val label = Var(obj.label)
			val tempPropList = Vars.empty[BoundCiteProperty]
			for (p <- obj.propertyList){
					val tempU = Var(p.urn)
					val tempV = Var(p.propertyValue.toString)
					val tempT = Var(p.propertyDef.propertyType)
					val tempP = BoundCiteProperty(tempU,tempT,tempV)
					val props = Var(tempP)
					tempPropList.value += tempP
			}
			val tempProtocolList = Vars.empty[BoundCiteProtocol]

			val tempProt0 = Var(CiteMainModel.objectProtocol)
			tempProtocolList.value += BoundCiteProtocol(tempProt0)
			// *********************
			// Now we load other extensions… images for now, but others later!
			// *********************
			/*
				ImageModel.imageExtensions match {
					case Some(ext) =>{
						for (ie <- ext.extensions(collUrn)) {
							val imageVector = ie
								if (imageVector.protocol == "Local jpeg string"){
								   val tempProt1 = Var(CiteMainModel.localImageProtocol)
									 tempProtocolList.value += BoundCiteProtocol(tempProt1)
								}
						}
					}
					case _ => { }
				}
				*/
		//BoundDisplayObject(urn:Var[Cite2Urn], label:Var[String],props:Vars[BoundCiteProperty], prot:Vars[BoundCiteProtocol])
		val tempBDO = BoundDisplayObject(urn,label,tempPropList,tempProtocolList)
		tempBDO
	}


	// Clears all current object data, and with it, displayed objects
	@dom
	def clearObject:Unit = {
			boundObjects.value.clear
			boundDisplayObjects.value.clear
			urn.value = None
			browsable.value = false
			currentPrev.value = None
			currentNext.value = None
			objectReport.value = ""
			offset.value = 1
	}

	@dom
	def updatePrevNext:Unit = {
		ObjectModel.objectOrCollection.value match {
			case "object" => {
				if (isOrdered.value) {
					val currentColl:Cite2Urn = urn.value.get.dropSelector
					val thisIndex = collRep.value.get.indexOf(boundObjects.value(0))
					val numInCollection:Int = collRep.value.get.objectsForCollection(currentColl).size
					if (thisIndex + 1 < numInCollection){
							currentNext.value = Option(Some(collRep.value.get.citableObjects(thisIndex + 1).urn),offset.value,limit.value)
					} else {
						currentNext.value = None
					}
					if (thisIndex > 0){
							currentPrev.value = Option(Some(collRep.value.get.citableObjects(thisIndex - 1).urn),offset.value,limit.value)
					} else {
						currentPrev.value = None
					}
				} else {
					currentPrev.value = None
					currentNext.value = None
				}
			}
			case "none" => {
					currentPrev.value = None
					currentNext.value = None
			}
			case "search" => {
				val numC = boundObjects.value.size
				if(limit.value >= numC){
					currentPrev.value = None
					currentNext.value = None
				} else {
					if ((offset.value + limit.value) > numC){
						currentNext.value = None
					} else {
						// get next
						val o:Int = offset.value + limit.value

						currentNext.value = Option(None,o,limit.value)
					}
					if (offset.value == 1 ){
						currentPrev.value = None
					} else {
						// get prev
						val o:Int = {
							if ((offset.value - limit.value) > 0){
								offset.value - limit.value
							} else { 1 }
						}
						//val u:Cite2Urn = objects.get(o).urn
						currentPrev.value = Option(None,o,limit.value)
					}
				}
			}
			case _ => {
				val numC = boundObjects.value.size
				if(limit.value >= numC){
					currentPrev.value = None
					currentNext.value = None
				} else {
					if ((offset.value + limit.value) > numC){
						currentNext.value = None
					} else {
						// get next
						val o:Int = offset.value + limit.value

						currentNext.value = Option(urn.value,o,limit.value)
					}
					if (offset.value == 1 ){
						currentPrev.value = None
					} else {
						// get prev
						val o:Int = {
							if ((offset.value - limit.value) > 0){
								offset.value - limit.value
							} else { 1 }
						}
						//val u:Cite2Urn = objects.get(o).urn
						currentPrev.value = Option(urn.value,o,limit.value)
					}
				}
			}
		}
	}

	// Returns two urns representing the ends of a range of objects in an ordered collection
	def rangeToTuple(u:Cite2Urn):Tuple2[Cite2Urn,Cite2Urn] = {
					val rb:String = u.rangeBegin
					val re:String = u.rangeEnd
					val coll:String = u.dropSelector.toString
					val rbU:Cite2Urn = Cite2Urn(s"${coll}${rb}")
					val reU:Cite2Urn = Cite2Urn(s"${coll}${re}")
					val rangeTuple:Tuple2[Cite2Urn,Cite2Urn] = (rbU,reU)
					rangeTuple
	}

	// Called by getObjects
	def getRangeObjects(fromUrn:Cite2Urn, toUrn:Cite2Urn):Unit = {
		try{
			var fromObject:CiteObject = ObjectModel.collRep.value.get.citableObjects.filter(_.urn == fromUrn)(0)
			var toObject:CiteObject = ObjectModel.collRep.value.get.citableObjects.filter(_.urn == toUrn)(0)
			var x:Int = ObjectModel.collRep.value.get.indexOf(fromObject)
			var y:Int = ObjectModel.collRep.value.get.indexOf(toObject)

			for (i <- x to y){
				ObjectModel.boundObjects.value += ObjectModel.collRep.value.get.objectsForCollection(fromUrn.dropSelector)(i)
			}
		} catch {
			case e: Exception => {
				//ObjectController.updateUserMessage(s"Unable to retrieve a range of objects between ${fromUrn} and ${toUrn}.",2)
				ObjectModel.clearObject
				//QueryObjectModel.clearAll
			}
		}
	}

	// Given a URN, gets all objects
	def getObjects(u:Cite2Urn) = {
			if (u.isRange){
				if (ObjectModel.collRep.value.get.isOrdered(u.dropSelector)){
					//ObjectController.updateUserMessage(s"Will deal with range ${u}.",1)
					val rangeTuple = rangeToTuple(u)
					val rb:Cite2Urn = rangeTuple._1
					val re:Cite2Urn = rangeTuple._2
					ObjectModel.getRangeObjects(rb, re)
					//ObjectController.setDisplay
				} else {
					//ObjectController.updateUserMessage(s"The collection ${u.dropSelector} is not an ordered collection, so range-citations are not applicable.",2)
				}
			} else {
				u.objectComponentOption match {
					// Just object
					case Some(o) => {
						 ObjectModel.boundObjects.value += ObjectModel.collRep.value.get.citableObjects.filter(_.urn == u)(0)
						 //ObjectController.setDisplay
					}
					// collection
					case None => {

					  val filteredData = ObjectModel.collRep.value.get.objectsForCollection(u)

					  filteredData.foreach( fc => {
							ObjectModel.boundObjects.value += fc
						})

					}
				}
			}
	}

	@dom
	def updateCollections = {
		ObjectModel.collections.value.clear
		for ( cc <- ObjectModel.collRep.value.get.catalog.collections){
			ObjectModel.collections.value += cc
		}
			//QueryObjectModel.currentQueryCollection.value = None
			//QueryObjectModel.currentQueryCollectionProps.value.clear
	}

	def countObjects(urn:Cite2Urn):Int = {
		val howMany:Int = ( ObjectModel.collRep.value.get.data ~~ urn ).objects.size
		howMany
	}


	/* This is how to pass data to the global JS scope */
	/*
	js.Dynamic.global.currentObjectUrn = "urn:cts"
	js.Dynamic.global.roiArray = Array("one","two","three")
	*/

}
