package com.education.kids_chat.services;


import com.education.kids_chat.clients.AzureContentSafetyClient;
import com.education.kids_chat.clients.AzureOpenAiClient;
import com.education.kids_chat.enums.BullingCategory;
import com.education.kids_chat.enums.ResponseMode;
import com.education.kids_chat.models.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.education.kids_chat.utils.Helper.*;

@Service
public class ChatService {


    @Autowired
    BullyingDetectionService bullyingDetectionService;

    @Autowired
    BlackListWordValidator blackListWordValidator;

    @Autowired
    AzureOpenAiClient azureOpenAiClient;

    @Autowired
    private AzureContentSafetyClient contentSafetyClient;


    private final static Logger LOGGER = LoggerFactory.getLogger(ChatService.class);


    public Response handelChat(Request request) {

                /*
                Call Content Safety
                 */
        if (!contentSafetyClient.contentSafetyCheck(request)) {
            return Response
                    .builder()
                    .answer("I cannot help you with that, but I am here if you want to talk about something else.")
                    .responseMode(ResponseMode.REFUSAL)
                    .token(new Token(0, 0, 0))
                    .build();
        }

                /*
                Bullying Detection Check
                 */

        BullyingResponse bullyingResult = bullyingDetectionService.handelBullying(request);
        ResponseMode responseMode;
        String systemPrompt = switch (bullyingResult.category()) {
            case BullingCategory.HIGH, BullingCategory.MODERATE -> {
                responseMode = ResponseMode.SUPPORTIVE;
                yield SYS_PROMPT_SUPPORTIVE_MSG;
            }
            default -> {
                responseMode = ResponseMode.NORMAL;
                yield SYS_PROMPT_NORMAL_MSG;
            }
        };

        Response originalResponse = azureOpenAiClient.generateResponse(request.question(), systemPrompt,responseMode);
        LOGGER.info("Original response: {}", originalResponse.answer());
        /*
        validate the response from the model
         */
        ValidationResult validateResultV1 = blackListWordValidator.validate(originalResponse.answer());

        if (!validateResultV1.valid()) {
            String sysPro = FIX_SYS_PROMPT_MSG.formatted(validateResultV1.violations(), originalResponse.answer());
            Response repaired = azureOpenAiClient.generateResponse(originalResponse.answer(),sysPro,responseMode);
            LOGGER.info("Repaired1 response: {}", repaired);
            ValidationResult validateResultV2 = blackListWordValidator.validate(repaired.answer());
            if (validateResultV2.valid()) {
                return repaired;
            }
            else
            {
            return Response
                    .builder()
                    .answer(CLARIFICATION_SYSTEM_PROMPT_MSG)
                    .token(new Token(0, 0, 0))
                    .responseMode(ResponseMode.CLARIFICATION).build();
            }

        }
        return Response
                .builder()
                .answer(originalResponse.answer())
                .token(new Token(originalResponse.token().promptToken(), originalResponse.token().completionToken(), originalResponse.token().totalToken()))
                .responseMode(responseMode)
                .build();
    }


}
