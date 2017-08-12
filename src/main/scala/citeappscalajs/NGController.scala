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

import scala.scalajs.js.annotation.JSExport
import js.annotation._

@JSExportTopLevel("citeapp.NGController")
object NGController {

	def returnCorpusScope: Option[CtsUrn] = {
		val corpusOrUrn:Option[CtsUrn] = js.Dynamic.global.document.getElementById("ngram_nGramScopeOption").value.toString match {
			case "current" => { Some(NGModel.urn.value.dropPassage) }
			case _ => { None }
		}
		corpusOrUrn
	}

	def setCorpusScope(qurn:Option[CtsUrn]):Unit = {
		qurn match {
				case Some(urn) => {
					NGModel.urn.value = urn.dropPassage
					js.Dynamic.global.document.getElementById("ngram_nGramScopeOption").value = "current"
				}
				case None => js.Dynamic.global.document.getElementById("ngram_nGramScopeOption").value = "corpus"
		}
	}

	def constructStringSearchObject:NGModel.StringSearch = {
		val s: String = js.Dynamic.global.document.getElementById("stringSearch_Input").value.toString
		val corpusOrUrn:Option[CtsUrn] = NGController.returnCorpusScope
		val ssq = NGModel.StringSearch(s, corpusOrUrn)
		ssq
	}

	def constructTokenSearchObject:NGModel.TokenSearch = {
		val s: String = js.Dynamic.global.document.getElementById("tokenSearch_Input").value.toString
		val prox = NGModel.tokenSearchProximity.value
		val searchVector:Vector[String] = s.split(" ").toVector
		val corpusOrUrn:Option[CtsUrn] = NGController.returnCorpusScope
		val tsq = NGModel.TokenSearch(searchVector, prox, corpusOrUrn)
		tsq
	}


	def constructNGramQueryObject:NGModel.NGramQuery = {
		val n:Int = js.Dynamic.global.document.getElementById("ngram_nlist").value.toString.toInt
		val occ:Int = js.Dynamic.global.document.getElementById("ngram_minOccurrances").value.toString.toInt
		val ignorePuncString: String = js.Dynamic.global.document.getElementById("ngram_ignorePuncBox").checked.toString
		val ignorePunc: Boolean = (ignorePuncString == "true")
		val filterString: String = js.Dynamic.global.document.getElementById("ngram_filterStringField").value.toString
		val corpusOrUrn:Option[CtsUrn] = NGController.returnCorpusScope

		val ngq = NGModel.NGramQuery(n, occ, filterString, ignorePunc, corpusOrUrn )
		ngq

	}

	def loadQuery(q:NGModel.StringSearch) = {
		NGController.clearInputs
		NGController.clearResults
		js.Dynamic.global.document.getElementById("stringSearch_Input").value = q.fs
		NGController.setCorpusScope(q.urn)
	}

	def loadQuery(q:NGModel.TokenSearch) = {
		NGController.clearInputs
		NGController.clearResults
		js.Dynamic.global.document.getElementById("tokenSearch_Input").value = q.tt.mkString(" ")
		NGModel.tokenSearchProximity.value = q.p
		NGController.setCorpusScope(q.urn)
	}

	def loadQuery(q:NGModel.NGramQuery) = {
		NGController.clearInputs
		NGController.clearResults
		js.Dynamic.global.document.getElementById("ngram_nlist").value = q.n.toString
		js.Dynamic.global.document.getElementById("ngram_minOccurrances").value = q.t.toString
		js.Dynamic.global.document.getElementById("ngram_ignorePuncBox").checked = q.ip.toString
		js.Dynamic.global.document.getElementById("ngram_filterStringField").value = q.fs
		NGController.setCorpusScope(q.urn)
	}

def executeQuery(q:NGModel.StringSearch) = {
		NGController.clearResults
		NGController.updateUserMessage(s"""Searching for string "${q.fs}". Please be patient…""",1)
		val timeStart = new js.Date().getTime()
		NGModel.nGramResults.value.clear
		NGModel.citationResults.value.clear
		q.urn match {
			case Some(urn) => {
				val tempCorpus = NGModel.findString(NGModel.urn.value.dropPassage, q.fs)
				for (n <- tempCorpus.nodes){
						NGModel.citationResults.value += NGModel.SearchResult(Var(n.urn), Var(n.kwic(q.fs,20)))
				}
			}
			case _ => {
				val tempCorpus = NGModel.findString(q.fs)
				for (n <- tempCorpus.nodes){
						NGModel.citationResults.value += NGModel.SearchResult(Var(n.urn), Var(n.kwic(q.fs,30)))
				}
			}
		}
		val timeEnd = new js.Date().getTime()
		NGModel.otherQueryReport.value = s"""${q.toString} Time: ${(timeEnd - timeStart)/1000} seconds. Results: ${NGModel.citationResults.value.size}."""
		NGController.updateUserMessage(s"Found ${NGModel.citationResults.value.size} passages in ${(timeEnd - timeStart)/1000} seconds.",0)
	}



def executeQuery(q:NGModel.TokenSearch):Unit = {
		NGController.clearResults
		NGController.updateUserMessage(s"""Searching for tokens "${q.tt.mkString(" ")}". Please be patient…""",1)
		val timeStart = new js.Date().getTime()
		NGModel.nGramResults.value.clear
		NGModel.citationResults.value.clear

		var tempCorpus:Corpus = null
		var foundCorpus:Corpus = null

		if (O2Model.textRepository == null){
			NGController.updateUserMessage("No library loaded.",2)
		} else {
		q.urn match {
			case Some(urn) => {
					tempCorpus = O2Model.textRepository.corpus ~~ NGModel.urn.value
				}
				case _ => {
					tempCorpus = O2Model.textRepository.corpus
			}
		}
	}

	// If there is only one token, do findToken
	q.tt.size match {
		case 0 => {
			NGController.updateUserMessage(s"No token entered.",2)
			foundCorpus = null
		}
		case 1 => {
			foundCorpus = tempCorpus.findToken(q.tt(0))
		}
		case _ => {
			foundCorpus = tempCorpus.findTokensWithin(q.tt,q.p)
		}
	}

	if ((tempCorpus != null) && (foundCorpus != null)){
		for (n <- foundCorpus.nodes){
				NGModel.citationResults.value += NGModel.SearchResult(Var(n.urn), Var(n.kwic(s" ${q.tt(0)} ",30)))
		}
	}

	val timeEnd = new js.Date().getTime()
	NGModel.otherQueryReport.value = s"""${q.toString} Time: ${(timeEnd - timeStart)/1000}. Found: ${NGModel.citationResults.value.size}."""

	NGController.updateUserMessage(s"Found ${NGModel.citationResults.value.size} passages in ${(timeEnd - timeStart)/1000} seconds.",0)
}




