package citeapp
import com.thoughtworks.binding.{Binding, dom}
import com.thoughtworks.binding.Binding.{BindingSeq, Var, Vars}
import com.thoughtworks.binding.Binding.{Var, Vars}
import com.thoughtworks.binding.dom
import org.scalajs.dom.document
import org.scalajs.dom.raw.Event
import org.scalajs.dom.ext.Ajax
import scala.concurrent
											.ExecutionContext
											.Implicits
											.global

import scala.scalajs.js.annotation.JSExport

@JSExport
class CiteMain(remoteUrl: String, remoteFileDelimiter: String) {

	@JSExport
	def main(): Unit = {
		CiteMainController.main(remoteUrl: String, remoteFileDelimiter: String)
	}


}
