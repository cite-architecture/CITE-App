package citeapp

import com.thoughtworks.binding.{Binding, dom}
import com.thoughtworks.binding.Binding.{BindingSeq, Var, Vars}
import scala.scalajs.js
import scala.scalajs.js._
import scala.scalajs.js.Dynamic.{ global => g }
import org.scalajs.dom._
import org.scalajs.dom.ext._
import org.scalajs.dom.raw._
import edu.holycross.shot.cite._
import edu.holycross.shot.ohco2._
import edu.holycross.shot.citeobj._
import edu.holycross.shot.citebinaryimage._
import scala.concurrent._
//import ExecutionContext.Implicits.global
import monix.execution.Scheduler.Implicits.global
import monix.eval._

import scala.scalajs.js.annotation.JSExport
import js.annotation._

@JSExportTopLevel("citeapp.ImageView")
object CiteBinaryImageView {


	// HTML Div holding messages
	@dom
	def imageMessageDiv = {
		<div id="image_message" class={ s"app_message ${CiteBinaryImageModel.userMessageVisibility.bind} ${CiteBinaryImageModel.userAlert.bind}"  }>
			<p>{ CiteBinaryImageModel.userMessage.bind }  </p>
			</div>
	}

	// HTML Div: main div for image display
	@dom
	def imageDiv = {

		val urnValidatingKeyUpHandler = { event: KeyboardEvent =>
			(event.currentTarget, event.keyCode) match {
				case (input: html.Input, KeyCode.Enter) => {
					event.preventDefault()
					CiteBinaryImageController.changeUrn(s"${input.value.toString}")
					//input.value = ""
				}
				case(input: html.Input, _) =>  CiteBinaryImageController.validateUrn(s"${input.value.toString}")
				case _ =>
			}
		}
	
		<div id="image_imageContainer">


		<div id="image_sidebar" class="app_sidebarDiv">
			{ imageCollectionsContainer.bind }
			{ imagePreviewDiv.bind }
			{ imageMappedDataDiv.bind }
		</div>

		{ imageMessageDiv.bind }

		<p id="image_reportingCurrentUrn" class="app_reportingCurrentUrn"> { 
			CiteBinaryImageModel.displayUrn.bind match {
				case Some(u) => {
					u.toString
				}
				case _ => {
					""	
					}
				}
			} 
		</p>

		<p id="image_urnInputP">
			<input
				class={ s"${CiteBinaryImageController.validUrnInField.bind}" }
				id="image_urnInput"
				size={ 70 }
				type="text"
				value={ 
						CiteBinaryImageModel.urn.bind match {
							case Some(u) => {
							u.toString
						}
						case _ => {
							""	
						}
					} 
				}
				onkeyup={ urnValidatingKeyUpHandler }>
			</input>

			{ CiteBinaryImageView.retrieveImageButton.bind }

			<br/>
		</p>

		{ CiteBinaryImageView.imageContainer.bind }
		</div>
	}

	/* Cited Works List */
	@dom
	def imageCollectionsContainer = {
		<div id="image_imageCollectionsContainer">
			<h2>Image Collections</h2>
			{
				<ul>
					{ for ( ic <- CiteBinaryImageModel.binaryImageCollections) yield {
						<li>
							<a
								onclick={ event: Event => {
								DataModelController.retrieveObject(None,ic) }
								}> { s"${ic}" } </a>
							<br/>
								{ ObjectController.labelForCollection(ic) }
							</li>
						} 
					}
				</ul>
			}
		</div>
	}


	// For holding a preview of an image or region thereof
	@dom
	def imagePreviewDiv = {
		<div id="image_imagePreviewDiv">
			{ 
				CiteBinaryImageModel.previewUrn.bind match {
					case Some(u) => {
						CiteBinaryImageView.imageThumbItem(u, CiteBinaryImageModel.imgUseLocal.bind).bind 
					}
					case None => { <!-- empty content --> }
				}
			}
		</div>
	}

	/* Search Image Properties Forms */
	@dom
	def imageMappedDataDiv = {
		<h2>Mapped Data</h2>
		<div id="image_mappedData">
		</div>

	}

	@dom
	def mappedUrnP(iroi:ImageRoiModel.Roi) = {
		<p>URN HERE!</p>
	}



