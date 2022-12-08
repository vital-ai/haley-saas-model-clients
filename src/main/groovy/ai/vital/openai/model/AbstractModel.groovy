package ai.vital.openai.model

import ai.vital.openai.api.ModelRequest
import groovy.json.JsonOutput

abstract class AbstractModel {
		
	abstract public String getModelName()
	
	abstract public String getModelVersion()
	
	abstract Map<String,Object> getParameterMap()
	
	abstract public <M extends ModelRequest> M  generatePredictionRequest(Map parameters) 
	
}
