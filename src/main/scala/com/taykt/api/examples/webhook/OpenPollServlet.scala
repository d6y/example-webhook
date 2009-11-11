package com.taykt.api.examples.webhook

class OpenPollServlet extends PollTrait {

	// we are expecting people to text "keyword pollOption"
	override def pollOptionFromString(raw:String):String = raw.split(" ").slice(0,2).reverse(0)

}
