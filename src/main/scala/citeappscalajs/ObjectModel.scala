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
import edu.holycross.shot.citeenv._

import scala.scalajs.js.annotation.JSExport

@JSExport
object ObjectModel {

	// Messages
	var msgTimer:scala.scalajs.js.timers.SetTimeoutHandle = null
	val userMessage = Var("")
	val userAlert = Var("default")
	val userMessageVisibility = Var("app_hidden")


	// urn is what the user requested
	val urn = Var(Cite2Urn("urn:cite2:hmt:vaimg.v1:VA012RN_0013"))
	// displayUrn is what will be shown
	val displayUrn = Var(Cite2Urn("urn:cite2:hmt:vaimg.v1:VA012RN_0013"))

	// Keeping track of current collection data
	val collections = Vars.empty[CiteCollectionDef]

	// Binding up objects, their properties, and extensions
	val citeObject:Var[CiteObject] = null

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
	val prevOption:Option[Tuple3[Cite2Urn,Int,Int]] = None
	val nextOption:Option[Tuple3[Cite2Urn,Int,Int]] = None
	val currentPrev = Var(prevOption)
	val currentNext = Var(nextOption)

	// Object-or-collection? (based on current request)
	//    Choices: "none","object","collection","range"
	val objectOrCollection = Var("none")



	// The big data repo from the .cex file
	var collectionRepository: CiteCollectionRepository = null

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
					tempPropList.get += tempP
			}
			val tempProtocolList = Vars.empty[BoundCiteProtocol]

			val tempProt0 = Var(CiteMainModel.objectProtocol)
			tempProtocolList.get += BoundCiteProtocol(tempProt0)
			// *********************
			// Now we load other extensions… images for now, but others later!
			// *********************
			if (ImageModel.imageCollections != null) {
				for (ie <- ImageModel.imageExtensions.extensions(collUrn)) {
					val imageVector = ie
						if (imageVector.protocol == "Local jpeg string"){
						   val tempProt1 = Var(CiteMainModel.localImageProtocol)
							 tempProtocolList.get += BoundCiteProtocol(tempProt1)
						}
				}
			}
		//BoundDisplayObject(urn:Var[Cite2Urn], label:Var[String],props:Vars[BoundCiteProperty], prot:Vars[BoundCiteProtocol])
		val tempBDO = BoundDisplayObject(urn,label,tempPropList,tempProtocolList)
		tempBDO
	}


	// Clears all current object data, and with it, displayed objects
	@dom
	def clearObject:Unit = {
			boundObjects.get.clear
			boundDisplayObjects.get.clear
			browsable := false
			currentPrev := None
			currentNext := None
			objectReport := ""
			offset := 1
	}

	@dom
	def updatePrevNext:Unit = {
		val currentColl:Cite2Urn = urn.get.dropSelector
		ObjectModel.objectOrCollection.get match {
			case "object" => {
				if (isOrdered.get) {
					val thisIndex = collectionRepository.indexOf(boundObjects.get(0))
					val numInCollection:Int = collectionRepository.citableObjects(currentColl).size
					if (thisIndex + 1 < numInCollection){
							currentNext := Option(collectionRepository.citableObjects(thisIndex + 1).urn,offset.get,limit.get)
					} else {
						currentNext := None
					}
					if (thisIndex > 0){
							currentPrev := Option(collectionRepository.citableObjects(thisIndex - 1).urn,offset.get,limit.get)
					} else {
						currentPrev := None
					}
				} else {
					currentPrev := None
					currentNext := None
				}
			}
			case "none" => {
					currentPrev := None
					currentNext := None
			}
			case _ => {
				//g.console.log("updating pn")
				//g.console.log(s"current p: ${currentPrev.get}; n: ${currentNext.get}")
				//g.console.log(s"num in c: ${ObjectModel.objects.get.size}")
				val numC = boundObjects.get.size
				if(limit.get >= numC){
					currentPrev := None
					currentNext := None
				} else {
					if ((offset.get + limit.get) > numC){
						currentNext := None
					} else {
						// get next
						val o:Int = offset.get + limit.get

						currentNext := Option(urn.get,o,limit.get)
					}
					if (offset.get == 1 ){
						currentPrev := None
					} else {
						// get prev
						val o:Int = {
							if ((offset.get - limit.get) > 0){
								offset.get - limit.get
							} else { 1 }
						}
						//val u:Cite2Urn = objects.get(o).urn
						currentPrev := Option(urn.get,o,limit.get)
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
			var fromObject:CiteObject = ObjectModel.collectionRepository.citableObjects.filter(_.urn == fromUrn)(0)
			var toObject:CiteObject = ObjectModel.collectionRepository.citableObjects.filter(_.urn == toUrn)(0)
			var x:Int = ObjectModel.collectionRepository.indexOf(fromObject)
			var y:Int = ObjectModel.collectionRepository.indexOf(toObject)

			for (i <- x to y){
				ObjectModel.boundObjects.get += ObjectModel.collectionRepository.citableObjects(fromUrn.dropSelector)(i)
			}
		} catch {
			case e: Exception => {
				ObjectController.updateUserMessage(s"Unable to retrieve a range of objects between ${fromUrn} and ${toUrn}.",2)
				ObjectModel.clearObject
			}
		}
	}

	// Given a URN, gets all objects
	def getObjects(u:Cite2Urn) = {
			if (u.isRange){
				if (ObjectModel.collectionRepository.isOrdered(u.dropSelector)){
					ObjectController.updateUserMessage(s"Will deal with range ${u}.",1)
					val rangeTuple = rangeToTuple(u)
					val rb:Cite2Urn = rangeTuple._1
					val re:Cite2Urn = rangeTuple._2
					ObjectModel.getRangeObjects(rb, re)
					//ObjectController.setDisplay
				} else {
					ObjectController.updateUserMessage(s"The collection ${u.dropSelector} is not an ordered collection, so range-citations are not applicable.",2)
				}
			} else {
				u.objectComponentOption match {
					// Just object
					case Some(o) => {
						 ObjectModel.boundObjects.get += ObjectModel.collectionRepository.citableObjects.filter(_.urn == u)(0)
						 //ObjectController.setDisplay
					}
					// collection
					case None => {

					  val filteredData = ObjectModel.collectionRepository.citableObjects(u)

					  filteredData.foreach( fc => {
							ObjectModel.boundObjects.get += fc
						})

					}
				}
			}
	}

	@dom
	def updateCollections = {
		ObjectModel.collections.get.clear
		for ( cc <- ObjectModel.collectionRepository.catalog.collections){
			ObjectModel.collections.get += cc
		}
			QueryObjectModel.currentQueryCollection := None
			QueryObjectModel.currentQueryCollectionProps.get.clear
	}

	def countObjects(urn:Cite2Urn):Int = {
		val howMany:Int = ( ObjectModel.collectionRepository.data ~~ urn ).objects.size
		howMany
	}


	/* This is how to pass data to the global JS scope */
	/*
	js.Dynamic.global.currentObjectUrn = "urn:cts"
	js.Dynamic.global.roiArray = Array("one","two","three")
	*/

}
