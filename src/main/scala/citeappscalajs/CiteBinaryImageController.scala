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
import edu.holycross.shot.scm._
import edu.holycross.shot.citebinaryimage._

import scala.scalajs.js.annotation.JSExport

@JSExportTopLevel("citeapp.CiteBinaryImageController")
object CiteBinaryImageController {

	

	val validUrnInField = Var(false)

	def updateUserMessage(msg: String, alert: Int): Unit = {
		CiteBinaryImageModel.userMessageVisibility.value = "app_visible"
		CiteBinaryImageModel.userMessage.value = msg
		alert match {
			case 0 => CiteBinaryImageModel.userAlert.value = "default"
			case 1 => CiteBinaryImageModel.userAlert.value = "wait"
			case 2 => CiteBinaryImageModel.userAlert.value = "warn"
		}
		js.timers.clearTimeout(CiteBinaryImageModel.msgTimer)
		CiteBinaryImageModel.msgTimer = js.timers.setTimeout(16000){ CiteBinaryImageModel.userMessageVisibility.value = "app_hidden" }
	}



	def setPreferredImageSource:Unit = {
		val imgSourceStr:String = js.Dynamic.global.document.getElementById("citeMain_localImageSwitch").checked.toString
		if (CiteBinaryImageModel.hasIiifApi.value && CiteBinaryImageModel.hasLocalDeepZoom.value){
			CiteBinaryImageModel.imgUseLocal.value = { imgSourceStr == "true" }
			g.console.log(s"hasIiif: ${CiteBinaryImageModel.hasIiifApi.value}; hasDZ: ${CiteBinaryImageModel.hasLocalDeepZoom.value}")
		}
		//ImageController.changeImage
	}

	// See if there is any binary image model implemented, and
	// 	if so, which protocols
	def discoverProtocols:Unit = {
		// Get urn in more concise form
		val biurn:Cite2Urn = CiteBinaryImageModel.binaryImageModelUrn	
		// Get vector of collections implementing that model
		CiteMainModel.mainLibrary.value match {
			case Some(ml) => {
				val bicolls:Vector[Cite2Urn] = ml.collectionsForModel(biurn)
				//g.console.log(s"bicolls.size = ${bicolls.size}")
				bicolls.size match {
					case s if (s > 0) => {
			  			CiteBinaryImageModel.hasBinaryImages.value = true
			  			CiteBinaryImageModel.hasIiifApi.value = {
			  				if (ObjectModel.collRep.value != None) {
		  						val iiifObjects:Vector[CiteObject] = ObjectModel.collRep.value.get.citableObjects.filter(_.valueEquals("iiifApi"))
		  						val hasIiif:Boolean = (iiifObjects.size > 0)
								hasIiif
			  				} else {
			  					false
			  				}
			  			}
			  			CiteBinaryImageModel.hasLocalDeepZoom.value = {
			  				if (ObjectModel.collRep.value != None) {
		  						val dzObjects:Vector[CiteObject] = ObjectModel.collRep.value.get.citableObjects.filter(_.valueEquals("localDeepZoom"))
		  						val hasDz:Boolean = (dzObjects.size > 0)
								hasDz
			  				} else {
			  					false
			  				}
			  			}
					}
					case _ => {
						// With no protocol represented, may as well say `false` to everything
			  			CiteBinaryImageModel.hasBinaryImages.value = false
			  			CiteBinaryImageModel.hasIiifApi.value = false
			  			CiteBinaryImageModel.hasLocalDeepZoom.value = false
					}
				}
			}	
			case None => {
	  			CiteBinaryImageModel.hasBinaryImages.value = false
	  			CiteBinaryImageModel.hasIiifApi.value = false
	  			CiteBinaryImageModel.hasLocalDeepZoom.value = false
			}
		}
	}

	/* 
	Based on current state of the protocol values, set the switch
	*/
	def setImageSwitch:Unit = {
		if( CiteBinaryImageModel.hasIiifApi.value && !(CiteBinaryImageModel.hasLocalDeepZoom.value)) {
			CiteBinaryImageModel.imgUseLocal.value = false
		}
		if( CiteBinaryImageModel.hasLocalDeepZoom.value && !(CiteBinaryImageModel.hasIiifApi.value)) {
			CiteBinaryImageModel.imgUseLocal.value = true 
		}
		g.console.log(s"hasIiif: ${CiteBinaryImageModel.hasIiifApi.value}; hasDZ: ${CiteBinaryImageModel.hasLocalDeepZoom.value}")
	}

