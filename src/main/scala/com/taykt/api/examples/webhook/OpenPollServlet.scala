package com.taykt.api.examples.webhook

/**
 * A servlet to capture votes for Polls with open voting options. 
 * 
 * The work happens in  <code>PollTrait<code>'s <code>doPost</code> function. This 
 * records the vote and generates the reponse to SMS back to the customer.
 * 
 */
class OpenPollServlet extends PollTrait {

	/**
	  * Extract from the raw SMS response the value of the poll option, in this 
      * case we want the second term.  
	  * E.g "keyword pollOption we dont care what comes next" 
	  * 
	  */
	override def pollOptionFromString(raw:String):Option[String] = raw match {
	  case null   => None
	  case option => Some(option.split(" ").slice(0,2).reverse(0))
	}

}
