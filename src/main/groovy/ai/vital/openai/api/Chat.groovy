package ai.vital.openai.api

class Chat {
	
	String systemPrompt = null
	
	String userPrompt = null
	
	List<ChatMessage> chatMessageList = null
	
	List<ChatFunction> chatFunctionList = null
	
	String function_call
	
	
}
