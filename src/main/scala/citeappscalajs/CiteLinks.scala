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
	<ul class="citeLinks_linksList">
		<li class="citeLinks_linkItem">
		<a
			onclick={ event: Event => {
				CiteMainController.retrieveTextPassage(u)
			}}>
			{u.toString}
			</a>
		</li>
	</ul>
}

	@dom
	def objectLinks(contextUrn:Option[Cite2Urn], propVal:Cite2Urn) = {
		val collUrn = propVal.dropSelector
		if (ObjectController.objectIsPresent(propVal)){

			ImageModel.imageExtensions match {
				case Some(ie) =>{
					if (ie.extensions(collUrn).size > 0){
						{
							<ul class="citeLinks_linksList">

								<li class="citeLinks_linkItem">
									<a
									onclick={ event: Event => {
											CiteMainController.retrieveObject(contextUrn,propVal)
										}
									} >View Object</a>
								</li>
								<li class="citeLinks_linkItem">
									<a
									onclick={ event: Event => {
											CiteMainController.retrieveImageLinks(propVal)
										}
									} >Links to Image</a>
								</li>
								<li class="citeLinks_linkItem">
									<a
										onclick={ event: Event => {
											CiteMainController.retrieveImage(contextUrn,propVal)
										}
									}>View as Image</a> <br/>
									{ ObjectView.thumbnailView(contextUrn, propVal).bind }
								</li>
							</ul>
						}
					} else {
						{
							<ul class="citeLinks_linksList">
								<li class="citeLinks_linkItem">
									<a
										onclick={ event: Event => {
											ObjectController.updateUserMessage("Retrieving object…",1)
											js.timers.setTimeout(500){ ObjectController.changeUrn(propVal) }
										}
									}>
										{ s"${propVal.toString}" }
									</a>
								</li>
							</ul>
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

	@dom
	def objectLink(contextUrn:Option[Cite2Urn],propVal:Cite2Urn):Unit = {
			<li class="citeLinks_linkItem"
					onclick={ event: Event => {
							CiteMainController.retrieveObject(contextUrn,propVal)
					}
			} >View Object</li>
	}

	@dom
	def imageLink(contextUrn:Option[Cite2Urn],propVal:Cite2Urn):Unit = {
			<li class="citeLinks_linkItem">
					<a onclick={ event: Event => {
							CiteMainController.retrieveImage(contextUrn,propVal)
						}
					}>View Image</a> <br/>
					{ ObjectView.thumbnailView(contextUrn, propVal).bind }
			</li>
	}

	@dom
	def textLink(u:CtsUrn):Unit = {
		<li class="citeLinks_linkItem">
			<a
				onclick={ event: Event => {
					CiteMainController.retrieveTextPassage(u)
				}}>
				{u.toString}
			</a>
		</li>
	}

	@dom
	def dseLink(u:Urn):Unit = {
		<li class="citeLinks_linkItem">
				View DSE Graph
		</li>
	}

	@dom
	def orcaLink(u:Urn):Unit = {
		<li class="citeLinks_linkItem">
				View Reading
		</li>
	}

	def objectIsPresent(u:Cite2Urn):Boolean = {
		true
	}
	def objectIsImage(u:Cite2Urn):Boolean = {
		true
	}
	def urnInDSE(u:Cite2Urn):Boolean = {
		true
	}
	def urnIsRelation(u:Cite2Urn):Boolean = {
		true
	}
	def workIsPresent(u:Cite2Urn):Boolean = {
		true
	}

}
