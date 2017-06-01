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
object CiteMainModel {

		val userMessage = Var("Main loaded.")
		val userAlert = Var("default")
	    val userMessageVisibility = Var("app_hidden")

		var msgTimer:scala.scalajs.js.timers.SetTimeoutHandle = null

		val currentLibraryMetadataString = Var("No library loaded.")

		val textProtocol = "TextProtocol"
		val objectProtocol = "ObjectProtocol"
		val localImageProtocol = "LocalImageProtocol"

}
