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
object NGModel {

	val urn = Var(CtsUrn("urn:cts:ns:group.work.version.exemplar:passage"))
	val shortWorkLabel = Var("default")

	val citedWorks = Vars.empty[CtsUrn]

	val userMessage = Var("")
	val userAlert = Var("default")
	val userMessageVisibility = Var("")

	/* Values for NGrams */
	val nGramThreshold = Var(3)

	val nGramResults = Vars.empty[StringCount]
	val nGramQuery = Var("")

	val nGramUrns =  Vars.empty[CtsUrn]
	val nGramUrnQuery = Var("")

	/* Some methods for working the model */

	@dom
	def updateShortWorkLabel = {
		if ( O2Model.textRepository == null){
					NGModel.shortWorkLabel := "- no selected text -"
		} else {
			val longS:String = O2Model.textRepository.catalog.label(NGModel.urn.get)
			if (longS.size > 50){
				val shortS:String = longS.take(24) + " … " + longS.takeRight(23)
				NGModel.shortWorkLabel := shortS
			} else {
				NGModel.shortWorkLabel := longS
			}
		}
	}

	@dom
	def updateCitedWorks = {
		NGModel.citedWorks.get.clear
		// N.b. The textRepository remains with the Ohco2 Model.
		for ( cw <- O2Model.textRepository.corpus.citedWorks){
			NGModel.citedWorks.get += cw
		}
	}

 def getNGram(filterString: String, n: Int, occ: Int, ignorePunc: Boolean ): StringHistogram = {
		 getNGram(O2Model.textRepository.corpus, filterString, n, occ, ignorePunc)
 }

 def getNGram(ngUrn: CtsUrn, filterString: String, n: Int, occ: Int, ignorePunc: Boolean): StringHistogram = {
	 val newCorpus: Corpus = O2Model.textRepository.corpus ~~ ngUrn
	 getNGram(newCorpus, filterString, n, occ, ignorePunc)
 }

  def getNGram(ngCorpus:Corpus, filterString: String, n: Int, occ: Int, ignorePunc: Boolean ): StringHistogram = {

		var hist: StringHistogram = null

		if( filterString == ""){
			hist = ngCorpus.ngramHisto(n, occ, ignorePunc)
		} else {
			hist = ngCorpus.ngramHisto(filterString, n, occ , ignorePunc)
		}
		hist
	}

 def getUrnsForNGram(s: String, ignorePunc: Boolean ): Vector[CtsUrn] ={
	 val vurn = O2Model.textRepository.corpus.urnsForNGram(s, 1, ignorePunc)
	 vurn
 }

 def getUrnsForNGram(ngUrn: CtsUrn, s: String, ignorePunc: Boolean ): Vector[CtsUrn] ={
	 val newCorpus: Corpus = O2Model.textRepository.corpus ~~ ngUrn
	 val vurn = newCorpus.urnsForNGram(s, 1, ignorePunc)
	 vurn
 }

 def getUrnsForNGram(ngCorpus: Corpus, s: String, ignorePunc: Boolean ): Vector[CtsUrn] = {
	 	val vurn = ngCorpus.urnsForNGram(s, 1, ignorePunc)
		vurn
 }

}
