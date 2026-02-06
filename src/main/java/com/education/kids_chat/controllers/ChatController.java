package com.education.kids_chat.controllers;

import com.education.kids_chat.models.Request;
import com.education.kids_chat.models.AiResponse;
import com.education.kids_chat.services.AnswerOrchestratorService;
import com.education.kids_chat.services.GPTAnswerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("v1/api/chat")
public class ChatController {

    @Autowired
    AnswerOrchestratorService answerOrchestratorService;

     private final Logger LOG = LoggerFactory.getLogger(ChatController.class);

     @PostMapping(
             consumes = "application/json",
             produces = "application/json",
             path = "/ask"
     )
    public AiResponse getGPTAnswer(@RequestBody Request request){

       AiResponse answer = answerOrchestratorService.generateAnswer(request);
         LOG.info(answer.toString());
         return answer;
     }
}
