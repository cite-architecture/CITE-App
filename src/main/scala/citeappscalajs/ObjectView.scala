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
import edu.holycross.shot.citeobj._
import scala.scalajs.js.Dynamic.{ global => g }
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
		class={ s"${ ObjectController.validObjectUrnInField.bind || ObjectController.validObjectUrnInField.bind }" }
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
			disabled={
						(ObjectController.validObjectUrnInField.bind == false) &&
						(ObjectController.validCollectionUrnInField.bind == false)
					 }

> {
	if ( ObjectController.validObjectUrnInField.bind == true ){
		"Retrieve object"
	} else {
		if ( ObjectController.validCollectionUrnInField.bind == true ){
			"Browse collection"
		} else {
			"Invalid URN"
		}
	}

}
</button>
}


/* Passage Container */
@dom
def objectContainer = {
	<div id="object_objectContainer" data:bgtext="No Object"
	class={ s"""${if( ObjectModel.objects.bind.size == 0 ){ "object_empty" } else {"object_not_empty"}}""" }
	>

<div class="app_witch">
  <input id="object_toggle1" class="cmn-toggle cmn-toggle-round-flat" type="checkbox"></input>
  <label for="object_toggle1"></label>
</div>

	{
		for (obj <- ObjectModel.objects ) yield {
			<p>{ obj.toString }</p>
		}
	}

	</div>
}



/* Cited Works List */
@dom
def objectCollectionsContainer = {
	<div id="object_objectCollectionsContainer">
	<h2>CITE Collections</h2>
	<ul>
	{
		for (cc <- ObjectModel.collections) yield {
			<li>
			{ collectionUrnSpan( cc.urn ).bind } <br/>
			{ cc.collectionLabel }
			{ if(cc.isOrdered) "[ordered]" else "[unordered]" }
			<br/>
			{ ObjectModel.countObjects(cc.urn).toString } objects.

			</li>
		}
	}
	</ul>
	</div>
}



/* General-use functions for making clickable URNs */
@dom
def collectionUrnSpan(urn:Cite2Urn) = {
	<span
	class="app_clickable"
	onclick={ event: Event => {
		ObjectController.insertFirstObjectUrn(urn)
		ObjectModel.clearObject
		}
	}>
	{ urn.toString }
	</span>
}



}
