package ai.vital.openai.api

import org.apache.http.client.methods.CloseableHttpResponse

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