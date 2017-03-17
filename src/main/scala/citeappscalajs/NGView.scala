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
object NGView {


	// HTML Div holding messages
	@dom
 	def nGmessageDiv = {
		<div
			id="ngram_message"
			class={
				s"app_message ${NGModel.userMessageVisibility.bind} ${NGModel.userAlert.bind}"
			}>
		<p>{
			NGModel.userMessage.bind
			}
		</p>
		</div>
	}

/* Cited Works List */
@dom
def citedWorksContainer = {
	<div id="ngram_citedWorksContainer">
	<h2>Works in this Corpus</h2>
	<ul>
	{
		for (urn <- NGModel.citedWorks) yield {
			<li>
			{ workUrnSpan( urn, O2Model.textRepository.catalog.label(urn) ).bind }
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
		NGModel.urn := urn.dropPassage
		NGModel.updateShortWorkLabel
	}
}>
	{ s }
	</span>
}

@dom
def passageUrnSpan(urn:CtsUrn, s:String) = {
	<span
	class="app_clickable"
	onclick={ event: Event => {
			CiteMainController.retrieveTextPassage(urn)
		}
	}>
	{ s }
	</span>
}

	// HTML Div: main div for text work
	@dom
	def nGdiv = {

		<div id="ngram_Container">

		<div id="ngram_sidebar" class="app_sidebarDiv">
		{ NGView.toolsContainer.bind }
		{ NGView.citedWorksContainer.bind }
		</div>

		{ nGmessageDiv.bind }

		<p id="ngram_reportingCurrentUrn" class="app_reportingCurrentUrn"> { NGModel.urn.bind.toString } </p>

		{ nGramSpace.bind }
		{ nGramUrnSpace.bind }

		</div>
	}


/* Analytical Tools Div */
@dom
def  toolsContainer = {
	<div id="ngram_toolsContainer">
	<h2>N-Gram Tools</h2>
	{ nGramForm.bind }
	</div>
}

/* NGram Form */
@dom
def nGramForm = {
	<label for="ngram_nlist">N-Gram</label>
	<select id="ngram_nlist">
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
	<label for="ngram_minOccurrances">Occurs</label>
	<input
	id="ngram_minOccurrances"
	type="text"
	size={ 4 }
	value={ NGModel.nGramThreshold.bind.toString }
	onchange={ event: Event => NGController.validateIntegerEntry( event )}
	/>
	<br/>

<select id="ngram_nGramScopeOption">
		<option value="current"> { NGModel.shortWorkLabel.bind }</option>
		<option value="corpus">Whole Corpus</option>
</select>

	<br/>
	<label for="ng_ngram_filterStringField">Filter String</label>
	<input type="text" size={ 20 } id="ngram_filterStringField"/>
	<br/>
	<label for="ngram_ignorePuncBox">Ignore Punctuation</label>
	<input type="checkbox" id="ngram_ignorePuncBox" checked={ true }/>
	<br/>
	<button
		id="ngram_Submit"
			onclick={ event: Event => {
					NGController.updateUserMessage("Getting N-Gram. Please be patient…",1)
					js.timers.setTimeout(500){ NGController.nGramQuery }
				}
			}
		>Query for N-Grams</button>
}


/* N-Gram Results */
@dom
def nGramSpace = {
	<div id="ngram_container"
	class={
		if (NGModel.nGramResults.get == null){
			"app_visible"
		} else {
			 "app_visible"
		}
	}
	>
	<h2>N-Grams</h2>
	<p id="ngram_query"> { NGModel.nGramQuery.bind }</p>
	<div id="ngram_ngrams">
	<p> {
		for (ng <- NGModel.nGramResults ) yield {
			<span class="ngram_ngram">
			({ ng.count.toString })
				<span class="ngram_string app_clickable"
				onclick={ event: Event => {

					NGController.updateUserMessage(s"Getting URNs for '${ng.s}'. Please be patient…",1)

					js.timers.setTimeout(500){ NGController.getUrnsForNGram( ng.s ) }
				} }
				>
				{ ng.s }
				</span>
			</span>
		}
	}</p>
	</div>
	</div>
}

/* N-Gram URNs */
@dom
def nGramUrnSpace = {
	<div id="ngram_urns_container">
	<h2>URNs for N-Gram</h2>
	<p id="ngram_urn_query"> { NGModel.nGramUrnQuery.bind }</p>
	<div id="ngram_urns">
		<ol>
		{
			for (ngurn <- NGModel.nGramUrns) yield {
				<li>
				{
					val s:String = s"${O2Model.textRepository.catalog.label(ngurn)}, ${ngurn.passageComponent}"
					passageUrnSpan( ngurn, s ).bind
				}
				</li>
			}
		}
		</ol>
	</div>
	</div>
}


}
