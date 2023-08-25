package ai.haley.saas.model.clients.anyscale

import groovy.json.JsonSlurper
import java.nio.charset.StandardCharsets
import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit
import org.apache.http.client.methods.CloseableHttpResponse
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.StringEntity
import org.apache.http.entity.mime.MultipartEntityBuilder
import org.apache.http.entity.mime.content.ByteArrayBody
import org.apache.http.entity.mime.content.ContentBody
import org.apache.http.entity.mime.content.StringBody
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.HttpClients
import org.apache.http.HttpEntity
import org.apache.http.NameValuePair
import org.apache.http.util.EntityUtils

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import ai.haley.saas.model.clients.api.ChatMessageType

import ai.haley.saas.model.clients.api.ChatFunctionCall
import ai.haley.saas.model.clients.api.ChatMessage
import ai.haley.saas.model.clients.api.ChatRequest
import ai.haley.saas.model.clients.api.ChatResponse
import ai.haley.saas.model.clients.api.PostStatus
import ai.haley.saas.model.clients.model.AbstractModel
import groovy.json.JsonOutput
import org.slf4j.Logger
import org.slf4j.LoggerFactory


// Main client
class AnyscaleJavaClient {
	
	private final static Logger log = LoggerFactory.getLogger( AnyscaleJavaClient.class)
	
	static JsonSlurper parser = new JsonSlurper()

	static String chatEndpoint = "https://api.endpoints.anyscale.com/v1/chat/completions"
	
	AbstractModel internalModel
	
	String modelName = null
	
	String modelVersion = null
		
	String apiKey = null
	
	// TODO capture which models support which endpoints in the
	// model class, then check that value in prediction calls
	
	AnyscaleJavaClient(String apiKey, AbstractModel model) {
		
		this.apiKey = apiKey
		
		this.internalModel = model
		
		this.modelName = model.getModelName()
		
		this.modelVersion = model.getModelVersion()
	}
		
	AbstractModel getModel() {
		
		return internalModel
	}
	
	
	ChatResponse generatePrediction(ChatRequest request, Integer timeout_ms) {
			
		CloseableHttpClient httpclient = null
		
		Map parameterMap = request.parameterMap
		
		String jsonParameters = JsonOutput.toJson(parameterMap)
		
		def pretty = JsonOutput.prettyPrint(jsonParameters)
	
		log.info( "Request:\n" + pretty )
		
		try {
			
			httpclient = HttpClients.createDefault()
					
			HttpPost httppost = new HttpPost ( chatEndpoint )
			
			StringEntity entity = new StringEntity(jsonParameters, "utf-8")
			
			httppost.setEntity(entity)
			
			httppost.setHeader("Authorization", "Bearer ${apiKey}")
			
			httppost.setHeader("Content-type", "application/json")
			
			PostStatus postStatus = execPost(httpclient, httppost, timeout_ms) 
					
			CloseableHttpResponse httpResponse = postStatus.httpResponse
			
			if(httpResponse == null) {
				
				log.error( "Exception: POST Failed" )
				
				ChatResponse errorResponse = new ChatResponse()
					
				errorResponse.errorMessage = "error"
					
				errorResponse.errorCode = 1
			
				return errorResponse
				
			}			
			
			String json_string = EntityUtils.toString( httpResponse.getEntity() )
			
			httpResponse.close()
						
			pretty = JsonOutput.prettyPrint(json_string)
			
			Map result = parser.parse(json_string.toCharArray())
						
			log.info( "Result:\n" + pretty )
			
			// TODO handle multiple choices
			// TODO handle other components of the response
			
			
			Integer choiceCount = 0
			
			if(result.choices != null) {
				
				choiceCount = result.choices.size()
				
			}
			
			
			boolean function_choice = false
			
			boolean text_choice = false
			
			if(choiceCount > 0) {
				
				for(Map choice in result.choices) {
				
					Map message = choice["message"]
					
					if(message != null) {
					
						if(message["function_call"] != null) {
						
							function_choice = true
						
						}
					
						
						if(message["content"] != null) {
							
							text_choice = true
						}
						
					}
					
					
				}	
			}
			
			ChatMessage chatMessage = new ChatMessage()
			
			chatMessage.messageType = ChatMessageType.BOT
			
			if(function_choice) {
				
				ChatFunctionCall functionCall = new ChatFunctionCall()
				
				functionCall.functionName = result.choices[0].message.function_call.name.trim()
				
				String paramJSON = result.choices[0].message.function_call.arguments.trim()
				
				if(paramJSON != null) {
					
					JsonSlurper parser = new JsonSlurper()
					
					Map paramMap = parser.parseText(paramJSON)
					
					functionCall.parameterMap = paramMap	
				}
				
				chatMessage.functionCall = functionCall
				
			}
			
			if(text_choice) {
				
				String text = result.choices[0].message.content.trim()
				
				log.info ("Generated Chat Text:\n" + text )

				chatMessage.messageText = text

				
			}
			
			
			ChatResponse response = new ChatResponse()
			
			response.chatMessage = chatMessage
						
			return response
						
		} catch(Exception ex) {
			
			log.error( "Exception: " + ex.localizedMessage )
			
			ChatResponse errorResponse = new ChatResponse()
				
			errorResponse.errorMessage = "error"
				
			errorResponse.errorCode = 1
		
			return errorResponse
									
		} finally {
			httpclient?.close()
		}
			
		// should not get here
		
		ChatResponse errorResponse = new ChatResponse()
		
		errorResponse.errorMessage = "error"
		
		errorResponse.errorCode = 1

		return errorResponse
	}
	
	

	
	// This is overkill for a single post but potentially needed
	// for supporting more complex scenarios like getting streaming results back
	
