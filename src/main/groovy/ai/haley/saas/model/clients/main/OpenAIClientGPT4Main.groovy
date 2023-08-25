package ai.haley.saas.model.clients.main

import ai.haley.saas.model.clients.api.Chat
import ai.haley.saas.model.clients.api.ChatMessage
import ai.haley.saas.model.clients.api.ChatMessageType
import ai.haley.saas.model.clients.api.ChatRequest
import ai.haley.saas.model.clients.api.ChatResponse
import ai.haley.saas.model.clients.api.TextCompletion
import ai.haley.saas.model.clients.api.TextCompletionRequest
import ai.haley.saas.model.clients.api.TextCompletionResponse
import ai.haley.saas.model.clients.model.GPT4ChatModel
import ai.haley.saas.model.clients.openai.OpenAIJavaClient

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import org.apache.log4j.BasicConfigurator

class OpenAIClientGPT4Main extends groovy.lang.Script {

	String apiKey = null
	
	static void main(args) {
		
		// sets up logging to defaults
		BasicConfigurator.configure()
		
		OpenAIClientGPT4Main app = new OpenAIClientGPT4Main()
		
		app.run()		
	}
	
	@Override
	public Object run() {
		
		// Get API Key
		
		String configFile = "./config/openaiClient.conf"
	
		Config conf = ConfigFactory.parseFileAnySyntax(new File(configFile))
	
		apiKey = conf.getString("apiKey")
		
		// Model to use
		GPT4ChatModel modelClass = new GPT4ChatModel()
			
		OpenAIJavaClient modelClient = new OpenAIJavaClient(apiKey, modelClass)
			
		Chat currentChat = new Chat()
		
		currentChat.systemPrompt = "You are named Haley, and you are a brilliant author of children's stories. In your stories, you follow Dan Harmon's story circle."
	
		ChatMessage cm1 = new ChatMessage()
		
		cm1.messageText = "Hi, my name is Haley! I'm here to assist you write children's stories."
			
		cm1.messageType = ChatMessageType.BOT
		
		currentChat.chatMessageList = [ cm1  ]
		
		String stateName = "New Jersey"
		
		String promptString = """
Write a short story set in ${stateName}.
The story should be about a monkey who saves the world by being very brave and smart.
The story has a twist ending.
Each part of the story circle should have a separate paragraph.
""".trim().replaceAll("\n","\\\\n")
				
		ChatMessage userChatMessage = new ChatMessage()
		
		userChatMessage.messageText = promptString 
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
		
			
	}
	
}
