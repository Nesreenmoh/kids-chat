package com.education.kids_chat.models;

import com.education.kids_chat.enums.BullingCategory;
import lombok.Builder;

@Builder
public record BullyingResponse(String message, boolean bullyingDetected, BullingCategory bullyingCategory,  Double confidence ) {
}