	/* Check to see if the Binary Image datamodel is:
			1. supported by this app
			2. present in this library
			3. implemented by the collection represented by `u`
			4. and finally, which collection-objects describe implementations
	*/
	def implementedByImageCollObjects(u:Cite2Urn):Option[Vector[Cite2Urn]] ={
		val collUrn:Cite2Urn = u.dropSelector
		val binaryImageModelUrn:Cite2Urn = CiteBinaryImageModel.binaryImageModelUrn

		// First, do we have some datamodels?
		DataModelModel.dataModels.value match {
			case None => {
				None
			}
			case Some(dms) => {
				// Next, is binaryImageModel here?
				val implementations:Vector[DataModel] = dms.filter(_.model == binaryImageModelUrn)
				implementations.size match {
					case 0 => {
						None
					}
					// Next, get the collections that implement it
					case _ => {
						val colls = implementations.map(i => i.collection)
						val returnValue:Option[Vector[Cite2Urn]] = {
							ObjectModel.collRep.value match {
								case Some(cr) => {
									// Find which collections connect the DataModel to the requested objectUrn, if any
									val collectionsImplementing:Vector[Cite2Urn] = colls.filter(c => { cr.urnMatch(DataModelController.propertyUrnFromPropertyName(c,"collection"),u).size > 0 })
									// Find out which Objects in (each of those/that) Collection implent(s) the collection of the requested URN
									val objectsImplementing:Vector[Cite2Urn] = collectionsImplementing.map( c => {
											val propUrn:Cite2Urn = DataModelController.propertyUrnFromPropertyName(c,"collection")
											val objMatchVec:Vector[CiteObject] = cr.urnMatch(propUrn,u.dropSelector)
											val objMatchUrns:Vector[Cite2Urn] = objMatchVec.map(_.urn)
											objMatchUrns
										}).flatten
									objectsImplementing.size match {
										case s if (s > 0) => {
											Some(objectsImplementing)
										}
										case _ => {
											None
										}
									}
								}
								case None => {
									None
								}
							}
						}
						returnValue
					}
				}	
			}
		}
	}	

	/*
	Given a CITE URN to an object, and a protocol string, report whether that object is implemented by the given protocol
	*/
	def implmentedByProtocol(urnV:Vector[Cite2Urn], protocol:String):Option[CiteObject] = {
		try {
			urnV.size match {
				case s if (s > 0) => {
					val implementedUrns:Vector[Cite2Urn] = urnV.filter(u => {
						val cr = ObjectModel.collRep.value.get	
						val oneObject:CiteObject = cr.citableObjects.filter(_.urn == u)(0)
						val propId:Cite2Urn = DataModelController.propertyUrnFromPropertyName(u, CiteBinaryImageModel.protocolPropertyName)
						oneObject.valueEquals(propId,protocol)
					})
					implementedUrns.size match {
						case s if (s > 0) => Some(ObjectModel.collRep.value.get.citableObject(implementedUrns(0)))
						case _ => None
					}
				}
				case _ => None
			}
		} catch {
			case e: Exception => {
				None
			}		
		}
	}

	/* Display an Image */
	def displayImage(u:Cite2Urn, implementingObject:CiteObject, contextUrn:Option[Cite2Urn], protocol:String, rois:Option[ImageRoiModel.ImageRoi] = None):Unit = {
		g.console.log(s"Will display: ${u}")	
	}

	/* return a string, the source of a remotely served image thumbnail */
	def thumbSourceRemote(urn:Cite2Urn, obj:CiteObject):String = {
		// We will use CiteBinaryImage for this. We need:
		// 1. the URN, which we have
		// 2. the base URL
		// 3. the local ImagePath. We can get these from the implementing object Obj
		val pathUrn:Cite2Urn = DataModelController.propertyUrnFromPropertyName(obj.urn, "path")
		g.console.log(s"pathUrn: ${pathUrn}")
		val path:String = obj.propertyValue(pathUrn).toString
		g.console.log(s"path: ${path}")
		val urlUrn:Cite2Urn = DataModelController.propertyUrnFromPropertyName(obj.urn, "url")
		g.console.log(s"urlUrn: ${urlUrn}")
		val url:String = obj.propertyValue(urlUrn).toString
		g.console.log(s"url: ${url}")

      val bis:IIIFApi = IIIFApi(baseUrl = url, imagePath = path, maxWidth = Some(CiteBinaryImageModel.thumbnailMaxWidth))
      val imageUrlString:String = bis.serviceRequest(urn)

      imageUrlString
	}

	/* return a string, the source of a locally served image thumbnail */
	def thumbSourceLocal(urn:Cite2Urn, obj:CiteObject):String = {
		"https://dummyimage.com/100x100/aaa/000&amp;text=localImage"
	}

	def getLocalThumbPath(urn:Cite2Urn):String = {
		val path:String = s"${CiteBinaryImageController.urnToLocalPath(urn)}${urn.objectComponent}.jpg"
 	   path
	}

	def urnToLocalPath(urn:Cite2Urn):String = {
		val s:String = s"/${urn.namespace}/${urn.collection}/${urn.version}/"	
		s
	}



}
