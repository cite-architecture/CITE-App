package citeappscalajs

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

	val nGramResults = Var[StringHistogram](null)
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



}
