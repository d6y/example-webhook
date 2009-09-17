package com.taykt.api.examples.webhook;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

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
 * 
 */
public class DateServlet extends HttpServlet {

	private static final long serialVersionUID = -3089985519358798911L;

	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    		resp.setContentType("text/plain");
    		resp.setCharacterEncoding("utf-8");

    		// Set up the format and the time we're going to report on:
    		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    		Date now = new Date();
    		
    		// See if we can find a time zone mentioned in the MO text:
    		TimeZone tz = findTimeZoneFrom( req.getParameter("text") );
    		
    		if (tz == null) {
    			// Failed to match anything in the message:
    			resp.getWriter().println("It's "+formatter.format(now)+" here");
    		} else {
    			// Found a time zone:
    			formatter.setTimeZone(tz);
    			resp.getWriter().println("It's "+formatter.format(now)+" in "+tz.getDisplayName());
    		}
	}
	
	
	/**
	 * Scan the SMS message text to see if we can spot anything like a time zone.
	 * 
	 * @param mo_message_text the text to check for a time zone mentioned.
	 * @return the first matching time zone or null if none found.
	 */
	private TimeZone findTimeZoneFrom(final String mo_message_text) {
	
		if (mo_message_text == null || mo_message_text.isEmpty()) {
			return null;
		}
		
	   	for(String tz_id : TimeZone.getAvailableIDs()) {
	   		
	   		// Exact match to "GMT" or "America/New_York" ?
	   		if (mo_message_text.contains(tz_id)) 
	   			return TimeZone.getTimeZone(tz_id);
	   			
	   		// Try for just "New York" from a tz_id like "America/New_York":
	   		String[] parts = tz_id.split("/");
	   		if (parts.length == 2 && mo_message_text.contains(parts[1].replace("_"," ")))
	   			return TimeZone.getTimeZone(tz_id);
	   		
	   	}
		
		return null; 
	}
}
