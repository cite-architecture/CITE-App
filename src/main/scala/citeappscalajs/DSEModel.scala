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

@JSExportTopLevel("citeapp.DSEModel")
object DSEModel {

	val dseModelUrn:Cite2Urn = Cite2Urn("urn:cite2:cite:datamodels.v1:dse")
	val dseImageProp:String = "imageroi"
	val dseTextProp:String = "passage"
	val dseSurfaceProp:String = "surface"

	val currentListOfDseUrns = Vars.empty[Cite2Urn]

 	def implementedByDSE_image(u:Cite2Urn):Option[Vector[Cite2Urn]] ={
 		val plainUrn = u.dropExtensions
		DataModelModel.dataModels.value match {
			case Some(dm) => {
				// get any collections that implement DSE	
				val dseColls:Vector[Cite2Urn] = dm.filter(_.model == dseModelUrn).map(_.collection)
				dseColls.size match {
					case 0 => None	
					case _ => {
						val dseUrns:Vector[Cite2Urn] = {
							dseColls.map(coll => {
								// Get property URN
								val propUrn:Cite2Urn = DataModelController.propertyUrnFromPropertyName(coll,dseImageProp)		
								val hits:Vector[CitePropertyValue] = ObjectModel.collRep.value.get.collectionData(coll).data.filter(_.urn ~~ propUrn).filter(pt => {
										 val v:Cite2Urn = pt.propertyValue.asInstanceOf[Cite2Urn]
										 v ~~  plainUrn 
								})
								val roiObjectUrns:Vector[Cite2Urn] = hits.map(_.urn.dropProperty)
								roiObjectUrns
							}).flatten
						}
						dseUrns.size match {
							case 0 => None
							case _ => Some(dseUrns)
						}
					}
				}
			}
			case None => None
		}
 	}

 	def implementedByDSE_text(urn:CtsUrn):Option[Vector[Cite2Urn]] ={
 		DataModelModel.dataModels.value match {
			case Some(dm) => { 
			// get any collections that implement DSE	
				val dseColls:Vector[Cite2Urn] = dm.filter(_.model == dseModelUrn).map(_.collection)
				dseColls.size match {
					case 0 => None		
					case _ => {
						val dseUrns:Vector[Cite2Urn] = {
							dseColls.map(coll => {
								// Get property URN
								val propUrn:Cite2Urn = DataModelController.propertyUrnFromPropertyName(coll,dseTextProp)		
								val hits:Vector[CitePropertyValue] = ObjectModel.collRep.value.get.collectionData(coll).data.filter(_.urn ~~ propUrn).filter(pt => {
										 val v:CtsUrn = pt.propertyValue.asInstanceOf[CtsUrn]
										 v ==  urn 
								})
								val roiObjectUrns:Vector[Cite2Urn] = hits.map(_.urn.dropProperty)
								roiObjectUrns
							}).flatten
						}
						dseUrns.size match {
							case 0 => None
							case _ => Some(dseUrns)
						}
					}
				}
			}
			case None => None
		}
 	}

 	def dseObjectsForCorpus(corp:Vector[CtsUrn]):Option[Vector[Cite2Urn]] = {
 		try {
	 		val outerObjectVector:Vector[Vector[Option[Cite2Urn]]] = {
	 			corp.map(c => {
	 				val innerObjectVector:Vector[Option[Cite2Urn]] = {
	 					implementedByDSE_text(c) match {
		 					case None => Vector(None)
							case Some(objVec) => objVec.map(o => Some(o)).toVector
		 				}
		 			}
		 			innerObjectVector
	 			})
	 		}
	 		val objectVector:Vector[Option[Cite2Urn]] = outerObjectVector.flatten
	 		val returnVector:Option[Vector[Cite2Urn]] = {
	 			val filteredObj: Vector[Cite2Urn] = objectVector.filter(obj => {
	 				obj match {
	 					case Some(o) => true
	 					case None => false
	 				}	
	 			}).toVector.map( _.get )

	 			val finalVector:Option[Vector[Cite2Urn]] = filteredObj.size match {
	 				case s if (s == 0) => None
	 				case _ => Some(filteredObj)
	 			}

	 			finalVector
	 		}
	 		returnVector
	 	} catch {
	 		case e:Exception => {
	 			CiteMainController.updateUserMessage(s"DSE Model: dseObjectsForCorpus error: ${e}.",2)	
	 			None
	 		}
	 	}
 	}

