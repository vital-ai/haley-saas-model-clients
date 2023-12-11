package ai.haley.saas.model.clients.main

import ai.haley.saas.model.clients.api.Chat
import ai.haley.saas.model.clients.api.ChatMessage
import ai.haley.saas.model.clients.api.ChatMessageType
import ai.haley.saas.model.clients.api.ChatRequest
import ai.haley.saas.model.clients.api.ChatResponse
import ai.haley.saas.model.clients.api.CommandModeEnum
import ai.haley.saas.model.clients.api.StreamResponseHandler
import ai.haley.saas.model.clients.api.TestModeEnum
import ai.haley.saas.model.clients.api.TextCompletion
import ai.haley.saas.model.clients.api.TextCompletionRequest
import ai.haley.saas.model.clients.api.TextCompletionResponse
import ai.haley.saas.model.clients.model.GPT4ChatModel
import ai.haley.saas.model.clients.openai.OpenAIJavaClient
import ai.haley.saas.model.clients.openai.OpenAIJavaStreamingClient
import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import org.apache.log4j.BasicConfigurator

class OpenAIStreamingClientGPT4TestMain extends groovy.lang.Script {

	String apiKey = null
	
	String staticContent = """1. Once upon a time, nestled amidst the bustling city of New Jersey, lived a lively and intelligent little monkey named Boomer. He was not ordinary but an extraordinary creature who was blessed with a tremendous capacity to observe and learn. 

2. One sunny day, Boomer noticed unusual changes in the city's routine; the children's laughter was missing, the streets were empty, and an unsettling silence had taken over. Being an inquisitive creature, he felt compelled to understand what was happening.

3. Using his wit, agility, and resourcefulness, Boomer sneaked into the mayor's office where he overheard the grim news that an enormous meteorite was heading towards New Jersey. The panic was palpable, and Boomer knew he had to do something.

4. Hesitant but determined, Boomer decided to take a leap of faith. Using his observational skills, he remembered seeing a large observatory on the outskirts of the city. With the destination in mind, he ventured into the unknown, overcoming numerous obstacles like busy highways and menacing predators.

5. Upon reaching the observatory, Boomer cleverly deciphered the various astronomical instruments. He found the meteorite's coordinates and, using his knowledge of the city's architecture, decided to reflect the sunlight off the city's various tall glass buildings onto the meteorite.

6. Daybreak arrived, and it was time for Boomer's brave plan to come into action. He rushed around the city, adjusting the angles of mirrors on the buildings, working tirelessly and fearlessly. The city's folks watched in awe as the little monkey went about his mission systematically.

7. As the sunlight hit the mirrors, a dazzling array of light merged to form a gigantic beam that shot towards the sky. The beam hit the meteor and deflected it from its path, saving New Jersey. The city rang with applause, and cheers for Boomer echoed throughout. He had saved the day with his bravery and intelligence.

8. But here's the twist! As everyone rejoiced and the mayor approached to thank Boomer, the clever little monkey swiftly disappeared. He'd slipped away to return to his usual life, watching over the city from atop the trees. Our superhero didn't need the fame and hoopla, for he was simply New Jersey’s silent and watchful guardian. The unexpected hero lived, not in the limelight, but in the hearts of the residents forever.
"""
	
	@Override
	public Object run() { return null }
	
	static void main(args) {
		
		OpenAIStreamingClientGPT4TestMain script = new OpenAIStreamingClientGPT4TestMain()
		
		script.run(args)	
	}
	
	public void run(args) {
		
		// sets up logging to defaults
		BasicConfigurator.configure()
		
		println "OpenAI Streaming Client GPT4 Test Main"

		String configFile = "./config/openaiClient.conf"

		Config conf = ConfigFactory.parseFileAnySyntax(new File(configFile))

		apiKey = conf.getString("apiKey")

		// Model to use
		GPT4ChatModel modelClass = new GPT4ChatModel()

		StreamResponseHandler handler = new StreamResponseHandler() {

			@Override
			void handleStreamResponse(Map dataMap) {

				println "HandleData: " + dataMap

			}
		}

		OpenAIJavaStreamingClient modelClient = new OpenAIJavaStreamingClient(apiKey, modelClass)

		modelClient.setTestStaticContent(staticContent)
		
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

		ChatResponse response = modelClient.generatePrediction(
			request, handler, 100_000, 
			CommandModeEnum.TEST, TestModeEnum.STATIC_CONTENT)

		if(response == null || response.errorCode != 0) {

			println "Timeout or other error."

			System.exit(1)
		}

		String messageType = response.chatMessage.messageType

		String messageText = response.chatMessage.messageText

		println "${messageType}: ${messageText}"

		// Add into history:
		currentChat.chatMessageList.add(userChatMessage)

		currentChat.chatMessageList.add(response.chatMessage)


		System.exit(0)
			
	}

}
