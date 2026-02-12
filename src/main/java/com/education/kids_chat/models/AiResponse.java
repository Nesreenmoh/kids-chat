package com.education.kids_chat.models;

import com.education.kids_chat.enums.ConfidenceLevel;
import com.education.kids_chat.enums.ResponseMode;
import lombok.Builder;
import lombok.With;

import java.util.List;

@Builder
@With
public record AiResponse(ResponseMode responseMode, String answer, Token token, List<KnowledgeChunk> sources, ConfidenceLevel confidenceLevel) {
}
