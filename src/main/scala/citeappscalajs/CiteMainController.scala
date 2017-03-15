package citeappscalajs
import com.thoughtworks.binding.{Binding, dom}
import com.thoughtworks.binding.Binding.{BindingSeq, Var, Vars}
import com.thoughtworks.binding.Binding.{Var, Vars}
import com.thoughtworks.binding.dom
import scala.scalajs.js
import scala.scalajs.js._
import org.scalajs.dom.document
import org.scalajs.dom.raw.Event
import org.scalajs.dom.ext.Ajax
import scala.concurrent
											.ExecutionContext
											.Implicits
											.global

import edu.holycross.shot.cite._
import edu.holycross.shot.ohco2._
import scala.scalajs.js.annotation.JSExport

@JSExport
object CiteMainController {

	@JSExport
	def main(libUrl: String): Unit = {

		//val mainModel = CiteMainModel
		//val mainView = CiteMainView

		def updateUserMessage(msg: String, alert: Int): Unit = {
			CiteMainModel.userMessageVisibility := "app_visible"
			CiteMainModel.userMessage := msg
			alert match {
				case 0 => CiteMainModel.userAlert := "default"
				case 1 => CiteMainModel.userAlert := "notice"
				case 2 => CiteMainModel.userAlert := "warn"
			}
			js.timers.setTimeout(12000){ CiteMainModel.userMessageVisibility := "app_hidden" }
		}

		def reportCEXLoad: Unit = {
			val cexSize = CiteMainModel.cexString.get.split("\n").size
			val msgString = s"Library file loaded: ${cexSize} lines."
			updateUserMessage(msgString,1)
		}

		def passOhco2Data: Unit = {
			O2Model.corpus = Corpus(CiteMainModel.cexString.get,"\t")
			O2Controller.updateUserMessage(s"Got data from main controller: ${O2Model.corpus.citedWorks.size} cited works.",1)
		}

		val remoteCall = Ajax.get(libUrl).onSuccess { case xhr =>
			CiteMainModel.cexString := xhr.responseText
			reportCEXLoad
			passOhco2Data

		}

		dom.render(document.body, CiteMainView.mainDiv)

	}

}
