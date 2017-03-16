package citeappscalajs
import com.thoughtworks.binding._
import scala.scalajs.js
import org.scalajs.dom.document
import org.scalajs.dom.raw.Event
import org.scalajs.dom.ext.Ajax
import scala.concurrent
.ExecutionContext
.Implicits
.global
import edu.holycross.shot.cite._
import edu.holycross.shot.ohco2._
import edu.holycross.shot.orca._
import edu.holycross.shot.citeenv._
import scala.scalajs.js.annotation.JSExport

@JSExport
object CiteMainController {

	@JSExport
	def main(libUrl: String): Unit = {

		def reportCEXLoad: Unit = {
			val cexSize = CiteMainModel.cexString.get.split("\n").size
			val msgString = s"Default, remote library file loaded: ${cexSize} lines."
			updateUserMessage(msgString,0)
		}

		def passOhco2Data: Unit = {
			O2Model.corpus = Corpus(CiteMainModel.cexString.get,"\t")
			O2Controller.updateUserMessage(s"Got data from main controller: ${O2Model.corpus.citedWorks.size} cited works.",0)
		}

		val remoteCall = Ajax.get(libUrl).onSuccess { case xhr =>
			CiteMainModel.cexString := xhr.responseText
			reportCEXLoad
			passOhco2Data
		}
		dom.render(document.body, CiteMainView.mainDiv)
	}

	def updateUserMessage(msg: String, alert: Int): Unit = {
		CiteMainModel.userMessageVisibility := "app_visible"
		CiteMainModel.userMessage := msg
		alert match {
			case 0 => CiteMainModel.userAlert := "default"
			case 1 => CiteMainModel.userAlert := "notice"
			case 2 => CiteMainModel.userAlert := "warn"
		}
		js.timers.setTimeout(9000){ CiteMainModel.userMessageVisibility := "app_hidden" }
	}

	def loadLocalLibrary(e: Event):Unit = {
		println(s"will load ${e}")
		val reader = new org.scalajs.dom.raw.FileReader()
		reader.readAsText(e.target.asInstanceOf[org.scalajs.dom.raw.HTMLInputElement].files(0))
		reader.onload = (e: Event) => {
//			val contents = reader.result.asInstanceOf[String]
val contents:String = """#!ctsdata
urn:cts:greekLit:tlg5026.msA.hmt:1.4.lemma#<div xmlns="http://www.tei-c.org/ns/1.0" n="lemma"> <p> θεά</p></div>
urn:cts:greekLit:tlg5026.msA.hmt:1.4.comment#<div xmlns="http://www.tei-c.org/ns/1.0" n="comment"> <p> οὕτως εἴωθε τὴν <persName n="urn:cite:hmt:pers.pers6"> Μοῦσαν</persName> καλεῖν· ἀμέλει καὶ ἐν <title> Ὀδυσσεία</title> · <cit> <q> ἄνδρα μοι ἔννεπε <persName n="urn:cite:hmt:pers.pers6"> Μοῦσα</persName> <ref type="urn">
#!ctscatalog
urn#citationScheme#groupName#workTitle#versionLabel#exemplarLabel#online
urn:cts:greekLit:tlg5026.msA.hmt:#book/scholion/part#Scholia to the Iliad#Main scholia of the Venetus A#HMT project edition##true
"""

			/* CRASHER BELOW!! */

			val cer = CiteExchangeReader(contents,"#")
			val tr = cer.textRepository
			println(s"Texts: ${tr.catalog.size}")
			println(s"Texts: ${tr.corpus.size}")

			CiteMainController.updateUserMessage(s"Loaded file of size ${contents.size}",0)
		}
	}

}
