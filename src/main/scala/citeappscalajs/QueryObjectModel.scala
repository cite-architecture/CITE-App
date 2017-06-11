package citeapp

import com.thoughtworks.binding.{Binding, dom}
import com.thoughtworks.binding.Binding.{BindingSeq, Var, Vars}
import scala.scalajs.js
import scala.scalajs.js._
import scala.scalajs.js.Dynamic.{ global => g }
import js.annotation._
import collection.mutable
import collection.mutable._
import org.scalajs.dom._
import org.scalajs.dom.ext._
import org.scalajs.dom.raw._
import edu.holycross.shot.cite._
import edu.holycross.shot.ohco2._
import edu.holycross.shot.citeobj._
import edu.holycross.shot.citeenv._

import scala.scalajs.js.annotation.JSExport

@JSExport
object QueryObjectModel {

	val pastQueries = Vars.empty[CiteCollectionQuery]
	val currentQuery = Var[Option[CiteCollectionQuery]](None)
	val isValidSearch = Var(false)

	val currentQueryCollection = Var[Option[Cite2Urn]](None)
	val currentQueryCollectionProps = Vars.empty[CitePropertyDef]
	val queryProperty = Var[Option[CitePropertyDef]](None)
	val selectedPropertyType = Var[Option[CitePropertyType]](Some(StringType))
	val currentControlledVocabulary = Vars.empty[String]
	val currentControlledVocabItem = Var[Option[String]](None)
	val currentSearchString = Var[Option[String]](None)
	val currentRegexState = Var(false)
	val currentCaseSensitiveState = Var(false)

	val currentNumericQuery1 = Var[Option[Double]](None)
	val currentNumericQuery2 = Var[Option[Double]](None)
	val currentNumericOperator = Var("eq")

	val currentBooleanVal = Var(true)

	val currentCtsUrnQuery = Var[Option[CtsUrn]](None)
	val currentCite2UrnQuery = Var[Option[Cite2Urn]](None)

	def clearAll = {
		currentQuery := None
		isValidSearch := false
		currentQueryCollectionProps.get.clear
		queryProperty := None
		selectedPropertyType := Some(StringType)
		currentControlledVocabulary.get.clear
		currentControlledVocabItem := None
		currentSearchString := None
		currentRegexState := false
		currentCaseSensitiveState := false
		currentNumericQuery1 := None
		currentNumericQuery2 := None
		currentBooleanVal := true
		currentCtsUrnQuery := None
	  currentCite2UrnQuery := None
	}

	case class CiteCollectionQuery(
		val qCollection: Option[Cite2Urn],
		val qProperty: Option[CitePropertyDef] = None ,
		val qPropertyType: Option[CitePropertyType],
		val qControlledVocabItem: Option[String] = None,
		val qSearchString: Option[String] = None,
		val qRegex:Option[Boolean] = None,
		val qCaseSensitive: Option[Boolean] = None,
		val qNum1: Option[Double] = None,
		val qNum2: Option[Double] = None,
		val qNumOperator: Option[String] = None,
		val qBoolVal: Option[Boolean] = None,
		val qCtsUrn: Option[CtsUrn] = None,
		val qCite2Urn: Option[Cite2Urn] = None
	) {

		var numResults:Int = 0

		override def toString:String = {
			var qds:String = "Search: "
			qCollection match {
				case Some(x) => qds += s"${x.toString} :"
				case None => qds += s"All collections. "
			}
			qProperty match {
				case Some(x) => qds += s"Property: ${x.label}. "
				case _ => qds += s"All properties. "
			}
			qPropertyType match {
				case Some(x) => qds += s"${x}. "
				case _ => qds += s"No property type. "
			}
			qSearchString match {
				case Some(x) => {
					qds += s"Search for “${x}”. "
					qCaseSensitive match {
						case Some(true) => qds += s"Case sensitive. "
						case _ => qds += ""
					}
					qRegex match {
						case Some(true) => qds += s"With Regex. "
						case _ => qds += ""
					}
				}
				case _ => qds += ""
			}
			qBoolVal match {
				case Some(x) => qds += s"Search for Boolean value [${x}]. "
				case _ => qds += ""
			}
			qCtsUrn match {
				case Some(x) => qds += s"Search for Cts Urn ${x}. "
				case _ => qds += ""
			}
			qCtsUrn match {
				case Some(x) => qds += s"Search for Cite2 Urn ${x}. "
				case _ => qds += ""
			}
			qNumOperator match {
				case Some(x) => {
					x match {
						case "inRange" => qds += s"Search for value in range ${qNum1.get}–${qNum2.get}. "
						case "eq" => qds += s"Search for value = ${qNum1.get}. "
						case "gt" => qds += s"Search for value > ${qNum1.get}. "
						case "lt" => qds += s"Search for value < ${qNum1.get}. "
						case "gteq" => qds += s"Search for value >= ${qNum1.get}. "
						case "lteq" => qds += s"Search for value <= ${qNum1.get}. "
					}
				}
				case _ => qds += ""
			}
			qds += s" (${numResults} Objects)"
			qds
		}
	}


