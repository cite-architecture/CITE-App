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
object ObjectView {


	// HTML Div holding messages
	@dom
	def objectMessageDiv = {
		<div id="object_message" class={ s"app_message ${ObjectModel.userMessageVisibility.bind} ${ObjectModel.userAlert.bind}"  }>
		<p>{ ObjectModel.userMessage.bind }  </p>
		</div>
	}


	// HTML Div: main div for object display
	@dom
	def objectDiv = {
		val urnValidatingKeyUpHandler = { event: KeyboardEvent =>
			(event.currentTarget, event.keyCode) match {
				case (input: html.Input, KeyCode.Enter) => {
					event.preventDefault()
					ObjectController.changeUrn(s"${input.value.toString}")
					//input.value = ""
				}
				case(input: html.Input, _) =>  ObjectController.validateUrn(s"${input.value.toString}")
				case _ =>
			}
		}

		<div id="object_Container">

		<div id="object_sidebar" class="app_sidebarDiv">
		{ objectCollectionsContainer.bind }
		</div>

		{ objectMessageDiv.bind }

		<p id="object_reportingCurrentUrn" class="app_reportingCurrentUrn"> { ObjectModel.urn.bind.toString } </p>


		<p id="object_urnInputP">
		<input
		class={ s"${ObjectController.validUrnInField.bind}" }
		id="object_urnInput"
		size={ 40 }
		type="text"
		value={ ObjectModel.urn.bind.toString }
		onkeyup={ urnValidatingKeyUpHandler }>
		</input>

	{ ObjectView.retrieveObjectButton.bind }

	<br/>
	</p>

	{ objectContainer.bind }

	</div>
}

@dom
def retrieveObjectButton = {
	<button
			onclick={ event: Event => {
				val s:String = js.Dynamic.global.document.getElementById("object_urnInput").value.toString
				ObjectModel.urn := Cite2Urn(s)
				ObjectController.updateUserMessage("Retrieving object…",1)
				js.timers.setTimeout(500){ ObjectController.changeObject }
				}
			}
			disabled={ (ObjectController.validUrnInField.bind == false) }
> {
	if ( ObjectController.validUrnInField.bind == true ){
		"Retrieve object"
	} else {
		"No object identified"
	}

}
</button>
}


/* Passage Container */
@dom
def objectContainer = {
	<div id="object_objectContainer"> </div>
}



/* Cited Works List */
@dom
def objectCollectionsContainer = {
	<div id="object_objectCollectionsContainer">
	<h2>CITE Collections</h2>
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



}
