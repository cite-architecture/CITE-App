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

	// Set the map of image-collections and their labels
	def setBinaryImageCollections:Unit = {
		val vColls:Option[Vector[Cite2Urn]] = getBinaryImageCollections
		vColls match {
			case Some(vc) => {
				CiteBinaryImageModel.binaryImageCollections.value.clear
				vc foreach ( c => CiteBinaryImageModel.binaryImageCollections.value += c)
			}
			case None => CiteBinaryImageModel.binaryImageCollections.value.clear
		}
	}

	/* returns a binary image collections with their labels */
	def getBinaryImageCollections:Option[Vector[Cite2Urn]] = {
		ObjectModel.collRep.value match {
			case Some(cr) => {
				val vColl:Vector[Cite2Urn] = {
					cr.collections.filter( coll => {
						val isBinaryImage:Boolean = (implementedByImageCollObjects(coll) != None)
						isBinaryImage
					}).toVector	
				}
				g.console.log(vColl.toString)
				vColl.size match {
					case s if (s > 0) => {
						Some(vColl)
					}
					case _ => None
				}
			}
			case None => None
		}	
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

	/* Given a URN and the value of CiteBinaryImageModel.useLocal, return an 
		implementing object for that image.
	*/
	def getImplmentingObject(u:Cite2Urn, useLocal:Boolean):Option[CiteObject] = {
		CiteBinaryImageController.implementedByImageCollObjects(u) match {
			case Some(uv) => {
				useLocal match {
					case true => {
						CiteBinaryImageController.implmentedByProtocol(uv,CiteBinaryImageModel.localDZProtocolString) match {
							case Some(obj) => {
								Some(obj)
							}
							case _ => None
						}
					}
					case _ => {
						CiteBinaryImageController.implmentedByProtocol(uv,CiteBinaryImageModel.iiifApiProtocolString) match {
							case Some(obj) => {
								Some(obj)
							}
							case _ => None
						}
					}
				}			}
			case None => None 
		}
	}

	def pathAndUrl(urn:Cite2Urn, obj:CiteObject):Map[String,String] = {

		val pathUrn:Cite2Urn = DataModelController.propertyUrnFromPropertyName(obj.urn, "path")
		val path:String = obj.propertyValue(pathUrn).toString
		val urlUrn:Cite2Urn = DataModelController.propertyUrnFromPropertyName(obj.urn, "url")
		val url:String = obj.propertyValue(urlUrn).toString
		val pathMap:Map[String,String] = Map("path" -> path, "url" -> url)
		pathMap
	}

	/* return a string, the source of a remotely served image thumbnail */
	def thumbSourceRemote(urn:Cite2Urn, obj:CiteObject):String = {
		// We will use CiteBinaryImage for this. We need:
		// 1. the URN, which we have
		// 2. the base URL
		// 3. the local ImagePath. We can get these from the implementing object Obj
		val pathMap:Map[String,String] = pathAndUrl(urn, obj)
		val path:String = pathMap("path")
		val url:String = pathMap("url")

      val bis:IIIFApi = IIIFApi(baseUrl = url, imagePath = path, maxWidth = Some(CiteBinaryImageModel.thumbnailMaxWidth))
      val imageUrlString:String = bis.serviceRequest(urn)

      imageUrlString
	}

	/* return a string, the source of a locally served image thumbnail */
	/*
	def thumbSourceLocal(urn:Cite2Urn, obj:CiteObject):String = {
		"https://dummyimage.com/100x100/aaa/000&amp;text=localImage"
	}
	*/

	def getLocalThumbPath(urn:Cite2Urn, obj:CiteObject):String = {
		val path:String = s"${CiteBinaryImageController.urnToLocalPath(urn,obj)}${urn.objectComponent}.jpg"
 	   path
	}

	// USE DataModel Stuff!!
	def urnToLocalPath(urn:Cite2Urn, obj:CiteObject):String = {
		val pathMap:Map[String,String] = pathAndUrl(urn, obj)
		// It is impossible to predict or remember whether to precede or follow
		// these paths with "/", so let's double-up, and clean…
		val s:String = "/" + pathMap("url") + pathMap("path") + "/"
		g.console.log(s"path for JPG: ${s}")
		g.console.log(s"cleaned: ${s.replaceAll("//","/")}")
		//val s:String = s"/${urn.namespace}/${urn.collection}/${urn.version}/"	
		s.replaceAll("//","/")
	}

	// *** Apropos Microservice ***
	def changeUrn(urn:Cite2Urn): Unit = 	{
		try {
			val useLocal:Boolean = CiteBinaryImageModel.imgUseLocal.value
			val implObj:Option[CiteObject] = getImplmentingObject(urn, useLocal)
			implObj match {
				case Some(io) => changeUrn(None, urn, io, None)
				case None => throw new Exception("Image not implemented by any appropriate datamodel.")
			}
		} catch {
			case e:Exception => {
					updateUserMessage(s"Unable to display image for ${urn}. ${e}",2)
			}
		}
	}

	// *** Apropos Microservice ***
	def changeUrn(urnString: String): Unit = {
		try {
			val urn:Cite2Urn = Cite2Urn(urnString)
			changeUrn(urn)
		} catch {
			case e: Exception => {
				validUrnInField.value = false
				updateUserMessage(s"Invalid URN. Current URN not changed. ${e}",2)
			}
		}
	}
		// *** Apropos Microservice ***
	def changeUrn(
		contextUrn:Option[Cite2Urn] = None, 
		urn:Cite2Urn, 
		implementingObject:CiteObject, 
		roiObj:Option[ImageRoiModel.ImageRoi] = None
	):Unit = {
		try {
			CiteBinaryImageModel.displayUrn.value = Some(urn)
			validUrnInField.value = true
			CiteBinaryImageModel.urn.value = Some(urn.dropExtensions)
			val plainUrn:Cite2Urn = urn.dropExtensions
			CiteBinaryImageController.updateRois(plainUrn,roiObj)
			CiteBinaryImageModel.previewUrn.value = {
				roiObj match {
					case None => Some(urn)
					case Some(r) if (r.rois.size > 1) => Some(plainUrn)
					case _ => Some(urn)
				}
			}
			CiteBinaryImageController.changeImage(urn,implementingObject,roiObj)
		} catch {
			case e: Exception => {
				validUrnInField.value = false
				updateUserMessage(s"Invalid URN [2]. Current URN not changed. ${e}",2)
			}
		}
	}


	def validateUrn(urnString: String): Unit = {
		try{
			val newUrn: Cite2Urn = Cite2Urn(urnString)
			validUrnInField.value = true
		} catch {
			case e: Exception => {
				validUrnInField.value = false
			}
		}
	}

	def updateRois(u:Cite2Urn, roiObject:Option[ImageRoiModel.ImageRoi] = None):Unit = {
			CiteBinaryImageModel.imageROIs.value = roiObject
	}

	def getZoomSource(urn:Cite2Urn, obj:CiteObject):String = {
		"zoomSource"
	}


	def changeImage(urn:Cite2Urn, implementingObject:CiteObject, roiObj:Option[ImageRoiModel.ImageRoi]):Unit = {
		CiteBinaryImageModel.urn.value match {
			case Some(u) => {
				val tempUrn:Cite2Urn = u
				val collection:Cite2Urn = tempUrn.dropSelector
				val ioo:Option[String] = tempUrn.objectComponentOption
				ioo match {
					case Some(s) => {
						CiteBinaryImageController.loadJsArray
						val zoomPath:String = CiteBinaryImageController.getZoomSource(urn, implementingObject)
						CiteBinaryImageController.updateImageJS(collection.toString, s, zoomPath )
					}
					case _ => {
						CiteBinaryImageController.updateUserMessage(s"No image-object specified in ${tempUrn}",2)
					}
				}
			}
			case _ => {
				CiteBinaryImageController.updateUserMessage(s"No image-object specified.",2)
			}

		}
	}

	def loadJsArray:Unit = {
		CiteBinaryImageController.clearJsRoiArray(true)
		/*
		for (iroi <- ImageModel.imageROIs.value){
			val tempRoi:String = {
				iroi.roi match {
					case Some(r) => r
					case _ => ""
				}
			}
			val tempMappedData:String = {
				iroi.roiData match {
					case Some(u) => u.toString
					case _ => ""
				}
			}
			// We will have to do something clever here to make groups
			val tempGroup:String = iroi.roiGroup.toString
			val tempIndex:Int = iroi.index
			ImageController.addToJsRoiArray(tempIndex, tempRoi,tempMappedData,tempGroup)
		}
		*/
	}

	/* Methods for connecting out to Javascript */
	@JSGlobal("clearJsRoiArray")
	@js.native
	object clearJsRoiArray extends js.Any {
		def apply(really:Boolean): js.Dynamic = js.native
	}

	@JSGlobal("addToJsRoiArray")
	@js.native
	object addToJsRoiArray extends js.Any {
		def apply(index:Int, roiString:String, urnString:String, groupString:String): js.Dynamic = js.native
	}

	@JSGlobal("updateImageJS")
	@js.native
	object updateImageJS extends js.Any {
		def apply(collection: String, imageObject: String, path:String): js.Dynamic = js.native
	}




}
