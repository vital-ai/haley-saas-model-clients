package ai.haley.saas.model.clients.main


import ai.haley.saas.model.clients.model.BlipModel
import ai.haley.saas.model.clients.model.YoadtewImageToTextModel
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

class ReplicateImageToTextMain extends groovy.lang.Script {

	String apiKey 

	static void main(args) {
		
		
		BasicConfigurator.configure()
		
		ReplicateImageToTextMain app = new ReplicateImageToTextMain()
		
		app.run()
	}

	@Override
	public Object run() {
		
		println "Replicate Client Main"
		
		String configFile = "./config/replicateClient.conf"
		
		Config conf = ConfigFactory.parseFileAnySyntax(new File(configFile))
		
		apiKey = conf.getString("apiKey")
		
		// YoadtewImageToTextModel modelClass = new YoadtewImageToTextModel()
		
		
		BlipModel modelClass = new BlipModel()
		
		String modelName = modelClass.getModelName()
		
		String modelVersion = modelClass.getModelVersion()
		
		ReplicateJavaClient modelClient = new ReplicateJavaClient(apiKey, modelName, modelVersion)
	
		String filePath = "/Users/hadfield/Desktop/"
		
		String imageFileName = "81Aja2LX59L._AC_UL320_.jpg"
	
		/*
		File file = new File(filePath + imageFileName)
		
		FileInputStream fis = new FileInputStream(file)
		ByteArrayOutputStream baos = new ByteArrayOutputStream()
		
		byte[] buffer = new byte[1024]
		int length
		while ((length = fis.read(buffer)) != -1) {
			baos.write(buffer, 0, length)
		}
		fis.close()
		
		byte[] imageBytes = baos.toByteArray()

		// Encode the image bytes as base64
		String base64Image = Base64.getEncoder().encodeToString(imageBytes);
		*/
		
		/*
		String fileContent = "https://res.cloudinary.com/dygasnscd/image/upload/v1673188818/81Aja2LX59L._AC_UL320_.jpg"
		String condText = "Include a description of the colors, image of a"
		Integer beamSize = 5
		Double endFactor = 1.01
		Integer maxSeqLength = 15
		Double ceLossScale = 0.2
		*/
		
		
		/*
		ReplicateRequest request = modelClass.generatePredictionRequest(
			fileContent,
			condText,
			beamSize,
			endFactor,
			maxSeqLength,
			ceLossScale)
		*/
		
		String imageURL = "https://res.cloudinary.com/dygasnscd/image/upload/v1673188818/81Aja2LX59L._AC_UL320_.jpg"
		
		String task = "image_captioning"
		String question = ""
		String caption = ""
		
		ReplicateRequest request = modelClass.generatePredictionRequest(
			imageURL,
			task,
			question,
			caption)
		
		
		String requestJSON = request.requestJSON

		String responseJSON = modelClient.generatePredictionPoll(requestJSON, 60_000)

		if(responseJSON == null) {
	
			println "Timeout or other error."
	
			System.exit(1)
	
		}
		
		
		String prettyJSON = JsonOutput.prettyPrint(responseJSON)
		
		println "Output:\n" + prettyJSON
		
		
		
		
	}
}
