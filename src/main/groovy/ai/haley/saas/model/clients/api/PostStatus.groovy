package ai.haley.saas.model.clients.api

import org.apache.http.client.methods.CloseableHttpResponse

// Utility class for capturing the results of a Post
// TODO potentially use something like this to return partial results
// for case of returning streaming results incrementally
class PostStatus {
	
	// TODO make into an enum?
	// "Ok" or "Error"
	String status = null
	
	CloseableHttpResponse httpResponse = null
}
