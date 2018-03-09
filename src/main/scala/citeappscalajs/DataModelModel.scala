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

@JSExportTopLevel("citeapp.DataModelModel")
object DataModelModel {

	val implementedDataModels:Vector[Cite2Urn] = Vector()

	/* 
	urn:cite2:cite:datamodels.v1:imagemodel
	urn:cite2:cite:datamodels.v1:binaryimg
	urn:cite2:cite:datamodels.v1:tbsmodel
	urn:cite2:cite:datamodels.v1:dse
	*/


	val dataModels = Var[Option[Vector[DataModel]]](None)

}
