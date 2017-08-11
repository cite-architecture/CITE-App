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

		try {
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
				ObjectController.updateUserMessage(s"Cannot construct query-object from the given parameters. ${e}",2)
			}
		}
	}

	def loadQuery(q:QueryObjectModel.CiteCollectionQuery):Unit = {
		QueryObjectModel.currentQuery := Some(q)
		QueryObjectModel.selectedPropertyType := q.qPropertyType
		initQuery
	}


	def initStringSearch:Unit = {
		val collUrn = {
			QueryObjectModel.currentQueryCollection.get match {
				case None => None
				case Some(u) => Some(u)
			}
		}
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
						cq.qCollection match {
							case Some(u) =>{
								val fv:Vector[CiteObject] = ov.filter(_.urn ~~ u)
								 loadSearchResults(cq,fv)
							}
						  case _ => loadSearchResults(cq,ov)
						}
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
						cq.qCollection match {
							case Some(u) =>{
								val fv:Vector[CiteObject] = ov.filter(_.urn ~~ u)
								 loadSearchResults(cq,fv)
							}
						  case _ => loadSearchResults(cq,ov)
						}
				}
				case _ => {
						val ov:Vector[CiteObject] = ObjectModel.collectionRepository.stringContains(cq.qProperty.get.urn,cq.qSearchString.get,cq.qCaseSensitive.get)
						loadSearchResults(cq,ov)
				}
			}
		}
	}

	def initNumericSearch:Unit = {
		val collUrn = {
			QueryObjectModel.currentQueryCollection.get match {
				case None => None
				case Some(u) => Some(u)
			}
		}
		val cq = QueryObjectModel.CiteCollectionQuery(
			qCollection = collUrn,
			qProperty = QueryObjectModel.queryProperty.get,
			qPropertyType = QueryObjectModel.selectedPropertyType.get,
			qNum1 = QueryObjectModel.currentNumericQuery1.get,
			qNum2 = QueryObjectModel.currentNumericQuery2.get,
			qNumOperator = Some(QueryObjectModel.currentNumericOperator.get)
		)
		doNumericSearch(cq)
	}

	def doNumericSearch(cq:QueryObjectModel.CiteCollectionQuery):Unit  = {

			cq.qProperty match {
				case None =>{
					cq.qNumOperator.get match {
						case "eq" => {
							val ov:Vector[CiteObject] = ObjectModel.collectionRepository.valueEquals(cq.qNum1.get)
							cq.qCollection match {
								case Some(u) =>{
									val fv:Vector[CiteObject] = ov.filter(_.urn ~~ u)
									 loadSearchResults(cq,fv)
								}
							  case _ => loadSearchResults(cq,ov)
							}
						}
						case "lt" => {
							val ov:Vector[CiteObject] = ObjectModel.collectionRepository.numericLessThan(cq.qNum1.get.toDouble)
							cq.qCollection match {
								case Some(u) =>{
									val fv:Vector[CiteObject] = ov.filter(_.urn ~~ u)
									 loadSearchResults(cq,fv)
								}
							  case _ => loadSearchResults(cq,ov)
							}
						}
						case "gt" => {
							val ov:Vector[CiteObject] = ObjectModel.collectionRepository.numericGreaterThan(cq.qNum1.get.toDouble)
							cq.qCollection match {
								case Some(u) =>{
									val fv:Vector[CiteObject] = ov.filter(_.urn ~~ u)
									 loadSearchResults(cq,fv)
								}
							  case _ => loadSearchResults(cq,ov)
							}
						}
						case "lteq" => {
							val ov:Vector[CiteObject] = ObjectModel.collectionRepository.numericLessThanOrEqual(cq.qNum1.get.toDouble)
							cq.qCollection match {
								case Some(u) =>{
									val fv:Vector[CiteObject] = ov.filter(_.urn ~~ u)
									 loadSearchResults(cq,fv)
								}
							  case _ => loadSearchResults(cq,ov)
							}
						}
						case "gteq" => {
							val ov:Vector[CiteObject] = ObjectModel.collectionRepository.numericGreaterThanOrEqual(cq.qNum1.get.toDouble)
							cq.qCollection match {
								case Some(u) =>{
									val fv:Vector[CiteObject] = ov.filter(_.urn ~~ u)
									 loadSearchResults(cq,fv)
								}
							  case _ => loadSearchResults(cq,ov)
							}
						}
						case "inRange" => {
							val ov:Vector[CiteObject] = ObjectModel.collectionRepository.numericWithin(cq.qNum1.get.toDouble,cq.qNum2.get.toDouble)
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
							val ov:Vector[CiteObject] = ObjectModel.collectionRepository.valueEquals(cq.qProperty.get.urn,cq.qNum1.get.toDouble)
							cq.qCollection match {
								case Some(u) =>{
									val fv:Vector[CiteObject] = ov.filter(_.urn ~~ u)
									 loadSearchResults(cq,fv)
								}
							  case _ => loadSearchResults(cq,ov)
							}
						}
						case "lt" => {
							val ov:Vector[CiteObject] = ObjectModel.collectionRepository.numericLessThan(cq.qProperty.get.urn,cq.qNum1.get.toDouble)
							cq.qCollection match {
								case Some(u) =>{
									val fv:Vector[CiteObject] = ov.filter(_.urn ~~ u)
									 loadSearchResults(cq,fv)
								}
							  case _ => loadSearchResults(cq,ov)
							}
						}
						case "gt" => {
							val ov:Vector[CiteObject] = ObjectModel.collectionRepository.numericGreaterThan(cq.qProperty.get.urn,cq.qNum1.get.toDouble)
							cq.qCollection match {
								case Some(u) =>{
									val fv:Vector[CiteObject] = ov.filter(_.urn ~~ u)
									 loadSearchResults(cq,fv)
								}
							  case _ => loadSearchResults(cq,ov)
							}
						}
						case "lteq" => {
							val ov:Vector[CiteObject] = ObjectModel.collectionRepository.numericLessThanOrEqual(cq.qProperty.get.urn,cq.qNum1.get.toDouble)
							cq.qCollection match {
								case Some(u) =>{
									val fv:Vector[CiteObject] = ov.filter(_.urn ~~ u)
									 loadSearchResults(cq,fv)
								}
							  case _ => loadSearchResults(cq,ov)
							}
						}
						case "gteq" => {
							val ov:Vector[CiteObject] = ObjectModel.collectionRepository.numericGreaterThanOrEqual(cq.qProperty.get.urn,cq.qNum1.get.toDouble)
							cq.qCollection match {
								case Some(u) =>{
									val fv:Vector[CiteObject] = ov.filter(_.urn ~~ u)
									 loadSearchResults(cq,fv)
								}
							  case _ => loadSearchResults(cq,ov)
							}
						}
						case "inRange" => {
							val ov:Vector[CiteObject] = ObjectModel.collectionRepository.numericWithin(cq.qProperty.get.urn,cq.qNum1.get.toDouble,cq.qNum2.get.toDouble)
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
			QueryObjectModel.currentQueryCollection.get match {
				case None => None
				case Some(u) => Some(u)
			}
		}
		val cq = QueryObjectModel.CiteCollectionQuery(
			qCollection = collUrn,
			qProperty = QueryObjectModel.queryProperty.get,
			qPropertyType = QueryObjectModel.selectedPropertyType.get,
			qControlledVocabItem = QueryObjectModel.currentControlledVocabItem.get
		)
		doContVocabSearch(cq)
	}

	def doContVocabSearch(cq:QueryObjectModel.CiteCollectionQuery):Unit = {
			cq.qProperty match {
				case None =>{
							val ov:Vector[CiteObject] = ObjectModel.collectionRepository.valueEquals(cq.qControlledVocabItem.get)
							cq.qCollection match {
								case Some(u) =>{
									val fv:Vector[CiteObject] = ov.filter(_.urn ~~ u)
									 loadSearchResults(cq,fv)
								}
							  case _ => loadSearchResults(cq,ov)
							}
				}
				case _ => {
							val ov:Vector[CiteObject] = ObjectModel.collectionRepository.valueEquals(cq.qProperty.get.urn, cq.qControlledVocabItem.get)
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
			QueryObjectModel.currentQueryCollection.get match {
				case None => None
				case Some(u) => Some(u)
			}
		}
		val cq = QueryObjectModel.CiteCollectionQuery(
			qCollection = collUrn,
			qProperty = QueryObjectModel.queryProperty.get,
			qPropertyType = QueryObjectModel.selectedPropertyType.get,
			qBoolVal = Some(QueryObjectModel.currentBooleanVal.get)
		)
		doBooleanSearch(cq)
	}

	def doBooleanSearch(cq:QueryObjectModel.CiteCollectionQuery):Unit = {
			cq.qProperty match {
				case None =>{
							val ov:Vector[CiteObject] = ObjectModel.collectionRepository.valueEquals(cq.qBoolVal.get)
							cq.qCollection match {
								case Some(u) =>{
									val fv:Vector[CiteObject] = ov.filter(_.urn ~~ u)
									 loadSearchResults(cq,fv)
								}
							  case _ => loadSearchResults(cq,ov)
							}
				}
				case _ => {
							val ov:Vector[CiteObject] = ObjectModel.collectionRepository.valueEquals(cq.qProperty.get.urn, cq.qBoolVal.get)
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
			QueryObjectModel.currentQueryCollection.get match {
				case None => None
				case Some(u) => Some(u)
			}
		}
		val cq = QueryObjectModel.CiteCollectionQuery(
			qCollection = collUrn,
			qProperty = QueryObjectModel.queryProperty.get,
			qPropertyType = QueryObjectModel.selectedPropertyType.get,
			qCtsUrn = QueryObjectModel.currentCtsUrnQuery.get
		)
		doCtsUrnSearch(cq)
	}

	def doCtsUrnSearch(cq:QueryObjectModel.CiteCollectionQuery):Unit = {
		g.console.log("got here 2")
			cq.qProperty match {
				case None =>{
							val ov:Vector[CiteObject] = ObjectModel.collectionRepository.urnMatch(cq.qCtsUrn.get)
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
							val ov:Vector[CiteObject] = ObjectModel.collectionRepository.urnMatch(cq.qProperty.get.urn, cq.qCtsUrn.get)
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
			QueryObjectModel.currentQueryCollection.get match {
				case None => None
				case Some(u) => Some(u)
			}
		}
		val cq = QueryObjectModel.CiteCollectionQuery(
			qCollection = collUrn,
			qProperty = QueryObjectModel.queryProperty.get,
			qPropertyType = QueryObjectModel.selectedPropertyType.get,
			qCite2Urn =  QueryObjectModel.currentCite2UrnQuery.get
		)
		doCite2UrnSearch(cq)
	}

	def doCite2UrnSearch(cq:QueryObjectModel.CiteCollectionQuery):Unit = {
			cq.qProperty match {
				case None =>{
							val ov:Vector[CiteObject] = ObjectModel.collectionRepository.urnMatch(cq.qCite2Urn.get)
							cq.qCollection match {
								case Some(u) =>{
									val fv:Vector[CiteObject] = ov.filter(_.urn ~~ u)
									 loadSearchResults(cq,fv)
								}
							  case _ => loadSearchResults(cq,ov)
							}
				}
				case _ => {
							val ov:Vector[CiteObject] = ObjectModel.collectionRepository.urnMatch(cq.qProperty.get.urn, cq.qCite2Urn.get)
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
