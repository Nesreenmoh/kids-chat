package com.education.kids_chat.utils;

import java.util.List;

public class Helper {

    public  static final String DEPLOYMENT_NAME = "gpt-5.2-chat";

    public static final List<String> FORBIDDEN_WORDS = List.of(
            "always",
            "never",
            "guaranteed",
            "everyone",
            "nobody"
    );

    public static final String BULLYING_DEPLOYMENT_NAME = "gpt-4.1-mini";

    public static final String BULLING_SYSTEM_PROMPT_MSG = """
                
                Classify the following message for bullying or emotional harm.
                Return JSON only with NO markdown, NO explanation, NO code fences.
                Return exactly this shape:
                {
                  "message": "<echo the original message>",
                  "bullyingDetected": true/false,
                  "category": "NONE" | "MILD" | "MODERATE" | "HIGH",
                  "confidence": number
                }
                
                Message: "%s"   
                """;


    public static final String SYS_PROMPT_NORMAL_MSG = "You are a child-safe educational assistant.\n" +
            "You must:\n" +
            "- Use simple language (age7–12)\n" +
            "- Avoid sensitive topics\n" +
            "- Never provide medical, legal,or unsafe advice\n" +
            "- Never assume facts you are unsure about\n" +
            "If you are not sure, say \"I don't know yet.\"";

    /*
The behavior of GPT is intentional not accidental
 */
    public static final String SYS_PROMPT_SUPPORTIVE_MSG = "You are a child-safe assistant.\n" +
            "You must:\n" +
            "-Response with empathy.\n" +
            "-Do not judge or blame\n" +
            "-Offer support and options\n" +
            "-Do not escalate or alarm\n";



    public static final String FIX_SYS_PROMPT_MSG = """
            Rewrite the following response.
                Rules:
                - Do not use absolute words such as "%s",
                - Use careful and gentle language.
                Response:
                "%s"
            """;

    public static final String CLARIFICATION_SYSTEM_PROMPT_MSG    = "I am not sure how to answer that safely yet. Can you ask it again differently?";


}
