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


	/* 
		Initiate app with a URL to an online CEX file	
	*/
	@JSExport
	def main(libUrl: String, localImagePath:String): Unit = {

		CiteBinaryImageModel.imgArchivePath.value = localImagePath

		CiteMainController.updateUserMessage("Loading default library. Please be patient…",1)
		val task = Task{ CiteMainController.loadRemoteLibrary(libUrl) }
		val future = task.runAsync

		dom.render(document.body, CiteMainView.mainDiv)
	}

	/*
		Use AJAX request to get remote CEX file; update repository with CEX data
	*/
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
}
	/*
	 	Handles displaying messages to the user, color-coded according to type.
	 	Fades after 10 seconds.		
	*/
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

	/*
		Loads library from local CEX file; updates repository
	*/
	def loadLocalLibrary(e: Event):Unit = {
		val reader = new org.scalajs.dom.raw.FileReader()
		CiteMainController.updateUserMessage("Loading local library.",0)
		reader.readAsText(e.target.asInstanceOf[org.scalajs.dom.raw.HTMLInputElement].files(0))
		reader.onload = (e: Event) => {
			val contents = reader.result.asInstanceOf[String]
			CiteMainController.updateRepository(contents)
		}
	}

	/*
		Hide all tabs. Done initially. Tabs are shown based on the contexts
		of the CEX library.
	*/
	def hideTabs:Unit = {

	  CiteMainModel.showTexts.value = false 
	  CiteMainModel.showNg.value = false
	  CiteMainModel.showCollections.value = false
	  CiteMainModel.showImages.value = false
	}

	/*
		Which tabe should be shown by default upon library load.
	*/
	def checkDefaultTab:Unit = {
		if (CiteMainModel.showTexts.value) {
			js.Dynamic.global.document.getElementById("tab-3").checked = true
		} 
	}

	/*
			Clear all data.
	*/
	def clearRepositories:Unit = {
		O2Model.textRepo.value = None
		ObjectModel.collRep.value = None
		CiteMainModel.mainLibrary.value = None
	}


	// Reads CEX file, creates repositories for Texts, Objects, and Images
	// *** Apropos Microservice ***
	@dom
	def updateRepository(cexString: String) = {

		hideTabs
		clearRepositories

		try {
			// Set up repo 
			var timeStart = new js.Date().getTime()
			val repo:CiteLibrary = CiteLibrary(cexString, CiteMainModel.cexMainDelimiter, CiteMainModel.cexSecondaryDelimiter)
			var timeEnd = new js.Date().getTime()
			g.console.log(s"Created CiteLibrary in ${(timeEnd - timeStart)/1000} seconds.")
			val mdString = s"Repository: ${repo.name}. Library URN: ${repo.urn}. License: ${repo.license}"
			var loadMessage:String = ""

			CiteMainModel.mainLibrary.value = Some(repo)

			// Text Repository Stuff
			timeStart = new js.Date().getTime()
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
			timeEnd = new js.Date().getTime()
			g.console.log(s"Initialized TextRepository in ${(timeEnd - timeStart)/1000} seconds.")

			// Collection Repository Stuff
			timeStart = new js.Date().getTime()
			repo.collectionRepository match {
				case Some(cr) => {
					CiteMainModel.showCollections.value = true
					val numCollections:Int = cr.collections.size
					val numObjects:Int = cr.citableObjects.size
					loadMessage += s" ${numCollections} collections. ${numObjects} objects. "
					ObjectModel.collRep.value	= Some(cr)			
					ObjectModel.updateCollections
				}
				case None => {
					loadMessage += "No Collections. "	
				}
			}
			timeEnd = new js.Date().getTime()
			g.console.log(s"Initialized collectionRepository in ${(timeEnd - timeStart)/1000} seconds.")

			// Data Model Stuff
			timeStart = new js.Date().getTime()
			repo.dataModels match {
				case Some(dm) => {
					DataModelModel.dataModels.value = Some(dm)
					CiteBinaryImageController.discoverProtocols
					CiteBinaryImageController.setImageSwitch
					CiteBinaryImageModel.hasBinaryImages.value match {
						case true => CiteMainModel.showImages.value = true 
						case _ => CiteMainModel.showImages.value = false
					}
				}
				case None => { 
					DataModelController.clearDataModels
				}
			}
			//g.console.log(s"hasBinaryImages = ${CiteBinaryImageModel.hasBinaryImages.value}")
			timeEnd = new js.Date().getTime()
			g.console.log(s"Initialized DataModels in ${(timeEnd - timeStart)/1000} seconds.")

			checkDefaultTab

			CiteMainController.updateUserMessage(loadMessage,0)

		} catch  {
			case e: Exception => {
				CiteMainController.updateUserMessage(s"""${e}. Invalid CEX file.""",2)
			}
		}

	}


}
