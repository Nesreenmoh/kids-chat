package com.education.kids_chat.services;


import com.azure.ai.openai.OpenAIClient;
import com.azure.ai.openai.OpenAIClientBuilder;
import com.azure.ai.openai.models.*;
import com.azure.core.credential.AzureKeyCredential;
import com.education.kids_chat.models.Request;
import com.education.kids_chat.models.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChatService {

    private final static Logger LOGGER = LoggerFactory.getLogger(ChatService.class);
    private final String OPEN_AI_ENDPOINT;
    private final String OPEN_AI_KEY;
    private final String DEPLOY_NAME = "gpt-5.2-chat";
    private final String SYS_MSG = "Youare a child-safe educational assistant.\n" +
            "You must:\n" +
            "- Use simplelanguage (age7–12)\n" +
            "- Avoidsensitive topics\n" +
            "- Never provide medical, legal,or unsafe advice\n" +
            "- Never assume facts youare unsure about\n" +
            "If youarenot sure, say \"I don't know yet.\"";

    public ChatService(@Value("${azure.open.ai.endpoint:}") String OPEN_AI_ENDPOINT, @Value("${azure.open.ai.key:}") String OPEN_AI_KEY) {
        this.OPEN_AI_ENDPOINT = OPEN_AI_ENDPOINT;
        this.OPEN_AI_KEY = OPEN_AI_KEY;
    }


    public Response handelChat(Request request) {

      /*
      Create Open AI Chat
       */
        OpenAIClient openAIClient = new OpenAIClientBuilder()
                .credential(new AzureKeyCredential(OPEN_AI_KEY))
                .endpoint(OPEN_AI_ENDPOINT)
                .buildClient();


        List<ChatRequestMessage> chatMessages = List.of(
                new ChatRequestSystemMessage(SYS_MSG),
                new ChatRequestUserMessage(request.question()));


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
                .build();
    }


}
