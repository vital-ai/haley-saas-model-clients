package ai.haley.saas.model.clients.model

import ai.haley.saas.model.clients.replicate.ReplicateRequest

class ArcherDiffusionModel extends AbstractReplicateModel {

	static String modelName = "nitrosocke/archer-diffusion"
	
	static String modelVersion = "ebcc7e582fbb09930e34d9bc2ab552d37411ee982585257c0a7c5fcdc05029c2"
	
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
		p["width"] = Integer.class
		p["height"] = Integer.class
		p["num_outputs"] = Integer.class
		p["num_inference_steps"] = Integer.class
		p["guidance_scale"] = Double.class
		p["seed"] = Integer.class
		
		return p
		
		
	}
	
	public ReplicateRequest generatePredictionRequest(
		String prompt,
		Integer width,
		Integer height,
		Integer numOutputs,
		Integer numInferenceSteps,
		Double guidanceScale,
		Integer seed = null) {
		
		// TODO check for allowed values
		
		Map m = [:]
		
		m["prompt"] = prompt
		m["width"] = width
		m["height"] = height
		m["num_outputs"] = numOutputs
		m["num_inference_steps"] = numInferenceSteps
		m["guidance_scale"] = guidanceScale
		m["seed"] = seed
		
		
		
		return generatePredictionRequestJSON(m)
		
		
	}
	
	
	
	public static String handleCallback(String webhookJSON) {
		
		
		
		
		
	}
	
	
	
}
