package citeapp

import com.thoughtworks.binding.{Binding, dom}
import com.thoughtworks.binding.Binding.{BindingSeq, Var, Vars}
import scala.scalajs.js
import scala.scalajs.js._
import scala.scalajs.js.Dynamic.{ global => g }
import org.scalajs.dom._
import org.scalajs.dom.ext._
import org.scalajs.dom.raw._
import edu.holycross.shot.cite._
import edu.holycross.shot.ohco2._
import edu.holycross.shot.citeobj._
import scala.collection.immutable.SortedMap

import scala.scalajs.js.annotation.JSExport
import js.annotation._

@JSExportTopLevel("citeapp.O2Model")
object O2Model {

	var msgTimer:scala.scalajs.js.timers.SetTimeoutHandle = null

	//val passage = Vars.empty[CitableNode]
	//var xmlPassage = new org.scalajs.dom.raw.DOMParser().parseFromString( "<cts:passage></cts:passage>", "text/xml" )
	var xmlPassage = js.Dynamic.global.document.createElement("div")
	val currentCitableNodes = Var(0)
	val isRtlPassage = Var(false)

	// urn is what the user requested
	val urn = Var(CtsUrn("urn:cts:ns:group.work.version.exemplar:passage"))
	// displayUrn is what will be shown
	val displayUrn = Var(CtsUrn("urn:cts:ns:group.work.version.exemplar:passage"))
	val versionsForCurrentUrn = Var(1)

	val userMessage = Var("")
	val userAlert = Var("default")
	val userMessageVisibility = Var("app_hidden")

	val textRepo = Var[Option[TextRepository]](None)
	val citedWorks = Vars.empty[CtsUrn]

	val currentNext = Var[Option[CtsUrn]](None)
	val currentPrev = Var[Option[CtsUrn]](None)


	/* Some methods for working the model */
	def versionsForUrn(urn:CtsUrn):Int = {
		var versions = 0
		if (O2Model.textRepo.value != None){
				val s = s"urn:cts:${urn.namespace}:${urn.textGroup}.${urn.work}:"
				val versionVector = O2Model.textRepo.value.get.catalog.entriesForUrn(CtsUrn(s))
				versions = versionVector.size
		}
		versions
	}

	def getPrevNextUrn(urn:CtsUrn):Unit = {
		O2Model.currentPrev.value = O2Model.textRepo.value.get.corpus.prevUrn(urn)
		O2Model.currentNext.value = O2Model.textRepo.value.get.corpus.nextUrn(urn)
	}

	def collapseToWorkUrn(urn:CtsUrn):CtsUrn = {
		val s = s"urn:cts:${urn.namespace}:${urn.textGroup}.${urn.work}:${urn.passageComponent}"
		val u = CtsUrn(s)
		u
	}

	def displayNewPassage(urn:CtsUrn):Unit = {
			O2Model.displayPassage(urn)
	}

	@dom
	def clearPassage:Unit = {
		O2Model.xmlPassage.innerHTML = ""
		O2Model.versionsForCurrentUrn.value = 0
	}

	@dom
	def displayPassage(newUrn: CtsUrn):Unit = {
		val tempCorpus: Corpus = O2Model.textRepo.value.get.corpus >= newUrn
		O2Model.currentCitableNodes.value = tempCorpus.size
		//O2Model.passage.get.clear
		O2Model.xmlPassage.innerHTML = ""

		var wholePassageElement:String = ""

		var currentVersionUrnStr = ""


		if (tempCorpus.size > 0){
			// set up columns

			
			for ( (cn,i) <- tempCorpus.nodes.zipWithIndex ) {

				var descEl = ""
				if (cn.urn.dropPassage.toString != currentVersionUrnStr ){
					currentVersionUrnStr = cn.urn.dropPassage.toString
					val desc = O2Model.textRepo.value.get.catalog.label(cn.urn)
					descEl = s"""<span class="o2_versionDescription ltr">${desc} : ${cn.urn.dropPassage.toString}</span>"""
				}
				val citString:String = s"""<span class="o2_passageUrn">${cn.urn.passageComponent}</span>"""

				val txtString:String = """<span class="o2_passage">""" + citString + cn.text + "</span>"

				O2Model.isRtlPassage.value = O2Model.checkForRTL(cn.text)

				val divClass =	if (O2Model.isRtlPassage.value){ "rtl" } else { "ltr" }
				val elString:String = s"""<div class="p ${divClass}">""" + descEl + txtString + "</div>"
				wholePassageElement += elString
				//O2Model.passage.get += cn
			}
		} else {
			// Display notice that there was no text found
			var elString = ""
			elString = s"""<div class="ltr o2_warnNoText">${newUrn} is not represented in the current library."""
			if (O2Model.versionsForCurrentUrn.value > 0){
				val s  = s"urn:cts:${newUrn.namespace}:${newUrn.textGroup}.${newUrn.work}:"
				elString += s""" However, there are versions of ${s} in the library. “See all versions of passage” will show the requested passage, if it is present in any other version of this work."""
			}
			elString += "</div>"
			wholePassageElement += elString

		}
		O2Model.xmlPassage.innerHTML = wholePassageElement
		js.Dynamic.global.document.getElementById("o2_xmlPassageContainer").appendChild(xmlPassage)

	}

def checkForRTL(s:String):Boolean = {
		val sStart = s.take(10)
		val arabicBlock = "[\u0600-\u06FF]".r
		val hebrewBlock = "[\u0591-\u05F4]".r
		var isRtl:Boolean = ((arabicBlock findAllIn sStart).nonEmpty || (hebrewBlock findAllIn sStart).nonEmpty)
		isRtl
}



	@dom
	def updateCitedWorks = {
		O2Model.citedWorks.value.clear
		for ( cw <- O2Model.textRepo.value.get.corpus.citedWorks){
			O2Model.citedWorks.value += cw
		}
	}


}
