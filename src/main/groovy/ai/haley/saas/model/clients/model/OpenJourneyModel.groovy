package ai.haley.saas.model.clients.model

import ai.haley.saas.model.clients.replicate.ReplicateRequest
import groovy.json.JsonOutput

class OpenJourneyModel extends AbstractReplicateModel {
	
	static String modelName = "prompthero/openjourney"
	
	// static String modelVersion = "9936c2001faa2194a261c01381f90e65261879985476014a0a37a334593a05eb"
	
	static String modelVersion = "ad59ca21177f9e217b9075e7300cf6e14f7e5b4505b87b9689dbd866e9768969"
	
	@Override
	public String getModelName() {
		
		return modelName		
	}
	
	@Override
	public String getModelVersion() {
		
		return modelVersion
	}
	
	
	/*
	 prompt string

width integer
Width of output image. Maximum size is 1024x768 or 768x1024 because of memory limits
Allowed values:128, 256, 512, 768, 1024


height integer
Height of output image. Maximum size is 1024x768 or 768x1024 because of memory limits
Allowed values:128, 256, 512, 768, 1024


num_outputs integer
Number of images to output
Allowed values:1, 4

guidance_scale number
Scale for classifier-free guidance


seed integer
Random seed. Leave blank to randomize the seed

	 */
	
	@Override
	public Map<String,Object> getParameterMap() {
		
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
