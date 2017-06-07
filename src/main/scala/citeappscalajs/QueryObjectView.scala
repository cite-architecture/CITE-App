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
		QueryObjectView.loadControlledVocabulary
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
			onclick={ event: Event => {
					ObjectController.updateUserMessage("Querying Collection. Please be patient…",1)
					js.timers.setTimeout(500){ QueryObjectController.initQuery }
				}
			}
		>Search Collection</button>
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
		onchange={ event: Event => {
			val thisSelect = document.getElementById("queryObject_typeSelector").asInstanceOf[HTMLSelectElement]
			thisSelect.value match {
				case "StringType" => QueryObjectModel.selectedPropertyType := Some(StringType)
				case "BooleanType" => QueryObjectModel.selectedPropertyType := Some(BooleanType)
				case "Cite2UrnType" => QueryObjectModel.selectedPropertyType := Some(Cite2UrnType)
				case "CtsUrnType" => QueryObjectModel.selectedPropertyType := Some(CtsUrnType)
				case "NumericType" => QueryObjectModel.selectedPropertyType := Some(NumericType)
				case "ControlledVocabType" => {
					QueryObjectView.loadControlledVocabulary
					QueryObjectModel.selectedPropertyType := Some(ControlledVocabType)
				}
				case _ => QueryObjectModel.selectedPropertyType := None
			}
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


/* Based on whether we're set up to query all properties or a single one,
load controlled vocabulary into a binding Vars */
@dom
def loadControlledVocabulary = {
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
		}
		case _ => {
			QueryObjectModel.currentControlledVocabulary.get.clear
			for (v <- QueryObjectModel.queryProperty.get.get.vocabularyList){
				QueryObjectModel.currentControlledVocabulary.get += v
			}
		}
	}
}

@dom
def controlledVocabSelect = {
	<div id="queryObject_controlledVocabSelectorDiv"
	class={
		QueryObjectModel.queryProperty.bind match {
			case None => {
				QueryObjectModel.selectedPropertyType.bind match {
					case Some(ControlledVocabType) => {
						QueryObjectModel.currentControlledVocabulary.bind.size match {
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
		QueryObjectModel.currentBooleanVal := { testText == "true" }
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
		QueryObjectModel.currentNumericOperator := testText
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
onchange={ event: Event => QueryObjectModel.validateNumericEntry( event )}
/>
<label for="queryObject_numeric2"
class={
	QueryObjectModel.currentNumericOperator.bind match {
		case "inRange" => "queryObject_fieldvisible"
		case _ => "queryObject_fieldhidden"
	}
} > – </label>
<input id="queryObject_numeric2" type="text" size={8} placeholder="2.0"
onchange={ event: Event => QueryObjectModel.validateNumericEntry( event )}
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
	onchange={ event: Event => QueryObjectModel.validateCtsUrnEntry( event )}
	/>
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
	onchange={ event: Event => QueryObjectModel.validateCite2UrnEntry( event )}
	/>
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
	<textarea id="queryObject_stringField"  cols={40} rows={3} placeholder="Query Text" />
	<br/>
	<input type="checkbox" id="queryObject_regexSelect" checked={ false }/>
	<label for="queryObject_regexSelect">Regular Expression</label>
	</div>
}

/* selector for which collection to search */
@dom
def collectionListSelect = {
	<label for="objectQuery_collectionList">Collection</label>
	<select id="objectQuery_collectionList"
	onchange={ event: Event => {
		val thisSelect = document.getElementById("objectQuery_collectionList").asInstanceOf[HTMLSelectElement]
		QueryObjectModel.currentQueryCollection := Some(Cite2Urn(thisSelect.value))
		QueryObjectModel.currentQueryCollectionProps.get.clear
		for (p <- ObjectModel.collectionRepository.collectionDefinition(Cite2Urn(thisSelect.value)).get.propertyDefs) {
			QueryObjectModel.currentQueryCollectionProps.get += p
		}
		QueryObjectModel.queryProperty := None
	}
} >
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
	<label for="objectQuery_propertyList">Limit to Property</label>
	<select id="objectQuery_propertyList"
	onchange={ event: Event => {
		val thisSelect = document.getElementById("objectQuery_propertyList").asInstanceOf[HTMLSelectElement]
		(thisSelect.value:String) match {
			case "all" => {
				QueryObjectModel.queryProperty := None
				QueryObjectModel.selectedPropertyType := Some(StringType)
			}
			case x => {
				val u = Cite2Urn(x)
				val collU = u.dropProperty
				val cd = ObjectModel.collectionRepository.collectionDefinition(collU).get
				QueryObjectModel.queryProperty := Some(cd.propertyDefs.filter(_.urn == u)(0))
				val pt = QueryObjectModel.queryProperty.get.get.propertyType
				QueryObjectModel.selectedPropertyType := Some(pt)
			}
		}
		QueryObjectView.loadControlledVocabulary
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
	<div
	class={
		{ if (QueryObjectModel.pastQueries.bind == 0) { "dropdown empty" } else {"dropdown"} }
	} >
	<span>Previous Queries</span>
	{ QueryObjectView.previousQueries.bind }
	</div>
}

@dom
def previousQueries = {
	<div class="dropdown-content">
	<p>one</p>
	<p>two</p>
	<p>three</p>
	</div>
}


}
