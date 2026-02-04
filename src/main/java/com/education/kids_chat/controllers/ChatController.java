package com.education.kids_chat.controllers;

import com.education.kids_chat.models.Request;
import com.education.kids_chat.models.Response;
import com.education.kids_chat.services.ChatService;
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
    ChatService chatService;

     private final Logger LOG = LoggerFactory.getLogger(ChatController.class);

     @PostMapping(
             consumes = "application/json",
             produces = "application/json",
             path = "/ask"
     )
    public Response getAnswer(@RequestBody Request request){

         Response response = chatService.handelChat(request);
         LOG.info(response.toString());
         return response;
     }
}
