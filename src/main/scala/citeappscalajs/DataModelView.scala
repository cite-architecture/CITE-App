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
		{ CiteBinaryImageView.imageThumbItem(propVal, CiteBinaryImageModel.imgUseLocal.bind).bind }
		{ DataModelView.objectLinkItem(contextUrn, propVal).bind }
		{ DataModelView.imageLinkItem(contextUrn, propVal).bind }
	</ul>
}

@dom
def textLinks(contextUrn:Option[Cite2Urn], u:CtsUrn) = {
	<ul class="citeLinks_linksList">
		{ DataModelView.textLinkItem(contextUrn, u).bind }
	</ul>
}

@dom
def textLinkItem(contextUrn:Option[Cite2Urn], u:CtsUrn) = {
	DataModelController.hasText(u) match {
		case true => {
			<li class="citeLinks_linkItem">
				<a
					onclick={ event: Event => {
						DataModelController.retrieveTextPassage(contextUrn, u)
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
		//g.console.log("CiteLinks objectLinkItem")
		DataModelController.hasObject(propVal) match {
			case true => {
				<li class="citeLinks_linkItem">
					<a onclick={ event: Event => {
							//g.console.log(s"clicked: ${propVal}")
							DataModelController.retrieveObject(contextUrn,propVal)
						}
				} >View Object </a></li>
			}
			case _ => {
				<li class="citeLinks_linkItem">Object not in Library</li>
			}
		}
	
	}	

	@dom
	def imageLinkItem(contextUrn:Option[Cite2Urn],propVal:Cite2Urn) = {
		//g.console.log("CiteLinks objectLinkItem")
		CiteBinaryImageController.implementedByImageCollObjects(propVal) match {
			case Some(uv) => {
				<li class="citeLinks_linkItem">
					{ DataModelView.iiifApiLink(propVal, uv, contextUrn).bind }
					{ DataModelView.dzLink(propVal, uv, contextUrn).bind }
				</li>
			}
			case None => {
				 <!-- empty content -->	
			}
		}
	}	

	@dom
	def iiifApiLink(urn:Cite2Urn, uv:Vector[Cite2Urn], contextUrn:Option[Cite2Urn]) = {
		CiteBinaryImageController.implementedByProtocol(uv,CiteBinaryImageModel.iiifApiProtocolString) match {
			case Some(co) => {
				CiteBinaryImageModel.imgUseLocal.bind match {
					case false => {
						<span class="citeLinks_linkSpan">
							<a onclick={ event: Event => {
									val roi:ImageRoiModel.Roi = ImageRoiModel.roiFromUrn(urn, contextUrn)
									DataModelController.viewImage(contextUrn, co, urn, roi)
							}
						} >Remote Image</a></span>
					}
					case _ => {
						<!-- empty content -->
					}
				}
			}
			case None => {
				<!-- empty content -->							
			}
		}
	}

	@dom
	def dzLink(urn:Cite2Urn, uv:Vector[Cite2Urn], contextUrn:Option[Cite2Urn]) = {
		CiteBinaryImageController.implementedByProtocol(uv,CiteBinaryImageModel.localDZProtocolString) match {
			case Some(co) => {
				CiteBinaryImageModel.imgUseLocal.bind match {
					case true => {
						<span class="citeLinks_linkSpan">
							<a onclick={ event: Event => {
									DataModelController.viewImage(contextUrn, co, urn, roiObj = None)
								}
						} >Local Image</a></span>
					}
					case _ => {
						<!-- empty content -->
					}
				}
			}
			case None => {
				<!-- empty content -->							
			}
		}
	}
	



}