	PostStatus execPost(CloseableHttpClient httpclient, HttpPost httppost, Integer timeout_ms) {
		
		// by default assume error
		// gets replaced if successful
		PostStatus execPostStatus = new PostStatus()
		
		execPostStatus.status = "Error"
		
		try {
		
			// Potentially used shared thread pool instead of one at a time
			ExecutorService executorService = Executors.newFixedThreadPool(1)
		
			def r = new Callable() {
				
				public Object call() throws Exception {
			
					try {
					
						PostStatus taskPostStatus = new PostStatus()
						
						taskPostStatus.httpResponse = httpclient.execute(httppost)
					
						taskPostStatus.status = "Ok"
						
						return taskPostStatus
					
					} catch(Exception ex) {
						
						log.error("Exception during POST cleanup: " + ex.localizedMessage )
					}
					
					PostStatus taskPostStatus = new PostStatus()
					
					taskPostStatus.status = "Error"
					
					return taskPostStatus
				}
			}
		
			// start the post
			Future<String> future  = executorService.submit( r )
		
			TimerTask task = new TimerTask() {
				
				public void run() {
				
					log.error( "Running interrupt." )
					
					if(!future.isDone()) {
						
						log.error( "Task is not done. initiating interrupt.")
						
						future.cancel(true)
						
						// cleanup
						
						try {
						
							log.error( "starting cleanup after status check timeout..." )
						
						
						} catch( Exception cleanupException) {
							
								
							log.error("Exception during status check cleanup: " + cleanupException.localizedMessage )
							
							// cleanupException.printStackTrace()
						}
					}
					else {
						
						// println "Task is already complete."
					}
				}
			}
		
			// start a timer for the timeout
			
			Timer timer = new Timer("Interruption Timer");
		
			long timeout = timeout_ms as long // 60000L
			
			timer.schedule(task, timeout)
	
			try {
				
				// wait here until the Post completes or the timer interrupts it
				execPostStatus = future.get()
					
				if(execPostStatus.status == "Ok") {
													
				}
					
				if(execPostStatus.status == "Error") {
					
					log.error( "Task had an internal error or an internal timeout." )
					
					log.error( "Canceling interrupt timer: " + task.cancel() )
				}
						
			} catch(Exception futureException) {
										
				log.error("The task may have been interrupted. Exception during future get: " + futureException.localizedMessage )
					
				// futureException.printStackTrace()
			}
				
			try {
					
				timer.cancel() // need to kill timer thread
					
				executorService.shutdown() // need to close threads
				
				executorService.awaitTermination(3, TimeUnit.SECONDS)
				
				executorService.shutdownNow();
					
			} catch(Exception serviceException) {
						
				log.error("Exception during timer and executive service shutdown in status check: " + serviceException.localizedMessage )
							
				// serviceException.printStackTrace()
			}
				
		} catch(Exception ex) {
			
			log.error("Unhandled Exception during status check: " + ex.localizedMessage )
			
		}
		
		// returns error case with null response if error
		return execPostStatus
	
	}
}
