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

@JSExportTopLevel("citeapp.ObjectView")
object ObjectView {


	// HTML Div holding messages
	@dom
	def objectMessageDiv = {
		<div id="object_message" class={ s"app_message ${ObjectModel.userMessageVisibility.bind} ${ObjectModel.userAlert.bind}"  }>
		<p>{ ObjectModel.userMessage.bind }  </p>
		</div>
	}


	// HTML Div: main div for object display
	@dom
	def objectDiv = {
		val urnValidatingKeyUpHandler = { event: KeyboardEvent =>
			(event.currentTarget, event.keyCode) match {
				case (input: html.Input, KeyCode.Enter) => {
					event.preventDefault()
					ObjectController.changeUrn(s"${input.value.toString}")
					//input.value = ""
				}
				case(input: html.Input, _) =>  ObjectController.validateUrn(s"${input.value.toString}")
				case _ =>
			}
		}

		<div id="object_Container">

		<div id="object_sidebar" class="app_sidebarDiv">
		{ objectQueryForms.bind }
		{ objectCollectionsContainer.bind }
		</div>

		{ objectMessageDiv.bind }

		<p id="object_reportingCurrentUrn" class="app_reportingCurrentUrn"> { ObjectModel.urn.bind match{
			case Some(u) => u.toString
			case _ => ""
		} } </p>

	{ QueryObjectView.searchReportContainer.bind }

<!--
		<p id="object_breadcrumbs">
			<span id="object_clearCrumbs" class="object_breadcrumb"><a>[ Clear Breadcrumbs ]</a></span>	
			<span class="object_breadcrumb"><a>urn:cite2:hmt:vaimg.2017a:1234</a></span>	
		</p>
	-->

		<p id="object_urnInputP">
		<input
		class={ s"object_inputFor_${ObjectModel.objectOrCollection.bind}" }
		id="object_urnInput"
		size={ 40 }
		type="text"
		value={ 
			ObjectModel.displayUrn.bind match {
				case Some(u) => { u.toString }
				case None => ""
			}
		}
		onkeyup={ urnValidatingKeyUpHandler }>
		</input>

	{ ObjectView.retrieveObjectButton.bind }
	{ ObjectView.objectToCollectionButton.bind }

	{ collectionBrowseControls.bind }

	</p>


	{ objectContainer.bind }

	</div>
	}

@dom
def retrieveObjectButton = {
	<button
			onclick={ event: Event => {
				val s:String = js.Dynamic.global.document.getElementById("object_urnInput").value.toString
				ObjectModel.urn.value = Some(Cite2Urn(s))
				ObjectController.updateUserMessage("Retrieving object…",1)
				val task = Task{ ObjectController.changeObject }
				val future = task.runAsync
				/*
				js.timers.setTimeout(200){
					Future{ ObjectController.changeObject }
				}
				*/
			}
			
		}
		disabled={
					(ObjectModel.objectOrCollection.bind == "none")
				 }

> {

	ObjectModel.objectOrCollection.bind match {
		case "object" => {"Retrieve object"}
		case "collection" => {"Browse collection"}
		case "range" => {"Retrieve range"}
		case _ => {"Invalid URN"}
	}

}
</button>
}

@dom
def objectToCollectionButton = {
	<button
			onclick={ event: Event => {
				val s:String = js.Dynamic.global.document.getElementById("object_urnInput").value.toString
				ObjectModel.urn.value = Some(Cite2Urn(s).dropSelector)
				//ObjectModel.offset.value = 1
				//ObjectModel.limit.value = 2
				ObjectModel.objectOrCollection.value = "collection"
				ObjectController.updateUserMessage("Retrieving collection…",1)
				val task = Task{ ObjectController.changeUrn(Cite2Urn(s).dropSelector) }
				val future = task.runAsync
				/*
				js.timers.setTimeout(200){
					Future{ ObjectController.changeObject }
				}
				*/
				}
			}
		class={
			ObjectModel.objectOrCollection.bind match {
				case "collection" => "app_hidden"
				case "range" => "app_visible"
				case "object" => "app_visible"
				case _ => "app_hidden"
			}
		}
			disabled={
						((ObjectModel.objectOrCollection.bind == "none") || (ObjectModel.objectOrCollection.bind == "collection"))
					 }

> Browse this Object’s Collection </button>
}

/* Passage Container */
@dom
def objectContainer = {
	<div id="object_objectContainer" data:bgtext="No Object"
	class={ s"""${if( ObjectModel.boundObjects.value.size == 0 ){ "object_empty" } else {"object_not_empty"}}""" }
	>

		<div id="object_navButtonContainer_top"
		class={ if(ObjectModel.browsable.bind){"app_visible"} else {"app_hidden"}}>
			{ prevButton.bind }
			{ nextButton.bind }
		</div>

	{ objectInfo.bind }

	{ renderObjects.bind }

		<div id="object_navButtonContainer_bottom"
		class={ if(ObjectModel.browsable.bind){"app_visible"} else {"app_hidden"}}>
			{ prevButton.bind }
			{ nextButton.bind }
		</div>

	</div>
}

@dom
def objectInfo = {
	<p id="objects_objectInfo">
		{ ObjectModel.objectReport.bind }
	</p>
}

/* Fancy switcher, either listing objects as urn+label, or showing all the object's propeties. */

@dom
def renderObjects = {
	<ul>
	{
		for (obj <- ObjectModel.boundDisplayObjects ) yield {
			if ((ObjectModel.showObjects.value) || (ObjectModel.objectOrCollection.value == "object")){

				<li class="tables">
					<table>
						<tr>
	            <th>Property</th>
	            <th>Type</th>
	            <th>Value</th>
						</tr>
						<tr>
							<td>URN</td>
							<td>Cite2UrnType</td>
							<td>
							{ ObjectView.renderCiteUrnProperty(Some(obj.urn.value), obj.urn.value).bind }
							</td>
						</tr>
						<tr class="object_view_table_value">
							<td>Label</td>
							<td>StringType</td>
							<td>{ 
								obj.label.bind 
								}</td>
						</tr>
						{
							for (p <- obj.props) yield {
									 	{ ObjectView .renderProperty(Some(obj.urn.value), p).bind }
							}
						}
					</table>
				</li>
			} else {
			 <li class="list"><strong>
					{ obj.urn.bind.toString }
					</strong>
					{ obj.label.bind }
				</li>
			}
		}
	}
	</ul>
}


@dom def renderCiteUrnProperty(propUrn:Option[Cite2Urn], propVal:Cite2Urn) = {
<p>
	{ 
		s"${propVal.toString}" 

	}
	{ DataModelView.objectLinks(propUrn, propVal).bind }
</p>
}

@dom
def propertyLabelFromPropertyUrn(urn:Cite2Urn) = {
	val collectionUrn:Cite2Urn = urn.dropSelector.dropProperty
	val propertyUrn:Cite2Urn = urn.dropSelector
	val collDef:Option[CiteCollectionDef] = ObjectModel.collRep.value.get.catalog.collection(collectionUrn)
	collDef match {
		case Some(cd) => {
			val propDefs:Vector[CitePropertyDef] = cd.propertyDefs.filter(_.urn == propertyUrn).toVector
			propDefs.size match {
				case s if (s > 0) => propDefs(0).label
				case _ => urn.toString
			}


		}
		case None => urn.toString
	}
}

@dom def renderProperty(contextUrn:Option[Cite2Urn], p:ObjectModel.BoundCiteProperty) = {
	<tr>
	<td>{ 
		propertyLabelFromPropertyUrn(p.urn.value).bind
	}</td>
	<td>{ p.propertyType.bind.toString }</td>
	<td>{
		p.propertyType.value match {
			case Cite2UrnType =>{ 
				<p>{ ObjectView.renderCiteUrnProperty(Some(p.urn.value),Cite2Urn(p.propertyValue.value)).bind }</p>
				}
			case CtsUrnType =>{ <p>{ DataModelView.textLinks(contextUrn, CtsUrn(p.propertyValue.value)).bind }</p>}
			case StringType => {
				<p id={ s"propertyField_${p.urn.bind}" }>{ ExtendedTextPropertyView.extendedTextLinks(contextUrn,p).bind}</p>
			}
			case _ =>{ 
				<p>{ s"${p.propertyValue.bind.toString}"}</p>
			}
		}

	}</td>
	</tr>
}


// contextUrn is the URN of the property of which propVal is the value; contextUrn, therefore, provides access to the collection and the object
/*
@dom def thumbnailView(contextUrn:Option[Cite2Urn], propVal:Cite2Urn) = {
	{ ImageView.thumbnailView(contextUrn:Option[Cite2Urn], propVal:Cite2Urn).bind }
}
*/


/* Controls for limit and offset, as well as listing or showing objects */

@dom
def collectionBrowseControls = {
		<div id="object_browseControls"
		class={
			ObjectModel.objectOrCollection.bind match {
				case "collection" => "app_visible"
				case "range" => "app_visible"
				case "search" => "app_visible"
				case _ => "app_hidden"
			}
		}
		>

			<label for="object_browseOffset">Start at</label>
			<input type="text" id="object_browseOffset" size={5} value={ObjectModel.offset.bind.toString}
			onchange={ event: Event => {
				val currentOffset = ObjectModel.offset.value
				ObjectController.validateNumericEntry( event )
				if (ObjectModel.offset.value != currentOffset){
					ObjectController.setDisplay
				}
			}
			}/>
			<label for="object_browseLimit">Show</label>
			<input type="text" id="object_browseLimit" size={3} value={ObjectModel.limit.bind.toString}
			onchange={ event: Event => {
				val currentLimit = ObjectModel.limit.value
				ObjectController.validateNumericEntry( event )
				if (ObjectModel.limit.value != currentLimit){
					ObjectController.setDisplay
				}

				}
			}/>

			<div class="onoffswitch">
			    <input type="checkbox" name="onoffswitch" class="onoffswitch-checkbox" id="object_browseOrListSwitch" checked={false}
					onchange={ event: Event => {
							js.timers.setTimeout(500){ 
								ObjectController.switchDisplay( event ) 
							}
						}
					} />
			    <label class="onoffswitch-label" for="object_browseOrListSwitch">
			        <span class="object_onoffswitch-inner onoffswitch-inner"></span>
			        <span class="object_onoffswitch-switch onoffswitch-switch"></span>
			    </label>
			</div>
		</div>
}



/* Cited Works List */
@dom
def objectCollectionsContainer = {
	<div id="object_objectCollectionsContainer">
	<h2>CITE Collections</h2>
	<ul>
	{
		for (cc <- ObjectModel.collections) yield {
			<li>
			{ collectionUrnSpan( cc.urn ).bind } <br/>
			{ cc.collectionLabel }
			{ if(cc.isOrdered) "[ordered]" else "[unordered]" }
			<br/>
			{ ObjectModel.countObjects(cc.urn).toString } objects.

			</li>
		}
	}
	</ul>
	</div>
}



/* General-use functions for making clickable URNs */
@dom
def collectionUrnSpan(urn:Cite2Urn) = {
	<a
	class="app_clickable"
	onclick={ event: Event => {
		val mouseEvent = event.asInstanceOf[MouseEvent]
		if (mouseEvent.metaKey){
			true
		} else {
			ObjectController.insertFirstObjectUrn(urn) 
			ObjectModel.clearObject
			false
		}
	}
	}
	href={ s"?urn=${urn}"}	
	>
	{ urn.toString }
	</a>
}

/* For URN properties in objects */
@dom
def propertyUrnSpan(urnStr:String) = {
		<span></span>
}


	/* Navigation Buttons */
	@dom
	def nextButton = {
		<button
		class="navButton"
		onclick={ event: Event => ObjectController.getNext }
		disabled= {
			(ObjectModel.currentNext.bind == None)
		}
		> → </button>
	}

	@dom
	def prevButton = {
		<button
		class="navButton"
		onclick={ event: Event => ObjectController.getPrev }
		disabled= {
			(ObjectModel.currentPrev.bind == None)
		}
		> ← </button>
	}

/* Search Properties Forms */
@dom
def objectQueryForms = {
	<div id="object_queryForms">
	{ QueryObjectView.collectionQueryDiv.bind }
	</div>
}

}
