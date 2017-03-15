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
			O2Model.passage := s"${O2Model.passage}#"
		}

		def updateUserMessage(msg: String, alert: Int): Unit = {
			O2Model.userMessageVisibility := "app_visible"
			O2Model.userMessage := msg
			alert match {
				case 0 => O2Model.userAlert := "default"
				case 1 => O2Model.userAlert := "notice"
				case 2 => O2Model.userAlert := "warn"
			}
			js.timers.setTimeout(12000){ O2Model.userMessageVisibility := "app_hidden" }
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
				validUrnInField := "validUrn"
				updateUserMessage("Current URN changed.",1)
			} catch {
					case e: Exception => {
						validUrnInField := "invalidUrn"
						updateUserMessage("Invalid URN. Current URN not changed.",2)
					}
			}
		}


		updateUserMessage("OHCO2 Module loaded.",0)


}
