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



	def updateUserMessage(msg: String, alert: Int): Unit = {
		NGModel.userMessageVisibility := "app_visible"
		NGModel.userMessage := msg
		alert match {
			case 0 => NGModel.userAlert := "default"
			case 1 => NGModel.userAlert := "notice"
			case 2 => NGModel.userAlert := "warn"
		}
		js.timers.setTimeout(90000){ NGModel.userMessageVisibility := "app_hidden" }
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
