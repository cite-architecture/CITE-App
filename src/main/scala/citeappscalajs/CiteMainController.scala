package citeapp
import com.thoughtworks.binding._
import scala.scalajs.js
import org.scalajs.dom.document
import scala.scalajs.js.Dynamic.{ global => g }
import org.scalajs.dom.raw._
import org.scalajs.dom.ext.Ajax
import scala.concurrent._
//import ExecutionContext.Implicits.global

import edu.holycross.shot.cite._
import edu.holycross.shot.scm._
import edu.holycross.shot.ohco2._
import edu.holycross.shot.citeobj._
import scala.scalajs.js.annotation.JSExport

import monix.execution.Scheduler.Implicits.global
import monix.eval._

@JSExport
object CiteMainController {

	@JSExport
	def main(libUrl: String, localImagePath:String): Unit = {

		ImageModel.imgArchivePath = localImagePath

		CiteMainController.updateUserMessage("Loading default library. Please be patient…",1)
		val task = Task{ CiteMainController.loadRemoteLibrary(libUrl) }
		val future = task.runAsync
		/*
		js.timers.setTimeout(200){
			Future{ 
					CiteMainController.loadRemoteLibrary(libUrl)			
			}
		}
		*/


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
		CiteMainModel.userMessageVisibility := "app_visible"
		CiteMainModel.userMessage := msg
		alert match {
			case 0 => CiteMainModel.userAlert := "default"
			case 1 => CiteMainModel.userAlert := "wait"
			case 2 => CiteMainModel.userAlert := "warn"
		}
		js.timers.clearTimeout(CiteMainModel.msgTimer)
		CiteMainModel.msgTimer = js.timers.setTimeout(10000){ CiteMainModel.userMessageVisibility := "app_hidden" }
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

	def retrieveImageLinks(urn:Cite2Urn) = {
		g.console.log(s"Will find image links to ${urn}.")
	}

	def retrieveObject(mappedUrn:Option[Cite2Urn], objUrn:Cite2Urn):Unit = {
			val urn:Cite2Urn = objUrn.dropExtensions
			ObjectController.updateUserMessage("Retrieving object…",1)
			js.Dynamic.global.document.getElementById("tab-3").checked = true
			val task = Task{ObjectController.changeUrn(urn) }
			val future = task.runAsync
			/*
			js.timers.setTimeout(200){
				Future{
					ObjectController.changeUrn(urn)
				}
			}
			*/
	}

	def retrieveImage(mappedUrn:Option[Cite2Urn], propVal:Cite2Urn):Unit = {
			val oe = propVal.objectExtensionOption
			ImageController.changeUrn(propVal,Vector((oe,mappedUrn)))
			js.Dynamic.global.document.getElementById("tab-4").checked = true
	}

	def hideTabs:Unit = {

	  CiteMainModel.showTexts := false
		CiteMainModel.showNg := false
		CiteMainModel.showCollections := false
		CiteMainModel.showImages :=false
	}

	def checkDefaultTab:Unit = {
		if (CiteMainModel.showTexts.get) {
			js.Dynamic.global.document.getElementById("tab-1").checked = true
		} else {
			if (CiteMainModel.showCollections.get) {
			js.Dynamic.global.document.getElementById("tab-3").checked = true
			}
		}
	}

	def clearRepositories:Unit = {
		O2Model.textRepository = null
		ObjectModel.collectionRepository = null
		ImageModel.imageCollections.get.clear
		ImageModel.imageExtensions = null
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
					CiteMainModel.showTexts := true
					CiteMainModel.showNg := true
					CiteMainModel.currentLibraryMetadataString := mdString
					O2Model.textRepository = tr
					CiteMainController.updateUserMessage(s"Updated text repository: ${ O2Model.textRepository.catalog.size } works. ",0)
					loadMessage += s"Updated text repository: ${ O2Model.textRepository.catalog.size } works. "
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
					CiteMainModel.showCollections := true
					ObjectModel.collectionRepository = cr
					ObjectModel.updateCollections
					ObjectController.clearResults
					ObjectController.clearHistory
					ObjectModel.clearObject
					ObjectController.preloadUrn
					QueryObjectModel.clearAll
					QueryObjectModel.currentQueryCollection := None
					loadMessage += s"Updated collection repository: ${ cr.collections.size  } collections."

				}

				case None => {
					loadMessage += "No collections. "
				}
			}

			repo.imageExtensions match {
				case Some(ie) => {
					  CiteMainModel.showImages := true
						ImageController.clearAll
						ImageModel.imageExtensions = Some(ie)
						ImageModel.updateImageCollections
						loadMessage += s"Image collections: ${ie.protocolMap.size}."
				}

				case None => {
					ImageController.clearAll
					loadMessage += "No image collections. "
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
