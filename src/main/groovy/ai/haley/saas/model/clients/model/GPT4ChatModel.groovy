package ai.haley.saas.model.clients.model

import ai.haley.saas.model.clients.api.Chat
import ai.haley.saas.model.clients.api.ChatMessage
import ai.haley.saas.model.clients.api.ChatMessageType
import ai.haley.saas.model.clients.api.ChatRequest
import ai.haley.saas.model.clients.api.ModelRequest
import ai.haley.saas.model.clients.api.TextCompletionRequest

class GPT4ChatModel extends AbstractChatModel {

	static String modelName = "gpt-4"
	
	static String modelVersion = ""
	
	@Override
	public String getModelName() {
		
		return modelName
	}

	@Override
	public String getModelVersion() {
		
		return modelVersion
	}

		
	@Override
	public Map<String, Object> getParameterMap() {
		
		Map p = [:]
		
		p["messages"] = List.class
			
		return p
	}
	
	
	
	@Override
	public ChatRequest generatePredictionRequest(Map parameters) {
		
		ChatRequest request = new ChatRequest()
		
		request.modelName = getModelName()
		
		request.modelVersion = getModelVersion()
		
		String modelString = getModelString()
				
		parameters["model"] = modelString
		
		request.parameterMap = parameters
		
		return request
	}
	
	@Override
	public ChatRequest generatePredictionRequest(Chat currentChat) {
		
		Map modelParameters = [:]
		
		String systemPrompt = currentChat.systemPrompt
	
		List<ChatMessage> chatMessageList = currentChat.chatMessageList
		
		String userPrompt = currentChat.userPrompt
			
		List<Map> messageList = []
		
		modelParameters["messages"] = messageList
		
		Map systemMap = [
			"role": "system",
			"content": systemPrompt
		]
		
		messageList.add(systemMap)
		
		for(ChatMessage chatMessage in chatMessageList) {
			
			ChatMessageType messageType = chatMessage.messageType
			
			String roleString = null
			
			if(messageType == ChatMessageType.USER) {
				
				roleString = "user"
			}
			
			if(messageType == ChatMessageType.BOT) {
				
				roleString = "assistant"
			}
			
			String messageText = chatMessage.messageText
			
			Map messageMap = [
				"role": roleString,
				"content": messageText
			]
			
			messageList.add(messageMap)
		}
		
		Map userMap = [
			"role": "user",
			"content": userPrompt
		]
		
		messageList.add(userMap)
		
		
		return generatePredictionRequest(modelParameters)
	}
}
