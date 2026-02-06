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

        AiResponse originalAiResponse = azureOpenAiClient.generateResponse(request.question(), systemPrompt, responseMode);
        LOGGER.info("Original response: {}", originalAiResponse.answer());
        /*
        validate the response from the model
         */
        ValidationResult validateResultV1 = blackListWordValidator.validate(originalAiResponse.answer());

        if (!validateResultV1.valid()) {
            String sysPro = FIX_SYS_PROMPT_MSG.formatted(validateResultV1.violations(), originalAiResponse.answer());
            AiResponse repaired = azureOpenAiClient.generateResponse(originalAiResponse.answer(), sysPro, responseMode);
            LOGGER.info("Repaired1 response: {}", repaired);
            ValidationResult validateResultV2 = blackListWordValidator.validate(repaired.answer());
            if (validateResultV2.valid()) {
                return repaired;
            } else {
                return AiResponse
                        .builder()
                        .answer(CLARIFICATION_SYSTEM_PROMPT_MSG)
                        .token(new Token(0, 0, 0))
                        .responseMode(ResponseMode.CLARIFICATION).build();
            }

        }
        return AiResponse
                .builder()
                .answer(originalAiResponse.answer())
                .token(new Token(originalAiResponse.token().promptToken(), originalAiResponse.token().completionToken(), originalAiResponse.token().totalToken()))
                .responseMode(responseMode)
                .build();
    }


}
