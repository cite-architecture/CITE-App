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
object NGController {

/*
wholeCorpus.getNGram(currentUrn.dropPassage, filterString, n, occ, ignorePunc)
wholeCorpus.getNGram(filterString, n, occ, ignorePunc)
*/

	def nGramQuery:Unit = {

		NGController.clearResults

		// This is ugly as hell, but what are you going to do… HTML forms.
		val n:Int = js.Dynamic.global.document.getElementById("ngram_nlist").value.toString.toInt
		val occ:Int = js.Dynamic.global.document.getElementById("ngram_minOccurrances").value.toString.toInt
		val ignorePuncString: String = js.Dynamic.global.document.getElementById("ngram_ignorePuncBox").checked.toString
		val ignorePunc: Boolean = (ignorePuncString == "true")
		val filterString: String = js.Dynamic.global.document.getElementById("ngram_filterStringField").value.toString

		var corpusOrUrn:String = ""
		NGController.updateUserMessage("Getting N-Gram. Please be patient…",0)
		val timeStart = new js.Date().getTime()

		if (O2Model.textRepository == null){
			NGController.updateUserMessage("No library loaded.",2)
		} else {
			NGModel.nGramResults.get.clear
			NGModel.citationResults.get.clear
			js.Dynamic.global.document.getElementById("ngram_nGramScopeOption").value.toString match {
				case "current" => {
					corpusOrUrn = NGModel.urn.get.toString
					for ( sc <- NGModel.getNGram(NGModel.urn.get, filterString, n, occ, ignorePunc).histogram ) {
						NGModel.nGramResults.get += sc
					}
				}
				case _ => {
					corpusOrUrn = "whole corpus"
					for ( sc <- NGModel.getNGram(filterString, n, occ, ignorePunc).histogram ) {
						NGModel.nGramResults.get += sc
					}
				}
			}
		}

		val timeEnd = new js.Date().getTime()

		NGModel.nGramQuery := s"""Fetched ${NGModel.nGramResults.get.size} N-Grams in ${(timeEnd - timeStart)/1000} seconds: n = ${n}; threshold = ${occ}; ignore-punctuation = ${ignorePunc}; filtered-by = '${filterString}'; queried on "${corpusOrUrn}"."""

		NGController.updateUserMessage(s"Fetched ${NGModel.nGramResults.get.size} NGrams in ${(timeEnd - timeStart)/1000} seconds.",0)

	}

	def stringSearchQuery:Unit = {

		NGController.clearResults

			val searchString: String = js.Dynamic.global.document.getElementById("stringSearch_Input").value.toString
			var corpusOrUrn:String = ""
			NGController.updateUserMessage(s"""Searching for "${searchString}". Please be patient…""",0)
			val timeStart = new js.Date().getTime()

			if (O2Model.textRepository == null){
				NGController.updateUserMessage("No library loaded.",2)
			} else {
				NGModel.citationResults.get.clear
				js.Dynamic.global.document.getElementById("ngram_nGramScopeOption").value.toString match {
					case "current" => {
						corpusOrUrn = NGModel.urn.get.toString
						// do search on current text
						val tempCorpus = O2Model.textRepository.corpus ~~ NGModel.urn.get
						for (n <- tempCorpus.find(searchString).nodes){
								NGModel.citationResults.get += NGModel.SearchResult(Var(n.urn), Var(n.kwic(searchString,20)))
						}
					}
					case _ => {
						corpusOrUrn = "whole corpus"
						for (n <- O2Model.textRepository.corpus.find(searchString).nodes){
								NGModel.citationResults.get += NGModel.SearchResult(Var(n.urn), Var(n.kwic(searchString,30)))
						}
					}
				}
			}

		val timeEnd = new js.Date().getTime()
		NGModel.nGramUrnQuery := s"""Found ${NGModel.citationResults.get.size} URNs containing "${searchString}"  in ${(timeEnd - timeStart)/1000} seconds; searched in "${corpusOrUrn}"."""

		NGController.updateUserMessage(s"Found ${NGModel.citationResults.get.size} URNs in ${(timeEnd - timeStart)/1000} seconds.",0)
	}

	def tokenSearchQuery:Unit = {

		NGController.clearResults

			val searchString: String = js.Dynamic.global.document.getElementById("tokenSearch_Input").value.toString
			println(s"Searching for ${searchString}")
	}