	@dom
	def imageThumbItem(u:Cite2Urn, useLocal:Boolean ) = {
		CiteBinaryImageController.implementedByImageCollObjects(u) match {
			case Some(uv) => {
				useLocal match {
					case true => {
						CiteBinaryImageController.implementedByProtocol(uv,CiteBinaryImageModel.jpgProtocolString) match {
							case Some(obj) => {
								<li id="image_imagePreviewDiv" >
									{ CiteBinaryImageView.localThumbnail(u, obj).bind }
								</li>
							}
							case _ => { 
								<!-- empty content -->	
							}
						}
					}
					case _ => {
						CiteBinaryImageController.implementedByProtocol(uv,CiteBinaryImageModel.iiifApiProtocolString) match {
							case Some(obj) => {
								<li id="image_imagePreviewDiv" >
									<img id="image_previewImg" src={
										CiteBinaryImageController.thumbSourceRemote(u, obj)
										}/>
								</li>
							}
							case _ => { 
								<!-- empty content -->	
							}
						}
					}
				}	
			}
			case None => {
				<!-- empty content -->
			}
		}
	}


	/* 
			This one is a little complicated.
			We laboriously construct a path where we expect to find a JPEG.
			Then we load it and scale it in an offscreen canvas.
			Then we get a data-url for the scaled image.
			Then we create an onscreen <img> element and load the data-url as its source.
	*/
	@dom
	def localThumbnail(propVal:Cite2Urn, obj:CiteObject) = {
		// Set up
		val maxWidth = CiteBinaryImageModel.thumbnailMaxWidth;
		val justUrn = propVal.dropExtensions
		val justROI = propVal.objectExtensionOption

		// Set up ROI: If there is no stated ROI, we define an ROI to be there
		// whole image: 0,0,100%,100%
		var rT:Float = 0; var rL:Float = 0; var rW:Float = 1; var rH:Float = 1;
		
		justROI match {
			case Some(r) => {
				rL = r.split(',')(0).toFloat
				rT = r.split(',')(1).toFloat
				rW = r.split(',')(2).toFloat
				rH = r.split(',')(3).toFloat
			}
			case _ => {
				rT = 0; rL = 0; rW = 1; rH = 1;
			}
		}

		// Set up path to images
		var pathPrefix:String = CiteBinaryImageModel.imgArchivePath.value
		//g.console.log(s"pathPrefix = ${pathPrefix}")
		var path:String = (pathPrefix + CiteBinaryImageController.getLocalThumbPath(justUrn, obj)).replaceAll("//","/") 

		// Create a canvas, drawing context, offscreen- and onscreen-image	
		val canvas = document.createElement("canvas").asInstanceOf[HTMLCanvasElement]
		canvas.setAttribute("crossOrigin","Anonymous")
		val ctx = canvas.getContext("2d").asInstanceOf[CanvasRenderingContext2D]
		val offScreenImg = document.createElement("img").asInstanceOf[HTMLImageElement]
		val onScreenImg = document.createElement("img").asInstanceOf[HTMLImageElement]

		// Attempt to get around crossOrigin policies
		offScreenImg.setAttribute("crossOrigin","Anonymous")
		offScreenImg.setAttribute("src",path)

		//Wait for that to load, then proceed
		offScreenImg.onload = (e: Event) => {
			canvas.width = (offScreenImg.width * rW).round
			canvas.height = (offScreenImg.height * rH).round
			ctx.drawImage(offScreenImg,(0-(offScreenImg.width * rL)).round.toDouble,(0-(offScreenImg.height*rT)).round.toDouble)
			val s:String = canvas.toDataURL("image/png")
			onScreenImg.setAttribute("crossOrigin","Anonymous")
			onScreenImg.setAttribute("src",s)
			onScreenImg.setAttribute("class","object_imgThumb")
		}
		onScreenImg

	}

	@dom
	def retrieveImageButton = {
		<button
			onclick={ event: Event => {
				val s:String = js.Dynamic.global.document.getElementById("image_urnInput").value.toString
				//ImageModel.urn := Cite2Urn(s)
				CiteBinaryImageController.updateUserMessage("Retrieving image…",1)
				val task = Task{ CiteBinaryImageController.changeUrn(s)}
				val future = task.runAsync
			} }
			disabled={ (CiteBinaryImageController.validUrnInField.bind == false) 
		} > {
				if ( CiteBinaryImageController.validUrnInField.bind == true ){
					"Retrieve Image"
				} else {
					"Invalid URN"
				}
			}
		</button>
	}

	/* Image Container: Where openSeaDragon does its thing */
	@dom
	def imageContainer = {
		<div id="image_zoomContainer"> </div>
	}



}
