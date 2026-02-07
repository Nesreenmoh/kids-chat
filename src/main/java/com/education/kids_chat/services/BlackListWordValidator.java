package com.education.kids_chat.services;

import com.education.kids_chat.clients.AzureOpenAiClient;
import com.education.kids_chat.contracts.AiOutputValidation;
import com.education.kids_chat.enums.ResponseMode;
import com.education.kids_chat.models.AiResponse;
import com.education.kids_chat.models.Token;
import com.education.kids_chat.models.ValidationResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.education.kids_chat.utils.Helper.*;

@Component
public class BlackListWordValidator implements AiOutputValidation {

    @Autowired
    AzureOpenAiClient azureOpenAiClient;

    private final static Logger LOGGER = LoggerFactory.getLogger(BlackListWordValidator.class);

    @Override
    public ValidationResult validate(AiResponse aiResponse) {
        List<String> violations = FORBIDDEN_WORDS.stream()
                .filter(word -> aiResponse.answer().toLowerCase().contains(word))
                .toList();

        return ValidationResult.builder()
                .valid(violations.isEmpty())
                .violations(violations)
                .build();
    }

    public AiResponse validateResponse(AiResponse aiResponse, ResponseMode responseMode) {
        ValidationResult validateResultV1  = this.validate(aiResponse);
        if(validateResultV1.valid()){
            String sysPro = FIX_SYS_PROMPT_MSG.formatted(validateResultV1.violations(), aiResponse);
            AiResponse repaired = azureOpenAiClient.generateGPTResponse(aiResponse.answer(), sysPro, responseMode);
            LOGGER.info("Validation 1 response: {}", repaired);
            ValidationResult validateResultV2 = validate(repaired);
            if (validateResultV2.valid()) {
                return repaired;
            }
            else {
                return AiResponse
                        .builder()
                        .answer(CLARIFICATION_SYSTEM_PROMPT_MSG)
                        .token(new Token(0, 0, 0))
                        .responseMode(ResponseMode.CLARIFICATION).build();
            }
        }
        return AiResponse
                .builder()
                .answer(aiResponse.answer())
                .token(new Token(aiResponse.token().promptToken(), aiResponse.token().completionToken(), aiResponse.token().totalToken()))
                .responseMode(responseMode)
                .build();
    }
}
