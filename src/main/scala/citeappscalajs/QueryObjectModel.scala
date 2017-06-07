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

	val currentQueryCollection = Var[Option[Cite2Urn]](None)
	val currentQueryCollectionProps = Vars.empty[CitePropertyDef]
	val queryProperty = Var[Option[CitePropertyDef]](None)
	val selectedPropertyType = Var[Option[CitePropertyType]](Some(StringType))
	val currentControlledVocabulary = Vars.empty[String]
	val currentControlledVocabItem = Var[Option[String]](None)
	val currentSearchString = Var[Option[String]](None)

	val currentNumericQuery1 = Var[Option[Double]](None)
	val currentNumericQuery2 = Var[Option[Double]](None)
	val currentNumericOperator = Var("eq")

	val currentBooleanVal = Var(true)

	val currentCtsUrnQuery = Var[Option[CtsUrn]](None)
	val currentCite2UrnQuery = Var[Option[Cite2Urn]](None)

	case class CiteCollectionQuery(
			val qCollection: Option[Cite2Urn],
			val qProperty: Option[CitePropertyDef],
			val qPropertyType: Option[CitePropertyType],
			val qControlledVocabItem: Option[String],
			val qSearchString: Option[String],
			val qNum1: Option[Double],
			val qNum2: Option[Double],
			val qNumOperator: Option[String],
			val qBoolVal: Option[Boolean],
			val qCtsUrn: Option[CtsUrn],
			val qCite2Urn: Option[Cite2Urn]
	)
	{
		override def toString:String = {
			var qds:String = "Query: "
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
				case Some(x) => qds += s"Search for “${x}”. "
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
			qds
		}
	}

	// Change this to Vars[ObjectQueries]
	val pastQueries = Var("yep")

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
}
