package com.education.kids_chat.services;

import com.education.kids_chat.clients.AzureContentSafetyClient;
import com.education.kids_chat.clients.AzureOpenAiClient;
import com.education.kids_chat.enums.ConfidenceLevel;
import com.education.kids_chat.enums.ResponseMode;
import com.education.kids_chat.models.AiResponse;
import com.education.kids_chat.models.KnowledgeChunk;
import com.education.kids_chat.models.Request;
import com.education.kids_chat.models.Token;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.education.kids_chat.utils.Helper.RAG_SYS_PROMPT_MSG;
import static com.education.kids_chat.utils.Helper.groundedPrompt;

@Service
public class RagAnswerService {


    @Autowired
    InMemoryKnowledgeRetrievalService inMemoryKnowledgeRetrievalService;

    @Autowired
    AzureOpenAiClient azureOpenAiClient;

    @Autowired
    private AzureContentSafetyClient contentSafetyClient;

    AiResponse ragOriginalAiResponse;

    private final static Logger LOGGER = LoggerFactory.getLogger(RagAnswerService.class);

    public AiResponse generateAnswer(Request request) {
//        System.out.println(inMemoryKnowledgeRetrievalService.retrieveKnowledgeBlock(request));
        if(inMemoryKnowledgeRetrievalService.retrieveKnowledgeBlock(request).isEmpty()){
            return  AiResponse
                    .builder()
                    .answer("I do not know yet.")
                    .responseMode(ResponseMode.CLARIFICATION)
                    .token(new Token(0, 0, 0))
                    .sources(List.of())
                    .confidenceLevel(ConfidenceLevel.NONE)
                    .build();
        }

        /*
        check content Safety
         */
        if (!contentSafetyClient.contentSafetyCheck(request)) {
            return AiResponse
                    .builder()
                    .answer("I cannot help you with that, but I am here if you want to talk about something else.")
                    .responseMode(ResponseMode.REFUSAL)
                    .token(new Token(0, 0, 0))
                    .build();
        }

        List<KnowledgeChunk> sources = inMemoryKnowledgeRetrievalService.retrieveKnowledgeBlock(request);

        ragOriginalAiResponse  =  azureOpenAiClient
                .generateGroundedAnswer(RAG_SYS_PROMPT_MSG, groundedPrompt.formatted(sources, request));


        System.out.println("sources: " + calculateConfidenceLevel(sources));
        return ragOriginalAiResponse
                .withSources(sources)
                .withConfidenceLevel(calculateConfidenceLevel(sources));
    }

    private static ConfidenceLevel calculateConfidenceLevel(List<KnowledgeChunk> chunks) {

       int count = chunks.size();


        if(count >= 4) return ConfidenceLevel.HIGH;
        if(count >= 2) return ConfidenceLevel.MEDIUM;
        return ConfidenceLevel.LOW;

    }

}
