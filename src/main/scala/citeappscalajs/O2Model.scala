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
object O2Model {

	val passage = Var("passage")
	val urn = Var(CtsUrn("urn:cts:ns:group.work.version.exemplar:passage"))
	//val urnPreview = Var("")

	val userMessage = Var("")
	val userAlert = Var("default")
	val userMessageVisibility = Var("")


	var textRepository: TextRepository = null
	val citedWorks = Vars.empty[CtsUrn]

	//var currentLibrary = Var(Corpus)

	val currentUrn =  Var(CtsUrn)
	val currentNext =  Var(Option)
	val currentPrev =  Var(Option)

	/* Values for NGrams */
	val nGramThreshold = Var(3)


	@dom
	def updateRepository(cexString: String, columnDelimiter: String = "\t") = {

		try {
		  val raw = cexString.split("#!").toVector.filter(_.nonEmpty)
		  val sections = raw.map(_.split("\n")).map(v => (v.head,v.drop(1).toVector))
	    val ctsCatalogLines = sections.filter(_._1 == "ctscatalog").flatMap(_._2)
	    val catalog = Catalog(ctsCatalogLines.mkString("\n"),columnDelimiter)
			O2Controller.updateUserMessage(s"Created catalog",0)
	    val ctsDataLines = sections.filter(_._1 == "ctsdata").flatMap(_._2)
	    val corpus = Corpus(ctsDataLines.mkString("\n"),columnDelimiter)
			O2Controller.updateUserMessage(s"Created new corpus",0)

			O2Model.textRepository = TextRepository(corpus, catalog)

			O2Controller.updateUserMessage(s"Updated text repository: ${ O2Model.textRepository.catalog.size } works.",0)

			updateCitedWorks

			preloadUrn

		} catch  {
				case e: Exception => {
						O2Controller.updateUserMessage(s"${e}",2)
				}
		}

	}

	@dom
	def updateCitedWorks = {
		citedWorks.get.clear
		for ( cw <- O2Model.textRepository.corpus.citedWorks){
			println(cw.toString)
			O2Model.citedWorks.get += cw
		}
	}

	@dom
	def preloadUrn = {
		O2Model.urn := O2Model.textRepository.corpus.firstNode.urn
	}


}
