package com.education.kids_chat.models;

import com.education.kids_chat.enums.ResponseMode;
import lombok.Builder;

@Builder
public record ResponseContext(String systemPrompt, ResponseMode responseMode) {
}
