package ai.haley.saas.model.clients.api


class Chat {
	
	String systemPrompt = null
	
	String userPrompt = null
	
	List<ChatMessage> chatMessageList = null
	
	List<ChatFunction> chatFunctionList = null
	
	String function_call
	
	
}
