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

	/* Passage Container */
	@dom
	def imageContainer = {
		<div id="image_imageContainer"> </div>
	}



	@dom
	def imageThumbItem(u:Cite2Urn, useLocal:Boolean ) = {
		CiteBinaryImageController.implementedByImageCollObjects(u) match {
			case Some(uv) => {
				useLocal match {
					case true => {
						CiteBinaryImageController.implmentedByProtocol(uv,CiteBinaryImageModel.dzProtocolString) match {
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
						CiteBinaryImageController.implmentedByProtocol(uv,CiteBinaryImageModel.iiifProtocolString) match {
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
		val pathUrn:Cite2Urn = DataModelController.propertyUrnFromPropertyName(obj.urn, "path")
		val pathFromCollection:String = obj.propertyValue(pathUrn).toString
		var archivePath:String = pathPrefix + pathFromCollection
		var path:String = archivePath + CiteBinaryImageController.getLocalThumbPath(justUrn) 

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



}
