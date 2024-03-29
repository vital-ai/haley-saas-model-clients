package ai.haley.saas.model.clients.main

import ai.haley.saas.model.clients.api.Transcription
import ai.haley.saas.model.clients.api.TranscriptionRequest
import ai.haley.saas.model.clients.api.TranscriptionResponse
import ai.haley.saas.model.clients.model.WhisperTranscriptionModel
import ai.haley.saas.model.clients.openai.OpenAIJavaClient

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import org.apache.log4j.BasicConfigurator

class OpenAIClientTranscribeMain extends groovy.lang.Script {

	String apiKey = null
	
	static void main(args) {
		
		BasicConfigurator.configure()
		
		OpenAIClientTranscribeMain app = new OpenAIClientTranscribeMain()
		
		app.run()
		
	}
	
	@Override
	public Object run() {
				
		String configFile = "./config/openaiClient.conf"
	
		Config conf = ConfigFactory.parseFileAnySyntax(new File(configFile))
	
		apiKey = conf.getString("apiKey")

		// Model to use
		WhisperTranscriptionModel modelClass = new WhisperTranscriptionModel()
		
		// Client is specific to a model
		OpenAIJavaClient modelClient = new OpenAIJavaClient(apiKey, modelClass)

		String fileName = "test1.mp3"
			
		String fileFormat = "mp3"
		
		String filePath = "./data/${fileName}"
		
		File audioFile = new File(filePath)
	
		byte[] bytes = audioFile.bytes
		
		TranscriptionRequest request = modelClass.generatePredictionRequest(fileName, fileFormat, bytes)
		
		TranscriptionResponse response = modelClient.generatePrediction(request, 60_000)
	
		if(response == null || response.errorCode != 0) {
		
			println "Timeout or other error."
		
			System.exit(1)
		}
	
		Transcription transcription = response.transcriptionList[0]
	
		println "Transcribed Text:\n----------\n" + transcription.audioTranscriptionText + "\n----------"
		
	}
	
}
