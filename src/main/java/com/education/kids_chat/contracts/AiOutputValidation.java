package com.education.kids_chat.contracts;

import com.education.kids_chat.models.AiResponse;
import com.education.kids_chat.models.ValidationResult;

public interface AiOutputValidation {
    ValidationResult validate(AiResponse aiResponse);
}
