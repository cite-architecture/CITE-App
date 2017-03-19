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
import edu.holycross.shot.citeenv._

import scala.scalajs.js.annotation.JSExport

@JSExport
object O2Model {

	val passage = Vars.empty[CitableNode]
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
		O2Model.passage.get.clear
		for ( cn <- tempCorpus.nodes ) {
			O2Model.passage.get += cn
		}
	}




	@dom
	def updateCitedWorks = {
		O2Model.citedWorks.get.clear
		for ( cw <- O2Model.textRepository.corpus.citedWorks){
			O2Model.citedWorks.get += cw
		}
	}


}
