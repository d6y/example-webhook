package com.taykt.api.examples.webhook

import javax.servlet.http.{HttpServlet, HttpServletRequest, HttpServletResponse}

/**
 * A Taykt API web hook which accepts a text message and sends back an SMS
 * message that echos what was sent. More-or-less.
 * 
 * See: http://api.taykt.com/ for details.
 */
class EchoServlet extends HttpServlet {
	
	override def doPost(req:HttpServletRequest, resp:HttpServletResponse) {
   
		// Extract details sent from Taykt to this web hook:
		val from = req.getParameter("pid")
    	val mo_msg = req.getParameter("text")
  
    	// Compose and send the response:
    	resp.setContentType("text/plain")
    	resp.setCharacterEncoding("utf-8");
    	resp.getWriter().println( from + " sent " + mo_msg  )
    }
  
}
