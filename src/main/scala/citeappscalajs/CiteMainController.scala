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
import edu.holycross.shot.scm._
import edu.holycross.shot.ohco2._
import edu.holycross.shot.citeobj._

import monix.execution.Scheduler.Implicits.global
import monix.eval._

import scala.scalajs.js.annotation.JSExport

@JSExportTopLevel("citeapp.CiteMainController")
object CiteMainController {

	@JSExport
	def main(libUrl: String): Unit = {

//		ImageModel.imgArchivePath = localImagePath

		CiteMainController.updateUserMessage("Loading default library. Please be patient…",1)
		val task = Task{ CiteMainController.loadRemoteLibrary(libUrl) }
		val future = task.runAsync

		dom.render(document.body, CiteMainView.mainDiv)
	}

	def loadRemoteLibrary(url: String):Unit = {

		val xhr = new XMLHttpRequest()
		xhr.open("GET", url )
		xhr.onload = { (e: Event) =>
			if (xhr.status == 200) {
				val contents:String = xhr.responseText
				CiteMainController.updateRepository(contents)
			} else {
				CiteMainController.updateUserMessage(s"Request for remote library failed with code ${xhr.status}",2)
			}
		}
		xhr.send()

		/* Ajax.get(url).onSuccess { case xhr =>
		CiteMainController.updateUserMessage("Got remote library.",0)
		val contents:String = xhr.responseText
		CiteMainController.updateRepository(contents, libDelim, fieldDelim)
	}
	*/
}

	def updateUserMessage(msg: String, alert: Int): Unit = {
		CiteMainModel.userMessageVisibility.value = "app_visible"
		CiteMainModel.userMessage.value = msg
		alert match {
			case 0 => CiteMainModel.userAlert.value = "default"
			case 1 => CiteMainModel.userAlert.value = "wait"
			case 2 => CiteMainModel.userAlert.value = "warn"
		}
		js.timers.clearTimeout(CiteMainModel.msgTimer)
		CiteMainModel.msgTimer = js.timers.setTimeout(10000){ CiteMainModel.userMessageVisibility.value = "app_hidden" }
	}


	def loadLocalLibrary(e: Event):Unit = {
		val reader = new org.scalajs.dom.raw.FileReader()
		CiteMainController.updateUserMessage("Loading local library.",0)
		reader.readAsText(e.target.asInstanceOf[org.scalajs.dom.raw.HTMLInputElement].files(0))
		reader.onload = (e: Event) => {
			val contents = reader.result.asInstanceOf[String]
			CiteMainController.updateRepository(contents)
		}
	}

	def retrieveTextPassage(urn:CtsUrn):Unit = {
			O2Controller.changeUrn(urn)
			js.Dynamic.global.document.getElementById("tab-1").checked = true
	}

	def hideTabs:Unit = {

	  CiteMainModel.showTexts.value = true
	  CiteMainModel.showNg.value = true
	}

	def checkDefaultTab:Unit = {
		if (CiteMainModel.showTexts.value) {
			js.Dynamic.global.document.getElementById("tab-1").checked = true
		} 
	}

	def clearRepositories:Unit = {
		O2Model.textRepo.value = None
	}


	// Reads CEX file, creates repositories for Texts, Objects, and Images
	// *** Apropos Microservice ***
	@dom
	def updateRepository(cexString: String) = {

		hideTabs
		clearRepositories

		try {

			val repo:CiteLibrary = CiteLibrary(cexString, CiteMainModel.cexMainDelimiter, CiteMainModel.cexSecondaryDelimiter)
			val mdString = s"Repository: ${repo.name}. Library URN: ${repo.urn}. License: ${repo.license}"
			var loadMessage:String = ""

			repo.textRepository match {
				case Some(tr) => {
					CiteMainModel.showTexts.value = true
					CiteMainModel.showNg.value = true
					CiteMainModel.currentLibraryMetadataString.value = mdString
					O2Model.textRepo.value = Some(tr)
					CiteMainController.updateUserMessage(s"Updated text repository: ${ O2Model.textRepo.value.get.catalog.size } works. ",0)
					loadMessage += s"Updated text repository: ${ O2Model.textRepo.value.get.catalog.size } works. "
					O2Model.updateCitedWorks
					NGModel.updateCitedWorks
					NGController.clearResults
					NGController.clearHistory
					O2Model.clearPassage

					O2Controller.preloadUrn
					NGController.preloadUrn
				}
				case None => {
					loadMessage += "No texts. "
				}
			}

			repo.collectionRepository match {
				case Some(cr) => {
					val numCollections:Int = cr.collections.size
					val numObjects:Int = cr.citableObjects.size
					loadMessage += s" ${numCollections} collections. ${numObjects} objects. "
					ObjectModel.collRep.value	= Some(cr)			
				}
				case None => {
					loadMessage += "No Collections. "	
				}
			}

			checkDefaultTab

			CiteMainController.updateUserMessage(loadMessage,0)

		} catch  {
			case e: Exception => {
				CiteMainController.updateUserMessage(s"""${e}. Invalid CEX file.""",2)
			}
		}

	}


}
