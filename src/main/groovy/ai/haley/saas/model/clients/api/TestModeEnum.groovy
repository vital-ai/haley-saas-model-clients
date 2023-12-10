package ai.haley.saas.model.clients.api


enum TestModeEnum {
	
	// return an API Error
	API_ERROR,
	
	// API Request will be interrupted, 
	INTERRUPT,
	
	// No test, normal request
	NONE,
	
	// return a rate limited error without making an API request
	RATE_LIMIT,
	
	// return static content without making an API request
	STATIC_CONTENT,
	
	// trigger a timeout error without making an API request
	TIMEOUT
	
	
}
