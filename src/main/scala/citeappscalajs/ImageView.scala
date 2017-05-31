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


import scala.scalajs.js.annotation.JSExport

@JSExport
object ImageView {


	// HTML Div holding messages
	@dom
	def imageMessageDiv = {
		<div id="image_message" class={ s"app_message ${ImageModel.userMessageVisibility.bind} ${ImageModel.userAlert.bind}"  }>
		<p>{ ImageModel.userMessage.bind }  </p>
		</div>
	}


	// HTML Div: main div for image display
	@dom
	def imageDiv = {
		val urnValidatingKeyUpHandler = { event: KeyboardEvent =>
			(event.currentTarget, event.keyCode) match {
				case (input: html.Input, KeyCode.Enter) => {
					event.preventDefault()
					ImageController.changeUrn(s"${input.value.toString}")
					//input.value = ""
				}
				case(input: html.Input, _) =>  ImageController.validateUrn(s"${input.value.toString}")
				case _ =>
			}
		}

		<div id="image_Container">

		<div id="image_sidebar" class="app_sidebarDiv">
		{ imageCollectionsContainer.bind }
		{ imageMappedDataDiv.bind }
		</div>

		{ imageMessageDiv.bind }

		<p id="image_reportingCurrentUrn" class="app_reportingCurrentUrn"> { ImageModel.displayUrn.bind.toString } </p>


		<p id="image_urnInputP">
		<input
		class={ s"${ImageController.validUrnInField.bind}" }
		id="image_urnInput"
		size={ 70 }
		type="text"
		value={ ImageModel.urn.bind.toString }
		onkeyup={ urnValidatingKeyUpHandler }>
		</input>

	{ ImageView.retrieveImageButton.bind }

	<br/>
	</p>

	{ imageContainer.bind }

	</div>
}

@dom
def retrieveImageButton = {
	<button
			onclick={ event: Event => {
				val s:String = js.Dynamic.global.document.getElementById("image_urnInput").value.toString
				//ImageModel.urn := Cite2Urn(s)
				ImageController.updateUserMessage("Retrieving image…",1)
				js.timers.setTimeout(500){
					  ImageController.changeUrn(s)
						//ImageController.changeImage
					}
				}
			}
			disabled={ (ImageController.validUrnInField.bind == false) }
> {
	if ( ImageController.validUrnInField.bind == true ){
		"Retrieve Image"
	} else {
		"Invalid URN"
	}

}
</button>
}


/* Passage Container */
@dom
def imageContainer = {
	<div id="image_imageContainer"> </div>
}

/* Search Image Properties Forms */
@dom
def imageMappedDataDiv = {
	<h2>Mapped Data</h2>
	<div id="image_mappedData">
		{
			for (iroi <- ImageModel.imageROIs) yield {
				iroi.roiData match {
					case Some(u) => {
						{ mappedUrnP(iroi).bind }
					}
					case _ => {
					   <p>Unmapped Region of Interest</p>
					}

				}
			}
		}
	</div>
}

@dom
def mappedUrnP(iroi:ImageModel.ImageROI) = {
	val u:Urn = iroi.roiData.get
	val idx:Int = iroi.index
	val pId = ImageModel.idForMappedUrn(idx)
	g.console.log(s"ImageView: ${u}, ${idx}, ${pId}")
	<p class={ s"image_mappedUrn image_roiGroup_${iroi.roiGroup} ${ImageModel.idForMappedUrn(idx)}"}
		id={ pId } >

		{ mappedUrnSpan(u).bind }

	</p>
}

@dom
def mappedUrnSpan(u:Urn) = {
	 { u match {
			case CtsUrn(_) => {
				<a onclick={ event: Event => { CiteMainController.retrieveTextPassage(u.asInstanceOf[CtsUrn]) }}>
				<strong>Text Passage:</strong> {u.toString}
				</a>
			}
			case Cite2Urn(_) => {
				val c2u = u.asInstanceOf[Cite2Urn].dropProperty
				val collUrn = c2u.dropSelector
				if (ObjectController.objectIsPresent(c2u)){
					if (ImageModel.imageExtensions.extensions(collUrn).size > 0){
						{
							<span>
							{ s"${c2u.toString}" } <br/>
							<a
							onclick={ event: Event => {
								CiteMainController.retrieveObject(None,c2u)
								}
							} >View as Object</a> |
							<a
								onclick={ event: Event => {
									CiteMainController.retrieveImage(None,c2u)
								}
							}>View as Image</a> <br/>
							{ ObjectView.thumbnailView(None, c2u).bind }
							</span>
						}
					} else {
						{
							<span>
							<a
							onclick={ event: Event => {
								CiteMainController.retrieveObject(None,c2u)
								}
							}>
								{ s"${c2u.toString}" }
							</a>
							</span>
						}
					}
				} else {
					<span> { s"${c2u}"} <br/> {"(This object is not present in the current library.)"} </span>
				}
			}
			case _ => {
				<a>Unknown</a>
			}
		}
	}
}


/* Cited Works List */
@dom
def imageCollectionsContainer = {
	<div id="image_imageCollectionsContainer">
	<h2>Image Collections</h2>
		{
			<ul>
			{ for (ic <- ImageModel.imageCollections ) yield {
				<li>
					<a
					onclick={ event: Event => {
						CiteMainController.retrieveObject(None,ic)
						}
					}>
						{ s"${ic}" }
					</a>
					<br/>
					{ ObjectController.labelForCollection(ic) }
				</li>
				}
			}
			</ul>
		}
	</div>
}


}
