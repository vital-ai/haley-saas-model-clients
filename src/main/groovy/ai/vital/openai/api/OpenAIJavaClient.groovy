package ai.vital.openai.api

import groovy.json.JsonSlurper
import org.apache.http.client.methods.CloseableHttpResponse
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.HttpClients
import org.apache.http.NameValuePair
import org.apache.http.util.EntityUtils
import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import groovy.json.JsonOutput
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class OpenAIJavaClient {
	
	private final static Logger log = LoggerFactory.getLogger( OpenAIJavaClient.class)
	
	static JsonSlurper parser = new JsonSlurper()

	static String textCompletionEndpoint = "https://api.openai.com/v1/completions"

	static String codeCompletionEndpoint = "https://api.openai.com/v1/completions"
	
	static String imageGenerationEndpoint = "https://api.openai.com/v1/completions"
	
	String modelName = null
	
	String modelVersion = null
		
	String apiKey = null
	
	OpenAIJavaClient(String apiKey, String modelName, String modelVersion) {
		
		this.apiKey = apiKey
		
		this.modelName = modelName
		
		this.modelVersion = modelVersion
				
	}
	
	TextCompletionResponse generatePrediction(TextCompletionRequest request, Integer timeout_ms) {
				
		CloseableHttpClient httpclient = null
		
		Map parameterMap = request.parameterMap
		
		String jsonParameters = JsonOutput.toJson(parameterMap)
		
		try {
			
			httpclient = HttpClients.createDefault()
					
			HttpPost httppost = new HttpPost ( textCompletionEndpoint )
			
			StringEntity entity = new StringEntity(jsonParameters, "utf-8")
			
			httppost.setEntity(entity)
			
			httppost.setHeader("Authorization", "Bearer ${apiKey}")
			
			httppost.setHeader("Content-type", "application/json")
			
			CloseableHttpResponse httpResponse = httpclient.execute(httppost)
			
			String json_string = EntityUtils.toString( httpResponse.getEntity() )
			
			httpResponse.close()
						
			def pretty = JsonOutput.prettyPrint(json_string)
			
			Map result = parser.parse(json_string.toCharArray())
						
			log.info( "Result:\n" + pretty )
			
			// check for multiple generated completions
			// add to completion response including other info, like scoring
			
			String text = result.choices[0].text.trim()
						
			log.info ("Generated Text:\n" + text )
			
			TextCompletionResponse response = new TextCompletionResponse()
			
			TextCompletion textCompletion = new TextCompletion()
			
			textCompletion.textCompletion = text
			
			response.textCompletionList = [ textCompletion ]
			
			return response
						
		} catch(Exception ex) {
			
			log.error( "Exception: " + ex.localizedMessage )
			
			TextCompletionResponse errorResponse = new TextCompletionResponse()
				
			errorResponse.errorMessage = "error"
				
			errorResponse.errorCode = 1
		
			return errorResponse
									
		} finally {
			httpclient?.close()
		}
			
		// should not get here
		
		TextCompletionResponse errorResponse = new TextCompletionResponse()
		
		errorResponse.errorMessage = "error"
		
		errorResponse.errorCode = 1

		return errorResponse
				
	}
		
	CodeCompletionResponse generatePrediction(CodeCompletionRequest request, Integer timeout_ms) {
	
	
	
	}
	
	
	ImageGenerationResponse generatePrediction(ImageGenerationRequest request, Integer timeout_ms) {
	
	
	
	}
	
	
}
