package citeapp

import com.thoughtworks.binding.{Binding, dom}
import com.thoughtworks.binding.Binding.{BindingSeq, Var, Vars}
import scala.scalajs.js
import scala.scalajs.js._
import org.scalajs.dom._
import org.scalajs.dom.ext._
import org.scalajs.dom.raw._
import collection.mutable
import collection.mutable._
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
				g.console.log(s"Cannot make query. ${e}")

			}
		}
	}

	/*
qCollection: Option[Cite2Urn]
qProperty: Option[CitePropertyDef]
qPropertyType: Option[CitePropertyType]
qControlledVocabItem: Option[String]
qSearchString: Option[String]
qRegex:Option[Boolean]
qNum1: Option[Double]
qNum2: Option[Double]
qNumOperator: Option[String]
qBoolVal: Option[Boolean]
qCtsUrn: Option[CtsUrn]
qCite2Urn: Option[Cite2Urn]

*/

	def initStringSearch:Unit = {
		g.console.log("Doing initStringSearch…")
		val collUrn = {
			QueryObjectModel.currentQueryCollection.get match {
				case None => None
				case Some(u) => Some(u)
			}
		}
		g.console.log(collUrn.toString)
		val cq = QueryObjectModel.CiteCollectionQuery(
			qCollection = collUrn,
			qProperty = QueryObjectModel.queryProperty.get,
			qPropertyType = QueryObjectModel.selectedPropertyType.get,
			qSearchString = QueryObjectModel.currentSearchString.get,
			qCaseSensitive = Some(QueryObjectModel.currentCaseSensitiveState.get),
			qRegex = Some(QueryObjectModel.currentRegexState.get)
		)
		doStringSearch(cq)
	}

	def doStringSearch(cq:QueryObjectModel.CiteCollectionQuery):Unit = {
		if (cq.qRegex.get) {
			cq.qProperty match {
				case None =>{
						val ov:Vector[CiteObject] = ObjectModel.collectionRepository.regexMatch(cq.qSearchString.get)
						loadSearchResults(cq,ov)
				}
				case _ => {
						val ov:Vector[CiteObject] = ObjectModel.collectionRepository.regexMatch(cq.qProperty.get.urn,cq.qSearchString.get)
						loadSearchResults(cq,ov)
				}
			}
		} else {
			cq.qProperty match {
				case None =>{
						val ov:Vector[CiteObject] = ObjectModel.collectionRepository.stringContains(cq.qSearchString.get,cq.qCaseSensitive.get)
						loadSearchResults(cq,ov)
				}
				case _ => {
						val ov:Vector[CiteObject] = ObjectModel.collectionRepository.stringContains(cq.qProperty.get.urn,cq.qSearchString.get,cq.qCaseSensitive.get)
						loadSearchResults(cq,ov)
				}
			}
		}
	}

	def initNumericSearch:Unit = {
		g.console.log("Doing initNumericSearch…")
		ObjectController.updateUserMessage("Numeric searching is not yet implemented.",1)
	}

	def doNumericSearch(cq:QueryObjectModel.CiteCollectionQuery):Unit  = {

	}

	def initContVocabSearch:Unit = {
		g.console.log("Doing initContVocabSearch…")
		ObjectController.updateUserMessage("Controlled vocabulary searching is not yet implemented.",1)
	}

	def doContVocabSearch(cq:QueryObjectModel.CiteCollectionQuery):Unit = {

	}

	def initBooleanSearch:Unit = {
		g.console.log("Doing initBooleanSearch…")
		ObjectController.updateUserMessage("Boolean searching is not yet implemented.",1)
	}

	def doBooleanSearch(cq:QueryObjectModel.CiteCollectionQuery):Unit = {

	}

	def initCtsUrnSearch:Unit = {
		g.console.log("Doing initCtsUrnSearch…")
		ObjectController.updateUserMessage("Cts Urn searching is not yet implemented.",1)
	}

	def doCtsUrnSearch(cq:QueryObjectModel.CiteCollectionQuery):Unit = {

	}

	def initCite2UrnSearch:Unit = {
		g.console.log("Doing initCite2UrnSearch…")
		ObjectController.updateUserMessage("Cite2 Urnk searching is not yet implemented.",1)
	}

	def doCite2UrnSearch(cq:QueryObjectModel.CiteCollectionQuery):Unit = {

	}

	def loadSearchResults(cq:QueryObjectModel.CiteCollectionQuery, ov:Vector[CiteObject]):Unit = {
				cq.numResults = ov.size
				if (ov.size > 0 ){
						ov.size match {
							case 1 => ObjectController.updateUserMessage(s"Search found ${ov.size} matching object.",0)
							case _ => ObjectController.updateUserMessage(s"Search found ${ov.size} matching objects.",0)
						}
						ObjectModel.clearObject
						QueryObjectModel.currentQuery := Some(cq)
						addToSearchHistory(cq)
						ObjectModel.objectOrCollection := "search"
						if (ObjectModel.limit.get > ov.size){ ObjectModel.limit := ov.size }
						ObjectModel.offset := 1
						ObjectModel.browsable := true
					  // display objects
						ObjectModel.clearObject
						ObjectModel.offset := 1
						if (ObjectModel.limit.get > ov.size){
							ObjectModel.limit := ov.size
						}
						ObjectModel.browsable := true
						ObjectModel.objectOrCollection := "search"
						for (o <- ov ){
							ObjectModel.boundObjects.get += o
						}
						ObjectController.setDisplay
				} else {
						ObjectModel.clearObject
						QueryObjectModel.currentQuery := Some(cq)
						ObjectController.updateUserMessage("Search found no matching objects.",1)
				}

	}

	def addToSearchHistory(cq:QueryObjectModel.CiteCollectionQuery):Unit = {
			var vList = new ListBuffer[QueryObjectModel.CiteCollectionQuery]
			for (qh <- QueryObjectModel.pastQueries.get ){
				vList += qh
			}
			vList += cq
			val vSet = vList.toSet
			QueryObjectModel.pastQueries.get.clear
			for (q <- vSet ){ QueryObjectModel.pastQueries.get += q }

	}

}
