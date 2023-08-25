package ai.haley.saas.model.clients.model

import ai.haley.saas.model.clients.replicate.ReplicateRequest
import groovy.json.JsonOutput

class BlipModel  extends AbstractReplicateModel {
	
	static String modelName = "salesforce/blip"
	
	static String modelVersion = "2e1dddc8621f72155f24cf2e0adbde548458d3cab9f00c0139eea840d0ac4746"
	
	@Override
	public String getModelName() {
		
		return modelName		
	}
	
	@Override
	public String getModelVersion() {
		
		return modelVersion
	}
	
	
	
	
	@Override
	public Map<String,Object> getParameterMap() {
		
		Map p = [:]
		
		p["image"] = String.class
		
		p["task"] = String.class
		
		p["question"] = String.class
		
		p["caption"] = String.class
		
		return p
	}
	
	
	public ReplicateRequest generatePredictionRequest(
		String imageURL,
		String task,
		String question,
		String caption) {
		
		// TODO check for allowed values
		
		Map m = [:]
		
		m["image"] = imageURL
		m["task"] = task
		m["question"] = question
		m["caption"] = caption
		
		return generatePredictionRequestJSON(m)
			
	}
	
	
	
	public static String handleCallback(String webhookJSON) {
		
		
		
		
		
	}
}
