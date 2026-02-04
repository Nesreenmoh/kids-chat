package com.education.kids_chat.services;


import com.azure.ai.openai.OpenAIClient;
import com.azure.ai.openai.OpenAIClientBuilder;
import com.azure.ai.openai.models.ChatCompletions;
import com.azure.ai.openai.models.ChatCompletionsOptions;
import com.azure.ai.openai.models.ChatRequestMessage;
import com.azure.ai.openai.models.ChatRequestSystemMessage;
import com.azure.core.credential.AzureKeyCredential;
import com.education.kids_chat.models.Request;
import com.education.kids_chat.models.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BullyingDetectionService {


    public final Logger logger = LoggerFactory.getLogger(BullyingDetectionService.class);

    private final String BULLYING_ENDPOINT;
    private final String BULLYING_APIKEY;
    private final String DEPLOY_NAME = "gpt-4.1-mini";

    public BullyingDetectionService(@Value("${azure.bullying.endpoint:}") String BULLYING_ENDPOINT, @Value("${azure.bullying.api.key}") String BULLYING_APIKEY) {
        this.BULLYING_ENDPOINT = BULLYING_ENDPOINT;
        this.BULLYING_APIKEY = BULLYING_APIKEY;
    }


    public Response handelBullying(Request request) {

        String prompt = """
                
                Classify the following message for bullying or emotional harm.
                
                Return JSON only:
                {
                  "message": "<echo the original message>",
                  "bullyingDetected": true/false,
                  "category": "NONE" | "MILD" | "MODERATE" | "HIGH",
                  "confidence": number
                }
                
                Message: "%s"   
                """.formatted(request.question());


        /*
       Define Bullying Model client
         */
        OpenAIClient client = new OpenAIClientBuilder()
                .credential(new AzureKeyCredential(BULLYING_APIKEY))
                .endpoint(BULLYING_ENDPOINT)
                .buildClient();

        List<ChatRequestMessage> messages = List.of(
                new ChatRequestSystemMessage(prompt)
        );

        ChatCompletionsOptions options = new ChatCompletionsOptions(messages);

        ChatCompletions chatCompletions = client.getChatCompletions(DEPLOY_NAME, options);
        return Response
                .builder()
                .answer(chatCompletions.getChoices().get(0).getMessage().getContent())
                .promptToken(chatCompletions.getUsage().getPromptTokens())
                .completionToken(chatCompletions.getUsage().getCompletionTokens())
                .totalToken(chatCompletions.getUsage().getTotalTokens())
                .build();

    }

}
