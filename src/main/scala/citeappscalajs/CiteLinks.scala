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

import monix.execution.Scheduler.Implicits.global
import monix.eval._

import scala.scalajs.js.annotation.JSExport

@JSExportTopLevel("citeapp.CiteLinks")
object CiteLinks {

// Refactor => CiteLinks
@dom
def textLinks(u:CtsUrn) = {
	<ul class="citeLinks_linksList">
		{ CiteLinks.textLinkItem(u).bind }
	</ul>
}

/*
@dom
def objectLinks(contextUrn:Option[Cite2Urn], propVal:Cite2Urn) = {
	g.console.log("CiteLinks: objectLinks")
	val collUrn = propVal.dropSelector
	if (ObjectController.objectIsPresent(propVal)){
		g.console.log("CiteLinks: object is present")
   		ImageModel.imageExtensions match {
				case Some(ie) =>{
					if (ie.extensions(collUrn).size > 0){
						g.console.log("CiteLinks: image and object")
		<ul class="citeLinks_linksList"> {
						 CiteLinks.imageLinkItem(None,propVal).bind 
						 CiteLinks.objectLinkItem(None,propVal).bind 
						 <li>empty</li>
		} </ul>
					} else {
						g.console.log("CiteLinks: object only")
		<ul class="citeLinks_linksList"> {
						{ CiteLinks.objectLinkItem(None,propVal).bind }
		} </ul>
					}
				}	
				case _ => {
		<ul class="citeLinks_linksList"> {
					{ CiteLinks.objectLinkItem(None,propVal).bind }
		} </ul>
				}
		} 
	} else {
		<span> { s"${propVal}"} <br/> {"(This object is not present in the current library.)"} </span>
	}
}
*/

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
									{ ImageView.thumbnailView(contextUrn, propVal).bind }
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
											val task = Task{ ObjectController.changeUrn(propVal)}
											val future = task.runAsync
											//js.timers.setTimeout(200){
												//Future{
													//ObjectController.changeUrn(propVal)
												//}
											//}
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
									val task = Task{ ObjectController.changeUrn(propVal) }
									val future = task.runAsync
									//js.timers.setTimeout(200){
										//Future{
											//ObjectController.changeUrn(propVal) 
										//}
									//}
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
	def objectLinkItem(contextUrn:Option[Cite2Urn],propVal:Cite2Urn) = {
		g.console.log("CiteLinks objectLinkItem")
			<li class="citeLinks_linkItem">
				<a onclick={ event: Event => {
							CiteMainController.retrieveObject(contextUrn,propVal)
					}
			} >View Object </a></li>
	}

	@dom
	def imageLinkItem(contextUrn:Option[Cite2Urn],propVal:Cite2Urn) = {
		g.console.log("CiteLinks imageLinkItem")
			<li class="citeLinks_linkItem">
					<a onclick={ event: Event => {
							CiteMainController.retrieveImage(contextUrn,propVal)
						}
					}>View Image</a> <br/>
					{ ImageView.thumbnailView(contextUrn, propVal).bind }
			</li>
	}

	@dom
	def textLinkItem(u:CtsUrn) = {
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
	def dseLinkItem(u:Urn) = {
		<li class="citeLinks_linkItem">
				View DSE Graph
		</li>
	}

	@dom
	def orcaLinkItem(u:Urn) = {
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
