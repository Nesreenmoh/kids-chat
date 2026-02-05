package com.education.kids_chat.services;


import com.azure.ai.openai.OpenAIClient;
import com.azure.ai.openai.OpenAIClientBuilder;
import com.azure.ai.openai.models.*;
import com.azure.core.credential.AzureKeyCredential;
import com.education.kids_chat.enums.BullingCategory;
import com.education.kids_chat.enums.ResponseMode;
import com.education.kids_chat.models.BullyingResponse;
import com.education.kids_chat.models.Request;
import com.education.kids_chat.models.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChatService {

    @Autowired
    ContentSafetyService contentSafetyService;

    @Autowired
    BullyingDetectionService  bullyingDetectionService;

    private final static Logger LOGGER = LoggerFactory.getLogger(ChatService.class);
    private final String OPEN_AI_ENDPOINT;
    private final String OPEN_AI_KEY;
    private final String DEPLOY_NAME = "gpt-5.2-chat";
    private final String SYS_PROMPT_NORMAL_MSG = "You are a child-safe educational assistant.\n" +
            "You must:\n" +
            "- Use simple language (age7–12)\n" +
            "- Avoid sensitive topics\n" +
            "- Never provide medical, legal,or unsafe advice\n" +
            "- Never assume facts you are unsure about\n" +
            "If you are not sure, say \"I don't know yet.\"";

    /*
    The behavior of GPT is intentional not accidental
     */
    private final String SYS_PROMPT_SUPPORTIVE_MSG = "You are a child-safe assistant.\n" +
            "You must:\n" +
            "-Response with empathy.\n" +
            "-Do not judge or blame\n" +
            "-Offer support and options\n" +
            "-Do not escalate or alarm\n" ;


    public ChatService(@Value("${azure.open.ai.endpoint:}") String OPEN_AI_ENDPOINT, @Value("${azure.open.ai.key:}") String OPEN_AI_KEY) {
        this.OPEN_AI_ENDPOINT = OPEN_AI_ENDPOINT;
        this.OPEN_AI_KEY = OPEN_AI_KEY;
    }


    public Response handelChat(Request request) {


        if (!contentSafetyService.contentSafetyCheck(request)) {
            return Response
                    .builder()
                    .answer("I cannot help you with that, but I am here if you want to talk about something else.")
                    .responseMode(ResponseMode.REFUSAL)
                    .totalToken(0)
                    .completionToken(0)
                    .promptToken(0)
                    .build();
        }

        /*
        Bullying Detection Check
         */

        BullyingResponse bullyingResult = bullyingDetectionService.handelBullying(request);
        ResponseMode responseMode;
        String  systemPrompt = switch(bullyingResult.category()){
            case BullingCategory.HIGH,BullingCategory.MODERATE ->{
                responseMode = ResponseMode.SUPPORTIVE;
               yield  SYS_PROMPT_SUPPORTIVE_MSG;
            }
            default ->  {
                responseMode = ResponseMode.CLARIFICATION;
                yield  SYS_PROMPT_NORMAL_MSG;
            }
        };


      /*
      Create Open AI Chat
       */
        OpenAIClient openAIClient = new OpenAIClientBuilder()
                .credential(new AzureKeyCredential(OPEN_AI_KEY))
                .endpoint(OPEN_AI_ENDPOINT)
                .buildClient();


        List<ChatRequestMessage> chatMessages = List.of(
                new ChatRequestSystemMessage(systemPrompt),
                new ChatRequestUserMessage(request.question()));

        LOGGER.info("System prompt {}",systemPrompt);
       /*
       define the Options
        */

        ChatCompletionsOptions options = new ChatCompletionsOptions(chatMessages);

       /*
       define chat completion to get the response
        */
        ChatCompletions chatCompletions = openAIClient.getChatCompletions(DEPLOY_NAME, options);
        return Response
                .builder()
                .answer(chatCompletions.getChoices().get(0).getMessage().getContent())
                .promptToken(chatCompletions.getUsage().getPromptTokens())
                .completionToken(chatCompletions.getUsage().getCompletionTokens())
                .totalToken(chatCompletions.getUsage().getTotalTokens())
                .responseMode(responseMode)
                .build();
    }


}