	def validateNumericEntry(thisEvent: Event):Unit = {
		val thisTarget = thisEvent.target.asInstanceOf[org.scalajs.dom.raw.HTMLInputElement]
		val testText = thisTarget.value.toString
		var previousEntry:Option[Double] = None
		thisTarget.id match {
			case "queryObject_numeric1" => previousEntry = currentNumericQuery1.get
			case "queryObject_numeric2" => previousEntry = currentNumericQuery2.get
			case _ => previousEntry = None
		}
		try{
			val mo:Double = testText.toDouble
			thisTarget.id match {
				case "queryObject_numeric1" => currentNumericQuery1 := Some(mo)
				case "queryObject_numeric2" => currentNumericQuery2 := Some(mo)
			}
		} catch {
			case e: Exception => {
				val badMo: String = testText
				thisTarget.value = ""
				ObjectController.updateUserMessage(s"${badMo} is not an numeric value.", 2)
			}
		}
	}

	def validateCtsUrnEntry(thisEvent: Event):Unit = {
		val thisTarget = thisEvent.target.asInstanceOf[org.scalajs.dom.raw.HTMLInputElement]
		val testText = thisTarget.value.toString
		try{
			val testCts:CtsUrn = CtsUrn(testText)
			currentCtsUrnQuery := Some(testCts)
		} catch {
			case e: Exception => {
				val badMo: String = testText
				thisTarget.value = ""
				ObjectController.updateUserMessage(s"${badMo} is not a valid CTS URN.", 2)
			}
		}
	}

	def validateCite2UrnEntry(thisEvent: Event):Unit = {
		val thisTarget = thisEvent.target.asInstanceOf[org.scalajs.dom.raw.HTMLInputElement]
		val testText = thisTarget.value.toString
		try{
			val testCite2:Cite2Urn = Cite2Urn(testText)
			currentCite2UrnQuery := Some(testCite2)
		} catch {
			case e: Exception => {
				val badMo: String = testText
				thisTarget.value = ""
				ObjectController.updateUserMessage(s"${badMo} is not a valid CITE2 URN.", 2)
			}
		}
	}

	/* Based on whether we're set up to query all properties or a single one,
	load controlled vocabulary into a binding Vars */
	@dom
	def loadControlledVocabulary = {
		QueryObjectModel.currentQueryCollection.get match {
			case None => loadControlledVocablAll
			case _ => loadControlledVocablOne
		}
	}

	@dom
	def loadControlledVocablAll = {
			QueryObjectModel.currentControlledVocabulary.get.clear
			var vList = new ListBuffer[String]
			for (c <- ObjectModel.collections.get ){
				for (p <- c.propertyDefs){
					for (v <- p.vocabularyList)
						vList += v
				}
			}
			val vSet = vList.toSet
			for (vs <- vSet ){
					QueryObjectModel.currentControlledVocabulary.get += vs
			}
			if (vSet.size > 0){
				QueryObjectModel.currentControlledVocabItem := Some(vSet.head)
			}


	}

	@dom
	def loadControlledVocablOne = {
		QueryObjectModel.queryProperty.bind match {
			case None => {
				QueryObjectModel.currentControlledVocabulary.get.clear
				for (p <- QueryObjectModel.currentQueryCollectionProps.get){
					if (p.propertyType == ControlledVocabType){
						for (v <- p.vocabularyList){
							QueryObjectModel.currentControlledVocabulary.get += v
						}
					}
				}
				if (QueryObjectModel.currentControlledVocabulary.get.size > 0){
						QueryObjectModel.currentControlledVocabItem := Some(QueryObjectModel.currentControlledVocabulary.get.head)
				}
			}
			case _ => {
				QueryObjectModel.currentControlledVocabulary.get.clear
				for (v <- QueryObjectModel.queryProperty.get.get.vocabularyList){
					QueryObjectModel.currentControlledVocabulary.get += v
				}
				QueryObjectModel.currentControlledVocabItem := Some(QueryObjectModel.currentControlledVocabulary.get.head)
			}
		}
		//QueryObjectModel.currentControlledVocabItem := Some(QueryObjectModel.currentControlledVocabulary.get.head)
	}



}
