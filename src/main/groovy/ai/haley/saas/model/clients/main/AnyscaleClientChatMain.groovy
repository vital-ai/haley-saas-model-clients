package ai.haley.saas.model.clients.main

import ai.haley.saas.model.clients.anyscale.AnyscaleJavaClient
import ai.haley.saas.model.clients.api.Chat
import ai.haley.saas.model.clients.api.ChatMessage
import ai.haley.saas.model.clients.api.ChatMessageType
import ai.haley.saas.model.clients.api.ChatRequest
import ai.haley.saas.model.clients.api.ChatResponse
import ai.haley.saas.model.clients.model.Llama2_13bChatModel
import ai.haley.saas.model.clients.model.Llama2_70bChatModel

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import org.apache.log4j.BasicConfigurator

class AnyscaleClientChatMain extends groovy.lang.Script {

	String apiKey = null
	
	static void main(args) {
		
		// sets up logging to defaults
		BasicConfigurator.configure()
		
		AnyscaleClientChatMain app = new AnyscaleClientChatMain()
		
		app.run()
	}
	
	@Override
	public Object run() {
		
		// Get API Key
		
		String configFile = "./config/anyscaleClient.conf"
	
		Config conf = ConfigFactory.parseFileAnySyntax(new File(configFile))
	
		apiKey = conf.getString("apiKey")

	
		Llama2_70bChatModel modelClass = new Llama2_70bChatModel()
					
		AnyscaleJavaClient modelClient = new AnyscaleJavaClient(apiKey, modelClass)
			
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
	
		// kills background threads
		System.exit(0)
		
			
	}
	
}
