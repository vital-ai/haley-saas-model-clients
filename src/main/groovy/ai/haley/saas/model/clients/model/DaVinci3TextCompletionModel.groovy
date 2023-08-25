package ai.haley.saas.model.clients.model

import ai.haley.saas.model.clients.api.ModelRequest
import ai.haley.saas.model.clients.api.TextCompletionRequest

class DaVinci3TextCompletionModel extends AbstractTextCompetionModel {

	static String modelName = "text-davinci"
	
	static String modelVersion = "003"
	
	@Override
	public String getModelName() {
		
		return modelName
	}

	@Override
	public String getModelVersion() {
		
		return modelVersion
	}
	
	@Override
	public Map<String, Object> getParameterMap() {
		
		Map p = [:]
		
		p["prompt"] = String.class
		p["suffix"] = String.class
		p["temperature"] = Double.class
		p["max_tokens"] = Integer.class
		p["n"] = Integer.class
		p["stream"] = Boolean.class
		p["logprobs"] = Integer.class
		p["echo"] = Boolean.class
		p["stop"] = ArrayList.class
		p["best_of"] = Integer.class
		p["logit_bias"] = HashMap.class
		p["top_p"] = Double.class
		p["frequency_penalty"] = Double.class
		p["presence_penalty"] = Double.class
		p["user"] = String.class
		
		return p
	}

	@Override
	public TextCompletionRequest generatePredictionRequest(Map parameters) {
		
		TextCompletionRequest request = new TextCompletionRequest()
		
		request.modelName = modelName
		
		request.modelVersion = modelVersion
		
		String modelString = "${modelName}-${modelVersion}"
		
		parameters["model"] = modelString
		
		request.parameterMap = parameters
		
		return request
	}
	
	public TextCompletionRequest generatePredictionRequest(
		String prompt,
		Double temperature,
		Integer maxTokens) {
		
		
		Map modelParameters = [:]
		
		modelParameters["prompt"] = prompt
		modelParameters["temperature"] = temperature
		modelParameters["max_tokens"] = maxTokens
				
		
		// defaults
		modelParameters["top_p"] = 1.0d
		modelParameters["frequency_penalty"] = 0.0d
		modelParameters["presence_penalty"] = 0.0d
		
		return generatePredictionRequest(modelParameters)
	}

}
