package citeapp

import com.thoughtworks.binding.{Binding, dom}
import com.thoughtworks.binding.Binding.{BindingSeq, Var, Vars}
import scala.scalajs.js
import scala.scalajs.js._
import org.scalajs.dom._
import org.scalajs.dom.ext._
import org.scalajs.dom.raw._
import edu.holycross.shot.cite._
import edu.holycross.shot.ohco2._
import edu.holycross.shot.citeobj._
import scala.scalajs.js.Dynamic.{ global => g }
import scala.scalajs.js.annotation.JSExport


@JSExport
object QueryObjectController {

	def isValidSearch:Unit = {
		//We need at least a propertyType
		// one num, or two+range || string || urn || boolean || vocab
		var isValid = false

		QueryObjectModel.selectedPropertyType.get match {
			case Some(StringType) =>{
				if (QueryObjectModel.currentSearchString.get != None ){ isValid = true }
			}
			case Some(BooleanType) =>{
				 isValid = true
			}
			case Some(NumericType) =>{
				if (QueryObjectModel.currentNumericQuery1.get != None ){
					QueryObjectModel.currentNumericOperator.get match {
						case "inRange" => {
							if (QueryObjectModel.currentNumericQuery2.get != None ){ isValid = true }
						}
						case _ => isValid = true
					}
				}
			}
			case Some(ControlledVocabType) =>{
				if (QueryObjectModel.currentControlledVocabItem.get != None ){ isValid = true }
			}
			case Some(CtsUrnType) =>{
				if (QueryObjectModel.currentCtsUrnQuery.get != None ){ isValid = true }
			}
			case Some(Cite2UrnType) =>{
				if (QueryObjectModel.currentCite2UrnQuery.get != None ){ isValid = true }
			}
			case _ => { isValid = false }
		}
		QueryObjectModel.isValidSearch := isValid
		g.console.log(s"Checked validity: ${isValid}")
		g.console.log(s"selected vocab: ${QueryObjectModel.currentControlledVocabItem.get}")

	}


	def initQuery:Unit = {
		g.console.log("Doing query…")

		try {
			g.console.log("Trying…")
			QueryObjectModel.selectedPropertyType.get match {
				case Some(StringType) => initStringSearch
				case Some(NumericType) => initNumericSearch
				case Some(ControlledVocabType) => initContVocabSearch
				case Some(BooleanType) => initBooleanSearch
				case Some(CtsUrnType) => initCtsUrnSearch
				case Some(Cite2UrnType) => initCite2UrnSearch
				case _ => { throw new Exception("unrecognized type")}
			}

		} catch {
			case e: Exception => {
				g.console.log(s"Cannot make query. ${}")

			}
		}
	}

	def initStringSearch = {
		g.console.log("Doing initStringSearch…")
	}

	def initNumericSearch = {
		g.console.log("Doing initNumericSearch…")
	}

	def initContVocabSearch = {
		g.console.log("Doing initContVocabSearch…")
	}

	def initBooleanSearch = {
		g.console.log("Doing initBooleanSearch…")
	}

	def initCtsUrnSearch = {
		g.console.log("Doing initCtsUrnSearch…")
	}

	def initCite2UrnSearch = {
		g.console.log("Doing initCite2UrnSearch…")
	}



}
