package ai.haley.saas.model.clients.api

class Transcription {
	
	String inputFileIdentifier = null
	
	Integer splitNumber = 0
	
	Integer splitTotal = 0
	
	String audioTranscriptionText = null
	
	String languageIdentifier = "en"
	
	Double durationMilleseconds
	
}
