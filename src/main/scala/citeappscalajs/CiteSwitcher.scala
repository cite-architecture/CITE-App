package citeapp

import com.thoughtworks.binding.{Binding, dom}
import com.thoughtworks.binding.Binding.{BindingSeq, Var, Vars}
import scala.scalajs.js
import scala.scalajs.js._
import js.annotation._
import collection.mutable
import collection.mutable._
import scala.scalajs.js.Dynamic.{ global => g }
import org.scalajs.dom._
import org.scalajs.dom.ext._
import org.scalajs.dom.raw._
import edu.holycross.shot.cite._
import edu.holycross.shot.ohco2._
import edu.holycross.shot.citeobj._

import scala.scalajs.js.annotation.JSExport

@JSExport
object CiteSwitcher {


	@dom
	def objectLinks(u:Cite2Urn) = {
		val collUrn = u.dropSelector
		if (ImageModel.imageExtensions.extensions(collUrn).size > 0){
			{
				<span>
				{ s"${u.toString}" } <br/>
				<a >View as Object</a> |
				<a >View as Image</a>
				</span>
			}
		} else {
			{
				<span>
				{ s"${u.toString}" }
				<a >View as Object</a>
				</span>
			}
		}
	}



}
