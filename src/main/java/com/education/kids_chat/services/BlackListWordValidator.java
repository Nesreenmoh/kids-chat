package com.education.kids_chat.services;

import com.education.kids_chat.contracts.AiOutputValidation;
import com.education.kids_chat.models.ValidationResult;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.education.kids_chat.utils.Helper.FORBIDDEN_WORDS;

@Component
public class BlackListWordValidator implements AiOutputValidation {


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