 	def roisForImage(urn:Cite2Urn, contextUrn:Option[Cite2Urn], dseUrns:Option[Vector[Cite2Urn]]):Option[Vector[ImageRoiModel.Roi]] = {
 		try {
	 		// If there is an ROI already on the URN, and a contextUrn, make an ROI object for those
			val originalRoi:Option[Vector[ImageRoiModel.Roi]] = {
				val tempRoi:Option[ImageRoiModel.Roi] = ImageRoiModel.roiFromUrn(urn, data = contextUrn, context = contextUrn)
				tempRoi match {
					case Some(r) => Some(Vector(r))
					case None => None
				}
			}

			//Get an ROI for the surface (there should be only one)

			// Get ROI objects for all objects in dseUrns
			val allRois:Option[Vector[ImageRoiModel.Roi]] = {
					dseUrns match {
						case None => None
						case Some(dus) => {
							val optionRoiVec:Vector[Option[ImageRoiModel.Roi]] = dus.map(du => {
								// Get Object for CiteUrn du
								val thisObject:CiteObject = ObjectModel.collRep.value.get.citableObject(du)
								val roiUrn:Cite2Urn = thisObject.propertyValue(DataModelController.propertyUrnFromPropertyName(du, dseImageProp )).asInstanceOf[Cite2Urn]
								val textUrn:CtsUrn = thisObject.propertyValue(DataModelController.propertyUrnFromPropertyName(du, dseTextProp )).asInstanceOf[CtsUrn]
								val thisRoiObject:Option[ImageRoiModel.Roi] = ImageRoiModel.roiFromUrn(roiUrn,data = Some(textUrn), context = Some(thisObject.urn))
								thisRoiObject match {
									case Some(tro) => tro
									case None =>
								}
								thisRoiObject
							}).toVector
							val filteredOptionRoiVec:Vector[Option[ImageRoiModel.Roi]] = optionRoiVec.filter(_ != None).toVector
							val roiVec:Vector[ImageRoiModel.Roi] = filteredOptionRoiVec.map(_.get)
							Some(roiVec)
						}
					}	
			}


			val totalRois:Option[Vector[ImageRoiModel.Roi]] = {
				originalRoi match {
					case None => {
						allRois match {
							case None => None
							case Some(arois) => Some(arois)
						}
					}
					case Some(oroi) => {
						allRois match {
							case None => Some(oroi)
							case Some(arois) => Some(oroi ++ arois)
						}
					}
				}
			}
			totalRois
		} catch {
			case e:Exception => throw new Exception(s"DSE Model: ${e}.")
		}
 	}

 	def ctsInDse(urn:CtsUrn):Vars[Cite2Urn] = {
 		ObjectModel.collRep.value match {
 			case None => Vars.empty[Cite2Urn]
 			case Some(cr) => {
 				val filterList:Vector[Cite2Urn] = {
	 				DSEModel.currentListOfDseUrns.value.filter( du => {
	 					val obj:CiteObject = cr.citableObject(du)
	 					val prop:Cite2Urn = DataModelController.propertyUrnFromPropertyName(obj.urn,"passage")
	 					obj.propertyValue(prop).asInstanceOf[CtsUrn] == urn
	 				}).toVector
	 			}
	 			filterList.size match {
	 				case 0 => Vars.empty[Cite2Urn]
	 				case _ => {
	 					val v = Vars.empty[Cite2Urn]
	 					for (f <- filterList) {
	 						v.value += f
	 					}
	 					v
	 				}
	 			}
 			}
 		}	
 	}

	def updateCurrentListOfDseUrns(c:Corpus):Unit = {
		DSEModel.currentListOfDseUrns.value.clear
		O2Model.currentListOfUrns.value.size match {
			case 0 => {
			}
			case _ =>{
				val urnVec:Vector[CtsUrn] = {
					var v:Vector[CtsUrn] = O2Model.currentListOfUrns.value.toVector
					v
				}
				val dseUrns = DSEModel.dseObjectsForCorpus(urnVec)
				dseUrns match {
					case Some(v) => {
						for (n <- v) DSEModel.currentListOfDseUrns.value += n
					}
					case None => {
					}
				}

			}
		}
	}

}
