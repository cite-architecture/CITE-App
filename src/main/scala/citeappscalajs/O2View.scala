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
object O2View {

	val o2Cont = O2Controller
	val o2Model = O2Model


  @dom
  def o2div = {

		val urnValidatingKeyUpHandler = { event: KeyboardEvent =>
			(event.currentTarget, event.keyCode) match {
				case (input: html.Input, KeyCode.Enter) => {
					println("enter")
					event.preventDefault()
					o2Cont.changeUrn(s"${input.value.toString}")
					input.value = ""
				}
				case(input: html.Input, _) =>  o2Cont.validateUrn(s"${input.value.toString}")
				case _ =>  println("keydown else")
			}
		}

		<div id="Ohco2TextContainer">
			<h2>Texts</h2>

			<div>
				<p>Message: { o2Model.userMessage.bind }  </p>
			</div>

			<p>
				{ o2Model.passage.bind }
			</p>

			<button onclick={ event: Event => o2Cont.changePassage }>Change Passage</button>

			<p>URN: { o2Model.urn.bind.toString } </p>

			<input class={ s"${o2Cont.validUrnInField.bind}" } size={ 40 } type="text" onkeyup={ urnValidatingKeyUpHandler }></input>

			<span class={ s"${o2Cont.validUrnInField.bind}" } id="validUrnFlag"></span>

		</div>
	}

}
