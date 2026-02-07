package com.education.kids_chat.clients;


import com.azure.ai.openai.OpenAIClient;
import com.azure.ai.openai.OpenAIClientBuilder;
import com.azure.ai.openai.models.*;
import com.azure.core.credential.AzureKeyCredential;
import com.education.kids_chat.enums.ResponseMode;
import com.education.kids_chat.models.AiResponse;
import com.education.kids_chat.models.KnowledgeChunk;
import com.education.kids_chat.models.Token;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.education.kids_chat.utils.Helper.DEPLOYMENT_NAME;

/*
 Check if the question/content has HIGH/MEDIUM
 self-harm
 violence
 hate
 sexual
 Refuse to answer
 */
@Service
public class AzureOpenAiClient {


    private final OpenAIClient openAIClient;


    public AzureOpenAiClient(@Value("${azure.open.ai.endpoint:}") String OPEN_AI_ENDPOINT, @Value("${azure.open.ai.key:}") String OPEN_AI_KEY) {
        this.openAIClient = new OpenAIClientBuilder()
                .credential(new AzureKeyCredential(OPEN_AI_KEY))
                .endpoint(OPEN_AI_ENDPOINT)
                .buildClient();
    }

    public AiResponse generateGPTResponse(String userPrompt, String systemPrompt, ResponseMode responseMode) {

        ChatCompletionsOptions options = new ChatCompletionsOptions(List.of(
                new ChatRequestSystemMessage(systemPrompt),
                new ChatRequestUserMessage(userPrompt)
        ));

        options.setN(1);
        options.setMaxCompletionTokens(1600);

        ChatCompletions completions = this.openAIClient.getChatCompletions(DEPLOYMENT_NAME, options);

        ChatChoice chatChoice = completions.getChoices().get(0);
        String content = chatChoice.getMessage().getContent();
        CompletionsUsage completionsUsage = completions.getUsage();

        return AiResponse
                .builder()
                .answer(content)
                .responseMode(responseMode)
                .token(new Token(completionsUsage.getPromptTokens(), completionsUsage.getCompletionTokens(), completionsUsage.getTotalTokens()))
                .build();
    }


    public AiResponse generateGroundedAnswer(String sysPrompt, String groundedPrompt) {


        ChatCompletionsOptions options = new ChatCompletionsOptions(List.of(
                new ChatRequestSystemMessage(sysPrompt),
                new ChatRequestUserMessage(groundedPrompt)
        ));

        ChatCompletions completions = openAIClient.getChatCompletions(DEPLOYMENT_NAME, options);
        options.setN(2);
        ChatChoice chatChoice = completions.getChoices().get(0);
        String content = chatChoice.getMessage().getContent();
        CompletionsUsage completionsUsage = completions.getUsage();

        return AiResponse
                .builder()
                .answer(content)
                .responseMode(ResponseMode.NORMAL)
                .token(new Token(completionsUsage.getPromptTokens(), completionsUsage.getCompletionTokens(), completionsUsage.getTotalTokens()))
                .build();

    }
}
