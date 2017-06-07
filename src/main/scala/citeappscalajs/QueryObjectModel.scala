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

	val currentNumericQuery1 = Var[Double](1.0)
	val currentNumericQuery2 = Var[Double](2.0)
	val currentNumericOperator = Var("eq")

	val currentBooleanVal = Var(true)

	var currentCtsUrnQuery:CtsUrn = null
	var currentCite2UrnQuery:Cite2Urn = null

	// Change this to Vars[ObjectQueries]
	val pastQueries = Var("yep")

	def validateNumericEntry(thisEvent: Event):Unit = {
		val thisTarget = thisEvent.target.asInstanceOf[org.scalajs.dom.raw.HTMLInputElement]
		val testText = thisTarget.value.toString
		var previousEntry:Double = 0
		thisTarget.id match {
				case "queryObject_numeric1" => previousEntry = currentNumericQuery1.get
				case "queryObject_numeric2" => previousEntry = currentNumericQuery2.get
				case _ => previousEntry = 0
		}
		try{
			val mo:Double = testText.toDouble
			thisTarget.id match {
					case "queryObject_numeric1" => currentNumericQuery1 := mo
					case "queryObject_numeric2" => currentNumericQuery2 := mo
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
			currentCtsUrnQuery = testCts
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
			currentCite2UrnQuery = testCite2
		} catch {
			case e: Exception => {
				val badMo: String = testText
				thisTarget.value = ""
				ObjectController.updateUserMessage(s"${badMo} is not a valid CITE2 URN.", 2)
			}
		}
	}
}
