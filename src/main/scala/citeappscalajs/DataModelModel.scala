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

	var implementedDataModels:Vector[Cite2Urn] = Vector(
		// Defines a basic citable image model
		Cite2Urn("urn:cite2:cite:datamodels.v1:imagemodel"),
		// Defines an image that can be delivered as binary image data
		Cite2Urn("urn:cite2:cite:datamodels.v1:binaryimg"),
		// Defines a "codex model" of ordered text-bearing surfaces
		//Cite2Urn("urn:cite2:cite:datamodels.v1:tbsmodel"),
		// Defines a "documented scholarly editions" model of surface + image + text
		Cite2Urn("urn:cite2:cite:datamodels.v1:dse"),
	)

	val dataModels = Var[Option[Vector[DataModel]]](None)



}
