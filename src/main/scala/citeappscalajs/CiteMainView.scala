package citeappscalajs

import com.thoughtworks.binding.{Binding, dom}
import com.thoughtworks.binding.Binding.{BindingSeq, Var, Vars}
import scala.scalajs.js
import scala.scalajs.js._
import org.scalajs.dom._
import org.scalajs.dom.ext._
import org.scalajs.dom.raw._
import org.scalajs.dom.document
import org.scalajs.dom.raw.Event
import org.scalajs.dom.ext.Ajax
import edu.holycross.shot.cite._
import edu.holycross.shot.ohco2._

import scala.scalajs.js.annotation.JSExport


@JSExport
object CiteMainView {

	val textView = O2View.o2div

	@dom
	def filePicker = {
		<input
			type="file"
			onchange={ event: Event => CiteMainController.loadLocalLibrary( event )}
			></input>
	}

	@dom
	def mainMessageDiv = {
			<div id="main_message" class={ s"app_message ${CiteMainModel.userMessageVisibility.bind} ${CiteMainModel.userAlert.bind}" } >
				<p> { CiteMainModel.userMessage.bind }  </p>
			</div>
	}

	@dom
	def mainDiv = {
		<div id="main_Container">
		<h1>CITE Environment</h1>
			{ filePicker.bind }
			{ mainMessageDiv.bind }
			{ textView.bind }
		</div>

	}

}
