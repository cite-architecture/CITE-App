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
									<img id="image_previewImg" src="https://dummyimage.com/100x100/aaa/000&amp;text=localImage"/>
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
									<img id="image_previewImg" src="https://dummyimage.com/100x100/000/fff&amp;text=remoteImage"/>
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



}
