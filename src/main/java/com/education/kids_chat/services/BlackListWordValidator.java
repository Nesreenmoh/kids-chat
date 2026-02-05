package com.education.kids_chat.services;

import com.education.kids_chat.contracts.AiOutputValidation;
import com.education.kids_chat.models.ValidationResult;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class BlackListWordValidator implements AiOutputValidation {



    private static final List<String> FORBIDDEN_WORDS = List.of(
            "always",
            "never",
            "guaranteed",
            "everyone",
            "nobody"
    );
    @Override
    public ValidationResult validate(String aiResponse) {
        List<String> violations = FORBIDDEN_WORDS.stream()
                .filter(word -> aiResponse.toLowerCase().contains(word))
                .toList();

        return ValidationResult.builder()
                .valid(!violations.isEmpty())
                .violations(violations)
                .build();
    }
}
