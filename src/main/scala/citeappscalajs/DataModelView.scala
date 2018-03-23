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
	propVal.objectComponentOption match {
		case Some(oc) => {
			<ul class="citeLinks_linksList">
				{ CiteBinaryImageView.imageThumbItem(propVal, CiteBinaryImageModel.imgUseLocal.bind).bind }
				{ DataModelView.objectLinkItem(contextUrn, propVal).bind }
				{ DataModelView.imageLinkItem(contextUrn, propVal).bind }
			</ul>
		}
		case None => { 
			<ul class="citeLinks_linksList">
				{ DataModelView.objectLinkItem(contextUrn, propVal).bind }
			</ul>
		}
	}
}

@dom
def textLinks(contextUrn:Option[Cite2Urn], u:CtsUrn) = {
	<ul class="citeLinks_linksList">
		{ DataModelView.textLinkItem(contextUrn, u).bind }
	</ul>
}

@dom
def textLinkItem(contextUrn:Option[Cite2Urn], u:CtsUrn, idString:String = "") = {
	DataModelController.hasText(u) match {
		case true => {
			<li class="citeLinks_linkItem" id={idString}>
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
	def objectLinkItem(contextUrn:Option[Cite2Urn],propVal:Cite2Urn,labeled:Boolean = false, idString:String = "") = {
		val tempUrn:Cite2Urn = propVal.dropExtensions.dropProperty
		DataModelController.hasObject(tempUrn) match {
			case true => {
				<li class="citeLinks_linkItem" id={idString}>
					<a onclick={ event: Event => {
							//g.console.log(s"clicked: ${propVal}")
							DataModelController.retrieveObject(contextUrn,tempUrn)
						}
				} > { 
					labeled match {
						case true => {
							s"${ObjectModel.collRep.value.get.citableObject(propVal.dropExtensions.dropProperty).label}" 
						}
						case _ => {
							//g.console.log("got here: view item")
							"View Item"
						}
					}
				}</a></li>
			}
			case _ => {
				// need to see if it is a collection or range
				var isBrowsable:Boolean = {
					( propVal.isRange ) || 
					( propVal.objectComponentOption == None)
				}
				//g.console.log(s"${propVal} isBrowsable == ${isBrowsable}")
				isBrowsable match {
					case false => {
						labeled match {
							case true => { <li class="citeLinks_linkItem">{ s"Object ${tempUrn} not in Library" }</li> }
							case _ => { <li>Object not in Library</li> }
						}
					}
					case _ => {
						val coll:Option[CiteCollectionDef] = {
							ObjectModel.collRep.value.get.catalog.collection(propVal)
						}
						coll match {
							case Some(c) => {
								<li class="citeLinks_linkItem" id={idString}>
									<a onclick={ event: Event => {
											//g.console.log(s"clicked: ${propVal}")
											DataModelController.retrieveObject(contextUrn,tempUrn)
										}
								} > { 
								labeled match {
									case true => {  
										val l:String = c.collectionLabel
										l
									}
									case _ => { "Browse Collection" }
								}

								}</a></li>
							}
							case None => {
								<li class="citeLinks_linkItem" id={idString}>
									{ "Collection not in library." }
								</li>
							}
						}
						
					}
				}
			}
		}
	
	}	

	@dom
	def imageLinkItem(contextUrn:Option[Cite2Urn],propVal:Cite2Urn) = {
		//g.console.log("CiteLinks objectLinkItem")
		CiteBinaryImageController.implementedByImageCollObjects(propVal) match {
			case Some(uv) => {
				<li class="citeLinks_linkItem">
					{ DataModelView.iipDZLink(propVal, uv, contextUrn).bind }
					{ DataModelView.localDZLink(propVal, uv, contextUrn).bind }
				</li>
			}
			case None => {
				 <!-- empty content -->	
			}
		}
	}	

	@dom
	def iipDZLink(urn:Cite2Urn, uv:Vector[Cite2Urn], contextUrn:Option[Cite2Urn]) = {
		CiteBinaryImageController.implementedByProtocol(uv,CiteBinaryImageModel.iiifApiProtocolString) match {
			case Some(co) => {
				CiteBinaryImageModel.imgUseLocal.bind match {
					case false => {
						<span class="citeLinks_linkSpan">
							<a onclick={ event: Event => {
									val roi:Option[Vector[ImageRoiModel.Roi]] = {
										val tempRoi:Option[ImageRoiModel.Roi] = ImageRoiModel.roiFromUrn(urn, contextUrn)
										CiteBinaryImageModel.currentContextUrn.value = contextUrn
										tempRoi match {
											case Some(r) => Some(Vector(r))
											case None => None
										}
									}
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
	def localDZLink(urn:Cite2Urn, uv:Vector[Cite2Urn], contextUrn:Option[Cite2Urn]) = {
		CiteBinaryImageController.implementedByProtocol(uv,CiteBinaryImageModel.localDZProtocolString) match {
			case Some(co) => {
				CiteBinaryImageModel.imgUseLocal.bind match {
					case true => {
						<span class="citeLinks_linkSpan">
							<a onclick={ event: Event => {
									val thisRoi:Option[ImageRoiModel.Roi] = ImageRoiModel.roiFromUrn(urn, contextUrn)
									CiteBinaryImageModel.currentContextUrn.value = contextUrn
									val returnRoi:Option[Vector[ImageRoiModel.Roi]] = {
										thisRoi match {
											case Some(r) => Some(Vector(r))
											case None => None
										}
									}
									DataModelController.viewImage(contextUrn, co, urn, returnRoi)
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