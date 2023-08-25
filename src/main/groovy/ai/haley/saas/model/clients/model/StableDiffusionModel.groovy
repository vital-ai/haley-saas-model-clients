package ai.haley.saas.model.clients.model

import ai.haley.saas.model.clients.replicate.ReplicateRequest

class StableDiffusionModel extends AbstractReplicateModel {

	static String modelName = "stability-ai/stable-diffusion"
	
	static String modelVersion = "0827b64897df7b6e8c04625167bbb275b9db0f14ab09e2454b9824141963c966"
	
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
		p["negative_prompt"] = String.class
		p["width"] = Integer.class
		p["height"] = Integer.class
		p["prompt_strength"] = Double.class
		p["num_outputs"] = Integer.class
		p["num_inference_steps"] = Integer.class
		p["guidance_scale"] = Double.class
		p["scheduler"] = String.class
		p["seed"] = Integer.class
		
		return p
		
		
	}
	
	public ReplicateRequest generatePredictionRequest(
		String prompt,
		String negative_prompt,
		Integer width,
		Integer height,
		Double prompt_strength,
		Integer numOutputs,
		Integer numInferenceSteps,
		Double guidanceScale,
		String scheduler,
		Integer seed = null) {
		
		// TODO check for allowed values
		
		Map m = [:]
		
		m["prompt"] = prompt
		m["negative_prompt"] = negative_prompt
		m["width"] = width
		m["height"] = height
		m["prompt_strength"] = prompt_strength
		m["num_outputs"] = numOutputs
		m["num_inference_steps"] = numInferenceSteps
		m["guidance_scale"] = guidanceScale
		m["scheduler"] = scheduler
		m["seed"] = seed
		
		
		
		return generatePredictionRequestJSON(m)
		
		
	}
	
	
	
	public static String handleCallback(String webhookJSON) {
		
		
		
		
		
	}
	
	
	
	
	
}
