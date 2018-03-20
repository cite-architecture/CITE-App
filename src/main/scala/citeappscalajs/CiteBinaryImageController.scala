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
				//g.console.log(vColl.toString)
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
		if (CiteBinaryImageModel.hasLocalOption.value && CiteBinaryImageModel.hasRemoteOption.value){
			CiteBinaryImageModel.imgUseLocal.value = { imgSourceStr == "true" }
			CiteBinaryImageController.changeImage(CiteBinaryImageModel.imageRoisToOptionVector)
		}
	}


	/* See if there is any binary image model implemented, and
	 	if so, which protocols

	   CiteApp needs two protocols each for local and remote images:
	   A. Local Images:
	   	1. jpgProtocolString (static images)
	   	2. localDZProtocolString (zooming images)
	   B. Remote Images:
	   	1. iiifApiProtocolString (static images)
	   	2. iipDZProtocolString  (zoomking images)
	   (Those values are defined in CiteBinaryImageModels.scala)
	*/

	def discoverProtocols:Unit = {
		// Get urn in more concise form
		val biurn:Cite2Urn = CiteBinaryImageModel.binaryImageModelUrn	
		// Get vector of collections implementing that model
		CiteMainModel.mainLibrary.value match {
			case Some(ml) => {
				val bicolls:Vector[Cite2Urn] = ml.collectionsForModel(biurn)
				bicolls.size match {
					case s if (s > 0) => {
			  			CiteBinaryImageModel.hasBinaryImages.value = true
			  			CiteBinaryImageModel.hasRemoteOption.value = {
			  				val hasIiifApiColls:Boolean = {
			  					ObjectModel.collRep.value match {
			  						case Some(cr) => {
					  					var colls:Vector[Cite2Urn] = bicolls.map( c => {
					  						cr.collectionsMap(c)	
					  					}).flatten
					  					val protocolReport:Option[CiteObject] = implementedByProtocol(colls,CiteBinaryImageModel.iiifApiProtocolString)
					  					protocolReport match {
					  						case Some(co) => true
					  						case _ => false
					  					}
			  						}
				  					case None => false
			  					}
			  				}
			  				val hasIipDZColls:Boolean = { 
			  					ObjectModel.collRep.value match {
			  						case Some(cr) => {
					  					var colls:Vector[Cite2Urn] = bicolls.map( c => {
					  						cr.collectionsMap(c)	
					  					}).flatten
					  					val protocolReport:Option[CiteObject] = implementedByProtocol(colls,CiteBinaryImageModel.iipDZProtocolString)
					  					protocolReport match {
					  						case Some(co) => true
					  						case _ => false
					  					}
			  						}
				  					case None => false
			  					}
				  			}
			  				( hasIiifApiColls && hasIipDZColls )	
			  			}
			  			CiteBinaryImageModel.hasLocalOption.value = {
			  				val hasJpgColls:Boolean = {
			  					ObjectModel.collRep.value match {
			  						case Some(cr) => {
					  					var colls:Vector[Cite2Urn] = bicolls.map( c => {
					  						cr.collectionsMap(c)	
					  					}).flatten
					  					val protocolReport:Option[CiteObject] = implementedByProtocol(colls,CiteBinaryImageModel.jpgProtocolString)
					  					protocolReport match {
					  						case Some(co) => true
					  						case _ => false
					  					}
			  						}
				  					case None => false
			  					}
			  				}
			  				val hasLocalDZColls:Boolean = {
			  					ObjectModel.collRep.value match {
			  						case Some(cr) => {
					  					var colls:Vector[Cite2Urn] = bicolls.map( c => {
					  						cr.collectionsMap(c)	
					  					}).flatten
					  					val protocolReport:Option[CiteObject] = implementedByProtocol(colls,CiteBinaryImageModel.localDZProtocolString)
					  					protocolReport match {
					  						case Some(co) => true
					  						case _ => false
					  					}
			  						}
				  					case None => false
			  					}
			  				}
			  				( hasJpgColls && hasLocalDZColls )	
			  			}
					}
					case _ => {
						// With no protocol represented, may as well say `false` to everything
			  			CiteBinaryImageModel.hasBinaryImages.value = false
			  			CiteBinaryImageModel.hasLocalOption.value = false
			  			CiteBinaryImageModel.hasRemoteOption.value = false
					}
				}
			}	
			case None => {
	  			CiteBinaryImageModel.hasBinaryImages.value = false
	  			CiteBinaryImageModel.hasLocalOption.value = false
	  			CiteBinaryImageModel.hasRemoteOption.value = false
			}
		}
	}

	/* 
	Based on current state of the protocol values, set the switch
	*/
	def setImageSwitch:Unit = {
		if( CiteBinaryImageModel.hasLocalOption.value && !(CiteBinaryImageModel.hasRemoteOption.value)) {
			CiteBinaryImageModel.imgUseLocal.value = true
		}
		if( CiteBinaryImageModel.hasRemoteOption.value && !(CiteBinaryImageModel.hasLocalOption.value)) {
			CiteBinaryImageModel.imgUseLocal.value = false
		}
		//g.console.log(s"hasIiif: ${CiteBinaryImageModel.hasIiifApi.value}; hasDZ: ${CiteBinaryImageModel.hasLocalDeepZoom.value}")
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
	def implementedByProtocol(urnV:Vector[Cite2Urn], protocol:String):Option[CiteObject] = {
	//	g.console.log(s"implementedByProtocol: ${protocol} :: ${urnV}")
		try {
			urnV.size match {
				case s if (s > 0) => {
					val implementedUrns:Vector[Cite2Urn] = urnV.filter(u => {
						val cr = ObjectModel.collRep.value.get	
						val oneObject:CiteObject = cr.citableObjects.filter(_.urn == u)(0)
					//	g.console.log(s"oneObject: ${oneObject.urn}")
						val propId:Cite2Urn = DataModelController.propertyUrnFromPropertyName(u, CiteBinaryImageModel.protocolPropertyName)
					//	g.console.log(s"propId = ${propId}")
						//g.console.log(s"equals: ${oneObject.valueEquals(propId,protocol)}")
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
	def getImplementingObject(u:Cite2Urn, useLocal:Boolean, zoom:Boolean):Option[CiteObject] = {
		CiteBinaryImageController.implementedByImageCollObjects(u) match {
			case Some(uv) => {
				zoom match {
					case false => {
						useLocal match {
							case true => {
								CiteBinaryImageController.implementedByProtocol(uv,CiteBinaryImageModel.jpgProtocolString) match {
									case Some(obj) => {
										Some(obj)
									}
									case _ => None
								}
							}
							case _ => {
								CiteBinaryImageController.implementedByProtocol(uv,CiteBinaryImageModel.iiifApiProtocolString) match {
									case Some(obj) => {
										Some(obj)
									}
									case _ => None
								}
							}
						}	
					}
					case _ => {
						useLocal match {
							case true => {
								CiteBinaryImageController.implementedByProtocol(uv,CiteBinaryImageModel.localDZProtocolString) match {
									case Some(obj) => {
										Some(obj)
									}
									case _ => None
								}
							}
							case _ => {
								CiteBinaryImageController.implementedByProtocol(uv,CiteBinaryImageModel.iipDZProtocolString) match {
									case Some(obj) => {
										Some(obj)
									}
									case _ => None
								}
							}
						}	
					}
				}								
			}
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
		s.replaceAll("//","/")
	}

	// *** Apropos Microservice ***
	def changeUrn(urn:Cite2Urn): Unit = 	{
		changeUrn(None, urn, None)
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
		roiObjs:Option[Vector[ImageRoiModel.Roi]] = None
	):Unit = {
		try {
			CiteBinaryImageModel.displayUrn.value = Some(urn)
			validUrnInField.value = true
			CiteBinaryImageModel.urn.value = Some(urn.dropExtensions)
			val plainUrn:Cite2Urn = urn.dropExtensions
			CiteBinaryImageController.updateRois(plainUrn,roiObjs)
			CiteBinaryImageModel.previewUrn.value = {
				roiObjs match {
					case None => Some(urn)
					case Some(r) if (r.size > 1) => Some(plainUrn)
					case _ => Some(urn)
				}
			}
			CiteBinaryImageController.changeImage(roiObjs)
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

	def updateRois(u:Cite2Urn, roiOptionVector:Option[Vector[ImageRoiModel.Roi]] = None):Unit = {
		roiOptionVector match {
			case Some(rov) => CiteBinaryImageModel.loadROIs(rov)
			case None => CiteBinaryImageModel.clearROIs
		}
	}

	def getZoomUrlAndPath(urn:Cite2Urn):String = {
		/* Remote, e.g.:
				'http://www.homermultitext.org/iipsrv?DeepZoom=/project/homer/pyramidal/VenA/VA012RN_0013.tif.dzi',
			Local, e.g.:
				'image_archive/hmt/vaimg/2017a/VA012RN_0013.dzi'

			Local needs the value in  property localDZProtocolString
			Remote needs the value in property iipDZProtocolString
		*/
		try {
			val useLocal:Boolean = CiteBinaryImageModel.imgUseLocal.value
			useLocal match {
				case true => {
					val implObj:Option[CiteObject] = getImplementingObject(urn,true,true)
					implObj match {
						case Some(io) => {

							// Get Url
							val urlPropertyUrn:Cite2Urn =  DataModelController.propertyUrnFromPropertyName(implObj.get.urn, "url") 
							// Sanitizing. We want this to end with a slash but not begin with one.
							val zoomUrl1:String = io.propertyValue(urlPropertyUrn).toString
							val zoomUrl2:String = if (zoomUrl1(0) == '/') zoomUrl1.tail else zoomUrl1
							val zoomUrl3:String = s"${zoomUrl2}/".replaceAll("//","/")
							// But we want to keep http://
							val zoomUrl:String = s"${zoomUrl3}".replaceAll("http:/","http://")

							// Get Path
							val pathPropertyUrn:Cite2Urn =  DataModelController.propertyUrnFromPropertyName(implObj.get.urn, "path") 
							// Do some sanitizing: We want a slash at the end _and_ at the beginning
							val zoomPath1:String = io.propertyValue(pathPropertyUrn).toString
							val zoomPath2:String = if (zoomPath1(0) == '/') zoomPath1.tail else zoomPath1
							val zoomPath:String = s"/${zoomPath2}/".replaceAll("//","/")

							// Get object-selector: this is the image-file name
							val imgId:String = urn.dropExtensions.objectComponent

							// assemble valid path for local Deep Zoom zooming
							val returnVal:String = 	s"${CiteBinaryImageModel.imgArchivePath.value}${zoomUrl}${zoomPath}${imgId}.dzi"
							returnVal
						}
						case None => {
							throw new Exception("No implementingObject")
						}
					}
				}
				case _ => {
					val implObj:Option[CiteObject] = getImplementingObject(urn,false,true)
					implObj match {
						case Some(io) => {

							// Get Url
							val urlPropertyUrn:Cite2Urn =  DataModelController.propertyUrnFromPropertyName(implObj.get.urn, "url") 
							// Sanitizing. We want this to end with a slash but not begin with one.
							val zoomUrl1:String = io.propertyValue(urlPropertyUrn).toString
							val zoomUrl2:String = if (zoomUrl1(0) == '/') zoomUrl1.tail else zoomUrl1
							val zoomUrl3:String = s"${zoomUrl2}".replaceAll("//","/")
							// But we want to keep http://
							val zoomUrl:String = s"${zoomUrl3}".replaceAll("http:/","http://")

							// Get Path
							val pathPropertyUrn:Cite2Urn =  DataModelController.propertyUrnFromPropertyName(implObj.get.urn, "path") 
							// Do some sanitizing: We want a slash at the end _and_ at the beginning
							val zoomPath1:String = io.propertyValue(pathPropertyUrn).toString
							val zoomPath2:String = s"${zoomPath1}"
							val zoomPath:String = s"${zoomPath2}/".replaceAll("//","/")

							// Get object-selector: this is the image-file name. For iipDeepZoom, it must end in '.tif.dzi'
							val imgId:String = s"${urn.dropExtensions.objectComponent}.tif.dzi"

							val returnVal:String = s"${zoomUrl}${zoomPath}${imgId}"
							returnVal
						}
						case None => {
							throw new Exception("No implementingObject")
						}
					}
				}
			}
		} catch {
			case e:Exception => throw new Exception(s"getZoomPath: cannot get ${if (CiteBinaryImageModel.imgUseLocal.value) "local" else "remote"} zoom path for ${urn}: ${e}")
		}
	}


	def changeImage(roiObj:Option[Vector[ImageRoiModel.Roi]]):Unit = {
		CiteBinaryImageModel.urn.value match {
			case Some(u) => {
				val tempUrn:Cite2Urn = u
				val collection:Cite2Urn = tempUrn.dropSelector
				val ioo:Option[String] = tempUrn.objectComponentOption

				// Double-check that we have an image, not a collection	
				ioo match {
					case Some(s) => {
						// If there is/are imageROI(s) grabbed, send them to JS for processing
						CiteBinaryImageController.loadJsArray(roiObj)
						// Based on local/remote, get zoom path
						val zoomPath:String = CiteBinaryImageController.getZoomUrlAndPath(u)
						// Alert user that ROIs will appear after a short delay
						updateUserMessage("Regions-of-interest will appear on the image after a short delay.",1)
						// Hand off to javascript for OpenSeadragon zooming
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

	def loadJsArray(roiObj:Option[Vector[ImageRoiModel.Roi]]):Unit = {
		g.console.log(roiObj.toString)
		CiteBinaryImageController.clearJsRoiArray(true)
		roiObj match {
			case Some(ro) => {
				for (roi <- ro){
					val tempRoi:String = roi.toSubrefString	
					val dataUrn:String = {
						roi.dataUrn match {
							case Some(du) => du.toString
							case None => ""
						}
					}
					// Fix this!!!
					val groupId:String = "1"
					val index:Int = 1
					CiteBinaryImageController.addToJsRoiArray(index,tempRoi,dataUrn,groupId)
				}

			}
			case None => // do nothing
		}		
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
