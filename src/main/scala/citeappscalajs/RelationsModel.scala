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

@JSExportTopLevel("citeapp.RelationsModel")
object RelationsModel {
	val citeRelations = Var[Option[CiteRelationSet]](None)
	val foundRelations = Vars.empty[CiteTriple]

	val allVerbs = Vars.empty[(Cite2Urn,String)]

	val urn = Var[Option[Urn]](None)
	val inputBoxUrnStr = Var("")
	val filterVerb = Var[Option[Cite2Urn]](None)
	val userMessageVisibility = Var("app_hidden")
	var msgTimer:scala.scalajs.js.timers.SetTimeoutHandle = null
	val userMessage = Var("")
	val userAlert = Var("default")

	case class HistoryItem(search:Urn, filter:Option[Cite2Urn]) {
		override def toString:String = {
			filter match {
				case Some(f) => s"${search} filtered by “${f.objectComponent}”."
				case None => s"${search}."
			}
		}
	}
	val searchHistory = Vars.empty[HistoryItem]

	def clearRelations:Unit = {
		g.console.log("clearing relations")
		urn.value = None
		filterVerb.value = None
		inputBoxUrnStr.value = ""
		foundRelations.value.clear
	}

	def updateHistory(hi:HistoryItem):Unit = {
		val oldHistory:Vector[HistoryItem] = {
			searchHistory.value.map(s => {
				s
			}).toVector

		}
		val newHistory:Vector[HistoryItem] = Vector(hi) ++ oldHistory
		searchHistory.value.clear
		for (i <- newHistory){
			searchHistory.value += i
		}
	}

	def getVerbLabel(u:Cite2Urn):String = {
		val label:String = ObjectModel.collRep.value match {
			case None => {
				val label:String = {
					u.objectOption match {
						case Some(o) => o
						case None => "< nothing specified >"
					}
				}	
				label
			}
			case Some(cr) => {
				val label:String = {
					(cr ~~ u) match {
						case objVec if (objVec.size > 0) => {
							objVec(0).label	
						}
						case _ => { 
							u.objectOption match {
								case Some(o) => o
								case None => "< nothing specified >"
							}
						}
					}
				}	
				label
			}
		}
		label
	}


	def loadAllVerbs:Unit = {
		citeRelations.value match {
			case None => allVerbs.value.clear
			case Some(crs) => {
				val verbs:Vector[Cite2Urn] = {
					crs.relations.map( _.relation ).toVector.distinct
				}	
				val verbPairs:Vector[(Cite2Urn, String)] = {
					ObjectModel.collRep.value match {
						case None => {
							verbs.map( v => {
								val label:String = {
									v.objectOption match {
										case Some(o) => o
										case None => "< nothing specified >"
									}
								}	
								(v, label)
							}).toVector	
						}
						case Some(cr) => {
							verbs.map( v => {
								val label:String = {
									(cr ~~ v) match {
										case objVec if (objVec.size > 0) => {
											objVec(0).label	
										}
										case _ => { 
											v.objectOption match {
												case Some(o) => o
												case None => "< nothing specified >"
											}
										}
									}
								}	
								(v, label)
							}).toVector
						}
					}	
				}
				allVerbs.value.clear
				for (vp <- verbPairs) { 
					allVerbs.value += vp
				}
			}	
		}		
	}
}