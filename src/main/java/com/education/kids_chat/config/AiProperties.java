package com.education.kids_chat.config;


import com.education.kids_chat.enums.AnswerMode;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "ai")
public class AiProperties {


    private AnswerMode answerMode;

    public AnswerMode getAnswerMode() {
        return answerMode;
    }

    public void setAnswerMode(AnswerMode answerMode) {
        this.answerMode = answerMode;
    }
}
