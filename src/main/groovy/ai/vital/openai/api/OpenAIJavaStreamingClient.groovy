package ai.vital.openai.api

import groovy.json.JsonSlurper
import java.nio.charset.StandardCharsets
import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit
import org.apache.http.client.ClientProtocolException
import org.apache.http.client.ResponseHandler
import org.apache.http.client.methods.CloseableHttpResponse
import org.apache.http.client.methods.HttpPost
import org.apache.http.concurrent.FutureCallback
import org.apache.http.entity.StringEntity
import org.apache.http.entity.mime.MultipartEntityBuilder
import org.apache.http.entity.mime.content.ByteArrayBody
import org.apache.http.entity.mime.content.ContentBody
import org.apache.http.entity.mime.content.StringBody
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.HttpClients
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient
import org.apache.http.impl.nio.client.HttpAsyncClients
import org.apache.http.HttpEntity
import org.apache.http.HttpResponse
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
class PostStreamingStatus {
	
	// TODO make into an enum?
	// "Ok" or "Error"
	String status = null
	
	String statusMessage = null
	
	Integer errorCode = 0
	
	CloseableHttpResponse httpResponse = null
	
	// change to string buffer
	
	StringBuffer completeContent = new StringBuffer()
	
	Date lastUpdateTime = new Date()
	
}


public interface StreamResponseHandler {
	
		 void handleStreamResponse(Map dataMap)
	
	}

	

// Main client
class OpenAIJavaStreamingClient {
	
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
	
	PostQueue postQueue = new PostQueue()
	
	
	// TODO capture which models support which endpoints in the
	// model class, then check that value in prediction calls
	
	OpenAIJavaStreamingClient(String apiKey, AbstractModel model) {
		
		this.apiKey = apiKey
		
		this.modelName = model.getModelName()
		
		this.modelVersion = model.getModelVersion()
				
	}
		
