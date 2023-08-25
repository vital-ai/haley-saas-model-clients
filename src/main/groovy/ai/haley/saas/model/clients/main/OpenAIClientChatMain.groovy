package ai.haley.saas.model.clients.main

import ai.haley.saas.model.clients.api.Chat
import ai.haley.saas.model.clients.api.ChatMessage
import ai.haley.saas.model.clients.api.ChatMessageType
import ai.haley.saas.model.clients.api.ChatRequest
import ai.haley.saas.model.clients.api.ChatResponse
import ai.haley.saas.model.clients.model.GPT35Turbo16KChatModel
import ai.haley.saas.model.clients.model.GPT35TurboChatModel
import ai.haley.saas.model.clients.model.GPT35Turbo_0301ChatModel
import ai.haley.saas.model.clients.openai.OpenAIJavaClient

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import org.apache.log4j.BasicConfigurator

class OpenAIClientChatMain extends groovy.lang.Script {

	String apiKey = null
	
	static void main(args) {
		
		// sets up logging to defaults
		BasicConfigurator.configure()
		
		OpenAIClientChatMain app = new OpenAIClientChatMain()
		
		app.run()
	}
	
	@Override
	public Object run() {
		
		// Get API Key
		
		String configFile = "./config/openaiClient.conf"
	
		Config conf = ConfigFactory.parseFileAnySyntax(new File(configFile))
	
		apiKey = conf.getString("apiKey")

		// GPT35Turbo_0301ChatModel modelClass = new GPT35Turbo_0301ChatModel()
		
		// GPT35TurboChatModel modelClass = new GPT35TurboChatModel()
		
		GPT35Turbo16KChatModel modelClass = new GPT35Turbo16KChatModel()
		
		OpenAIJavaClient modelClient = new OpenAIJavaClient(apiKey, modelClass)
			
		Chat currentChat = new Chat()
		
		currentChat.systemPrompt = "You are a helpful assistant named Haley."
		
		ChatMessage cm1 = new ChatMessage()
		
		cm1.messageText = "Hi, my name is Haley! I'm here to assist you."
		
		cm1.messageType = ChatMessageType.BOT
		
		
		ChatMessage cm2 = new ChatMessage()
		
		cm2.messageText = "Hi, I'm Marc.  What is your name?"
		
		cm2.messageType = ChatMessageType.USER
	
		
		ChatMessage cm3 = new ChatMessage()
		
		cm3.messageText = "Hi, my name is Haley! Nice to meet you Marc."
		
		cm3.messageType = ChatMessageType.BOT
	
		currentChat.chatMessageList = [ cm1, cm2, cm3 ]
		
		
		ChatMessage userChatMessage = new ChatMessage()
		
		userChatMessage.messageText = "Tell me a story about how a courageous child overcomes adversity and wins a big sporting game. Use your name in the story."
	
		userChatMessage.messageType = ChatMessageType.USER
			
		// New Message:
		currentChat.userPrompt = userChatMessage.messageText
		
		ChatRequest request = modelClass.generatePredictionRequest(currentChat)
		
		ChatResponse response = modelClient.generatePrediction(request,  60_000)
		
		if(response == null || response.errorCode != 0) {
			
			println "Timeout or other error."
			
			System.exit(1)
		}
		
		String messageText = response.chatMessage.messageText
		
		String messageType = response.chatMessage.messageType
		
		println "${messageType}: ${messageText}"
	
		// Add into history:
		currentChat.chatMessageList.add(userChatMessage)
		
		currentChat.chatMessageList.add(response.chatMessage)
		
		// New Message:
		currentChat.userPrompt = "That's a great story.  Why did you make that choice of sport?"
		
		request = modelClass.generatePredictionRequest(currentChat)
		
		response = modelClient.generatePrediction(request,  60_000)
		
		if(response == null || response.errorCode != 0) {
			
			println "Timeout or other error."
			
			System.exit(1)
		}
		
		messageText = response.chatMessage.messageText
		
		messageType = response.chatMessage.messageType
		
		println "${messageType}: ${messageText}"
		
	}
	
}
