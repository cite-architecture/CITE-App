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
import edu.holycross.shot.citeobj._
import scala.concurrent._
//import ExecutionContext.Implicits.global
import monix.execution.Scheduler.Implicits.global
import monix.eval._


import scala.scalajs.js.annotation.JSExport

@JSExport
object O2Controller {


	val validUrnInField = Var(false)


	/* A lot of work gets done here */
	def changePassage: Unit = {
		val timeStart = new js.Date().getTime()
		val newUrn: CtsUrn = O2Model.urn.get
		val task1 = Task{
				O2Model.versionsForCurrentUrn := O2Model.versionsForUrn(newUrn)
				O2Model.displayPassage(newUrn)
				val timeEnd = new js.Date().getTime()
				O2Controller.updateUserMessage(s"Fetched ${O2Model.currentCitableNodes.get} citation objects in ${(timeEnd - timeStart)/1000} seconds.",0)
		}
		val fuure1 = task1.runAsync
		/*
		js.timers.setTimeout(200){
			Future{
				O2Model.versionsForCurrentUrn := O2Model.versionsForUrn(newUrn)
				O2Model.displayPassage(newUrn)
				val timeEnd = new js.Date().getTime()
				O2Controller.updateUserMessage(s"Fetched ${O2Model.currentCitableNodes.get} citation objects in ${(timeEnd - timeStart)/1000} seconds.",0)
			}
		}
		*/
		val task2 = Task{ O2Model.getPrevNextUrn(O2Model.urn.get) }
		val future2 = task2.runAsync
	}


	def updateUserMessage(msg: String, alert: Int): Unit = {
		O2Model.userMessageVisibility := "app_visible"
		O2Model.userMessage := msg
		alert match {
			case 0 => O2Model.userAlert := "default"
			case 1 => O2Model.userAlert := "wait"
			case 2 => O2Model.userAlert := "warn"
		}
		js.timers.clearTimeout(O2Model.msgTimer)
		O2Model.msgTimer = js.timers.setTimeout(6000){ O2Model.userMessageVisibility := "app_hidden" }
	}


	def validateUrn(urnString: String): Unit = {
		try{
			val newUrn: CtsUrn = CtsUrn(urnString)
			validUrnInField := true
		} catch {
			case e: Exception => {
				validUrnInField := false
			}
		}
	}

	def getNext:Unit = {
		if (O2Model.currentNext.get != None){
			changeUrn(O2Model.currentNext.get.get)
		}
	}

	def getPrev:Unit = {
		if (O2Model.currentPrev.get != None){
			changeUrn(O2Model.currentPrev.get.get)
		}
	}

	def changeUrn(urnString: String): Unit = {
		changeUrn(CtsUrn(urnString))
	}


	def changeUrn(urn: CtsUrn): Unit = {
		try {
			O2Model.urn := urn
			O2Model.displayUrn := urn
			validUrnInField := true
			O2Controller.updateUserMessage("Retrieving passage…",1)
			val task = Task{	O2Controller.changePassage }
			val future = task.runAsync
			/*
			js.timers.setTimeout(200){
				Future{
					O2Controller.changePassage
				}
			}
			*/

		} catch {
			case e: Exception => {
				validUrnInField := false
				updateUserMessage("Invalid URN. Current URN not changed.",2)
			}
		}
	}


	def insertFirstNodeUrn(urn: CtsUrn): Unit = {
		val firstUrn = O2Model.textRepository.corpus.firstNode(urn).urn
		js.Dynamic.global.document.getElementById("o2_urnInput").value = firstUrn.toString
		validUrnInField := true
	}


	@dom
	def preloadUrn = {
		O2Model.urn := O2Model.textRepository.corpus.firstNode(O2Model.textRepository.corpus.citedWorks(0)).urn
		O2Controller.validUrnInField := true
	}


}
