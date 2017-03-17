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
	val ngramView = NGView.nGdiv

	@dom
	def filePicker = {
		<span id="app_filePickerSpan">
			<label for="app_filePicker">Choose a local <code>.cex</code> file</label>
			<input
				id="app_filePicker"
				type="file"
				onchange={ event: Event => CiteMainController.loadLocalLibrary( event )}
				></input>
		</span>
	}

	@dom
	def mainMessageDiv = {
			<div id="main_message" class={ s"app_message ${CiteMainModel.userMessageVisibility.bind} ${CiteMainModel.userAlert.bind}" } >
				<p> { CiteMainModel.userMessage.bind }  </p>
			</div>
	}

	@dom
	def mainDiv = {

		<header>
			{ filePicker.bind }
			CITE Environment
			<span id="app_header_versionInfo">version 1.0.0</span>
		</header>

		<article id="main_Container">

			{ mainMessageDiv.bind }
			<div class="app_tabs">

				<div class="app_tab">
					<input type="radio" id="tab-1" name="tab-group-1" checked={ true }/>
					<label class="tab_label" for="tab-1">Browse Texts</label>
						<div class="content">
						 { textView.bind }
						</div>
				</div>

				<div class="app_tab">
					<input type="radio" id="tab-2" name="tab-group-1" checked={ false }/>
					<label class="tab_label" for="tab-2">Explore Texts</label>
						<div class="content">
						 { ngramView.bind }
						</div>
				</div>


			</div>
		</article>
		<footer>
		{ footer.bind }
		</footer>

	}


	@dom
	def footer = {
		<p>
		CITE/CTS is ©2002–2017 Neel Smith and Christopher Blackwell. This implementation of the CITE data models was written by Neel Smith and Christopher Blackwell using <a href="https://www.scala-lang.org">Scala</a>, <a href="http://www.scala-js.org">Scala-JS</a>, and <a href="https://github.com/ThoughtWorksInc/Binding.scala">Binding.scala</a>. Licensed under the <a href="https://www.gnu.org/licenses/gpl-3.0.en.html">GPL 3.0</a>. Sourcecode on <a href="https://github.com/cite-architecture/ScalaJS-CITE-Environment">GitHub</a>.
		</p>
	}

}
