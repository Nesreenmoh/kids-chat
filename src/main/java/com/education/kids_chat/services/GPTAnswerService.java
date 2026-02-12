package com.education.kids_chat.services;


import com.education.kids_chat.clients.AzureContentSafetyClient;
import com.education.kids_chat.clients.AzureOpenAiClient;
import com.education.kids_chat.enums.ConfidenceLevel;
import com.education.kids_chat.enums.ResponseMode;
import com.education.kids_chat.models.AiResponse;
import com.education.kids_chat.models.Request;
import com.education.kids_chat.models.ResponseContext;
import com.education.kids_chat.models.Token;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GPTAnswerService {


    @Autowired
    BullyingDetectionService bullyingDetectionService;

    @Autowired
    BlackListWordValidator blackListWordValidator;

    @Autowired
    AzureOpenAiClient azureOpenAiClient;

    @Autowired
    private AzureContentSafetyClient contentSafetyClient;


    private final static Logger LOGGER = LoggerFactory.getLogger(GPTAnswerService.class);


    public AiResponse generateAnswer(Request request) {

                /*
                Call Content Safety
                 */
        if (!contentSafetyClient.contentSafetyCheck(request)) {
            return AiResponse
                    .builder()
                    .answer("I cannot help you with that, but I am here if you want to talk about something else.")
                    .responseMode(ResponseMode.REFUSAL)
                    .token(new Token(0, 0, 0))
                    .build();
        }

                /*
                Bullying Detection Check
                 */

        ResponseContext bullingServiceResult = bullyingDetectionService.generateResponseContext(request);


        AiResponse originalAiResponse = azureOpenAiClient.generateGPTResponse(request.question(), bullingServiceResult.systemPrompt(), bullingServiceResult.responseMode());

        LOGGER.info("Original response: {}", originalAiResponse.answer());

        /*
        validate the response from the model
         */

        AiResponse gptResponse = blackListWordValidator.validateResponse(originalAiResponse, bullingServiceResult.responseMode());
        return gptResponse.withConfidenceLevel(ConfidenceLevel.LOW);


    }

}