	def executeQuery(q:NGModel.NGramQuery) = {
		NGController.clearResults
		NGController.updateUserMessage("Getting N-Grams. Please be patient…",0)
		val timeStart = new js.Date().getTime()
		NGModel.nGramResults.value.clear
		NGModel.citationResults.value.clear
		q.urn match {
			case Some(urn) => {
				for ( sc <- NGModel.getNGram(urn, q.fs, q.n, q.t, q.ip).histogram ) {
					NGModel.nGramResults.value += sc
				}
			}
			case _ => {
				for ( sc <- NGModel.getNGram(q.fs, q.n, q.t, q.ip).histogram ) {
					NGModel.nGramResults.value += sc
				}
			}
		}
		val timeEnd = new js.Date().getTime()
		NGModel.nGramQueryReport.value = s"""${q.toString} Time: ${(timeEnd - timeStart)/1000} seconds. Results: ${NGModel.nGramResults.value.size}."""
		NGController.updateUserMessage(s"Fetched ${NGModel.nGramResults.value.size} NGrams in ${(timeEnd - timeStart)/1000} seconds.",0)
	}

	def executeQuery(q:NGModel.CtsQuery):Unit = {
		q.getClass.getName match {
			case "citeapp.NGModel$NGramQuery" => {
				NGController.executeQuery(q.asInstanceOf[NGModel.NGramQuery])
			}
			case "citeapp.NGModel$StringSearch" => {
				NGController.executeQuery(q.asInstanceOf[NGModel.StringSearch])
			}
			case "citeapp.NGModel$TokenSearch" => {
				NGController.executeQuery(q.asInstanceOf[NGModel.TokenSearch])
			}
			case _ => NGController.updateUserMessage("Unrecognized type of search!",2)
		}
	}

	def loadQuery(q:NGModel.CtsQuery):Unit = {
		q.getClass.getName match {
			case "citeapp.NGModel$NGramQuery" => {
				NGController.loadQuery(q.asInstanceOf[NGModel.NGramQuery])
			}
			case "citeapp.NGModel$StringSearch" => {
				NGController.loadQuery(q.asInstanceOf[NGModel.StringSearch])
			}
			case "citeapp.NGModel$TokenSearch" => {
				NGController.loadQuery(q.asInstanceOf[NGModel.TokenSearch])
			}
			case _ => NGController.updateUserMessage("Unrecognized type of search!",2)
		}
	}

	def nGramQuery:Unit = {
		val newQuery = NGController.constructNGramQueryObject
		if (O2Model.textRepository == null){
			NGController.updateUserMessage("No library loaded.",2)
		} else {
			NGModel.pastQueries.value += newQuery
			NGController.executeQuery(newQuery)
		}
	}

	def tokenSearchQuery:Unit = {
		val newQuery = NGController.constructTokenSearchObject
		if (O2Model.textRepository == null){
			NGController.updateUserMessage("No library loaded.",2)
		} else {
			NGModel.pastQueries.value += newQuery
			NGController.executeQuery(newQuery)
		}
	}



	def stringSearchQuery:Unit = {
		val newQuery = NGController.constructStringSearchObject

		if (O2Model.textRepository == null){
			NGController.updateUserMessage("No library loaded.",2)
		} else {
			NGModel.pastQueries.value += newQuery
			NGController.executeQuery(newQuery)
		}
	}


