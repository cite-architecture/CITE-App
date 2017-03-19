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
object O2Controller {


	val validUrnInField = Var(false)


	/* A lot of work gets done here */
	def changePassage: Unit = {
		val timeStart = new js.Date().getTime()
		val newUrn: CtsUrn = O2Model.urn.get
		O2Model.getPassage(newUrn)
		O2Model.getPrevNextUrn(O2Model.urn.get)
		val timeEnd = new js.Date().getTime()
		O2Controller.updateUserMessage(s"Fetched text in ${(timeEnd - timeStart)/1000} seconds.",0)
	}


	def updateUserMessage(msg: String, alert: Int): Unit = {
		O2Model.userMessageVisibility := "app_visible"
		O2Model.userMessage := msg
		alert match {
			case 0 => O2Model.userAlert := "default"
			case 1 => O2Model.userAlert := "wait"
			case 2 => O2Model.userAlert := "warn"
		}
		js.timers.setTimeout(90000){ O2Model.userMessageVisibility := "app_hidden" }
	}


	def validateUrn(urnString: String): Unit = {
		try{
			val newUrn: CtsUrn = CtsUrn(urnString)
			validUrnInField := true
		} catch {
			case e: Exception => {
				validUrnInField := false
			}
		}
	}

	def getNext:Unit = {
		if (O2Model.currentNext.get != None){
			changeUrn(O2Model.currentNext.get.get)
		}
	}

	def getPrev:Unit = {
		if (O2Model.currentPrev.get != None){
			changeUrn(O2Model.currentPrev.get.get)
		}
	}

	def changeUrn(urnString: String): Unit = {
		changeUrn(CtsUrn(urnString))
	}


	def changeUrn(urn: CtsUrn): Unit = {
		try {
			O2Model.urn := urn
			validUrnInField := true
			O2Controller.updateUserMessage("Retrieving passage…",1)
			js.timers.setTimeout(500){ O2Controller.changePassage }

		} catch {
			case e: Exception => {
				validUrnInField := false
				updateUserMessage("Invalid URN. Current URN not changed.",2)
			}
		}
	}


	def insertFirstNodeUrn(urn: CtsUrn): Unit = {
		val firstUrn = O2Model.textRepository.corpus.firstNode(urn).urn
		js.Dynamic.global.document.getElementById("o2_urnInput").value = firstUrn.toString
		validUrnInField := true
	}


	@dom
	def preloadUrn = {
		O2Model.urn := O2Model.textRepository.corpus.firstNode.urn
		O2Controller.validUrnInField := true
	}


	def validateIntegerEntry(thisEvent: Event):Unit = {
		val thisTarget = thisEvent.target.asInstanceOf[org.scalajs.dom.raw.HTMLInputElement]
		val testText = thisTarget.value.toString
		try{
			val mo: Int = testText.toInt
			O2Model.nGramThreshold := mo
		} catch {
			case e: Exception => {
				val badMo: String = testText
				O2Model.nGramThreshold := 3
				O2Controller.updateUserMessage(s"Minimum Occurrances value must be an integer. '${badMo}' is not an integer.", 2)
				js.Dynamic.global.document.getElementById("o2_ngram_minOccurrances").value =  O2Model.nGramThreshold.get.toString
			}
		}
	}

}
