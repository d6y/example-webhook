package com.taykt.api.examples.webhook

import java.io.IOException
import java.util.Collections

import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

import scala.collection.mutable.{Map, SynchronizedMap, HashMap}

trait PollTrait  extends HttpServlet{


	/*Non persistent storage, Would be a database if this wasn't an example.  */
	protected val polls = Map[String, Map[String, Int]]()

	/**
	  * Records votes and generates the response SMS message to send to the customer. 
	  * Two parameters are expected :
	  * <code>name</name> - the name of the poll 
	  * <code>text</name> - the raw SMS message content from the customer
	  * 
	  * For completeness, Taykt will also pass the following, we don't use it for this.
	  * <code>pid</name> - public identifier, a 36 character unique ID for the customers phone number.
	  *	  
	  */
	override def doPost(request:HttpServletRequest , response:HttpServletResponse ) {
		val pollName = request.getParameter("name")
  		response.setContentType("text/plain")
		response.setCharacterEncoding("utf-8")
		if (null == pollName) 
			response.getWriter().println("No poll name provided, contact Poll organiser")
	    else {
			 pollOptionFromString(request.getParameter("text")) match {
	      	case None 			  => response.getWriter().println("You didn't say what you wanted to vote for!")
	      	case Some(pollOption) => 
	      		polls.get(pollName) match {
	      			case None => polls += (pollName -> Map(pollOption -> 1)) // new poll
	      			case Some(poll) =>  //existing poll
	      			poll.get(pollOption) match {
	      				case Some(value) => poll += (pollOption -> (value + 1))  // existing poll option 
	      				case None 	   => poll += (pollOption -> 1) 		   // new poll option  
			    }
			}
           response.getWriter().println(sMSResponse(pollName, pollOption))
			}
		}
	} 
 
 
	/**
	  * Generates an HTML page to view poll stat, including a list poll options, number of votes and a pie chart. 
	  * Takes a single request parameter <code>name</code> which is the name of the poll.
	  * 
	  */
	override def  doGet(request:HttpServletRequest , response:HttpServletResponse ) = {

		val  pollName = request.getParameter("name")
		response.setContentType("text/html")

		if (null == pollName) {
			response.getWriter().println(NO_POLL)
		} else {
			polls.get(pollName) match {
			  case Some(poll) =>
			    val table    = poll.map { entry => "<tr><td>" +  entry._1+ "</td><td>" + entry._2 + "</td></tr>" }.mkString
			    response.getWriter().println(
			      RESULTS_HTML.replaceAll("POLL_NAME",pollName).replaceAll("RESULTS_TABLE", table).replaceAll("CHART_URL",chartUrl(poll)))
			  case None =>  
				response.getWriter().println(NO_RESULTS_HTML.replaceAll("POLL_NAME",pollName))
			}
		}
	}

	//Override this to extract the name of the poll option from the raw SMS message.
	def  pollOptionFromString(raw:String ):Option[String]

    //Generate URL to google charts api for 3D pie chart, we know pie charts suck, its an example get over it. 
	def chartUrl(poll:Map[String, Int] ) = "http://chart.apis.google.com/chart?cht=p3&chs=500x250&chd=t:" + poll.values.toList.mkString(",") + "&chl=" + poll.keys.toList.mkString("|")

    //Generate text for SMS response. 
	def sMSResponse(pollName:String , option:String ) = RESPONSE_TEXT.replace("OPTION", option).replace("PERCENT", percentage(polls(pollName), option)).replace("LEADER", leader(polls(pollName)))
 
    // calculate the percentage of the votes the poll option has.
	def percentage(poll:Map[String, Int] , option:String) = { (poll(option) / total(poll).toDouble * 100).toString }
    
	//Calculate the total number of votes for the poll
	//this could also be poll.values.reduceLeft{_+_}
	def total(poll:Map[String, Int] ):Int =  { (0 /: poll) ( _ + _._2 ) }

	//Find which option has the most votes, is a little incorrect for ties, okay is just wrong.
  	def leader(poll:Map[String, Int] ) =  poll.toList.sort( (_._2 >_._2) )(0)._1  

   	val RESPONSE_TEXT = "You've voted for OPTION. It has PERCENT% of the vote. Currently LEADER is in the lead."
    
    val NO_RESULTS_HTML = """<html>
   	<head>
   		<title>Poll results for POLL_NAME</title>
   	</head>
   		<body>
   			<p>No votes have been collected for the POLL_NAME poll yet.</p>
   		</body>
</html>"""
    
    val RESULTS_HTML = """<html>
	<head>
		<title>Poll results for POLL_NAME</title>
	</head>	
	<body>
		<table>RESULTS_TABLE</table>
		<img src="CHART_URL">
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
