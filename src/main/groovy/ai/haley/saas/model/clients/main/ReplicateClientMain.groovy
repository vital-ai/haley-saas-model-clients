package ai.haley.saas.model.clients.main

import ai.haley.saas.model.clients.model.ArcherDiffusionModel
import ai.haley.saas.model.clients.model.OpenJourneyModel
import ai.haley.saas.model.clients.model.StableDiffusionModel
import ai.haley.saas.model.clients.replicate.ReplicateJavaClient
import ai.haley.saas.model.clients.replicate.ReplicateRequest

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import java.util.stream.Collectors
import org.apache.http.HttpResponse
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.HttpClients
import org.apache.log4j.BasicConfigurator

class ReplicateClientMain extends groovy.lang.Script {

	String apiKey 
	
	static void main(args) {
		
		// sets up logging to defaults
		BasicConfigurator.configure()
		
		ReplicateClientMain app = new ReplicateClientMain()
		
		app.run()
		
	}
	
	@Override
	public Object run() {
		
		println "Replicate Client Main"
		
		String configFile = "./config/replicateClient.conf"
		
		Config conf = ConfigFactory.parseFileAnySyntax(new File(configFile))
		
		apiKey = conf.getString("apiKey")
		
		// Test with Open Journey
			
		OpenJourneyModel modelClass = new OpenJourneyModel()
		
		String modelName = modelClass.getModelName()
		
		String modelVersion = modelClass.getModelVersion()
		
		ReplicateJavaClient modelClient = new ReplicateJavaClient(apiKey, modelName, modelVersion)
		
		// String prompt = "mdjrny-v4 style portrait of female dragon, intricate, elegant, highly detailed, digital painting, artstation, concept art, smooth, sharp focus, illustration, art by artgerm and greg rutkowski and alphonse mucha, 8k"
		
		// String prompt = "mdjrny-v4 cute man, black, fun, happy, wearing hoodie, Disney style, Disney illustration Disney princess"
		
		// String prompt = "mdjrny-v4 fantasypunk, pinkpunk huge hovering green dragon flies, iridiscent, sparkle, flow in the wind"
		
		// String prompt = "mdjrny-v4 super cute steampunk fox with astronaut helmet, flying through the sparkling milky way"
		
		String prompt = "mdjrny-v4 spectacular dark detailed alcohol ink luminist painting of sweeping vibrant terraced city streets"
		
		Integer width = 512
		
		Integer height = 512
		
		Integer numOutputs = 1
		
		Integer numInferenceSteps = 50
		
		Double guidanceScale = 7.0
		
		Integer seed = 500
		
		// ReplicaterRequest is returned so we keep model name, version, and request in one object
		
		ReplicateRequest request = modelClass.generatePredictionRequest(prompt, width, height, numOutputs, numInferenceSteps, guidanceScale, seed)
		
		String requestJSON = request.requestJSON
		
		String responseJSON = modelClient.generatePredictionPoll(requestJSON, 60_000)
		
		String prettyJSON = JsonOutput.prettyPrint(responseJSON)
		
		
		 
		
		
		// StableDiffusionModel modelClass2 = new StableDiffusionModel()
		/*
		ArcherDiffusionModel modelClass2 = new ArcherDiffusionModel()
		
		
		// String prompt2 = "an astronaut riding a horse on mars artstation, hd, dramatic lighting, detailed"
		
		String prompt2 = "an astronaut smiling and waving hello, archer style"
		
		
		String negative_prompt2 = null

		
		Integer width2 = 768
		
		Integer height2 = 768
		
		Integer numOutputs2 = 1
		
		Integer numInferenceSteps2 = 50
		
		Double prompt_strength2 = 0.8
		
		Double guidanceScale2 = 7.5
		
		String scheduler2 = "K_EULER"
		
		Integer seed2 = null



		// ReplicateRequest request2 = modelClass2.generatePredictionRequest(prompt2, negative_prompt2, width2, height2, prompt_strength2, numOutputs2, numInferenceSteps2, guidanceScale2, scheduler2)
		
		ReplicateRequest request2 = modelClass2.generatePredictionRequest(prompt2, width2, height2, numOutputs2, numInferenceSteps2, guidanceScale2)
		
		ReplicateJavaClient modelClient2 = new ReplicateJavaClient(apiKey, modelClass2.getModelName(), modelClass2.getModelVersion())
				
		String requestJSON2 = request2.requestJSON
		
		String responseJSON2 = modelClient2.generatePredictionPoll(requestJSON2, 60_000)
		
		if(responseJSON2 == null) {
			
			println "Timeout or other error."
			
			System.exit(1)
			
		}
		
	
		String prettyJSON = JsonOutput.prettyPrint(responseJSON2)

		*/
		
		
		println "Output:\n" + prettyJSON
		
		JsonSlurper parser = new JsonSlurper()
		
		Map responseMap = parser.parseText(prettyJSON)
		
		List<String> outputList = responseMap["output"]
		
		int count = 0
		
		for(f in outputList) {
			
			count++
			
			CloseableHttpClient httpclient = HttpClients.createDefault()
			
			HttpGet httpget = new HttpGet( f )
			
			HttpResponse httpresponse = httpclient.execute(httpget)
			
			InputStream inputStream = httpresponse.getEntity().getContent()
			
			byte[] buff = new byte[64*1024]
			
			BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream)
			
			byte[] fileBytes = null
			
			ByteArrayOutputStream baos = new ByteArrayOutputStream()
			
			int n = 0
			
			while ((n = bufferedInputStream.read(buff)) >= 0) {
				baos.write(buff, 0, n)
			}
			
			fileBytes =  baos.toByteArray()
			
			String fileName = "./outputImages/image${count}.png"
					
			File imageFile = new File(fileName)
			
			imageFile.bytes = fileBytes
			
		}
		
		
	}
	
	
}
