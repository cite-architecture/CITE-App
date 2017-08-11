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
import monix.execution.Scheduler.Implicits.global
import monix.eval._


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

/* Previous Searches */
@dom
def previousSearchMenu = {
	<div
		class={
			{ if (NGModel.pastQueries.bind.size < 1) { "dropdown empty" } else {"dropdown"} }
		}

	>
			<span>Previous Searches</span>
			{ NGView.previousSearches.bind }
	</div>
}


@dom
def previousSearches = {
	<div class="dropdown-content">
		{
			for ( q <- NGModel.pastQueries) yield {
					<p
							onclick={ event: Event => {
								NGController.loadQuery(q)
								val task = Task { NGController.executeQuery(q) }
								val future = task.runAsync
								/*
								js.timers.setTimeout(200){
									Future {
										NGController.loadQuery(q)
										NGController.executeQuery(q)
									}
								}
								*/
							}
							}
					>
					{ q.toString}
					</p>
			}
		}
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
		NGController.clearResults

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

	// HTML Div: main div for text work
	@dom
	def nGdiv = {

		<div id="ngram_Container">

		<div id="ngram_sidebar" class="app_sidebarDiv">
		{ NGView.previousSearchMenu.bind }
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

	<h2>Scope for Exploration</h2>
	<select id="ngram_nGramScopeOption">
			<option value="current"> { NGModel.shortWorkLabel.bind }</option>
			<option value="corpus">Whole Corpus</option>
	</select>

	<br/>
	<h2>N-Gram Tools</h2>
	{ nGramForm.bind }
	<h2>String Search</h2>
	{ stringSearchForm.bind }
	<h2>Token Search</h2>
	{ tokenSearchForm.bind }
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
	onchange={ event: Event => NGController.validateThresholdEntry( event )}
	/>
	<br/>

	<label for="ng_ngram_filterStringField">Filter String</label>
	<input
		type="text"
		placeholder="String to filter results"
		size={ 20 }
		id="ngram_filterStringField"/>
	<br/>
	<label for="ngram_ignorePuncBox">Ignore Punctuation</label>
	<input type="checkbox" id="ngram_ignorePuncBox" checked={ true }/>
	<br/>
	<button
		id="ngram_Submit"
			onclick={ event: Event => {
					NGController.updateUserMessage("Getting N-Grams. Please be patient…",1)
					val task = Task{ NGController.nGramQuery}
					val future = task.runAsync
					/*
					js.timers.setTimeout(200){
						Future{ NGController.nGramQuery }
					}
					*/
				}
			}
		>Query for N-Grams</button>
}

/* String Search Form */
@dom
def stringSearchForm = {
	<label for="stringSearch_Input">String Search:</label>
	<input
		type="text"
		size={ 20 }
		placeholder="String to find"
		id="stringSearch_Input"/>
	<button
		id="stringSearch_Submit"
			onclick={ event: Event => {
					NGController.updateUserMessage("Searching for string. Please be patient…",1)
					val task = Task{ NGController.stringSearchQuery }
					val future = task.runAsync
					/*
					js.timers.setTimeout(200){
						Future{ NGController.stringSearchQuery }
					}
					*/
				}
			}
		>Search</button>
}

/* Token Search Form */
@dom
def tokenSearchForm = {
	<p>Enter one or more word-tokens, separated by a space:</p>
	<label for="tokenSearch_Input">Token Search:</label>
	<input
		type="text"
		placeholder="word1 word2 word3"
		size={ 40 }
		id="tokenSearch_Input"/>
	<br/>
	<label for="tokenSearch_proximityInput">Proximity:</label>
	<input
	id="tokenSearch_proximityInput"
	type="text"
	size={ 4 }
	value={ NGModel.tokenSearchProximity.bind.toString }
	onchange={ event: Event => NGController.validateProximityEntry( event )}
	/>
	<button
		id="tokenSearch_Submit"
			onclick={ event: Event => {
					NGController.updateUserMessage("Conducting token search. Please be patient…",1)
					val task = Task{NGController.tokenSearchQuery}
					val future = task.runAsync
					/*
					js.timers.setTimeout(200){
						Future{ NGController.tokenSearchQuery }
					}
					*/
				}
			}
		>Search</button>
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
	<p id="ngram_query"> { NGModel.nGramQueryReport.bind }</p>
	<div id="ngram_ngrams">
	<p> {
		for (ng <- NGModel.nGramResults ) yield {
			<span class="ngram_ngram">
			({ (ng.count - 1).toString })
				<span class="ngram_string app_clickable"
				onclick={ event: Event => {

					NGController.updateUserMessage(s"Getting URNs for '${ng.s}'. Please be patient…",1)
					val task = Task{ NGController.getUrnsForNGram( ng.s ) }
					val future = task.runAsync
					/*
					js.timers.setTimeout(200){
						Future{ NGController.getUrnsForNGram( ng.s ) }
					}
					*/
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
		<h2>Citation Results</h2>
		<p id="ngram_urn_query"> { NGModel.otherQueryReport.bind }</p>
		<div id="ngram_urns">
			{ citationResultsList.bind }
		</div>
	</div>
}


/* NGram URN Results List */
@dom
def citationResultsList = {
		<ol class={ if ( NGModel.citationResults.bind.size > 10 ){ "cols" } else { "" } } >

		{
			for (ng <- NGModel.citationResults) yield {
				<li>
				{
					val s:String = s"${O2Model.textRepository.catalog.label(ng.urn.get)}, ${ng.urn.get.passageComponent}"

					passageUrnSpan( ng.urn.get, s ).bind
				}

				{ s"  “${ng.kwic.get}”" }

				</li>
			}
		}
		</ol>
}


/* Download link for NGram URNs */
/* Will enable when we figure out what this should look like */
/*
@dom
def nGramUrnDownload = {
	<a
		id="ngram_urns_download"
		data:download="ngram-urns"
		href = {
			"data:text/plain;charset=utf-8," + scala.scalajs.js.URIUtils.encodeURIComponent("urns here")
		}
		>Download N-Grams URNs</a>
}
*/


}
