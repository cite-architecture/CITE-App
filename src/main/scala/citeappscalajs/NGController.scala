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
object NGController {

/*
wholeCorpus.getNGram(currentUrn.dropPassage, filterString, n, occ, ignorePunc)
wholeCorpus.getNGram(filterString, n, occ, ignorePunc)
*/

	def nGramQuery:Unit = {
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

		NGModel.nGramQuery := s"Fetched ${NGModel.nGramResults.get.size} N-Grams in ${(timeEnd - timeStart)/1000} seconds: n = ${n}; threshold = ${occ}; ignore-punctuation = ${ignorePunc}; filtered-by = '${filterString}'; queried on '${corpusOrUrn}'."

		NGController.updateUserMessage(s"Fetched ${NGModel.nGramResults.get.size} NGrams in ${(timeEnd - timeStart)/1000} seconds.",0)

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
			NGModel.nGramUrns.get.clear
			js.Dynamic.global.document.getElementById("ngram_nGramScopeOption").value.toString match {
				case "current" => {
					corpusOrUrn = NGModel.urn.get.toString
					for ( ngurn <- NGModel.getUrnsForNGram(NGModel.urn.get, s,ignorePunc)) {
						NGModel.nGramUrns.get += ngurn
					}
				}
				case _ => {
					corpusOrUrn = "whole corpus"
					for ( ngurn <- NGModel.getUrnsForNGram(s,ignorePunc) ) {
						NGModel.nGramUrns.get += ngurn
					}
				}
			}
		}

		val timeEnd = new js.Date().getTime()

		NGModel.nGramUrnQuery := s"Fetched ${NGModel.nGramUrns.get.size} URNs in ${(timeEnd - timeStart)/1000} seconds: threshold = ${occ}; ignore-punctuation = ${ignorePunc}; queried on '${corpusOrUrn}'."

		NGController.updateUserMessage(s"Fetched ${NGModel.nGramUrns.get.size} URNs  in ${(timeEnd - timeStart)/1000} seconds.",0)

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

	def validateIntegerEntry(thisEvent: Event):Unit = {
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

}
