package ai.haley.saas.model.clients.replicate

class ReplicateRequest {
	
	public String modelName
	
	public String modelVersion
	
	public String requestJSON
	
	public ReplicateRequest(String modelName, String modelVersion, String requestJSON) {
		
		this.modelName = modelName
		
		this.modelVersion = modelVersion
		
		this.requestJSON = requestJSON
	
	}
		
}
