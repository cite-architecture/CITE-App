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
	val objects = Vars.empty[CiteObject]
	val displayObjects = Vars.empty[CiteObject]

	// For Display
	val offset = Var(1)
	val limit = Var(10)
	val showObjects = Var(false) // if true, show a whole object; false, URN+label

	// for navigation
	val currentPrev = Var(Tuple2(Int,Int))
	val currentNext = Var(Tuple2(Int,Int))

	// Object-or-collection?
	//    Choices: "none","object","collection","range"
	val objectOrCollection = Var("none")


	var collectionRepository: CiteCollectionRepository = null



	// Clears all current object data, and with it, displayed objects
	@dom
	def clearObject = {
			objects.get.clear
	}


	@dom
	def setDisplay:Unit = {
		 val collUrn:Cite2Urn = ObjectModel.urn.get.dropSelector
		 val numObj:Int = objects.get.size
		 val tLim:Int = ObjectModel.limit.get
		 val tOff:Int = ObjectModel.offset.get
		 val startIndex:Int = tOff - 1
		 val endIndex:Int = {
			 if ( (tLim + tOff - 1)  >= numObj ) {
				 g.console.log(s"first if: ${tLim + tOff - 1}")
				 (numObj - 1)
			 } else {
				 ((tOff - 1) + (tLim - 1))
			 }
		 }
		 if (tOff > numObj){
			 ObjectController.updateUserMessage(s"There are ${numObj} objects in the requested ${ObjectModel.objectOrCollection.get}, so an offset of ${tOff} is invalid.",2)
		 } else {
		 	g.console.log(s"numObj = ${numObj}")
		 	g.console.log(s"tLim = ${tLim}")
		 	g.console.log(s"tOff = ${tOff}")
		 	g.console.log(s"startIndex = ${startIndex}")
		 	g.console.log(s"endIndex = ${endIndex}")
		 	g.console.log(s"------------------------")
			ObjectModel.displayObjects.get.clear
			for (i <- startIndex to endIndex){
				ObjectModel.displayObjects.get += ObjectModel.collectionRepository.citableObjects(collUrn)(i)
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
				ObjectModel.objects.get += ObjectModel.collectionRepository.citableObjects(fromUrn.dropSelector)(i)
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
					ObjectModel.setDisplay
				} else {
					ObjectController.updateUserMessage(s"The collection ${u.dropSelector} is not an ordered collection, so range-citations are not applicable.",2)
				}
			} else {
				u.objectComponentOption match {
					// Just object
					case Some(o) => {
						 ObjectModel.objects.get += ObjectModel.collectionRepository.citableObjects.filter(_.urn == u)(0)
					}
					// collection
					case None => {
						g.console.log(s"no objectComponent for ${u}")
					  val filteredData = ObjectModel.collectionRepository.citableObjects(u)
					  filteredData.foreach( fc => {
							ObjectModel.objects.get += fc
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

	val urn1 = Cite2Urn("urn:cite2:hmt:vaimg.v1:VA012RN_0013")
	val urn2 = Cite2Urn("urn:cite2:hmt:vaimg.v1:VA012VN_0514")
	val urn3 = Cite2Urn("urn:cite2:hmt:vaimg.v1:VA013RN_0014")



}
