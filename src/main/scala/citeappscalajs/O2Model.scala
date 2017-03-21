package citeapp

import com.thoughtworks.binding.{Binding, dom}
import com.thoughtworks.binding.Binding.{BindingSeq, Var, Vars}
import scala.scalajs.js
import scala.scalajs.js._
import scala.scalajs.js.Dynamic.global
import org.scalajs.dom._
import org.scalajs.dom.ext._
import org.scalajs.dom.raw._
import edu.holycross.shot.cite._
import edu.holycross.shot.ohco2._
import edu.holycross.shot.citeenv._

import scala.scalajs.js.annotation.JSExport

@JSExport
object O2Model {

	//val passage = Vars.empty[CitableNode]
	//var xmlPassage = new org.scalajs.dom.raw.DOMParser().parseFromString( "<cts:passage></cts:passage>", "text/xml" )
	var xmlPassage = js.Dynamic.global.document.createElement("div")

	val urn = Var(CtsUrn("urn:cts:ns:group.work.version.exemplar:passage"))

	val userMessage = Var("")
	val userAlert = Var("default")
	val userMessageVisibility = Var("")

	var textRepository: TextRepository = null
	val citedWorks = Vars.empty[CtsUrn]

	val currentNext = Var[Option[CtsUrn]](None)
	val currentPrev = Var[Option[CtsUrn]](None)

	/* Values for NGrams */
	val nGramThreshold = Var(3)

	val nGramResults = Var[StringHistogram](null)
	val nGramQuery = Var("")

	val nGramUrns =  Vars.empty[CtsUrn]
	val nGramUrnQuery = Var("")

	/* Some methods for working the model */

	def getPrevNextUrn(urn:CtsUrn):Unit = {
		O2Model.currentPrev := O2Model.textRepository.corpus.prevUrn(urn)
		O2Model.currentNext := O2Model.textRepository.corpus.nextUrn(urn)
	}


	@dom
	def getPassage(newUrn: CtsUrn):Unit = {
		val tempCorpus: Corpus = O2Model.textRepository.corpus ~~ newUrn
		//O2Model.passage.get.clear
		O2Model.xmlPassage.innerHTML = ""

		var wholePassageElement:String = ""

		// set up columns
		for ( cn <- tempCorpus.nodes ) {
			//val xml = new org.scalajs.dom.raw.DOMParser().parseFromString( cn.text, "text/xml" )
			val citString:String = s"""<span class="o2_passageUrn">${cn.urn.passageComponent}</span>"""
			val txtString:String = cn.text
			val elString:String = """<div class="p">""" + citString + txtString + "</div>"
			wholePassageElement += elString
			//O2Model.passage.get += cn
		}
		O2Model.xmlPassage.innerHTML = wholePassageElement
		js.Dynamic.global.document.getElementById("o2_xmlPassageContainer").appendChild(xmlPassage)

	}




	@dom
	def updateCitedWorks = {
		O2Model.citedWorks.get.clear
		for ( cw <- O2Model.textRepository.corpus.citedWorks){
			O2Model.citedWorks.get += cw
		}
	}


}
