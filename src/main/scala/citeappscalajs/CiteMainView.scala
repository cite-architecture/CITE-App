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
object CiteMainView {

	val textView = O2View.o2div

	@dom
	def nGramView = { <div id="NGramContainer"><h2>NGram Analysis</h2></div> }  // will replace with proper mvc, as for textView above

	@dom
	def mainDiv = {
		<div id="MainCITEContainer">
		<h1>CITE Environment</h1>
			 { textView.bind }
			 { nGramView.bind }
		</div>

	}

}
