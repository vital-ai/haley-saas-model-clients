package ai.haley.saas.model.clients.api

enum CommandModeEnum {

	// Standard command functionality	
	STANDARD,
	
	// Standard but stateless, do not save any results
	EPHEMERAL,
	
	// test mode
	TEST,
	
	// test mode but stateless, do not save any results
	EPHEMERAL_TEST
	
	
}
