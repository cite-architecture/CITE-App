package citeappscalajs
import com.thoughtworks.binding.Binding.{Var, Vars}
import com.thoughtworks.binding.dom
import org.scalajs.dom.document
import org.scalajs.dom.raw.Event

import scala.scalajs.js.annotation.JSExport

@JSExport
object CiteMain {

  @JSExport
  def main(): Unit = {

		dom.render(document.body, CiteMainView.mainDiv)

  }

}
