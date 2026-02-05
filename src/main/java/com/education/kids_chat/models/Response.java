package com.education.kids_chat.models;

import com.education.kids_chat.enums.ResponseMode;
import lombok.Builder;
import org.springframework.web.bind.annotation.ResponseBody;

@Builder
public record Response(ResponseMode responseMode, String answer, Token token) {
}
