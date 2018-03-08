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
import js.annotation._


@JSExportTopLevel("citeapp.QueryObjectController")
object QueryObjectController {

	def isValidSearch:Unit = {
		//We need at least a propertyType
		// one num, or two+range || string || urn || boolean || vocab
		var isValid = false

		QueryObjectModel.selectedPropertyType.value match {
			case Some(StringType) =>{
				if (QueryObjectModel.currentSearchString.value != None ){ isValid = true }
			}
			case Some(BooleanType) =>{
				 isValid = true
			}
			case Some(NumericType) =>{
				if (QueryObjectModel.currentNumericQuery1.value != None ){
					QueryObjectModel.currentNumericOperator.value match {
						case "inRange" => {
							if (QueryObjectModel.currentNumericQuery2.value != None ){ isValid = true }
						}
						case _ => isValid = true
					}
				}
			}
			case Some(ControlledVocabType) =>{
				if (QueryObjectModel.currentControlledVocabItem.value != None ){ isValid = true }
			}
			case Some(CtsUrnType) =>{
				if (QueryObjectModel.currentCtsUrnQuery.value != None ){ isValid = true }
			}
			case Some(Cite2UrnType) =>{
				if (QueryObjectModel.currentCite2UrnQuery.value != None ){ isValid = true }
			}
			case _ => { isValid = false }
		}
		QueryObjectModel.isValidSearch.value =  isValid

	}


	def initQuery:Unit = {
		// Clear URN field
		js.Dynamic.global.document.getElementById("object_urnInput").value = ""
		try {
			QueryObjectModel.selectedPropertyType.value match {
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
				ObjectController.updateUserMessage(s"Cannot construct query-object from the given parameters. ${e}",2)
			}
		}
	}

	def loadQuery(q:QueryObjectModel.CiteCollectionQuery):Unit = {
		QueryObjectModel.currentQuery.value =  Some(q)
		QueryObjectModel.selectedPropertyType.value =  q.qPropertyType
		initQuery
	}


	def initStringSearch:Unit = {
		val collUrn = {
			QueryObjectModel.currentQueryCollection.value match {
				case None => None
				case Some(u) => Some(u)
			}
		}
		val cq = QueryObjectModel.CiteCollectionQuery(
			qCollection = collUrn,
			qProperty = QueryObjectModel.queryProperty.value,
			qPropertyType = QueryObjectModel.selectedPropertyType.value,
			qSearchString = QueryObjectModel.currentSearchString.value,
			qCaseSensitive = Some(QueryObjectModel.currentCaseSensitiveState.value),
			qRegex = Some(QueryObjectModel.currentRegexState.value)
		)
		doStringSearch(cq)
	}

	def doStringSearch(cq:QueryObjectModel.CiteCollectionQuery):Unit = {
		if (cq.qRegex.get) {
			cq.qProperty match {
				case None =>{
						val ov:Vector[CiteObject] = ObjectModel.collRep.value.get.regexMatch(cq.qSearchString.get)
						cq.qCollection match {
							case Some(u) =>{
								val fv:Vector[CiteObject] = ov.filter(_.urn ~~ u)
								 loadSearchResults(cq,fv)
							}
						  case _ => loadSearchResults(cq,ov)
						}
				}
				case _ => {
						val ov:Vector[CiteObject] = ObjectModel.collRep.value.get.regexMatch(cq.qProperty.get.urn,cq.qSearchString.get)
						loadSearchResults(cq,ov)
				}
			}
		} else {
			cq.qProperty match {
				case None =>{
						val ov:Vector[CiteObject] = ObjectModel.collRep.value.get.stringContains(cq.qSearchString.get,cq.qCaseSensitive.get)
						cq.qCollection match {
							case Some(u) =>{
								val fv:Vector[CiteObject] = ov.filter(_.urn ~~ u)
								 loadSearchResults(cq,fv)
							}
						  case _ => loadSearchResults(cq,ov)
						}
				}
				case _ => {
						val ov:Vector[CiteObject] = ObjectModel.collRep.value.get.stringContains(cq.qProperty.get.urn,cq.qSearchString.get,cq.qCaseSensitive.get)
						loadSearchResults(cq,ov)
				}
			}
		}
	}

	def initNumericSearch:Unit = {
		val collUrn = {
			QueryObjectModel.currentQueryCollection.value match {
				case None => None
				case Some(u) => Some(u)
			}
		}
		val cq = QueryObjectModel.CiteCollectionQuery(
			qCollection = collUrn,
			qProperty = QueryObjectModel.queryProperty.value,
			qPropertyType = QueryObjectModel.selectedPropertyType.value,
			qNum1 = QueryObjectModel.currentNumericQuery1.value,
			qNum2 = QueryObjectModel.currentNumericQuery2.value,
			qNumOperator = Some(QueryObjectModel.currentNumericOperator.value)
		)
		doNumericSearch(cq)
	}

