package ai.haley.saas.model.clients.model

import ai.haley.saas.model.clients.api.ModelRequest
import groovy.json.JsonOutput

abstract class AbstractModel {
		
	abstract public String getModelName()
	
	abstract public String getModelVersion()
	
	public String getModelString() {
		
		String modelName = getModelName()
		
		String modelVersion = getModelVersion()
		
		String modelString
		
		if(modelVersion == null || modelVersion == "") {
			
			modelString = "${modelName}"
			
		}
		else {
			
			modelString = "${modelName}-${modelVersion}"
			
		}
			
		return modelString
		
	}
	
	
	abstract Map<String,Object> getParameterMap()
	
	abstract public <M extends ModelRequest> M  generatePredictionRequest(Map parameters) 
	
}
