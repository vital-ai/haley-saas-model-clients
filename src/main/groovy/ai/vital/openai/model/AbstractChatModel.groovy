package ai.vital.openai.model

import ai.vital.openai.api.Chat
import ai.vital.openai.api.ChatRequest

abstract class AbstractChatModel extends AbstractModel {
	
	abstract public ChatRequest generatePredictionRequest(Chat currentChat) 
		
	
}
