package com.taykt.api.examples.webhook

import java.text.SimpleDateFormat
import java.util.{Date, TimeZone}

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Example web hook for use with api.Taykt.com which sends back a message
 * containing the current time, either of this server or the timzone
 * mentioned in the message text.
 * 
 * <p>For example, if the word "demodate" is set up to POST here, and someone
 * texts in "demodate London", this servlet will text back the time in
 * London ("It's 2009-09-16 19:38 in BST").</p>
 */
class DateServlet extends HttpServlet {


	override def doPost(req:HttpServletRequest, resp:HttpServletResponse) {
	  
		resp setContentType "text/plain"
		resp setCharacterEncoding "utf-8"

		// Set up the format and the time we're going to report on:
		val formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm")
		val now = new Date()
		
		// See if we can find a time zone mentioned in the MO text:
		findTimeZoneIn(req getParameter "text") match {
		  case None => 
	    	// Failed to match anything in the message:
	  		resp.getWriter.println("It's "+formatter.format(now)+" here")
	 
		  case Some(tz_id) =>
			// Found a time zone:
		    val tz = TimeZone.getTimeZone(tz_id)
			formatter.setTimeZone(tz)
			resp.getWriter.println("It's "+formatter.format(now)+" in "+tz.getDisplayName())
		}
      
	}
	
	
	/**
	 * Scan the SMS message text to see if we can spot anything like a time zone.
	 * 
	 * @param mo_message_text the text to check for a time zone mentioned.
	 * @return the first match, possibly None.
	 */
	def findTimeZoneIn(mo_message_text: String): Option[String] = {

		if (mo_message_text == null) 
			return None
		
		// For a time zone ID such as "GMT" or "America/New_York" see if the message
		// contains "GMT" or "New York":
		def timezone_mentioned_in_text(tz_id: String) = tz_id.split("/") match {
		  case Array(region, city) => mo_message_text.contains(city.replace("_"," "))
		  case _ => mo_message_text.contains(tz_id) 
		}
  
		TimeZone.getAvailableIDs find timezone_mentioned_in_text   
	}
  
  
}
