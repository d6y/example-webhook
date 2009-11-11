package com.taykt.api.examples.webhook

import java.io.IOException;
import java.util.Collections;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import scala.collection.mutable.{Map, SynchronizedMap, HashMap}

trait PollTrait  extends HttpServlet{


	// Put this in an object so it is static. Non Persistent Storage
	protected val polls = Map[String, Map[String, Int]]()
	//		protected def polls = new HashMap[String, String] with SynchronizedMap[String, String]

	override def  doGet(request:HttpServletRequest , response:HttpServletResponse ) = {

		val  pollName = request.getParameter("name");
		response.setContentType("text/html");

		if (null == pollName) {
			response.getWriter().println("<html><head><title>Poll results</title><body><p>You need to provide a poll name.</p></body></html>")
		} else {
			polls.get(pollName) match {
			  case Some(poll) =>
			    val buffer = new StringBuffer()
			    poll.foreach { entry => buffer.append("<tr><td>" +  entry._1+ "</td><td>" + entry._2 + "</td><tr>") }
				response.getWriter().println(
						"<html><head><title>Poll results for " + pollName
								+ "</title><body><table>" + buffer.toString()
								+ "</table><img src=\"" + chartUrl(poll)
								+ "\"></body></html>")
			  case None =>  
				response.getWriter().println(
								"<html><head><title>Poll results for " + pollName
								+ "</title><body><p>No votes have been collected for the "
								+ pollName + " poll yet.</p></body></html>")
			}
		}
	}

	override def doPost(request:HttpServletRequest , response:HttpServletResponse ) {
		response.setContentType("text/plain");
		response.setCharacterEncoding("utf-8");

		val pollName = request.getParameter("name");

		if (null == pollName) {
			response.getWriter().println(
					"No poll name provided, contact Poll organiser");
		} else {
			val pollOption:String = pollOptionFromString(request.getParameter("text"))
			polls.get(pollName) match {
			  case Some(poll) =>
				if (poll.contains(pollOption)) {
					val count = poll(pollOption)
					poll += (pollOption -> (count + 1))
				} else 
					poll += (pollOption -> 1);
			  case None => polls += (pollName -> Map(pollOption -> 1))
			}
			response.getWriter().println(sMSResponse(pollName, pollOption))
		}
	}

	 def  pollOptionFromString(raw:String ):String

	 def chartUrl(poll:Map[String, Int] ) = {
		val data = new StringBuffer();
		val label = new StringBuffer();
		var first = true;
        
		 poll.foreach { entry => 
			if (!first) {
				label.append("|");
				data.append(",");
			} else {
				first = false;
			}
			data.append(entry._2);
			label.append(entry._1);
		}
		"http://chart.apis.google.com/chart?cht=p3&chs=500x250&chd=t:" + data.toString() + "&chl=" + label.toString()
	}

	def percentage(poll:Map[String, Int] , option:String) = { (poll.get(option).get / total(poll).toDouble * 100).toString }

	def total(poll:Map[String, Int] ):Int =  { (0 /: poll) ( _ + _._2 ) }

	val RESPONSE_TEXT = "You've voted for {OPTION}. It has {PERCENT}% of the vote. Currently {LEADER} is in the lead."

  	def leader(poll:Map[String, Int] ) =  poll.toList.sort( (_._2 >_._2) )(0)._1  

 
	def sMSResponse(pollName:String , option:String ) = RESPONSE_TEXT.replace("{OPTION}", option).replace("{PERCENT}", percentage(polls(pollName), option)).replace("{LEADER}", leader(polls(pollName)))

}
