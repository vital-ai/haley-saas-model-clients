package ai.vital.openai.model

import ai.vital.openai.api.Chat
import ai.vital.openai.api.ChatRequest
import ai.vital.openai.api.ChatFunction
import ai.vital.openai.api.ChatMessage
import ai.vital.openai.api.ChatMessageType
import ai.vital.openai.api.ModelRequest

class GPT4_0613ChatModel extends AbstractChatModel {

	static String modelName = "gpt-4"
	
	static String modelVersion = "0613"
	
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
		
		if( currentChat.function_call != null) {
			
			modelParameters["function_call"] = [ "name" : currentChat.function_call ]
		}
		
		if(currentChat.chatFunctionList != null && currentChat.chatFunctionList.size()> 0) {
		
		List<Map> functionList = []
		
		for(ChatFunction function in currentChat.chatFunctionList ) {
							
			Map parameterMap = [:]
			
			parameterMap["type"] = "object"
			
			parameterMap["properties"] = function.parameterMap
			
			parameterMap["required"] = function.requiredParameterList
			
			Map functionMap = [:]
			
			functionMap["name"] = function.name
			
			functionMap["description"] = function.description
		
			functionMap["parameters"] = parameterMap
			
			functionList.add(functionMap)
			
		}
				
		modelParameters["functions"] = functionList
		
		}
		
		return generatePredictionRequest(modelParameters)
	}
}
