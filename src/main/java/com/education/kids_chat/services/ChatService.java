package com.education.kids_chat.services;


import com.education.kids_chat.enums.BullingCategory;
import com.education.kids_chat.enums.ResponseMode;
import com.education.kids_chat.models.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ChatService {

    @Autowired
    ContentSafetyService contentSafetyService;

    @Autowired
    BullyingDetectionService bullyingDetectionService;

    @Autowired
    BlackListWordValidator blackListWordValidator;

    @Autowired
    AzureOpenAiClient azureOpenAiClient;


    private final static Logger LOGGER = LoggerFactory.getLogger(ChatService.class);


    private final String SYS_PROMPT_NORMAL_MSG = "You are a child-safe educational assistant.\n" +
            "You must:\n" +
            "- Use simple language (age7–12)\n" +
            "- Avoid sensitive topics\n" +
            "- Never provide medical, legal,or unsafe advice\n" +
            "- Never assume facts you are unsure about\n" +
            "If you are not sure, say \"I don't know yet.\"";

    /*
    The behavior of GPT is intentional not accidental
     */
    private final String SYS_PROMPT_SUPPORTIVE_MSG = "You are a child-safe assistant.\n" +
            "You must:\n" +
            "-Response with empathy.\n" +
            "-Do not judge or blame\n" +
            "-Offer support and options\n" +
            "-Do not escalate or alarm\n";

    private final String FIX_SYS_PROMPT_MSG = """
            Rewrite the following response.
                Rules:
                - Do not use absolute words such as "%s",
                - Use careful and gentle language.
                Response:
                "%s"
            
            """;


    public Response handelChat(Request request) {

                /*
                Call Content Safety
                 */
        if (!contentSafetyService.contentSafetyCheck(request)) {
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

        Response orginalResponse = azureOpenAiClient.generateResponse(request.question(), systemPrompt);
        /*
        validate the response from the model
         */
        ValidationResult validateResultV1 = blackListWordValidator.validate(orginalResponse.answer());

        if (!validateResultV1.valid()) {

            Response repaired = azureOpenAiClient.generateResponse(FIX_SYS_PROMPT_MSG.formatted(validateResultV1.violations()), orginalResponse.answer());

            ValidationResult validateResultV2 = blackListWordValidator.validate(repaired.answer());
            if (validateResultV2.valid()) {
                return repaired;
            }
            return Response
                    .builder()
                    .answer("I am not sure how to answer that safely yet. Can you ask it again differently?")
                    .token(new Token(0, 0, 0))
                    .responseMode(ResponseMode.CLARIFICATION)
                    .build();

        }
        return Response
                .builder()
                .answer(orginalResponse.answer())
                .token(new Token(orginalResponse.token().promptToken(), orginalResponse.token().completionToken(), orginalResponse.token().totalToken()))
                .responseMode(ResponseMode.NORMAL)
                .responseMode(responseMode)
                .build();
    }


}