	TextCompletionResponse generatePrediction(TextCompletionRequest request, StreamResponseHandler handler, Integer timeout_ms) {
				
		String requestIdentifier = request.requestIdentifier
		
		request.parameterMap.stream = true
		
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
			
			PostStreamingStatus postStatus = execPost(requestIdentifier, httpclient, httppost, handler, timeout_ms)
			
			// CloseableHttpResponse httpResponse = postStatus.httpResponse
	
			if(postStatus.status != "Ok") {
				
				log.error( "Exception: POST Failed" )
				
				TextCompletionResponse errorResponse = new TextCompletionResponse()
				
				errorResponse.errorMessage = "error"
				
				errorResponse.errorCode = 1
		
				return errorResponse
				
			}
			
			// String json_string = EntityUtils.toString( httpResponse.getEntity() )
			
			// httpResponse.close()
						
			// def pretty = JsonOutput.prettyPrint(json_string)
			
			// Map result = parser.parse(json_string.toCharArray())
						
			// log.info( "Result:\n" + pretty )
			
			// check for multiple generated completions
			// add to completion response including other info, like scoring
			
			String text = postStatus.completeContent.toString() // result.choices[0].text.trim()
						
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
	
	ChatResponse generatePrediction(ChatRequest request, StreamResponseHandler handler, Integer timeout_ms) {
		
		String requestIdentifier = request.requestIdentifier
		
		request.parameterMap.stream = true
		
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
			
			PostStreamingStatus postStatus = execPost(requestIdentifier, httpclient, httppost, handler, timeout_ms)
					
			// CloseableHttpResponse httpResponse = postStatus.httpResponse
			
			if(postStatus.status != "Ok") {
				
				log.error( "Exception: POST Failed" )
				
				ChatResponse errorResponse = new ChatResponse()
				
				// errorResponse.chatMessage = postStatus.completeContent.toString()
					
				String text = postStatus.completeContent.toString() // result.choices[0].message.content.trim()
				
				log.info ("Error: Generated Chat Text:\n" + text )
	
				ChatMessage chatMessage = new ChatMessage()
	
				chatMessage.messageText = text

				chatMessage.messageType = ChatMessageType.BOT
		
				errorResponse.chatMessage = chatMessage
						
				errorResponse.errorMessage = postStatus.statusMessage
					
				errorResponse.errorCode = 1
				
				if( postStatus.errorCode ) {
					
					errorResponse.errorCode = postStatus.errorCode
				}
			
				return errorResponse
			}
			
			// String json_string = EntityUtils.toString( httpResponse.getEntity() )
			
			// httpResponse.close()
						
			// pretty = JsonOutput.prettyPrint(json_string)
			
			// Map result = parser.parse(json_string.toCharArray())
						
			// log.info( "Result:\n" + pretty )
			
			// TODO handle multiple choices
			// TODO handle other components of the response
			
			String text = postStatus.completeContent.toString() // result.choices[0].message.content.trim()
						
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
	
	
	ChatResponse cancelPrediction(String requestIdentifier) {
	
		log.info("Cancel Prediction.  Checking for Request: " + requestIdentifier)
		
		PostStreamingStatus streamingStatus = postQueue.stopPost(requestIdentifier)

		if(streamingStatus == null) {
			
			return null
			
		}
		
		String text = streamingStatus.completeContent.toString() // result.choices[0].message.content.trim()
		
		log.info("Canceled --- Current Generated Chat Text:\n" + text )

		ChatMessage chatMessage = new ChatMessage()

		chatMessage.messageText = text

		chatMessage.messageType = ChatMessageType.BOT
	
		ChatResponse response = new ChatResponse()

		response.chatMessage = chatMessage
		
		return response
			
	}
	
	CodeCompletionResponse generatePrediction(CodeCompletionRequest request, StreamResponseHandler handler, Integer timeout_ms) {
	
	
	
	}
	
	ImageGenerationResponse generatePrediction(ImageGenerationRequest request, StreamResponseHandler handler, Integer timeout_ms) {
	
	
	
	}
	
	TranscriptionResponse generatePrediction(TranscriptionRequest request, StreamResponseHandler handler, Integer timeout_ms) {
	
		String requestIdentifier = request.requestIdentifier
		
		request.parameterMap.stream = true
		
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

			PostStreamingStatus postStatus = execPost(requestIdentifier, httpclient, httppost, handler, timeout_ms)
			
			// CloseableHttpResponse httpResponse = postStatus.httpResponse
	
			if(postStatus.status != "Ok") {
				
				log.error( "Exception: POST Failed" )
				
				TranscriptionResponse errorResponse = new TranscriptionResponse()
				
				errorResponse.errorMessage = "error"
				
				errorResponse.errorCode = 1
		
				return errorResponse
			}
					
			// String json_string = EntityUtils.toString( httpResponse.getEntity() )
			
			// httpResponse.close()
						
			// def pretty = JsonOutput.prettyPrint(json_string)
			
			// Map result = parser.parse(json_string.toCharArray())
						
			// log.info( "Result:\n" + pretty )
			
			String text = postStatus.completeContent.toString() // .text.trim()
			
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
	
	
	// TODO pass in a task id such that the id can be used to kill the task as it's running
	// keep a map of id --> task for the instance of the client
	// the task is the interrupt task:
	// TimerTask task = new TimerTask() {
	// remove task from map when it completes
	// add additional method to cancel a task given the id
	// cancel method should just need to do:
	// synchronized (task) {
    //  task.notify()
 	// }
	// to notify the timer task to run immediately
	
	// alternatively the "future" could be put into a similar map and 
	// canceled directly.
	// but then the timer, etc. would need to be cleaned up
	// whereas triggering the timer interrupt should do the cleanup
	
	/*
	 
	 DATA: data: {"id":"chatcmpl-71b1myp21cgVkbPorp2DC8Tmu3PAY","object":"chat.completion.chunk","created":1680615018
,"model":"gpt-3.5-turbo-0301","choices":[{"delta":{"content":" especially"},"index":0,"finish_reason":null}]}

data: {"id":"chatcmpl-71b1myp21cgVkbPorp2DC8Tmu3PAY","object":"chat.completion.chunk","created":1680615018,"model":"gpt-3.5-turbo-0301","choi
ces":[{"delta":{"content":" for"},"index":0,"finish_reason":null}]}

data: {"id":"chatcmpl-71b1myp21cgVkbPorp2DC8Tmu3PAY","object":"chat.completion.chunk","created":1680615018,"model":"gpt-3.5-turbo-0301","choi
ces":[{"delta":{"content":" larger"},"index":0,"finish_reason":null}]}

data: {"id":"chatcmpl-71b1myp21cgVkbPorp2DC8Tmu3PAY","object":"chat.completion.chunk","created":1680615018,"model":"gpt-3.5-turbo-0301","choi
ces":[{"delta":{"content":" arrays"},"index":0,"finish_reason":null}]}

data: {"id":"chatcmpl-71b1myp21cgVkbPorp2DC8Tmu3PAY","object":"chat.completion.chunk","created":1680615018,"model":"gpt-3.5-turbo-0301","choi
ces":[{"delta":{"content":"."}
	 
	 
The current character read is '}' with an int value of 125
issue parsing JSON array
line number 1
index number 166
{"id":"chatcmpl-71b1myp21cgVkbPorp2DC8Tmu3PAY","object":"chat.completion.chunk","created":1680615018,"model":"gpt-3.5-turbo-0301","choices":[
{"delta":{"content":"."}
.............................................................................................................................................
.........................^
 2023-04-04 13:30:46 INFO  - DATA: ,"index":0,"finish_reason":null}]}

data: {"id":"chatcmpl-71b1myp21cgVkbPorp2DC8Tmu3PAY","object":"chat.completion.chunk","created":1680615018,"model":"gpt-3.5-turbo-0301","choi
ces":[{"delta":{"content":" This"},"index":0,"finish_reason":null}]}




,"index":0,"finish_reason":null}]}

	 */

	// the JSON can be split across "data" lines.
	// how to buffer and recombine into valid json data?
	
	
	class PostQueue {
		
		List<PostCallable> postList = []
		
		PostQueue() {
		
			TimerTask cleanTask = new TimerTask() {
				
				public void run() {
				
					log.info( "Running Post Clean Task Check." )
					
					List<PostCallable> removeList = []
					
					for(p in postList) {
						
						if(p.future.isCancelled()) {
							
							removeList.add(p)
							
						}
						
						if(p.future.isDone()) {
							
							removeList.add(p)
							
						}
					}
					
					for(r in removeList) {
						
						postList.remove(r)
					}		
				}
			}
			
			Timer cleanTimer = new Timer("Post Clean Timer");
			
			long cleanTime = 10_000L
				
			cleanTimer.schedule(cleanTask, 0, cleanTime)
		}
		
		public void addPost(PostCallable post) {
			
			postList.add(post)
			
		}
		
		public PostStreamingStatus stopPost(String requestIdentifier) {
			
			for(p in postList) {
				
				String id = p.requestIdentifier
				
				log.info("Compare: ${requestIdentifier} to ${id}")
				
				if(id == requestIdentifier) {
					
					PostStreamingStatus postStatus = p.getStatus()
					
					p.stopPost()
					
					return postStatus
				}
			}
			
			return null
			
		}
	}

	class PostCallable implements Callable {

		String requestIdentifier = null
		
		CloseableHttpClient httpclient = null
		
		HttpPost httppost = null
		
		StreamResponseHandler handler = null
		
		Future<String> future = null
		
		boolean interrupted = false
		
		PostCallable(String requestIdentifier, CloseableHttpClient httpclient, HttpPost httppost, StreamResponseHandler handler) {
			this.requestIdentifier = requestIdentifier
			this.httpclient = httpclient
			this.httppost = httppost
			this.handler = handler
		}
		
		PostStreamingStatus taskPostStatus = new PostStreamingStatus()
		
		public PostStreamingStatus getStatus() {
			
			return taskPostStatus	
		}
		
		public void stopPost() {
			
			log.info("Attempting Cancel of: " + requestIdentifier) 
			
			
			synchronized(this) {
				
				interrupted = true
				
			}
			
			
			if(future != null) {
			
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
		
		@Override
		public Object call() throws Exception {
			
					try {
										
						httpclient.execute(httppost, new ResponseHandler<Void>() {
							
							@Override
							public Void handleResponse(final HttpResponse response) throws IOException {
								
								int statusCode = response.getStatusLine().getStatusCode()
								
								if (statusCode >= 200 && statusCode < 300) {
									
									HttpEntity entity = response.getEntity()
									
									if (entity != null) {
										
										try {
											
											InputStream inputStream = entity.getContent()
											
											byte[] buffer = new byte[1024]
											
											int bytesRead
											
											// TODO change to keep reading until newline on separate line?
											// fully support Server Sent Events
											
											String chunkBuffer = ""
											
											while ((bytesRead = inputStream.read(buffer)) != -1) {
												
												String newChunkBuffer = new String(buffer, 0, bytesRead, StandardCharsets.UTF_8)
												
												if(!interrupted) {
												
													log.info("DATA: " + newChunkBuffer)
												} else {
													
													log.info("DATA INTERRUPTED: " + newChunkBuffer)
												}
												
												// DATA: data: {"id":"chatcmpl-6xJrz7Z6nKtEoNeiUEcHjxb61weK2","object":"chat.completion.chunk","created":1679595751,"model":"gpt-4-0314","choices":[{"delta":{"content":"."},"index":0,"finish_reason":null}]}

												boolean processBuffer = true
												
												chunkBuffer = chunkBuffer + newChunkBuffer
												
												if(newChunkBuffer.endsWith("\n")) {
													
													log.info("Chunk Buffer ends with newline: " + chunkBuffer)
														
													processBuffer = true
														
												}
												else {
													
													// check for this case to confirm we should add the next chunk to this before trying to parse
													
													log.error("Chunk Buffer DOES NOT end with newline: " + newChunkBuffer)
													
													processBuffer = false
												}
												
												// don't process when the line is split
												if(!processBuffer) {
													
													log.info("Not processing event yet.  Chunk Buffer DOES NOT end with newline: " + chunkBuffer)
													
													continue
												}
												
												List<String> chunkList = chunkBuffer.split("\n")
												
												// reset for next time
												chunkBuffer = ""
												
												List<String> adjChunkList = []
												
												// skip empty lines
												for(s in chunkList) {
													
													if(s != "" && s != "\n") {
							
														adjChunkList.add(s)
													}
												}
																
												chunkList = adjChunkList
												
												log.info("ChunkLineCount: " + chunkList.size())
												
												for(String chunk in chunkList) {
												
												if(chunk.startsWith("data: ")) {
													
													if(chunk == "data: [DONE]") {
															
													}
													else {
																												
														String data = chunk.replaceFirst("data: ", "")
														
														try {
														
															Map result = parser.parse(data.toCharArray())

															// "choices":[{"delta":{"content":"."}

															List choices = result["choices"]

															if(choices != null && choices.size()> 0) {

																Map choice = choices[0]

																Map delta = choice.delta

																if(delta != null) {

																	String content = delta.content

																	if(content != null && content != "") {

																		taskPostStatus.completeContent.append(content)
																		
																		taskPostStatus.lastUpdateTime = new Date()
																		
																		// log.info("Appending: " + content)
																		
																		// log.info( "ResultMap: " + result)
																		
																		try {
																			
																			if(!interrupted) {
																																		
																				handler.handleStreamResponse(result)
																			
																			}
																			
																																		
																		} catch(Exception ex) {
																																		
																		log.error("Exception handling data: " + ex.localizedMessage)
																																			
																		}
																	}
																}
															}

														} catch(Exception ex) {
															
															log.error("Exception parsing data map: " + ex.localizedMessage)
															log.error("Exception parsing data: " + data)
															
														}
													}
												}
											}
											}
											
											
										} catch(Exception ex) {
											
											if(interrupted) {
												log.info("Interrupted (Expected): Exception reading from stream: " + ex.localizedMessage)
											} else {
											
												log.error("Exception reading from stream: " + ex.localizedMessage)
											}
											
											// ex.printStackTrace()
										}
									
									}
								} else {
									
									// log error?
									
									if (statusCode == 429) {
										
										log.error("Rate limit Error")
										
										throw new RateLimitException("Rate Limit Error. Status code: " + 429)
									}
									
									throw new ClientProtocolException("Unexpected status code: " + statusCode)
								}
								return null;
							}
						})
						
						taskPostStatus.status = "Ok"
						
						return taskPostStatus
						
					} catch(RateLimitException) {
						
						PostStreamingStatus taskPostStatus = new PostStreamingStatus()
						
						taskPostStatus.statusMessage = "Rate Limit Error."
						
						taskPostStatus.errorCode = 429
						
						return taskPostStatus
							
					} catch(Exception ex) {
						
						log.error("Exception during POST cleanup: " + ex.localizedMessage )
					}
					
					PostStreamingStatus taskPostStatus = new PostStreamingStatus()
					
					taskPostStatus.status = "Error during request."
					
					taskPostStatus.errorCode = 1
					
					return taskPostStatus
				}
		
		
		
	}
	
	
		
	PostStreamingStatus execPost(String requestIdentifier, CloseableHttpClient httpclient, HttpPost httppost, StreamResponseHandler handler, Integer timeout_ms) {
		
		// by default assume error
		// gets replaced if successful
		PostStreamingStatus execPostStatus = new PostStreamingStatus()
		
		execPostStatus.status = "Error"
		
		try {
		
			// Potentially used shared thread pool instead of one at a time
			ExecutorService executorService = Executors.newFixedThreadPool(1)
		
			def r = new PostCallable(requestIdentifier, httpclient, httppost, handler)
		
			postQueue.addPost(r)
			
			// start the post
			Future<String> future  = executorService.submit( r )
		
			r.future = future
			
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
	
			TimerTask progressTask = new TimerTask() {
				
				public void run() {
				
					log.error( "Running Progress Check." )
					
					if(!future.isDone()) {
						
						log.error( "Task is not done. Checking progress.")
						
						boolean progress = true
						
						PostStreamingStatus postStatus = r.getStatus()
						
						Date now = new Date()
						
						Date lastTime = postStatus.lastUpdateTime
						
						long delta_ms = now.getTime() - lastTime.getTime()
						
						if(delta_ms > 15_000) {
							
							progress = false
							
						}
						
						if(!progress) {
						
							future.cancel(true)
						
							// cleanup
						
							try {
						
								log.error( "starting cleanup after progress check timeout..." )
						
							} catch( Exception cleanupException) {
									
								log.error("Exception during progress check cleanup: " + cleanupException.localizedMessage )
							
								// cleanupException.printStackTrace()
							}
						}
					}
					else {
						
						// println "Task is already complete."
					}
				}
			}
			
			Timer progressTimer = new Timer("Progress Interruption Timer");
			
			long progressTime = 10_000L // 60000L
				
			progressTimer.schedule(progressTask, 0, progressTime)
				
			try {
				
				// wait here until the Post completes or the timer interrupts it
				execPostStatus = future.get()
					
				if(execPostStatus.status == "Ok") {
													
				}
					
				if(execPostStatus.status == "Error") {
					
					log.error( "Task had an internal error or an internal timeout." )
					
					log.error( "Canceling interrupt timer: " + task.cancel() )
					
					// execPostStatus.statusMessage // should have a value
					// execPostStatus.errorCode // may have a value
					
				}
						
			} catch(Exception futureException) {
										
				log.error("The task may have been interrupted. Exception during future get: " + futureException.localizedMessage )
					
				execPostStatus.statusMessage = "Exception during model request."
				
				if(r.interrupted == true) {
					
					log.error("The task was interrupted by the user." )
					
					// Treat as not an error
					execPostStatus = r.getStatus()
					
					execPostStatus.status = "Ok"
					
					execPostStatus.statusMessage = "Task was interrupted by user."
				}
								
				// futureException.printStackTrace()
			}
			
			
			
			try {
					
				progressTimer.cancel() // kill progress checker
				
				timer.cancel() // need to kill time out thread
					
				executorService.shutdown() // need to close threads
				
				executorService.awaitTermination(3, TimeUnit.SECONDS)
				
				executorService.shutdownNow();
					
			} catch(Exception serviceException) {
						
				log.error("Exception during timer and executive service shutdown in status check: " + serviceException.localizedMessage )
						
				execPostStatus.statusMessage = "Exception during timer shutdown."
				
				// serviceException.printStackTrace()
			}

		} catch(Exception ex) {
			
			log.error("Unhandled Exception during status check: " + ex.localizedMessage )
			
			execPostStatus.statusMessage = "Unhandled Exception during status check."
		}
		
		// returns error case with null response if error
		return execPostStatus
	}
	
}