	def clearResults: Unit = {
		NGModel.nGramResults.value.clear
		NGModel.citationResults.value.clear
		NGModel.nGramQueryReport.value = ""
		NGModel.otherQueryReport.value = ""
	}

	def clearInputs: Unit = {
		/*
		js.Dynamic.global.document.getElementById("ngram_filterStringField").value = ""
		js.Dynamic.global.document.getElementById("stringSearch_Input").value = ""
		js.Dynamic.global.document.getElementById("tokenSearch_Input").value = ""
		*/
	}

	def clearHistory:Unit = {
		NGModel.pastQueries.value.clear
	}

	def getUrnsForNGram(s: String): Unit = {
		val timeStart = new js.Date().getTime()
		val occ:Int = js.Dynamic.global.document.getElementById("ngram_minOccurrances").value.toString.toInt
		val ignorePuncString: String = js.Dynamic.global.document.getElementById("ngram_ignorePuncBox").checked.toString
		val ignorePunc: Boolean = (ignorePuncString == "true")
		var corpusOrUrn:String = ""
		NGController.updateUserMessage("Getting N-Grams. Please be patient…",0)

		if (O2Model.textRepository == null){
			NGController.updateUserMessage("No library loaded.",2)
		} else {
			NGModel.citationResults.value.clear
			// begin
			//val corpusOrUrn:Option[CtsUrn] = NGController.returnCorpusScope
			NGController.returnCorpusScope match {
					case Some(urn:CtsUrn) => {
						val tempVector = NGModel.getUrnsForNGram(urn, s,ignorePunc)
						val tempCorpus = O2Model.textRepository.corpus ~~ tempVector
						for ( n <- tempCorpus.nodes) {
								NGModel.citationResults.value += NGModel.SearchResult(Var(n.urn), Var(n.kwic(s,30)))
						}
					}
					case None => {
						val tempVector = NGModel.getUrnsForNGram(s,ignorePunc)
						val tempCorpus = O2Model.textRepository.corpus ~~ tempVector
						for ( n <- tempCorpus.nodes) {
								NGModel.citationResults.value += NGModel.SearchResult(Var(n.urn), Var(n.kwic(s,30)))
						}
					}
			}
			// end
		}

		val timeEnd = new js.Date().getTime()

		NGModel.otherQueryReport.value = s"""Fetched ${NGModel.citationResults.value.size} passages in ${(timeEnd - timeStart)/1000} seconds: threshold = ${occ}; ignore-punctuation = ${ignorePunc}; queried on '${ NGController.returnCorpusScope match { case Some(urn:CtsUrn) => urn.toString; case _ => "Whole Corpus" }}'."""

		NGController.updateUserMessage(s"Fetched ${NGModel.citationResults.value.size} passages  in ${(timeEnd - timeStart)/1000} seconds.",0)

	}

	def updateUserMessage(msg: String, alert: Int): Unit = {
		NGModel.userMessageVisibility.value = "app_visible"
		NGModel.userMessage.value = msg
		alert match {
			case 0 => NGModel.userAlert.value = "default"
			case 1 => NGModel.userAlert.value = "wait"
			case 2 => NGModel.userAlert.value = "warn"
		}
		js.timers.clearTimeout(NGModel.msgTimer)
		NGModel.msgTimer = js.timers.setTimeout(6000){ NGModel.userMessageVisibility.value = "app_hidden" }
	}

	@dom
	def preloadUrn = {
		NGModel.urn.value = O2Model.textRepository.corpus.firstNode(O2Model.textRepository.corpus.citedWorks(0)).urn
		NGModel.updateShortWorkLabel
	}

	def validateThresholdEntry(thisEvent: Event):Unit = {
		val thisTarget = thisEvent.target.asInstanceOf[org.scalajs.dom.raw.HTMLInputElement]
		val testText = thisTarget.value.toString
		try{
			val mo: Int = testText.toInt
			NGModel.nGramThreshold.value = mo
		} catch {
			case e: Exception => {
				val badMo: String = testText
				NGModel.nGramThreshold.value = 3
				NGController.updateUserMessage(s"Minimum Occurrances value must be an integer. '${badMo}' is not an integer.", 2)
				js.Dynamic.global.document.getElementById("ngram_minOccurrances").value =  NGModel.nGramThreshold.value.toString
			}
		}
	}
	def validateProximityEntry(thisEvent: Event):Unit = {
		val thisTarget = thisEvent.target.asInstanceOf[org.scalajs.dom.raw.HTMLInputElement]
		val testText = thisTarget.value.toString
		try{
			val mo: Int = testText.toInt
			NGModel.tokenSearchProximity.value = mo
		} catch {
			case e: Exception => {
				val badMo: String = testText
				NGModel.tokenSearchProximity.value = 3
				NGController.updateUserMessage(s"Proximity value must be an integer. '${badMo}' is not an integer.", 2)
				js.Dynamic.global.document.getElementById("tokenSearch_proximityInput").value =  NGModel.tokenSearchProximity.value.toString
			}
		}
	}

}