	def clearResults: Unit = {
		NGModel.nGramResults.get.clear
		NGModel.citationResults.get.clear

		NGModel.nGramQuery := ""
		NGModel.nGramUrnQuery := ""

	}

	def getUrnsForNGram(s: String): Unit = {
		val timeStart = new js.Date().getTime()
		val occ:Int = js.Dynamic.global.document.getElementById("ngram_minOccurrances").value.toString.toInt
		val ignorePuncString: String = js.Dynamic.global.document.getElementById("ngram_ignorePuncBox").checked.toString
		val ignorePunc: Boolean = (ignorePuncString == "true")
		var corpusOrUrn:String = ""
		NGController.updateUserMessage("Getting N-Gram. Please be patient…",0)

		if (O2Model.textRepository == null){
			NGController.updateUserMessage("No library loaded.",2)
		} else {
			NGModel.citationResults.get.clear
			js.Dynamic.global.document.getElementById("ngram_nGramScopeOption").value.toString match {
				case "current" => {
					corpusOrUrn = NGModel.urn.get.toString
					val tempVector = NGModel.getUrnsForNGram(NGModel.urn.get, s,ignorePunc)
					val tempCorpus = O2Model.textRepository.corpus ~~ tempVector
					for ( n <- tempCorpus.nodes) {
							NGModel.citationResults.get += NGModel.SearchResult(Var(n.urn), Var(n.kwic(s,30)))
					}
				}
				case _ => {
					corpusOrUrn = "whole corpus"
					val tempVector = NGModel.getUrnsForNGram(s,ignorePunc)
					val tempCorpus = O2Model.textRepository.corpus ~~ tempVector
					for ( n <- tempCorpus.nodes) {
							NGModel.citationResults.get += NGModel.SearchResult(Var(n.urn), Var(n.kwic(s,30)))
					}
				}
			}
		}

		val timeEnd = new js.Date().getTime()

		NGModel.nGramUrnQuery := s"Fetched ${NGModel.citationResults.get.size} URNs in ${(timeEnd - timeStart)/1000} seconds: threshold = ${occ}; ignore-punctuation = ${ignorePunc}; queried on '${corpusOrUrn}'."

		NGController.updateUserMessage(s"Fetched ${NGModel.citationResults.get.size} URNs  in ${(timeEnd - timeStart)/1000} seconds.",0)

	}

	def updateUserMessage(msg: String, alert: Int): Unit = {
		NGModel.userMessageVisibility := "app_visible"
		NGModel.userMessage := msg
		alert match {
			case 0 => NGModel.userAlert := "default"
			case 1 => NGModel.userAlert := "wait"
			case 2 => NGModel.userAlert := "warn"
		}
		js.timers.setTimeout(900000){ NGModel.userMessageVisibility := "app_hidden" }
	}

	@dom
	def preloadUrn = {
		NGModel.urn := O2Model.textRepository.corpus.firstNode.urn
		NGModel.updateShortWorkLabel
	}

	def validateThresholdEntry(thisEvent: Event):Unit = {
		val thisTarget = thisEvent.target.asInstanceOf[org.scalajs.dom.raw.HTMLInputElement]
		val testText = thisTarget.value.toString
		try{
			val mo: Int = testText.toInt
			NGModel.nGramThreshold := mo
		} catch {
			case e: Exception => {
				val badMo: String = testText
				NGModel.nGramThreshold := 3
				NGController.updateUserMessage(s"Minimum Occurrances value must be an integer. '${badMo}' is not an integer.", 2)
				js.Dynamic.global.document.getElementById("ngram_minOccurrances").value =  NGModel.nGramThreshold.get.toString
			}
		}
	}
	def validateProximityEntry(thisEvent: Event):Unit = {
		val thisTarget = thisEvent.target.asInstanceOf[org.scalajs.dom.raw.HTMLInputElement]
		val testText = thisTarget.value.toString
		try{
			val mo: Int = testText.toInt
			NGModel.tokenSearchProximity := mo
		} catch {
			case e: Exception => {
				val badMo: String = testText
				NGModel.tokenSearchProximity := 3
				NGController.updateUserMessage(s"Proximity value must be an integer. '${badMo}' is not an integer.", 2)
				js.Dynamic.global.document.getElementById("tokenSearch_proximityInput").value =  NGModel.tokenSearchProximity.get.toString
			}
		}
	}

}
