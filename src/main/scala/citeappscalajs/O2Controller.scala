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
object O2Controller {

		//val o2Model = O2Model

	  val validUrnInField = Var("noUrn") // Options: noUrn, invalidUrn, validUrn

		def changePassage: Unit = {
			var currentPassage = O2Model.passage.get
			O2Model.passage := s"${currentPassage}#"
		}

		def updateUserMessage(msg: String, alert: Int): Unit = {
			O2Model.userMessageVisibility := "app_visible"
			O2Model.userMessage := msg
			alert match {
				case 0 => O2Model.userAlert := "default"
				case 1 => O2Model.userAlert := "notice"
				case 2 => O2Model.userAlert := "warn"
			}
			js.timers.setTimeout(9000){ O2Model.userMessageVisibility := "app_hidden" }
		}

		def validateUrn(urnString: String): Unit = {
			try{
				val newUrn: CtsUrn = CtsUrn(urnString)
				println(s"valid: [${urnString}]")

				validUrnInField := "validUrn"
			} catch {
				case e: Exception => {
					println(s"invalid: [${urnString}]")
					validUrnInField := "invalidUrn"
				}
			}
		}

		def changeUrn(urnString: String): Unit = {
			try {
				val newUrn: CtsUrn = CtsUrn(urnString)
				O2Model.urn := newUrn
				//O2Model.urnPreview := O2Model.textRepository
				validUrnInField := "validUrn"
				updateUserMessage("Current URN changed.",0)
			} catch {
					case e: Exception => {
						validUrnInField := "invalidUrn"
						updateUserMessage("Invalid URN. Current URN not changed.",2)
					}
			}
		}

		def insertFirstNodeUrn(urn: CtsUrn): Unit = {
				val firstUrn = O2Model.textRepository.corpus.firstNode(urn).urn
				js.Dynamic.global.document.getElementById("o2_urnInput").value = firstUrn.toString
		}

		def loadTextRepository(cex: String){
				println("Will load repository.")
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


		updateUserMessage("OHCO2 Module loaded.",0)


}
