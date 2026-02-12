package com.education.kids_chat.services;

import com.education.kids_chat.config.AiProperties;
import com.education.kids_chat.models.AiResponse;
import com.education.kids_chat.models.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class AnswerOrchestratorService {

    private final static Logger LOGGER = LoggerFactory.getLogger(AnswerOrchestratorService.class);

    private final AiProperties properties;
    private final GPTAnswerService gptAnswerService;
    private final RagAnswerService ragAnswerService;


    public AnswerOrchestratorService(AiProperties properties, GPTAnswerService gptAnswerService, RagAnswerService ragAnswerService) {
        this.properties = properties;
        this.gptAnswerService = gptAnswerService;
        this.ragAnswerService = ragAnswerService;
    }

    public AiResponse generateAnswer(Request request) {

        System.out.println("RAG: "+request.useRag());
         if (request.useRag()) {
            return ragAnswerService.generateAnswer(request);
        }
         else
             return gptAnswerService.generateAnswer(request);
    }
}