	def doNumericSearch(cq:QueryObjectModel.CiteCollectionQuery):Unit  = {

			cq.qProperty match {
				case None =>{
					cq.qNumOperator.get match {
						case "eq" => {
							val ov:Vector[CiteObject] = ObjectModel.collRep.value.get.valueEquals(cq.qNum1.get)
							cq.qCollection match {
								case Some(u) =>{
									val fv:Vector[CiteObject] = ov.filter(_.urn ~~ u)
									 loadSearchResults(cq,fv)
								}
							  case _ => loadSearchResults(cq,ov)
							}
						}
						case "lt" => {
							val ov:Vector[CiteObject] = ObjectModel.collRep.value.get.numericLessThan(cq.qNum1.get.toDouble)
							cq.qCollection match {
								case Some(u) =>{
									val fv:Vector[CiteObject] = ov.filter(_.urn ~~ u)
									 loadSearchResults(cq,fv)
								}
							  case _ => loadSearchResults(cq,ov)
							}
						}
						case "gt" => {
							val ov:Vector[CiteObject] = ObjectModel.collRep.value.get.numericGreaterThan(cq.qNum1.get.toDouble)
							cq.qCollection match {
								case Some(u) =>{
									val fv:Vector[CiteObject] = ov.filter(_.urn ~~ u)
									 loadSearchResults(cq,fv)
								}
							  case _ => loadSearchResults(cq,ov)
							}
						}
						case "lteq" => {
							val ov:Vector[CiteObject] = ObjectModel.collRep.value.get.numericLessThanOrEqual(cq.qNum1.get.toDouble)
							cq.qCollection match {
								case Some(u) =>{
									val fv:Vector[CiteObject] = ov.filter(_.urn ~~ u)
									 loadSearchResults(cq,fv)
								}
							  case _ => loadSearchResults(cq,ov)
							}
						}
						case "gteq" => {
							val ov:Vector[CiteObject] = ObjectModel.collRep.value.get.numericGreaterThanOrEqual(cq.qNum1.get.toDouble)
							cq.qCollection match {
								case Some(u) =>{
									val fv:Vector[CiteObject] = ov.filter(_.urn ~~ u)
									 loadSearchResults(cq,fv)
								}
							  case _ => loadSearchResults(cq,ov)
							}
						}
						case "inRange" => {
							val ov:Vector[CiteObject] = ObjectModel.collRep.value.get.numericWithin(cq.qNum1.get.toDouble,cq.qNum2.get.toDouble)
							cq.qCollection match {
								case Some(u) =>{
									val fv:Vector[CiteObject] = ov.filter(_.urn ~~ u)
									 loadSearchResults(cq,fv)
								}
							  case _ => loadSearchResults(cq,ov)
							}
						}
						case _ @ op => {
							ObjectController.updateUserMessage(s"Unrecognized numeric operator: ${op}.",2)
						}

					}
				}
				case _ => {
					cq.qNumOperator.get match {
						case "eq" => {
							val ov:Vector[CiteObject] = ObjectModel.collRep.value.get.valueEquals(cq.qProperty.get.urn,cq.qNum1.get.toDouble)
							cq.qCollection match {
								case Some(u) =>{
									val fv:Vector[CiteObject] = ov.filter(_.urn ~~ u)
									 loadSearchResults(cq,fv)
								}
							  case _ => loadSearchResults(cq,ov)
							}
						}
						case "lt" => {
							val ov:Vector[CiteObject] = ObjectModel.collRep.value.get.numericLessThan(cq.qProperty.get.urn,cq.qNum1.get.toDouble)
							cq.qCollection match {
								case Some(u) =>{
									val fv:Vector[CiteObject] = ov.filter(_.urn ~~ u)
									 loadSearchResults(cq,fv)
								}
							  case _ => loadSearchResults(cq,ov)
							}
						}
						case "gt" => {
							val ov:Vector[CiteObject] = ObjectModel.collRep.value.get.numericGreaterThan(cq.qProperty.get.urn,cq.qNum1.get.toDouble)
							cq.qCollection match {
								case Some(u) =>{
									val fv:Vector[CiteObject] = ov.filter(_.urn ~~ u)
									 loadSearchResults(cq,fv)
								}
							  case _ => loadSearchResults(cq,ov)
							}
						}
						case "lteq" => {
							val ov:Vector[CiteObject] = ObjectModel.collRep.value.get.numericLessThanOrEqual(cq.qProperty.get.urn,cq.qNum1.get.toDouble)
							cq.qCollection match {
								case Some(u) =>{
									val fv:Vector[CiteObject] = ov.filter(_.urn ~~ u)
									 loadSearchResults(cq,fv)
								}
							  case _ => loadSearchResults(cq,ov)
							}
						}
						case "gteq" => {
							val ov:Vector[CiteObject] = ObjectModel.collRep.value.get.numericGreaterThanOrEqual(cq.qProperty.get.urn,cq.qNum1.get.toDouble)
							cq.qCollection match {
								case Some(u) =>{
									val fv:Vector[CiteObject] = ov.filter(_.urn ~~ u)
									 loadSearchResults(cq,fv)
								}
							  case _ => loadSearchResults(cq,ov)
							}
						}
						case "inRange" => {
							val ov:Vector[CiteObject] = ObjectModel.collRep.value.get.numericWithin(cq.qProperty.get.urn,cq.qNum1.get.toDouble,cq.qNum2.get.toDouble)
							cq.qCollection match {
								case Some(u) =>{
									val fv:Vector[CiteObject] = ov.filter(_.urn ~~ u)
									 loadSearchResults(cq,fv)
								}
							  case _ => loadSearchResults(cq,ov)
							}
						}
						case _ @ op => {
							ObjectController.updateUserMessage(s"Unrecognized numeric operator: ${op}.",2)
						}

					}
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
qNum1: Option[BigDecimal]
qNum2: Option[BigDecimal]
qNumOperator: Option[String]
qBoolVal: Option[Boolean]
qCtsUrn: Option[CtsUrn]
qCite2Urn: Option[Cite2Urn]

*/

	def initContVocabSearch:Unit = {
		val collUrn = {
			QueryObjectModel.currentQueryCollection.value match {
				case None => None
				case Some(u) => Some(u)
			}
		}
		val cq = QueryObjectModel.CiteCollectionQuery(
			qCollection = collUrn,
			qProperty = QueryObjectModel.queryProperty.value,
			qPropertyType = QueryObjectModel.selectedPropertyType.value,
			qControlledVocabItem = QueryObjectModel.currentControlledVocabItem.value
		)
		doContVocabSearch(cq)
	}

	def doContVocabSearch(cq:QueryObjectModel.CiteCollectionQuery):Unit = {
			cq.qProperty match {
				case None =>{
							val ov:Vector[CiteObject] = ObjectModel.collRep.value.get.valueEquals(cq.qControlledVocabItem.get)
							cq.qCollection match {
								case Some(u) =>{
									val fv:Vector[CiteObject] = ov.filter(_.urn ~~ u)
									 loadSearchResults(cq,fv)
								}
							  case _ => loadSearchResults(cq,ov)
							}
				}
				case _ => {
							val ov:Vector[CiteObject] = ObjectModel.collRep.value.get.valueEquals(cq.qProperty.get.urn, cq.qControlledVocabItem.get)
							cq.qCollection match {
								case Some(u) =>{
									val fv:Vector[CiteObject] = ov.filter(_.urn ~~ u)
									 loadSearchResults(cq,fv)
								}
							  case _ => loadSearchResults(cq,ov)
							}
				}
			}
	}

	def initBooleanSearch:Unit = {
		val collUrn = {
			QueryObjectModel.currentQueryCollection.value match {
				case None => None
				case Some(u) => Some(u)
			}
		}
		val cq = QueryObjectModel.CiteCollectionQuery(
			qCollection = collUrn,
			qProperty = QueryObjectModel.queryProperty.value,
			qPropertyType = QueryObjectModel.selectedPropertyType.value,
			qBoolVal = Some(QueryObjectModel.currentBooleanVal.value)
		)
		doBooleanSearch(cq)
	}

	def doBooleanSearch(cq:QueryObjectModel.CiteCollectionQuery):Unit = {
			cq.qProperty match {
				case None =>{
							val ov:Vector[CiteObject] = ObjectModel.collRep.value.get.valueEquals(cq.qBoolVal.get)
							cq.qCollection match {
								case Some(u) =>{
									val fv:Vector[CiteObject] = ov.filter(_.urn ~~ u)
									 loadSearchResults(cq,fv)
								}
							  case _ => loadSearchResults(cq,ov)
							}
				}
				case _ => {
							val ov:Vector[CiteObject] = ObjectModel.collRep.value.get.valueEquals(cq.qProperty.get.urn, cq.qBoolVal.get)
							cq.qCollection match {
								case Some(u) =>{
									val fv:Vector[CiteObject] = ov.filter(_.urn ~~ u)
									 loadSearchResults(cq,fv)
								}
							  case _ => loadSearchResults(cq,ov)
							}
				}
			}
	}

	def initCtsUrnSearch:Unit = {
		g.console.log("got here 1")
		val collUrn = {
			QueryObjectModel.currentQueryCollection.value match {
				case None => None
				case Some(u) => Some(u)
			}
		}
		val cq = QueryObjectModel.CiteCollectionQuery(
			qCollection = collUrn,
			qProperty = QueryObjectModel.queryProperty.value,
			qPropertyType = QueryObjectModel.selectedPropertyType.value,
			qCtsUrn = QueryObjectModel.currentCtsUrnQuery.value
		)
		doCtsUrnSearch(cq)
	}

	def doCtsUrnSearch(cq:QueryObjectModel.CiteCollectionQuery):Unit = {
		g.console.log("got here 2")
			cq.qProperty match {
				case None =>{
							val ov:Vector[CiteObject] = ObjectModel.collRep.value.get.urnMatch(cq.qCtsUrn.get)
							g.console.log("got here 3")
							cq.qCollection match {
								case Some(u) =>{
									val fv:Vector[CiteObject] = ov.filter(_.urn ~~ u)
									 loadSearchResults(cq,fv)
								}
							  case _ => loadSearchResults(cq,ov)
							}
				}
				case _ => {
							val ov:Vector[CiteObject] = ObjectModel.collRep.value.get.urnMatch(cq.qProperty.get.urn, cq.qCtsUrn.get)
							cq.qCollection match {
								case Some(u) =>{
									val fv:Vector[CiteObject] = ov.filter(_.urn ~~ u)
									 loadSearchResults(cq,fv)
								}
							  case _ => loadSearchResults(cq,ov)
							}
				}
			}
	}

	def initCite2UrnSearch:Unit = {
		val collUrn = {
			QueryObjectModel.currentQueryCollection.value match {
				case None => None
				case Some(u) => Some(u)
			}
		}
		val cq = QueryObjectModel.CiteCollectionQuery(
			qCollection = collUrn,
			qProperty = QueryObjectModel.queryProperty.value,
			qPropertyType = QueryObjectModel.selectedPropertyType.value,
			qCite2Urn =  QueryObjectModel.currentCite2UrnQuery.value
		)
		doCite2UrnSearch(cq)
	}

	def doCite2UrnSearch(cq:QueryObjectModel.CiteCollectionQuery):Unit = {
			cq.qProperty match {
				case None =>{
							val ov:Vector[CiteObject] = ObjectModel.collRep.value.get.urnMatch(cq.qCite2Urn.get)
							cq.qCollection match {
								case Some(u) =>{
									val fv:Vector[CiteObject] = ov.filter(_.urn ~~ u)
									 loadSearchResults(cq,fv)
								}
							  case _ => loadSearchResults(cq,ov)
							}
				}
				case _ => {
							val ov:Vector[CiteObject] = ObjectModel.collRep.value.get.urnMatch(cq.qProperty.get.urn, cq.qCite2Urn.get)
							cq.qCollection match {
								case Some(u) =>{
									val fv:Vector[CiteObject] = ov.filter(_.urn ~~ u)
									 loadSearchResults(cq,fv)
								}
							  case _ => loadSearchResults(cq,ov)
							}
				}
			}
	}

	def loadSearchResults(cq:QueryObjectModel.CiteCollectionQuery, ov:Vector[CiteObject]):Unit = {
				cq.numResults = ov.size
				if (ov.size > 0 ){
						ov.size match {
							case 1 => ObjectController.updateUserMessage(s"Search found ${ov.size} matching object.",0)
							case _ => ObjectController.updateUserMessage(s"Search found ${ov.size} matching objects.",0)
						}
						ObjectModel.clearObject
						QueryObjectModel.currentQuery.value =  Some(cq)
						addToSearchHistory(cq)
						ObjectModel.objectOrCollection.value =  "search"
						if (ObjectModel.limit.value > ov.size){ ObjectModel.limit.value =  ov.size }
						ObjectModel.offset.value =  1
						ObjectModel.browsable.value =  true
					  // display objects
						ObjectModel.clearObject
						ObjectModel.offset.value =  1
						if (ObjectModel.limit.value > ov.size){
							ObjectModel.limit.value =  ov.size
						}
						ObjectModel.browsable.value =  true
						ObjectModel.objectOrCollection.value =  "search"
						for (o <- ov ){
							ObjectModel.boundObjects.value += o
						}
						ObjectController.setDisplay
				} else {
						ObjectModel.clearObject
						QueryObjectModel.currentQuery.value =  Some(cq)
						ObjectController.updateUserMessage("Search found no matching objects.",1)
				}

	}

	def addToSearchHistory(cq:QueryObjectModel.CiteCollectionQuery):Unit = {
			var vList = new ListBuffer[QueryObjectModel.CiteCollectionQuery]
			for (qh <- QueryObjectModel.pastQueries.value ){
				vList += qh
			}
			vList += cq
			val vSet = vList.toSet
			QueryObjectModel.pastQueries.value.clear
			for (q <- vSet ){ QueryObjectModel.pastQueries.value += q }

	}

}
