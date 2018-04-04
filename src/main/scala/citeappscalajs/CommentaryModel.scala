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

@JSExportTopLevel("citeapp.CommentaryModel")
object CommentaryModel {

	val commentaryVerb:Cite2Urn = Cite2Urn("urn:cite2:cite:verbs.v1:commentsOn")
	val commentaryModel:Cite2Urn = Cite2Urn("urn:cite2:cite:datamodels.v1:commentary")
	// commentsOn not used yet…
	val commentsOn = Var[Boolean](true)

	case class CiteComment(comment:Urn, text:Urn)

	val commentList = Vars.empty[CiteComment]
	val currentCommentsAll = Vars.empty[CiteComment]
	val currentCommentsDistinctComments = Vars.empty[CiteComment]

	def ctsHasCommentary(urn:CtsUrn):Vars[Urn] = {
		DataModelController.hasCommentaryModel match {
			case false => Vars.empty[Urn]	
			case _ => {
				val relevantComments:Vector[CiteComment] = currentCommentsAll.value.filter(_.text.asInstanceOf[CtsUrn] == urn).toVector
				val v = Vars.empty[Urn]	
				for (c <- relevantComments) {
					v.value += c.comment
				}
				v
			}
		}
	}


	def clearComments:Unit = {
		currentCommentsAll.value.clear
		currentCommentsDistinctComments.value.clear
	}

	def updateCurrentListOfComments(corp:Corpus):Unit = {
		clearComments	
		//val corpUrns:Vector[CtsUrn] = corp.urns
		commentList.value.size match {
			case s if (s > 0) => {
				val twiddledComments:Vector[CiteComment] = {
					commentList.value.filter( c => {
						c.text match {
							case CtsUrn(_) => {
								((corp >= c.text.asInstanceOf[CtsUrn]).size > 0)	
							}
							case _ => false
						}
					}).toVector
				}
				// If any of the twiddledComments were ranges, let's expand those…
				val rangeComments:Vector[CiteComment] = {
					twiddledComments.filter( c => {
						c.text.asInstanceOf[CtsUrn].isRange
					}).toVector
				}
				val expandedRangeComments:Vector[CiteComment] = {
					(
					for (rc <- rangeComments) yield {
						val urns:Vector[CtsUrn] = (corp ~~ rc.text.asInstanceOf[CtsUrn]).urns	
						urns.map( u => { CiteComment(rc.comment,u)})
					}
					).flatten
				}
				// And then we need to ditch the range comments from the original
				val nonRangeComments:Vector[CiteComment] = {
					twiddledComments.filter( c => {
						c.text.asInstanceOf[CtsUrn].isRange == false
					}).toVector
				}
				// And we concatenate those, and eliminate dups
				val finalCurrentComments:Vector[CiteComment] = {
						(nonRangeComments ++ expandedRangeComments).distinct.toVector
				}
				for (c <- finalCurrentComments){
					currentCommentsAll.value += c
				}

				// And let's get a version that just has unique comments, for the sidebar
				val uniquedComments:Vector[CiteComment] = finalCurrentComments.groupBy(_.comment).map(_._2.head).toVector
				// And why not group by work/collection, while we're at it… the list isn't going to be long
				val map1:Vector[Tuple2[String,CiteComment]] = uniquedComments.map(c => {
					val mapString:String = {
						c.comment match {
							case CtsUrn(_) => c.comment.asInstanceOf[CtsUrn].dropPassage.toString
							case _ => c.comment.asInstanceOf[Cite2Urn].dropSelector.toString
						}
					}
					Tuple2(mapString,c)
				})
				val map2:Vector[(String,Vector[Tuple2[String,CiteComment]])] = map1.groupBy(_._1).toVector
				val map3:Vector[Tuple2[String,CiteComment]] = map2.map(_._2).flatten
				val map4:Vector[CiteComment] = map3.map(_._2)

				for (c <- map4){
					currentCommentsDistinctComments.value += c
				}

			}
			case _ => //do nothing
		}
	}

	def loadAllComments:Unit = {
		commentList.value.clear
		if (DataModelController.hasCommentaryModel) {	
			val tempComments:Option[CiteRelationSet] = {
				RelationsModel.citeRelations.value match {
					case Some(cr) => {
						val cv:CiteRelationSet = cr.verb(commentaryVerb)
						cv.size match {
							case s if (s > 0) => Some(cv)
							case _ => None
						}
					}
					case None => None
				}
			}
			if (tempComments != None) {
				for (c <- tempComments.get.relations ){
					commentList.value += CiteComment(comment = c.urn1, text = c.urn2)
				}
			}
		}
	}


}
