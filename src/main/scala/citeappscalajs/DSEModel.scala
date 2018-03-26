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

 	def implementedByDSE_text(u:CtsUrn):Option[Vector[Cite2Urn]] ={
 		None
 	}


}
