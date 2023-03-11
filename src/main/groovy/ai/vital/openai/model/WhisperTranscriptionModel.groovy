package ai.vital.openai.model

import ai.vital.openai.api.ModelRequest
import ai.vital.openai.api.TranscriptionRequest

class WhisperTranscriptionModel extends AbstractTranscriptionModel {

	static String modelName = "whisper"
	
	static String modelVersion = "1"
	
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
		
		p["audioFileBytes"] = byte[].class

		p["audioFileFormat"] = String.class
		
		p["audioFileName"] = String.class
		
		return p
	}

	@Override
	public <M extends ModelRequest> M generatePredictionRequest(Map parameters) {
		
		TranscriptionRequest request = new TranscriptionRequest()
		
		request.modelName = modelName
		
		request.modelVersion = modelVersion
		
		String modelString = "${modelName}-${modelVersion}"
		
		parameters["model"] = modelString
		
		request.parameterMap = parameters
		
		return request		
	}
	
	
	public <M extends ModelRequest> M generatePredictionRequest(String fileName, String fileFormat, byte[] audioBytes) {
		
		Map v = [:]
		
		v["audioFileName"] = fileName
		
		v["audioFileFormat"] = fileFormat
		
		v["audioFileBytes"] = audioBytes
		
		return generatePredictionRequest(v) 
		
	}

}
