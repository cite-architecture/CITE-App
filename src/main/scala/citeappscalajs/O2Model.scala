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
object O2Model {

	val passage = Var("passage")
	val urn = Var(CtsUrn("urn:cts:greekLit:tlg0012.tlg001.msA:1.1"))

	val userMessage = Var("No message.")
	val userAlert = Var(false)

}
