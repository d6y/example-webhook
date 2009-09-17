package com.taykt.api.examples.webhook;

import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class EchoServlet extends HttpServlet {
	
	private static final long serialVersionUID = -8879998764264360723L;

	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    	
    	String from = req.getParameter("pid");
    	String mo_msg = req.getParameter("text");
    	
    	resp.setContentType("text/plain");
    	resp.setCharacterEncoding("utf-8");	
    	resp.getWriter().println( from + " sent " + mo_msg  );
    }
   
}
