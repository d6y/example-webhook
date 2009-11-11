package com.taykt.api.examples.webhook

import java.io.IOException
import java.util.Collections

import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

import scala.collection.mutable.{Map, SynchronizedMap, HashMap}

trait PollTrait  extends HttpServlet{


	//Non Persistent Storage
	protected val polls = Map[String, Map[String, Int]]()

	override def  doGet(request:HttpServletRequest , response:HttpServletResponse ) = {

		val  pollName = request.getParameter("name")
		response.setContentType("text/html")

		if (null == pollName) {
			response.getWriter().println(NO_POLL)
		} else {
			polls.get(pollName) match {
			  case Some(poll) =>
			    val table    = poll.map { entry => "<tr><td>" +  entry._1+ "</td><td>" + entry._2 + "</td><tr>" }.mkString
			    response.getWriter().println(
			      RESULTS_HTML.replaceAll("${POLL_NAME}",pollName).replaceAll("${RESULTS_TABLE}", table).replaceAll("${CHART_URL}",chartUrl(poll)))
			  case None =>  
				response.getWriter().println(NO_RESULTS_HTML.replaceAll("${POLL_NAME}",pollName))
			}
		}
	}

	override def doPost(request:HttpServletRequest , response:HttpServletResponse ) {
		val pollName = request.getParameter("name")
  		response.setContentType("text/plain")
		response.setCharacterEncoding("utf-8")
		if (null == pollName) 
			response.getWriter().println("No poll name provided, contact Poll organiser")
	    else {
			val pollOption:String = pollOptionFromString(request.getParameter("text"))
			polls.get(pollName) match {
			  case Some(poll) =>
			    poll.get(pollOption) match {
			      case Some(value)  => poll += (pollOption -> (value + 1))
			      case None 		  => poll += (pollOption -> 1) 
			    }
			  case None => polls += (pollName -> Map(pollOption -> 1))
			}
			response.getWriter().println(sMSResponse(pollName, pollOption))
		}
	}

	def  pollOptionFromString(raw:String ):String

	def chartUrl(poll:Map[String, Int] ) = "http://chart.apis.google.com/chart?cht=p3&chs=500x250&chd=t:" + poll.values.toList.mkString(",") + "&chl=" + poll.keys.toList.mkString("|")

	def sMSResponse(pollName:String , option:String ) = RESPONSE_TEXT.replace("{OPTION}", option).replace("{PERCENT}", percentage(polls(pollName), option)).replace("{LEADER}", leader(polls(pollName)))
 
	def percentage(poll:Map[String, Int] , option:String) = { (poll.get(option).get / total(poll).toDouble * 100).toString }

	def total(poll:Map[String, Int] ):Int =  { (0 /: poll) ( _ + _._2 ) }

  	def leader(poll:Map[String, Int] ) =  poll.toList.sort( (_._2 >_._2) )(0)._1  

   	val RESPONSE_TEXT = "You've voted for {OPTION}. It has {PERCENT}% of the vote. Currently {LEADER} is in the lead."
    
    val NO_RESULTS_HTML = """<html>
   	<head>
   		<title>Poll results for ${POLL_NAME}</title>
   	</head>
   		<body>
   			<p>No votes have been collected for the ${POLL_NAME} poll yet.</p>
   		</body>
</html>"""
    
    val RESULTS_HTML = """<html>
	<head>
		<title>Poll results for ${POLL_NAME}</title>
	</head>	
	<body>
		<table>${RESULTS_TABLE}</table>
		<img src="${CHART_URL}">
	</body>
</html>"""

    val NO_POLL = """<html>
	<head>
		<title>Poll results</title>
	<head>
	<body>
			<p>You need to provide a poll name.</p>
    </body>
</html>"""
}
