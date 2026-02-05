package com.education.kids_chat.services;


import com.azure.ai.openai.OpenAIClient;
import com.azure.ai.openai.OpenAIClientBuilder;
import com.azure.ai.openai.models.ChatCompletions;
import com.azure.ai.openai.models.ChatCompletionsOptions;
import com.azure.ai.openai.models.ChatRequestMessage;
import com.azure.ai.openai.models.ChatRequestSystemMessage;
import com.azure.core.credential.AzureKeyCredential;
import com.education.kids_chat.models.BullyingResponse;
import com.education.kids_chat.models.Request;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.education.kids_chat.utils.Helper.BULLING_SYSTEM_PROMPT_MSG;
import static com.education.kids_chat.utils.Helper.BULLYING_DEPLOYMENT_NAME;

@Service
public class BullyingDetectionService {


    public final Logger logger = LoggerFactory.getLogger(BullyingDetectionService.class);

    private final String BULLYING_ENDPOINT;
    private final String BULLYING_APIKEY;


    public BullyingDetectionService(@Value("${azure.bullying.endpoint:}") String BULLYING_ENDPOINT, @Value("${azure.bullying.api.key}") String BULLYING_APIKEY) {
        this.BULLYING_ENDPOINT = BULLYING_ENDPOINT;
        this.BULLYING_APIKEY = BULLYING_APIKEY;
    }


    public BullyingResponse handelBullying(Request request) {

        /*
       Define Bullying Model client
         */
        OpenAIClient client = new OpenAIClientBuilder()
                .credential(new AzureKeyCredential(BULLYING_APIKEY))
                .endpoint(BULLYING_ENDPOINT)
                .buildClient();

        /*
        Instruct the system prompt
         */
        List<ChatRequestMessage> messages = List.of(
                new ChatRequestSystemMessage(BULLING_SYSTEM_PROMPT_MSG.formatted(request.question()))
        );

        ChatCompletionsOptions options = new ChatCompletionsOptions(messages);

        /*
        Connect to GPT4.1-mini - cheap GPT Model
         */
        ChatCompletions chatCompletions = client.getChatCompletions(BULLYING_DEPLOYMENT_NAME, options);
        /*
        extract the content of the result
         */
        BullyingResponse result= extractBullyingResponse(chatCompletions.getChoices().get(0).getMessage().getContent());
        return result;

    }

    private BullyingResponse extractBullyingResponse(String content) {
        ObjectMapper mapper = new ObjectMapper();
        BullyingResponse bullyingResponse;
        try {
            bullyingResponse= mapper.readValue(content,BullyingResponse.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Cannot deserialize BullyingResponse", e);
        }
        return BullyingResponse.builder()
                .category(bullyingResponse.category())
                .confidence(bullyingResponse.confidence())
                .message(bullyingResponse.message())
                .bullyingDetected(bullyingResponse.bullyingDetected())
                .build();

    }

}
