package com.education.kids_chat.models;

import lombok.Builder;

@Builder
public record Response(String answer, Integer promptToken, Integer completionToken, Integer totalToken) {
}
