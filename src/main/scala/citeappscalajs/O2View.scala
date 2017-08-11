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
import scala.concurrent._
//import ExecutionContext.Implicits.global

import scala.scalajs.js.annotation.JSExport
import monix.execution.Scheduler.Implicits.global
import monix.eval._

@JSExport
object O2View {


	// HTML Div holding messages
	@dom
	def o2messageDiv = {
		<div id="o2_message" class={ s"app_message ${O2Model.userMessageVisibility.bind} ${O2Model.userAlert.bind}"  }>
		<p>{ O2Model.userMessage.bind }  </p>
		</div>
	}


	// HTML Div: main div for text work
	@dom
	def o2div = {
		val urnValidatingKeyUpHandler = { event: KeyboardEvent =>
			(event.currentTarget, event.keyCode) match {
				case (input: html.Input, KeyCode.Enter) => {
					event.preventDefault()
					O2Controller.changeUrn(s"${input.value.toString}")
					//input.value = ""
				}
				case(input: html.Input, _) =>  O2Controller.validateUrn(s"${input.value.toString}")
				case _ =>
			}
		}

		<div id="o2_Container">

		<div id="02_sidebar" class="app_sidebarDiv">
		{ citedWorksContainer.bind }
		</div>

		{ o2messageDiv.bind }

		<p id="o2_reportingCurrentUrn" class="app_reportingCurrentUrn"> { O2Model.urn.bind.toString } </p>


		<p id="o2_urnInputP">
		<input
		class={ s"${O2Controller.validUrnInField.bind}" }
		id="o2_urnInput"
		size={ 40 }
		type="text"
		value={ O2Model.urn.bind.toString }
		onkeyup={ urnValidatingKeyUpHandler }>
		</input>

	{ O2View.retrievePassageButton.bind }
	{ O2View.seeAllVersionsButton.bind }

	<br/>
	</p>

	{ passageContainer.bind }

	</div>
}

@dom
def retrievePassageButton = {
	<button
			onclick={ event: Event => {
				val s:String = js.Dynamic.global.document.getElementById("o2_urnInput").value.toString
				O2Model.urn := CtsUrn(s)
				O2Controller.updateUserMessage("Retrieving passage…",1)
				val task = Task{ O2Controller.changePassage }
				val future = task.runAsync
//				js.timers.setTimeout(200){
					//Future{ O2Controller.changePassage }
//				}
			}
		}
		disabled={ (O2Controller.validUrnInField.bind == false) }
> {
	if ( O2Controller.validUrnInField.bind == true ){
		"Retrieve Passage"
	} else {
		"Invalid URN"
	}

}
</button>
}

@dom
def seeAllVersionsButton = {
	<button
		disabled = { if (O2Model.versionsForCurrentUrn.bind > 0) false else true }
		onclick = { event: Event => {
				O2Model.displayUrn := O2Model.collapseToWorkUrn(O2Model.urn.get)
				O2Model.displayNewPassage(O2Model.displayUrn.get)
		}}
	>
		See All Versions of Passage
	</button>
}

/* Passage Container */
@dom
def passageContainer = {
	<div id="o2_passageContainer">
		<div id="o2_navButtonContainer_top">
			{ prevButton.bind }
			{ nextButton.bind }
		</div>
		<div id="o2_xmlPassageContainer"></div>
		<div id="o2_navButtonContainer_bottom">
			{ prevButton.bind }
			{ nextButton.bind }
		</div>

	</div>

}


/* Navigation Buttons */
@dom
def nextButton = {
	<button
	class="navButton"
	onclick={ event: Event => O2Controller.getNext }
	disabled= {
		(O2Model.currentNext.bind == None)
	}
	> → </button>
}

@dom
def prevButton = {
	<button
	class="navButton"
	onclick={ event: Event => O2Controller.getPrev }
	disabled= {
		(O2Model.currentPrev.bind == None)
	}
	> ← </button>
}

/* Cited Works List */
@dom
def citedWorksContainer = {
	<div id="o2_citedWorksContainer">
	<h2>Works in this Corpus</h2>
	<ul>
	{
		for (urn <- O2Model.citedWorks) yield {
			<li>
			{ workUrnSpan( urn, O2Model.textRepository.catalog.label(urn) ).bind }
			<br/>( { O2Model.textRepository.catalog.entriesForUrn(urn)(0).citationScheme  } )
			</li>
		}
	}
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
	class="app_clickable"
	onclick={ event: Event => println(s"Passage-click: ${urn}") }>
	{ s }
	</span>
}


}
