package com.education.kids_chat.services;

import com.azure.ai.contentsafety.ContentSafetyClient;
import com.azure.ai.contentsafety.ContentSafetyClientBuilder;
import com.azure.ai.contentsafety.models.AnalyzeTextOptions;
import com.azure.ai.contentsafety.models.AnalyzeTextResult;
import com.azure.ai.contentsafety.models.TextCategoriesAnalysis;
import com.azure.core.credential.KeyCredential;
import com.azure.core.exception.HttpResponseException;
import com.education.kids_chat.models.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/*
 Check if the question/content has HIGH/MEDIUM
 self-harm
 violence
 hate
 sexual
 Refuse to answer
 */
@Service
public class ContentSafetyService {

    private final static Logger LOGGER = LoggerFactory.getLogger(ContentSafetyService.class);

    private final String CONTENT_SAFETY_ENDPOINT;
    private final String CONTENT_SAFETY_API_KEY;


    public ContentSafetyService(@Value("${azure.content.safety.endpoint:}")String CONTENT_SAFETY_ENDPOINT, @Value("${azure.content.safety.api.key:}")String CONTENT_SAFETY_API_KEY) {
        this.CONTENT_SAFETY_ENDPOINT = CONTENT_SAFETY_ENDPOINT;
        this.CONTENT_SAFETY_API_KEY = CONTENT_SAFETY_API_KEY;
    }

    public boolean contentSafetyCheck(Request request){

        /*
        Define Content Safety Client
         */
        ContentSafetyClient client = new ContentSafetyClientBuilder()
                .endpoint(CONTENT_SAFETY_ENDPOINT)
                .credential(new KeyCredential(CONTENT_SAFETY_API_KEY))
                .buildClient();
        
        // Define Analyze options
        AnalyzeTextOptions options = new AnalyzeTextOptions(request.question());
        AnalyzeTextResult analyzeTextResult;
        try {
            analyzeTextResult = client.analyzeText(options);
        }catch(HttpResponseException ex)    {
            System.out.println("Analyze text failed.\nStatus code: " + ex.getResponse().getStatusCode() + ", Error message: " + ex.getMessage());
            throw ex;
        }


        for (TextCategoriesAnalysis result : analyzeTextResult.getCategoriesAnalysis()) {
            if (result.getSeverity() >= 4) {
                LOGGER.warn("Content Safety Alert: Category {} has severity {}", result.getCategory(), result.getSeverity());
                return false;
            }
        }

        return true;
    }
}
