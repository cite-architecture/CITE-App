package citeapp

import com.thoughtworks.binding.{Binding, dom}
import com.thoughtworks.binding.Binding.{BindingSeq, Var, Vars}
import scala.scalajs.js
import scala.scalajs.js._
import js.annotation._
import scala.concurrent._
//import ExecutionContext.Implicits.global
import collection.mutable
import collection.mutable._
import scala.scalajs.js.Dynamic.{ global => g }
import org.scalajs.dom._
import org.scalajs.dom.ext._
import org.scalajs.dom.raw._
import edu.holycross.shot.cite._
import edu.holycross.shot.ohco2._
import edu.holycross.shot.citeobj._
import edu.holycross.shot.scm._

import monix.execution.Scheduler.Implicits.global
import monix.eval._
import com.karasiq.highlightjs.HighlightJS
import com.karasiq.markedjs.{Marked, MarkedOptions, MarkedRenderer}
import scala.scalajs.js.annotation.JSExport

@JSExportTopLevel("ExtendedTextPropertyView")
object ExtendedTextPropertyView {

	// Returns None, or the type of extension
	def textPropertyIsExtended(propUrn:Cite2Urn):Option[String] = {
		None	
	}

/*
	@dom
	def xxxxxxxextendedTextLinks(contextUrn:Option[Cite2Urn], p:ObjectModel.BoundCiteProperty) = {
		<span> String Type: <br/> { 
			val extendedOption:Option[String] = DataModelController.textPropertyIsExtended(p.urn.value)
			extendedOption match {
				case Some("markdown") => {
					initiateMarked(s"${p.propertyValue.bind}", s"propertyField_${p.urn.bind}")
				}
				case _ => {
					g.console.log(s"No extension.")
				}
			}
		}
		<br/>
		{ s"${p.propertyValue.bind.toString}" }
		</span> 
	}
	*/

	@dom
	def extendedTextLinks(contextUrn:Option[Cite2Urn], p:ObjectModel.BoundCiteProperty) = {
		val extendedOption:Option[String] = DataModelController.textPropertyIsExtended(p.urn.value)
		extendedOption match {
			case Some("markdown") => {
				markdownTextContent(p).bind
			}
			case Some("teixml") => {
				teiTextContent(p).bind	
			}
			case Some("pleiadesuri") => {
				pleiadesTextContent(p).bind	
			}
			case Some("latlong") => {
				latLongTextContent(p).bind
			}
			case Some("geojson") => {
				geojsonTextContent(p).bind
			}
			case _ => {
				plainTextContent(p).bind
			}
		}
	}

	@dom
	def geojsonTextContent(p:ObjectModel.BoundCiteProperty) = {
		val geojson:String = p.propertyValue.bind.trim
		g.console.log(geojson)
		val elId:String = s"propertyField_${p.urn.bind}"
		initiateLeafletWithGeoJson(geojson, elId)
		<div class="leafletMap" id={ elId }> { s"Leaflet.js map will be drawn here: ${p.propertyValue.bind.toString}" } </div>
	}

	@dom
	def latLongTextContent(p:ObjectModel.BoundCiteProperty) = {
		val lat:String = p.propertyValue.bind.split(",")(0).trim
		val long:String = p.propertyValue.bind.split(",")(1).trim
		val elId:String = s"propertyField_${p.urn.bind}"
		initiateLeafletWithLatLong(lat, long, elId)
		<div class="leafletMap" id={ elId }> { s"Leaflet.js map will be drawn here: ${p.propertyValue.bind.toString}" } </div>
	}

	@dom
	def plainTextContent(p:ObjectModel.BoundCiteProperty) = {
		<span> { s"${p.propertyValue.bind.toString}" } </span>
	}

	@dom
	def markdownTextContent(p:ObjectModel.BoundCiteProperty) = {
		initiateMarked(s"${p.propertyValue.bind}", s"propertyField_${p.urn.bind}")
		<span id={ s"propertyField_${p.urn.bind}" }>
			{ s"${p.propertyValue.bind.toString}" }
		</span>
	}

	@dom
	def teiTextContent(p:ObjectModel.BoundCiteProperty) = {
		val thisSpan = document.createElement("span").asInstanceOf[HTMLSpanElement]		
		thisSpan.innerHTML = p.propertyValue.bind
		thisSpan
	}

	@dom
	def pleiadesTextContent(p:ObjectModel.BoundCiteProperty) = {
		<span>
			{ "Pleiades place resource: "}
			<a href={ p.propertyValue.bind }>
				{ p.propertyValue.bind }
			</a>
		</span>
	}


	def getMarkdownProperyAndId(p:ObjectModel.BoundCiteProperty):Map[String,String] = {
		val propertyName:String = ""
		Map("id" -> "some id", "content" -> "some content.")
	}

	/* Methods for connecting out to Javascript */
	@JSGlobal("initiateMarked")
	@js.native
	object initiateMarked extends js.Any {
		def apply(markdownString:String, elementId:String): js.Dynamic = js.native
	}

	@JSGlobal("initiateLeafletWithLatLong")
	@js.native
	object initiateLeafletWithLatLong extends js.Any {
		def apply(lat:String, long:String, elementId:String): js.Dynamic = js.native
	}

	@JSGlobal("initiateLeafletWithGeoJson")
	@js.native
	object initiateLeafletWithGeoJson extends js.Any {
		def apply(geojson:String, elementId:String): js.Dynamic = js.native
	}
}