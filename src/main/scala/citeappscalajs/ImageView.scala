package citeapp

import com.thoughtworks.binding.{Binding, dom}
import com.thoughtworks.binding.Binding.{BindingSeq, Var, Vars}
import scala.scalajs.js
import scala.scalajs.js._
import org.scalajs.dom._
import org.scalajs.dom.ext._
import org.scalajs.dom.raw._
import edu.holycross.shot.cite._
import edu.holycross.shot.ohco2._

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
		{ imageSearchForm.bind }
		{ imageSearchResults.bind }
		{ imageMappedUrn.bind }
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
def imageMappedUrn = {
		<input
		id="image_mappedUrn"
		size={ 30 }
		type="text"
		value=""
		placeholder="Mapped URNs will appear here"
		onchange={ event: Event => {
			 	println("Mapped URN changed")
			}
		}>
		</input>
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
def imageSearchForm = {
	<h2>Search Image Metadata</h2>
	<label for="image_searchImageTextbox">Search text</label>
	<input
		type="text"
		placeholder="Find text in image metadata"
		size={ 20 }
		id="image_searchImageTextbox"/>
	<br/>
	<button
		id="image_searchSubmit"
			onclick={ event: Event => {
					ImageController.updateUserMessage("Searching image metadata. Please be patient…",1)
				}
			}
		>Search Image Metadata</button>
}

/* Image metadata search results */
@dom
def imageSearchResults = {
		<div id="image_searchResults">
			<h2>Search Results</h2>
			<ol>
			{
				for (i <- ImageModel.imageSearchResults) yield {
					<li>
						{ imageUrnSpan(i.urn, i.urn.toString).bind }
						<br/>
						{ i.label }
					</li>
				}
			}
			</ol>
		</div>
}

/* Cited Works List */
@dom
def imageCollectionsContainer = {
	<div id="image_imageCollectionsContainer">
	<h2>Image Collections</h2>
	<ul>
			<li>
			urn <br/> description
			</li>
	</ul>
	</div>
}

/* General-use functions for making clickable URNs */
@dom
def workUrnSpan(urn:CtsUrn, s:String) = {
	<span
	class="app_clickable"
	onclick={ event: Event => {
		O2Controller.insertFirstNodeUrn(urn)
		O2Model.clearPassage
		}
	}>
	{ s }
	</span>
}

@dom
def passageUrnSpan(urn:CtsUrn, s:String) = {
	<span
	class="app_clickable app_urn"
	onclick={ event: Event => {
			CiteMainController.retrieveTextPassage(urn)
		}
	}>
	{ s }
	</span>
}

@dom
def imageUrnSpan(urn:Cite2Urn, s:String) = {
	<span
	class="app_clickable app_urn"
	onclick={ event: Event => {
			CiteMainController.retrieveImage(urn)
		}
	}>
	{ s }
	</span>
}

}
