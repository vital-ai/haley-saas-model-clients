package ai.haley.saas.model.clients.model


import ai.haley.saas.model.clients.replicate.ReplicateRequest
import groovy.json.JsonOutput


class YoadtewImageToTextModel extends AbstractReplicateModel {
	
	static String modelName = "yoadtew/zero-shot-image-to-text"
	
	static String modelVersion = "7f2735bab48ff6caa414a3fff239b0d5de77a97f1791dcb7e0eb17c259aa11be"
	
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
		
		p["cond_text"] = String.class
		
		p["beam_size"] = Integer.class
		
		p["end_factor"] = Double.class
		
		p["max_seq_length"] = Integer.class
		
		p["ce_loss_scale"] = Double.class
		
		
		
		return p
	}
	
	
	public ReplicateRequest generatePredictionRequest(
		String fileContent,
		String condText,
		Integer beamSize,
		Double endFactor,
		Integer maxSeqLength,
		Double ceLossScale) {
		
		// TODO check for allowed values
		
		Map m = [:]
		
		m["image"] = fileContent
		m["cond_text"] = condText
		m["beam_size"] = beamSize
		m["end_factor"] = endFactor
		m["max_seq_length"] = maxSeqLength
		m["ce_loss_scale"] = ceLossScale
		
		return generatePredictionRequestJSON(m)
			
	}
	
	
	
	public static String handleCallback(String webhookJSON) {
		
		
		
		
		
	}
	
	
}
