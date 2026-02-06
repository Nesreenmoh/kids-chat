package com.education.kids_chat.services;

import com.education.kids_chat.clients.AzureOpenAiClient;
import com.education.kids_chat.models.AiResponse;
import com.education.kids_chat.models.Request;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.education.kids_chat.utils.Helper.RAG_SYS_PROMPT_MSG;
import static com.education.kids_chat.utils.Helper.groundedPrompt;

@Service
public class RagAnswerService {


    @Autowired
    InMemoryKnowledgeRetrievalService inMemoryKnowledgeRetrievalService;

    @Autowired
    AzureOpenAiClient azureOpenAiClient;


    public AiResponse generateAnswer(Request userQuestion) {

        return azureOpenAiClient.generateGroundedAnswer(RAG_SYS_PROMPT_MSG, groundedPrompt.formatted(inMemoryKnowledgeRetrievalService.retrieveKnowledgeBlock(), userQuestion));
    }
}
