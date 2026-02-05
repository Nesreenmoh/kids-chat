package com.education.kids_chat.services;


import com.azure.ai.openai.OpenAIClient;
import com.azure.ai.openai.OpenAIClientBuilder;
import com.azure.ai.openai.models.*;
import com.azure.core.credential.AzureKeyCredential;
import com.education.kids_chat.models.Response;
import com.education.kids_chat.models.Token;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AzureOpenAiClient {


    private final OpenAIClient openAIClient;
    private final String deploymentName = "gpt-5.2-chat";

    @Value("${azure.open.ai.endpoint:}")
    private  String OPEN_AI_ENDPOINT;

    @Value("${azure.open.ai.ai.key:}")
    private String OPEN_AI_KEY;

    public AzureOpenAiClient() {
        this.openAIClient = new OpenAIClientBuilder()
                .credential(new AzureKeyCredential(OPEN_AI_KEY))
                .endpoint(OPEN_AI_ENDPOINT)
                .buildClient();
    }

    public Response generateResponse(String userPrompt, String systemPrompt) {

        ChatCompletionsOptions options = new ChatCompletionsOptions(List.of(
                new ChatRequestSystemMessage(systemPrompt),
                new ChatRequestUserMessage(userPrompt)
        ));

        options.setN(1);
        options.setMaxTokens(1600);
        options.setTemperature(.5);

        ChatCompletions completions = this.openAIClient.getChatCompletions(deploymentName, options);

        ChatChoice chatChoice = completions.getChoices().get(0);
        String content = chatChoice.getMessage().getContent();
        CompletionsUsage completionsUsage = completions.getUsage();

        return Response
                .builder()
                .answer(content)
                .token(new Token(completionsUsage.getPromptTokens(), completionsUsage.getCompletionTokens(), completionsUsage.getTotalTokens()))
                .build();
    }
}
