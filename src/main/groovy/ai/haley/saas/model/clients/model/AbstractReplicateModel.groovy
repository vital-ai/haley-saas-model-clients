package ai.haley.saas.model.clients.model

import ai.haley.saas.model.clients.replicate.ReplicateRequest
import groovy.json.JsonOutput

abstract class AbstractReplicateModel {
	
	abstract String getModelName()
	
	abstract String getModelVersion()
	
	abstract Map<String,Object> getParameterMap()
	
	// this is overridden for more precise validation
	public ReplicateRequest  generatePredictionRequestJSON(Map parameters) {
		
		Map<String,Object> paramMap = getParameterMap()
		
		String mapJSON = JsonOutput.toJson(parameters)
		
		// TODO compare parameter map with incoming parameters for basic validation
		
		ReplicateRequest result = new ReplicateRequest( getModelName(),  getModelVersion(), mapJSON)
		
		return result

	}
		
	
}
