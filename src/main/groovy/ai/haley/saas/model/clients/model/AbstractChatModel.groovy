package ai.haley.saas.model.clients.model

import ai.haley.saas.model.clients.api.Chat
import ai.haley.saas.model.clients.api.ChatRequest

abstract class AbstractChatModel extends AbstractModel {
	
	abstract public ChatRequest generatePredictionRequest(Chat currentChat) 
		
	
}
