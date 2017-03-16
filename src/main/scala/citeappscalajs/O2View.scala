package citeappscalajs

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
				{ toolsContainter.bind }
			</div>

			{ o2messageDiv.bind }

			<p id="o2_urnInput">
				<input
					class={ s"${O2Controller.validUrnInField.bind}" }
					size={ 40 }
					type="text"
					value={ O2Model.urn.bind.toString }
					onkeyup={ urnValidatingKeyUpHandler }>
				</input>
				<span class={ s"${O2Controller.validUrnInField.bind}" } id="o2_validUrnFlag"></span>
				<br/>
				<span>Current: { O2Model.urn.bind.toString } </span>
			</p>

				{ passageContainer.bind }

			<button onclick={ event: Event => O2Controller.changePassage }>Change Passage</button>


		</div>
	}

/* Passage Container */
@dom
def passageContainer = {
	<div id="o2_passageContainer">

		<p>
				{ O2Model.passage.bind }
		</p>
		{ prevButton.bind }
		{ nextButton.bind }
	</div>
}


/* Navigation Buttons */
@dom
def nextButton = {
	<button class="navButton"> → </button>
}

@dom
def prevButton = {
	<button class="navButton"> ← </button>
}

/* Cited Works List */
@dom
def citedWorksContainer = {
	<div id="o2_citedWorksContainer">
	<h2>Cited Works</h2>
	<ul>
		<li class="app_clickable">Herodotus, Histories, English</li>
		<li class="app_clickable">Herodotus, Histories, Greek</li>
		<li class="app_clickable">Plutarch, Life of Pericles, Greek</li>
		<li class="app_clickable">Ammianus Marcelinus, Rerum Gestarum, Latin</li>
	</ul>
	</div>
}

/* Analytical Tools Div */
@dom
def  toolsContainter = {
	<div id="o2_toolsContainer">
	<h2>Analytical Tools</h2>
	{ nGramForm.bind }
	</div>
}

/* NGram Form */
@dom
def nGramForm = {
	<label for="o2_ngram-nlist">N-Gram</label>
	<select id="o2_ngram_nlist">
		<option value="1">1</option>
		<option value="2">2</option>
		<option value="3">3</option>
		<option value="4">4</option>
		<option value="5">5</option>
		<option value="6">6</option>
		<option value="7">7</option>
		<option value="8">8</option>
		<option value="9">9</option>
	</select>
	<label for="o2_ngram_minOccurrances">Occurs</label>
	<input
			id="o2_ngram_minOccurrances"
			type="text"
			size={ 4 }
			value={ O2Model.nGramThreshold.bind.toString }
			onchange={ event: Event => O2Controller.validateIntegerEntry( event )}
			/>
	<br/>
	<select id="o2_ngram_nGramScopeOption"><option value="current">Current Text</option><option value="corpus">Whole Corpus</option></select>
	<br/>
	<label for="o2_ngram_filterStringField">Filter String</label>
	<input type="text" size={ 20 } id="o2_ngram_filterStringField"/>
	<br/>
	<label for="o2_ngram_ignorePuncBox">Ignore Punctuation</label>
	<input type="checkbox" id="o2_ngram_ignorePuncBox" checked={ true }/>
	<br/>
	<input id="o2_ngram_Submit" type="Submit"></input>
}



}
