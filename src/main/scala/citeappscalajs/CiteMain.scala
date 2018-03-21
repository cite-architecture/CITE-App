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

import scala.scalajs.js
import scala.scalajs.js._
import js.annotation._
import scala.scalajs.js.annotation.JSExport

@JSExportTopLevel("CiteMain")
class CiteMain(remoteUrl:String, localImagePath:String) {

	@JSExport
	def main(): Unit = {
		CiteMainController.main(remoteUrl: String, localImagePath:String)
	}


}
