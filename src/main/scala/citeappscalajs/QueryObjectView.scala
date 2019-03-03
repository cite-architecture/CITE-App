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
import js.annotation._
import scala.concurrent._
//import ExecutionContext.Implicits.global
import monix.execution.Scheduler.Implicits.global
import monix.eval._

@JSExportTopLevel("QueryObjectView")
object QueryObjectView {

	@dom
	def collectionQueryDiv = {
		<div>
		{ previousQueryMenu.bind }
		<h2>Search Collection</h2>
		{ collectionListSelect.bind }
		<br/>
		{ propertyListSelect.bind }
		{ queryAllProps.bind}
		{ queryOneProp.bind}
		</div>
	}

	/* Div containing controls for querying all properties of a collection */
	@dom
	def queryAllProps = {
		QueryObjectModel.loadControlledVocabulary
		<div class={
			//val pselect = QueryObjectModel.queryProperty.get
			QueryObjectModel.queryProperty.bind match {
				case None => "queryObject_qform queryObject_formvisible"
				case _ => "queryObject_qform queryObject_formhidden"
			}
		}
		id="queryObject_qAllProperties">

		{ propertyTypeSelector.bind }
		{ controlledVocabSelect.bind }
		{ numericSearch.bind }
		{ booleanSearch.bind }
		{ ctsUrnSearch.bind }
		{ cite2UrnSearch.bind }
		{ stringSearch.bind }
		{ queryButton.bind }

		</div>
	}

	@dom
	def queryButton = {
	<button
		id="queryObject_Submit"
		disabled={ QueryObjectModel.isValidSearch.bind == false }
			onclick={ event: Event => {
					ObjectController.updateUserMessage("Querying Collection. Please be patient…",1)
					val task = Task{QueryObjectController.initQuery}	
					val future = task.runAsync
					/*
					js.timers.setTimeout(200){
						Future{ QueryObjectController.initQuery }
					}
					*/
				}
			}
		>{ if (QueryObjectModel.isValidSearch.bind){
			"Search Collections"
		} else {
			"Enter Search Terms"
		}
			}
	</button>
	}


	/* Div containing controls for querying all properties of a collection */
	@dom
	def queryOneProp = {
		<div class={
			//val pselect = QueryObjectModel.queryProperty.get
			QueryObjectModel.queryProperty.bind match {
				case None => "queryObject_qform queryObject_formhidden"
				case _ => "queryObject_qform queryObject_formvisible"
			}
		}
		id="queryObject_qOneProperty">

		{ controlledVocabSelect.bind }
		{ numericSearch.bind }
		{ booleanSearch.bind }
		{ ctsUrnSearch.bind }
		{ cite2UrnSearch.bind }
		{ stringSearch.bind }
		{ queryButton.bind }

		</div>
	}

