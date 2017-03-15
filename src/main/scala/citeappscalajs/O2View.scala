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


 	// HTML Div holding messages
	@dom
	def o2messageDiv = {
			<div id="o2_message" class={ s"app_message ${O2Model.userMessageVisibility.bind} ${O2Model.userAlert.bind}"  }>
				<p>{ O2Model.userMessage.bind }  </p>
			</div>
	}


	// HTML Div: main div for text work
  @dom
  def o2div = {

		val urnValidatingKeyUpHandler = { event: KeyboardEvent =>
			(event.currentTarget, event.keyCode) match {
				case (input: html.Input, KeyCode.Enter) => {
					println("enter")
					event.preventDefault()
					O2Controller.changeUrn(s"${input.value.toString}")
					//input.value = ""
				}
				case(input: html.Input, _) =>  O2Controller.validateUrn(s"${input.value.toString}")
				case _ =>  println("keydown else")
			}
		}

		<div id="o2_Container">
			<h2>Texts</h2>

			{ o2messageDiv.bind }

			<p>
				{ O2Model.passage.bind }
			</p>

			<button onclick={ event: Event => O2Controller.changePassage }>Change Passage</button>

			<p>URN: { O2Model.urn.bind.toString } </p>

			<input
				class={ s"${O2Controller.validUrnInField.bind}" }
				size={ 40 }
				type="text"
				value={ O2Model.urn.bind.toString }
				onkeyup={ urnValidatingKeyUpHandler }>
				</input>

			<span class={ s"${O2Controller.validUrnInField.bind}" } id="o2_validUrnFlag"></span>

		</div>
	}

}
