package com.education.kids_chat.models;

import com.education.kids_chat.enums.ResponseMode;
import lombok.Builder;

@Builder
public record AiResponse(ResponseMode responseMode, String answer, Token token) {
}