	/* Div containing controls for querying all properties of a collection */
	@dom
	def propertyTypeSelector = {
		<label for="queryObject_typeSelector">Select Data Type</label>
		<select id="queryObject_typeSelector"
		value={ QueryObjectModel.selectedPropertyType.bind match{
				case Some(t) => t.toString
				case None => ""
			}
		}
		onchange={ event: Event => {
			val thisSelect = document.getElementById("queryObject_typeSelector").asInstanceOf[HTMLSelectElement]
			thisSelect.value match {
				case "StringType" => QueryObjectModel.selectedPropertyType.value =  Some(StringType)
				case "BooleanType" => QueryObjectModel.selectedPropertyType.value =  Some(BooleanType)
				case "Cite2UrnType" => QueryObjectModel.selectedPropertyType.value =  Some(Cite2UrnType)
				case "CtsUrnType" => QueryObjectModel.selectedPropertyType.value =  Some(CtsUrnType)
				case "NumericType" => QueryObjectModel.selectedPropertyType.value =  Some(NumericType)
				case "ControlledVocabType" => {
					QueryObjectModel.loadControlledVocabulary
					QueryObjectModel.selectedPropertyType.value =  Some(ControlledVocabType)
				}
				case _ => QueryObjectModel.selectedPropertyType.value =  None
			}
			QueryObjectController.isValidSearch
		}
	}>
	<option value="StringType">String</option>
	<option value="BooleanType">Boolean</option>
	<option value="Cite2UrnType">Cite2Urn</option>
	<option value="CtsUrnType">CtsUrn</option>
	<option value="NumericType">Numeric</option>
	<option value="ControlledVocabType">Controlled Vocabulary</option>
	</select>
}



@dom
def controlledVocabSelect = {
	<div id="queryObject_controlledVocabSelectorDiv"
	onchange={ event: Event => {
		val thisTarget = event.target.asInstanceOf[org.scalajs.dom.raw.HTMLSelectElement]
		val testText:String = thisTarget.value.toString
		QueryObjectModel.currentControlledVocabItem.value =  Some(testText)
		QueryObjectController.isValidSearch
		}
	}
	class={
		QueryObjectModel.queryProperty.bind match {
			case None => {
				QueryObjectModel.selectedPropertyType.bind match {
					case Some(ControlledVocabType) => {
						QueryObjectModel.currentControlledVocabulary.value.size match {
							case x if (x > 0) => "queryObject_formvisible"
							case _ => "queryObject_formhidden"
						}
					}
					case _ => "queryObject_formhidden"
				}
			}
			case _ => {
				QueryObjectModel.selectedPropertyType.bind match {
					case Some(ControlledVocabType) => "queryObject_formvisible"
					case _ => "queryObject_formhidden"
				}
			}
		}
	} >
	<label for="queryObject_vocabularySelector">Controlled Vocabulary</label>
	<select id="queryObject_vocabularySelector">
	{ controlledVocabOptions.bind }
	</select>
	</div>
}


@dom
def controlledVocabOptions = {
	for (vc <- QueryObjectModel.currentControlledVocabulary) yield {
		<option value={ vc }>{ vc }</option>
	}
}

@dom
def booleanSearch = {
	<div id="queryObject_booleanDiv"
	class={
		QueryObjectModel.selectedPropertyType.bind match {
			case Some(BooleanType) =>  "queryObject_formvisible"
			case _ => "queryObject_formhidden"
		}
	}>
	<label for="queryObject_booleanSelect">Value is </label>
	<select id="queryObject_booleanSelect"
	onchange={ event: Event => {
		val thisTarget = event.target.asInstanceOf[org.scalajs.dom.raw.HTMLSelectElement]
		val testText:String = thisTarget.value.toString
		QueryObjectModel.currentBooleanVal.value =  { testText == "true" }
	}
}>
<option value="true">true</option>
<option value="false">false</option>
</select>

</div>

}

@dom
def numericSearch = {
	<div id="queryObject_numericDiv"
	class={
		QueryObjectModel.selectedPropertyType.bind match {
			case Some(NumericType) =>  "queryObject_formvisible"
			case _ => "queryObject_formhidden"
		}
	}>
	<label for="queryObject_numericOperatorSelect">Value is </label>
	<select id = "queryObject_numericOperatorSelect"
	onchange={ event: Event => {
		val thisTarget = event.target.asInstanceOf[org.scalajs.dom.raw.HTMLSelectElement]
		val testText = thisTarget.value.toString
		QueryObjectModel.currentNumericOperator.value =  testText
		QueryObjectController.isValidSearch
	}
} >
<option value="eq">=</option>
<option value="lt">&lt;</option>
<option value="gt">&gt;</option>
<option value="lteq">&lt;=</option>
<option value="gteq">&gt;=</option>
<option value="inRange">in range</option>
</select>
<input id="queryObject_numeric1" type="text" size={8} placeholder="1.0"
onchange={
	event: Event => {
		QueryObjectModel.validateNumericEntry( event )
		QueryObjectController.isValidSearch
	}
}
onkeyup={
	event: Event => {
		QueryObjectModel.validateNumericEntry( event )
		QueryObjectController.isValidSearch
	}
} />
<label for="queryObject_numeric2"
class={
	QueryObjectModel.currentNumericOperator.bind match {
		case "inRange" => "queryObject_fieldvisible"
		case _ => "queryObject_fieldhidden"
	}
} > – </label>
<input id="queryObject_numeric2" type="text" size={8} placeholder="2.0"
onchange={ event: Event => {
	QueryObjectModel.validateNumericEntry( event )
	QueryObjectController.isValidSearch
	}
}
onkeyup={
	event: Event => {
		QueryObjectModel.validateNumericEntry( event )
		QueryObjectController.isValidSearch
	}
}
class={
	QueryObjectModel.currentNumericOperator.bind match {
		case "inRange" => "queryObject_fieldvisible"
		case _ => "queryObject_fieldhidden"
	}
} />

</div>
}

@dom
def ctsUrnSearch = {

	<div id="queryObject_ctsUrnDiv"
	class={
		QueryObjectModel.selectedPropertyType.bind match {
			case Some(CtsUrnType) =>  "queryObject_formvisible"
			case _ => "queryObject_formhidden"
		}
	}>
	<label for="queryObject_ctsUrnField">Cts Urn</label>
	<input id="queryObject_ctsUrnField" type="text" size={30} placeholder="CTS URN Here"
	onchange={ event: Event => {
			QueryObjectModel.validateCtsUrnEntry( event )
			QueryObjectController.isValidSearch
		}
	} />
	</div>
}

@dom
def cite2UrnSearch = {

	<div id="queryObject_cite2UrnDiv"
	class={
		QueryObjectModel.selectedPropertyType.bind match {
			case Some(Cite2UrnType) =>  "queryObject_formvisible"
			case _ => "queryObject_formhidden"
		}
	}>
	<label for="queryObject_cite2UrnField">Cite2 Urn</label>
	<input id="queryObject_cite2UrnField" type="text" size={30} placeholder="CITE2 URN Here"
	onchange={ event: Event => {
			QueryObjectModel.validateCite2UrnEntry( event )
			QueryObjectController.isValidSearch
		}
	} />
	</div>
}

@dom
def stringSearch = {

	<div id="queryObject_stringSearchDiv"
	class={
		QueryObjectModel.selectedPropertyType.bind match {
			case Some(StringType) =>  "queryObject_formvisible"
			case _ => "queryObject_formhidden"
		}
	}>
	<textarea id="queryObject_stringField"  cols={40} rows={3} placeholder="Query Text"
	  value={
			QueryObjectModel.currentSearchString.bind match {
				case Some(s) => s
				case _ => ""
			}
		}
		onchange={ event: Event => {
			val thisTarget = event.target.asInstanceOf[org.scalajs.dom.raw.HTMLTextAreaElement]
			val thisVal = thisTarget.value
			if (thisVal.size == 0 ){
				QueryObjectModel.currentSearchString.value =  None
			} else {
			  QueryObjectModel.currentSearchString.value =  Some(thisTarget.value)
			}
			QueryObjectController.isValidSearch
			}
		}
		onkeyup={ event: Event => {
			val thisTarget = event.target.asInstanceOf[org.scalajs.dom.raw.HTMLTextAreaElement]
			val thisVal = thisTarget.value
			if (thisVal.size == 0 ){
				QueryObjectModel.currentSearchString.value =  None
			} else {
			  QueryObjectModel.currentSearchString.value =  Some(thisTarget.value)
			}
			QueryObjectController.isValidSearch
			}
		}
	/>
	<br/>
	<input type="checkbox" id="queryObject_caseSensitiveSelect" checked={ QueryObjectModel.currentCaseSensitiveState.bind }
	  class={
			QueryObjectModel.currentRegexState.bind match{
					case true => "queryObject_formhidden"
					case _ => "queryObject_fieldvisible"
			}
		}
		onchange={ event: Event => {
			val thisTarget = event.target.asInstanceOf[org.scalajs.dom.raw.HTMLInputElement]
			val thisVal = thisTarget.checked
			QueryObjectModel.currentCaseSensitiveState.value =  thisVal
		}
	}/>
	<label for="queryObject_caseSensitiveSelect"
	  class={
			QueryObjectModel.currentRegexState.bind match{
					case true => "queryObject_formhidden"
					case _ => "queryObject_fieldvisible"
			}
	} >Case Sensitive</label>
	<input type="checkbox" id="queryObject_regexSelect" checked={ QueryObjectModel.currentRegexState.bind }
		onchange={ event: Event => {
			val thisTarget = event.target.asInstanceOf[org.scalajs.dom.raw.HTMLInputElement]
			val thisVal = thisTarget.checked
			QueryObjectModel.currentRegexState.value =  thisVal
		}
	}/>
	<label for="queryObject_regexSelect">Regular Expression</label>
	</div>
}

/* selector for which collection to search */
@dom
def collectionListSelect = {
	<label for="objectQuery_collectionList">Collection</label>
	<select id="objectQuery_collectionList"
	onchange={ event: Event => {
		QueryObjectModel.clearAll
		val thisSelect = document.getElementById("objectQuery_collectionList").asInstanceOf[HTMLSelectElement]
		if (thisSelect.value == "all") {
			QueryObjectModel.currentQueryCollection.value =  None
			QueryObjectModel.queryProperty.value =  None
			QueryObjectModel.loadControlledVocabulary
		} else {
			QueryObjectModel.currentQueryCollection.value =  Some(Cite2Urn(thisSelect.value))
			QueryObjectModel.currentQueryCollectionProps.value.clear
			QueryObjectModel.selectedPropertyType.value =  Some(StringType)
			for (p <- ObjectModel.collRep.value.get.collectionDefinition(Cite2Urn(thisSelect.value)).get.propertyDefs) {
				QueryObjectModel.currentQueryCollectionProps.value += p
			}
			QueryObjectModel.queryProperty.value =  None
			QueryObjectModel.loadControlledVocabulary
		}
		QueryObjectController.isValidSearch
	}
} >
	<option value="all">All Collections</option>
{
	for (c <- ObjectModel.collections) yield {
		<option value={ c.urn.toString } >{ c.collectionLabel }</option>
	}

}
</select>
}

/* selector for which property of the current selector to search */
@dom
def propertyListSelect = {
	<label for="objectQuery_propertyList"
	class={
			QueryObjectModel.currentQueryCollection.bind match {
				case None => "queryObject_formhidden"
				case _ => {
					"queryObject_fieldvisible"
				}
			}
	}>Property</label>

	<select id="objectQuery_propertyList"
	class={
			QueryObjectModel.currentQueryCollection.bind match {
				case None => "queryObject_formhidden"
				case _ => "queryObject_fieldvisible"
			}
	}
	onchange={ event: Event => {
		val thisSelect = document.getElementById("objectQuery_propertyList").asInstanceOf[HTMLSelectElement]
		(thisSelect.value:String) match {
			case "all" => {
				QueryObjectModel.queryProperty.value =  None
				QueryObjectModel.selectedPropertyType.value =  Some(StringType)
			}
			case x => {
				val u = Cite2Urn(x)
				val collU = u.dropProperty
				val cd = ObjectModel.collRep.value.get.collectionDefinition(collU).get
				QueryObjectModel.queryProperty.value =  Some(cd.propertyDefs.filter(_.urn == u)(0))
				val pt = QueryObjectModel.queryProperty.value.get.propertyType
				QueryObjectModel.selectedPropertyType.value =  Some(pt)
			}
		}
		if (QueryObjectModel.selectedPropertyType.value == Some(ControlledVocabType)){
			QueryObjectModel.loadControlledVocabulary
		}
		QueryObjectController.isValidSearch
	}
}>
<option value="all">-All Properties-</option>
{ propertyListEnumeration.bind }
</select>
}

@dom
def propertyListEnumeration = {
	for (p <- QueryObjectModel.currentQueryCollectionProps) yield {
		<option value={ p.urn.toString }>{ p.label } [{ p.propertyType.toString }]</option>
	}
}

/* Previous Searches */
@dom
def previousQueryMenu = {
	<div id="queryObject_previousMenu"
	class={
			{ if ((QueryObjectModel.pastQueries.length.bind < 1) && (ObjectModel.objectHistory.length.bind < 1)) { 
				"dropdown empty" 
			} else {
				"dropdown"
			} 
		}
	} >
	<span>History</span>
	{ QueryObjectView.previousQueriesMenu.bind }
	</div>
}

@dom
def previousQueriesMenu = {
	<div class="dropdown-content">
		{ QueryObjectView.loadPreviousSearches.bind }
		{ QueryObjectView.loadPreviousObjects.bind }
	</div>
}

@dom
def loadPreviousSearches = {
for (q <- QueryObjectModel.pastQueries) yield {

				<p onclick={ event: Event => {
						QueryObjectController.loadQuery(q)
						
					}
				}>{ q.toString }</p>
			}
}

@dom
def loadPreviousObjects = {
for (u <- ObjectModel.objectHistory) yield {
				<p onclick={ event: Event => {
					ObjectController.changeUrn(u)	
					}
				}>{ u.toString }</p>
			}	
}

@dom
def searchReportContainer = {
	<p id="queryObject_searchReport"
	class={
		QueryObjectModel.currentQuery.bind match {
			case None => "queryObject_formhidden"
			case _ => "queryObject_formvisible"
		}
	}>

		{ QueryObjectModel.currentQuery.bind match{
			case Some(c) => c.toString
			case _ => ""

			}
		}

	</p>
}


}
