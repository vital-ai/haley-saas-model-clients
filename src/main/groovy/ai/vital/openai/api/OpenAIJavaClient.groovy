package ai.vital.openai.api

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
import ai.vital.openai.model.AbstractModel
import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import groovy.json.JsonOutput
import org.slf4j.Logger
import org.slf4j.LoggerFactory



// Utility class for capturing the results of a Post
// TODO potentially use something like this to return partial results
// for case of returning streaming results incrementally
class PostStatus {
	
	// TODO make into an enum?
	// "Ok" or "Error"
	String status = null
	
	CloseableHttpResponse httpResponse = null
}

// Main client
class OpenAIJavaClient {
	
	private final static Logger log = LoggerFactory.getLogger( OpenAIJavaClient.class)
	
	static JsonSlurper parser = new JsonSlurper()

	static String textCompletionEndpoint = "https://api.openai.com/v1/completions"

	static String chatEndpoint = "https://api.openai.com/v1/chat/completions"
	
	static String codeCompletionEndpoint = "https://api.openai.com/v1/completions"
	
	static String imageGenerationEndpoint = "https://api.openai.com/v1/completions"
	
	static String transcriptionEndpoint = "https://api.openai.com/v1/audio/transcriptions"
	
	String modelName = null
	
	String modelVersion = null
		
	String apiKey = null
	
	// TODO capture which models support which endpoints in the
	// model class, then check that value in prediction calls
	
	OpenAIJavaClient(String apiKey, AbstractModel model) {
		
		this.apiKey = apiKey
		
		this.modelName = model.getModelName()
		
		this.modelVersion = model.getModelVersion()
				
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
			
			// CloseableHttpResponse httpResponse = httpclient.execute(httppost)
			
			PostStatus postStatus = execPost(httpclient, httppost, timeout_ms)
			
			CloseableHttpResponse httpResponse = postStatus.httpResponse
	
			if(httpResponse == null) {
				
				TextCompletionResponse errorResponse = new TextCompletionResponse()
				
				errorResponse.errorMessage = "error"
				
				errorResponse.errorCode = 1
		
				return errorResponse
				
			}
			
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
			
			String text = result.choices[0].message.content.trim()
						
			log.info ("Generated Chat Text:\n" + text )
			
			ChatMessage chatMessage = new ChatMessage()
			
			chatMessage.messageText = text
		
			chatMessage.messageType = ChatMessageType.BOT
					
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
	
	CodeCompletionResponse generatePrediction(CodeCompletionRequest request, Integer timeout_ms) {
	
	
	
	}	
	
	ImageGenerationResponse generatePrediction(ImageGenerationRequest request, Integer timeout_ms) {
	
	
	
	}
	
	TranscriptionResponse generatePrediction(TranscriptionRequest request, Integer timeout_ms) {
	
		CloseableHttpClient httpclient = null
		
		Map parameterMap = request.parameterMap
	
		try {
			
			httpclient = HttpClients.createDefault()
			
			byte[] fileBytes = parameterMap.audioFileBytes
					
			String fileName = parameterMap.audioFileName
			
			String fileFormat = parameterMap.audioFileFormat
			
			log.info("Transcription FileName: " + fileName)
			log.info("Transcription FileFormat: " + fileFormat)
			log.info("Transcription File Byte Size: " + fileBytes.length)
			
			// TODO check file format
			
			HttpPost httppost = new HttpPost ( transcriptionEndpoint )
			
			httppost.setHeader("Authorization", "Bearer ${apiKey}")
			
			// for some reason this breaks things
			// httppost.setHeader("Content-type", "multipart/form-data")
		
			MultipartEntityBuilder builder = MultipartEntityBuilder.create()
			
			// builder.setCharset(StandardCharsets.UTF_8)
		
			// name needs to be set
			ContentBody fileBody = new ByteArrayBody(fileBytes, fileName)
			
			builder.addPart("file", fileBody)
	
			StringBody modelBody = new StringBody("whisper-1")
			
			builder.addPart("model", modelBody)
			
			HttpEntity multipartEntity = builder.build()
			
			httppost.setEntity(multipartEntity)
			
			// CloseableHttpResponse httpResponse = httpclient.execute(httppost)

			PostStatus postStatus = execPost(httpclient, httppost, timeout_ms)
			
			CloseableHttpResponse httpResponse = postStatus.httpResponse
	
			if(httpResponse == null) {
				
				log.error( "Exception: POST Failed" )
				
				TranscriptionResponse errorResponse = new TranscriptionResponse()
				
				errorResponse.errorMessage = "error"
				
				errorResponse.errorCode = 1
		
				return errorResponse	
			}
					
			String json_string = EntityUtils.toString( httpResponse.getEntity() )
			
			httpResponse.close()
						
			def pretty = JsonOutput.prettyPrint(json_string)
			
			Map result = parser.parse(json_string.toCharArray())
						
			log.info( "Result:\n" + pretty )
			
			String text = result.text.trim()
			
			log.info ("Transcribed Text:\n" + text )
			
			TranscriptionResponse response = new TranscriptionResponse()
			
			Transcription transcription = new Transcription()
			
			transcription.audioTranscriptionText = text
			
			response.transcriptionList = [ transcription ]
			
			return response
	
		} catch(Exception ex) {
			
			log.error( "Exception: " + ex.localizedMessage )
			
			TranscriptionResponse errorResponse = new TranscriptionResponse()
				
			errorResponse.errorMessage = "error"
				
			errorResponse.errorCode = 1
		
			return errorResponse
									
		} finally {
			httpclient?.close()
		}
		
		TranscriptionResponse errorResponse = new TranscriptionResponse()
		
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
