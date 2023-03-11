package ai.vital.openai.model

import ai.vital.openai.api.ChatRequest

class GPT35Turbo_0301ChatModel extends GPT35TurboChatModel{
	
	static String modelName = "gpt-3.5-turbo"
	
	static String modelVersion = "0301"
	
	@Override
	public String getModelName() {
		
		return modelName
	}

	@Override
	public String getModelVersion() {
		
		return modelVersion
	}
}
