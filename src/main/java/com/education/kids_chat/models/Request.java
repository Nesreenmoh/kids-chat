package com.education.kids_chat.models;

import lombok.Builder;

@Builder
public record Request(String question) {
}
