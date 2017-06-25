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
object CiteLinks {

// Refactor => CiteLinks
@dom
def textLinks(u:CtsUrn) = {
	<p><a
	onclick={ event: Event => {
		CiteMainController.retrieveTextPassage(u)
	}}>
	<strong>Text Passage:</strong> {u.toString}
	</a>
	</p> 
}

	@dom
	def objectLinks(contextUrn:Option[Cite2Urn], propVal:Cite2Urn) = {
		val collUrn = propVal.dropSelector
		if (ObjectController.objectIsPresent(propVal)){

			ImageModel.imageExtensions match {
				case Some(ie) =>{
					if (ie.extensions(collUrn).size > 0){
						{
							<span>
							{ s"${propVal.toString}" } <br/>
							<a
							onclick={ event: Event => {
								CiteMainController.retrieveObject(contextUrn,propVal)
							}
						} >View as Object</a> |
						<a
						onclick={ event: Event => {
							CiteMainController.retrieveImageLinks(propVal)
						}
					} >Links to Image</a> |
					<a
					onclick={ event: Event => {
						CiteMainController.retrieveImage(contextUrn,propVal)
					}
				}>View as Image</a> <br/>
				{ ObjectView.thumbnailView(contextUrn, propVal).bind }
				</span>
			}
		} else {
			{
				<span>
				<a
				onclick={ event: Event => {
						ObjectController.updateUserMessage("Retrieving object…",1)
						js.timers.setTimeout(500){ ObjectController.changeUrn(propVal) }
					}
				}>
				{ s"${propVal.toString}" }
				</a>
				</span>
			}
		}
}
	case _ => {
		{
			<span>
			<a
				onclick={ event: Event => {
					ObjectController.updateUserMessage("Retrieving object…",1)
					js.timers.setTimeout(500){ ObjectController.changeUrn(propVal) }
				}
			}>
			{ s"${propVal.toString}" }
			</a>
			</span>
		}
	}
}
} else {
	<span> { s"${propVal}"} <br/> {"(This object is not present in the current library.)"} </span>
}
}
}
