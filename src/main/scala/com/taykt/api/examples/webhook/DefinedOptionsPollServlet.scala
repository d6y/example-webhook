package com.taykt.api.examples.webhook

/**
 * A servlet to capture votes for Polls with defined voting options. 
 * 
 * The work happens in  <code>PollTrait<code>'s <code>doPost</code> function. This 
 * records the vote and generates the reponse to SMS back to the customer.
 * 
 */
class DefinedOptionsPollServlet extends PollTrait {
  
	/**
	  * Extract from the raw SMS response the value of the poll option, in this 
      * case we want the first term.  
	  * E.g "pollOption we dont care what comes next" 
	  * 
	  */
	override def pollOptionFromString(raw:String):String =  raw.split(" ")(0)
  
}
 