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

		val o2Model = O2Model

	  val validUrnInField = Var("noUrn") // Options: noUrn, invalidUrn, validUrn

		val testFeedbackk = {
			println("loaded")
		}

		def changePassage: Unit = {
			o2Model.passage := s"${o2Model.passage}#"
		}

		def updateUserMessage(msg: String, alert: Boolean): Unit = {
			o2Model.userMessage := msg
			o2Model.userAlert := true
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
				o2Model.urn := newUrn
				validUrnInField := "validUrn"
			} catch {
					case e: Exception => {
						validUrnInField := "invalidUrn"
						updateUserMessage("badurn",true)
					}
			}
		}

}
