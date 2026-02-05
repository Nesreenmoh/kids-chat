package com.education.kids_chat.models;

import lombok.Builder;

import java.util.List;

@Builder
public record ValidationResult(boolean valid, List<String> violations) {

}
