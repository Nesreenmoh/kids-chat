package com.education.kids_chat.services;

import com.education.kids_chat.clients.AzureOpenAiClient;
import com.education.kids_chat.enums.ResponseMode;
import com.education.kids_chat.models.AiResponse;
import com.education.kids_chat.models.KnowledgeChunk;
import com.education.kids_chat.models.Request;
import com.education.kids_chat.models.Token;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.education.kids_chat.utils.Helper.RAG_SYS_PROMPT_MSG;
import static com.education.kids_chat.utils.Helper.groundedPrompt;

@Service
public class RagAnswerService {


    @Autowired
    InMemoryKnowledgeRetrievalService  inMemoryKnowledgeRetrievalService;

    @Autowired
    AzureOpenAiClient azureOpenAiClient;


    public AiResponse generateAnswer(Request userQuestion) {

        return azureOpenAiClient.generateGroundedAnswer(RAG_SYS_PROMPT_MSG, groundedPrompt.formatted(inMemoryKnowledgeRetrievalService.returnKnowledgeBlock(), userQuestion));
    }
}
