package citeapp

import com.thoughtworks.binding.{Binding, dom}
import com.thoughtworks.binding.Binding.{BindingSeq, Var, Vars}
import scala.scalajs.js
import scala.scalajs.js._
import js.annotation._
import scala.concurrent._
//import ExecutionContext.Implicits.global
import collection.mutable
import collection.mutable._
import scala.scalajs.js.Dynamic.{ global => g }
import org.scalajs.dom._
import org.scalajs.dom.ext._
import org.scalajs.dom.raw._
import edu.holycross.shot.cite._
import edu.holycross.shot.ohco2._
import edu.holycross.shot.citeobj._
import edu.holycross.shot.scm._

import monix.execution.Scheduler.Implicits.global
import monix.eval._

import scala.scalajs.js.annotation.JSExport

@JSExportTopLevel("citeapp.DataModelView")
object DataModelView {

@dom
def objectLinks(contextUrn:Option[Cite2Urn], propVal:Cite2Urn) = {
	<ul class="citeLinks_linksList">
		{ DataModelView.objectLinkItem(contextUrn, propVal).bind }
	</ul>
}

@dom
def textLinks(u:CtsUrn) = {
	<ul class="citeLinks_linksList">
		{ DataModelView.textLinkItem(u).bind }
	</ul>
}

@dom
def textLinkItem(u:CtsUrn) = {
	DataModelController.hasText(u) match {
		case true => {
			<li class="citeLinks_linkItem">
				<a
					onclick={ event: Event => {
						DataModelController.retrieveTextPassage(u)
					}}>
					{u.toString}
				</a>
			</li>
		}
		case _ => {
			<li class="citeLinks_linkItem">
					{u.toString} <br/> 
					[text not in this library]
			</li>
		}
	}
}

@dom
	def objectLinkItem(contextUrn:Option[Cite2Urn],propVal:Cite2Urn) = {
		g.console.log("CiteLinks objectLinkItem")
		DataModelController.hasObject(propVal) match {
			case true => {
				<li class="citeLinks_linkItem">
					<a onclick={ event: Event => {
							DataModelController.retrieveObject(contextUrn,propVal)
						}
				} >View Object </a></li>
			}
			case _ => {
				<li class="citeLinks_linkItem">Object not in Library</li>
			}
		}
	}	

}
