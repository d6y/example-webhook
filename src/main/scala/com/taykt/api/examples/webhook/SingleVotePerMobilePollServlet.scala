package com.taykt.api.examples.webhook

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import scala.collection.mutable.{Map, Set,SynchronizedMap,SynchronizedSet}

/**
 * A servlet to capture votes for Polls with defined voting options, which only
 * allows one vote per mobile, per poll.
 */
class SingleVotePerMobilePollServlet extends PollTrait{
  
    //Keep track of the customers who have voted for different polls.
  	val numbers = Map[String, Set[String]]()

   
	/**
	  * Records votes and generates the response SMS message to send to the customer, allows a single vote per poll per mobile. 
	  * 	  
	  * Three parameters are expected :
	  * <code>name</name> - the name of the poll 
	  * <code>text</name> - the raw SMS message content from the customer
	  * <code>pid</name>  - public identifier, a 36 character unique ID for the customers phone number.
	  *	  
	  */
 	override def doPost(request:HttpServletRequest , response:HttpServletResponse ) {
		response.setContentType("text/plain");
		response.setCharacterEncoding("utf-8");

		request.getParameter("name") match {
		  case pollName:String =>
			val pid =  request.getParameter("pid")
			val pollOption:String = pollOptionFromString(request.getParameter("text"))
			if (hasntVoted(pollName,pid)) {
			  recordVote(pollName,pid)
			  polls.get(pollName) match{
			  	case None => polls += (pollName -> Map(pollOption -> 1))
			    case Some(poll) =>
					if (poll.contains(pollOption)) {
						val count = poll(pollOption)
						poll.put(pollOption, count + 1)
					} else {
						poll.put(pollOption, 1)
					}
			}
			response.getWriter().println(sMSResponse(pollName, pollOption))
			} else { response.getWriter().println("You can only vote once in the " + pollName + " poll.")}
		  case null => response.getWriter().println("No poll name provided, contact Poll organiser")   
		}

	}
	  
    //  Checks if <code>pid</code> has voted in <code>pollName</code> Poll.
	private def hasntVoted( pollName:String, id:String ):Boolean  = {
  		numbers.get(pollName) match {
  		  case None       =>  numbers.put(pollName, Set[String]());true
  		  case Some(poll) =>  !poll.contains(id) 
  		}
	}

    // Records that <code>pid</code> has voted in <code>pollName</code> Poll.
	private def recordVote(pollName:String , pid:String) = { numbers(pollName) += pid }
 
	/**
	  * Extract from the raw SMS response the value of the poll option, in this 
      * case we want the first term.  
	  * E.g "pollOption we dont care what comes next" 
	  * 
	  */
	override def pollOptionFromString(raw:String):String =  raw.split(" ")(0)

 
}
