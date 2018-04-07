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
import edu.holycross.shot.ohco2._
import edu.holycross.shot.citeobj._
import edu.holycross.shot.citerelation._
import edu.holycross.shot.scm._

import monix.execution.Scheduler.Implicits.global
import monix.eval._

import scala.scalajs.js.annotation.JSExport

@JSExportTopLevel("citeapp.RelationsView")
object RelationsView {

	@dom
 	def relationsMessageDiv = {
		<div id="relations_message"
			class= { s"app_message ${RelationsModel.userMessageVisibility.bind} ${RelationsModel.userAlert.bind}"}>
		<p>
			{
			RelationsModel.userMessage.bind
			}
		</p>
		</div>
	}
	@dom
	def relationsDiv = {

		val urnValidatingKeyUpHandler = { event: KeyboardEvent =>
			(event.currentTarget, event.keyCode) match {
				case (input: html.Input, KeyCode.Enter) => {
					event.preventDefault()
					RelationsController.changeUrn(s"${input.value.toString}")
					//input.value = ""
				}
			case(input: html.Input, _) =>  RelationsController.validateUrn(s"${input.value.toString}")
			case _ =>
			}
		}

		<div id="relations_Container">

			<div id="relations_sidebar" class="app_sidebarDiv">
				<h2>Find URNs in Relations</h2>
				<p id="relations_urnInputP">
					{ RelationsView.verbFilterSelect.bind }
					<br/>
					<input
						class={ s"${RelationsController.validUrnInField.bind}" }
						id="relations_urnInput"
						size={ 50 }
						type="text"
						value={ RelationsModel.inputBoxUrnStr.bind }
						onkeyup={ urnValidatingKeyUpHandler }>
					</input>
					{ RelationsView.findRelationsButton.bind }
					<br/>
				</p>
			</div>

			{ relationsMessageDiv.bind }

			<p id="relations_reportingCurrentSearch" class="app_reportingCurrentUrn"> 
				{ 
					RelationsModel.urn.bind match {
						case Some(u) => u.toString
						case None => "[ no URN requested ]"
					} 
				} 

				{  RelationsModel.filterVerb.bind match {
						case Some(u) => {
							{ s" filtered by ${u}" }
						}
						case None => ""
					}	
				}
			</p>

			{ relationsSpace.bind }

			</div>
		}

		@dom 
		def relationsSpace = {
				<div id="relations_relationsContainer">
					<ul id="relations_relationsList">
						{ relationsListItems.bind }	
					</ul>
				</div>
		}

		@dom
		def relationsListItems = {
			for ( r <- RelationsModel.foundRelations ) yield {
				<li class="relations_listItem">
					<ul class="relation_listItemList">
						{ relationsUrnDisplay(r.urn1).bind }					
						{ <li> { RelationsModel.getVerbLabel(r.relation) }	</li> }
						{ relationsUrnDisplay(r.urn2).bind }					
					</ul>
				</li>
			}
		}

		@dom 
		def relationsUrnDisplay(u:Urn) = {
			u match {
				case CtsUrn(_) => {
					DataModelView.textLinkItem(None,u.asInstanceOf[CtsUrn]).bind
				}
				case Cite2Urn(_) => {
					DataModelView.objectLinkItem(None,u.asInstanceOf[Cite2Urn],true).bind
				}
				case _ => <!-- empty content -->
			}
		}

		@dom
		def findRelationsButton = {
			<button id="relations_findRelationsButton"
				onclick={ event: Event => {
					val s:String = js.Dynamic.global.document.getElementById("relations_urnInput").value.toString
					RelationsModel.urn.value = {
						s.take(8) match {
							case "urn:cts:" => Some(CtsUrn(s))
							case _ => {
								s.take(10) match {
									case "urn:cite2:" => Some(Cite2Urn(s))
									case _ => None
								}
							}
						}		
					}
					RelationsModel.inputBoxUrnStr.value = {
						RelationsModel.urn.value match {
							case Some(u) => u.toString
							case _ => ""
						}
					}
					RelationsController.updateUserMessage("Retrieving passage…",1)
					val task = Task{ RelationsController.findRelations }
					val future = task.runAsync
					//				js.timers.setTimeout(200){
						//Future{ O2Controller.changePassage }
						//				}
					}
					}
					disabled={ (RelationsController.validUrnInField.bind == false) }
					> {
						if ( RelationsController.validUrnInField.bind == true ){
							"Find Relations"
						} else {
							"Invalid URN"
						}

					}
					</button>
				}

	@dom
	def verbFilterSelect = {
		<label for="relations_verbFilterSelect">Filter by Relation</label>
		<select id="nrelations_verbFilterSelect"
			onchange={ event: Event => {
				val thisTarget = event.target.asInstanceOf[org.scalajs.dom.raw.HTMLSelectElement]
		 		val filterVerbString:String = thisTarget.value.toString
				RelationsController.setFilterVerb(filterVerbString)}
			}>
			 <option value="None">All Relations</option> 
			{
				for ( v <- RelationsModel.allVerbs ) yield { 
					<option value={ v._1.toString }>{ v._2 }</option>	
				}

			}
		</select>
	}

}
