package ai.vital.openai.main

import ai.vital.openai.api.OpenAIJavaClient
import ai.vital.openai.api.TextCompletion
import ai.vital.openai.api.TextCompletionRequest
import ai.vital.openai.api.TextCompletionResponse
import ai.vital.openai.model.DaVinci2Model
import ai.vital.openai.model.DaVinci3Model
import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import org.apache.log4j.BasicConfigurator

class OpenAIClientMain extends groovy.lang.Script {

	String apiKey = null
	
	static void main(args) {
		
		// sets up logging to defaults
		BasicConfigurator.configure()
		
		OpenAIClientMain app = new OpenAIClientMain()
		
		app.run()		
	}

	@Override
	public Object run() {
		
		// Get API Key
		
		String configFile = "./config/openaiClient.conf"
	
		Config conf = ConfigFactory.parseFileAnySyntax(new File(configFile))
	
		apiKey = conf.getString("apiKey")
			
		// Model to use
		DaVinci3Model modelClass = new DaVinci3Model()
		
		// Client is specific to a model
		OpenAIJavaClient modelClient = new OpenAIJavaClient(apiKey, modelClass)
	
		String stateName = "New Jersey"
	
		// Note: encoding of newlines is necessary in promptString
		// Should be moved within creating request?
		
		String promptString = """
Write a short story set in ${stateName}.
The story should be about a monkey who saves the world by being very brave and smart.
The story has a twist ending.
""".trim().replaceAll("\n","\\\\n")
	
		Double temperature = 0.80d
	
		Integer maxTokens = 900
	
		// Helper to Generate Request
		TextCompletionRequest request = modelClass.generatePredictionRequest(promptString, temperature, maxTokens)
		
		// Get Response for Request
		TextCompletionResponse response = modelClient.generatePrediction(request,  60_000)
	
		if(response == null || response.errorCode != 0) {
		
			println "Timeout or other error."
		
			System.exit(1)	
		}
	
		TextCompletion textCompletion = response.textCompletionList[0]
	
		println "Generated Text:\n" + textCompletion.textCompletion
	
	}
	
}
