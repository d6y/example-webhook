package com.taykt.api.examples.webhook

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import scala.collection.mutable.{Map, Set,SynchronizedMap,SynchronizedSet}

class SingleVotePerMobilePollServlet extends PollTrait{
  
  	val numbers = Map[String, Set[String]]()

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
 
	private def hasntVoted( pollName:String, id:String ):Boolean  = {
  		numbers.get(pollName) match {
  		  case None       =>  numbers.put(pollName, Set[String]());true
  		  case Some(poll) =>  !poll.contains(id) 
  		}
	}

	private def recordVote(pollName:String , id:String) = { numbers(pollName) += id }
 
   	// we are expecting people to text "pollValue"
	override def pollOptionFromString(raw:String):String =  raw.split(" ")(0)

 
}
