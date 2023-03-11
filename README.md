# openai-java-client

This is an early stage implementation of the OpenAI API to utilize OpenAI models.

Copy ```openaiClient.conf.template``` to ```openaiClient.conf``` into the config directory and set your API key.

To build:

```mvn package```

sample run with:

```./bin/runOpenAIClientMain.sh```

Output is printed out.  Generated images are put into the outputImages directory.

There is a build dependency on an older version of groovy related to the Eclipse groovy-compiler plugin used in the maven POM.  
The POM can be modified to switch to a more recent version of groovy, but the maven groovy plugins would need to be adjusted as well.  
This is based on an Eclipse IDE project with a groovy plugin that has specific requirements which should be upgraded eventually.

The runtime shell script uses the groovy jars in the "lib" directory.

The "main" class ```OpenAIClientMain``` provides an example use, such as specifying the prompt used.

A class is specified for each OpenAI model to help with data validation and composing requests.

The currently defined models are:
* text-davinci-002
* text-davinci-003
* gpt 35 chat turbo 0301
* transcription (whisper)

