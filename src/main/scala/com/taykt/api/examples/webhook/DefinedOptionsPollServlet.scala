package com.taykt.api.examples.webhook

class DefinedOptionsPollServlet extends PollTrait {
  
	// we are expecting people to text "pollValue"
	override def pollOptionFromString(raw:String):String =  raw.split(" ")(0)
  
}
 