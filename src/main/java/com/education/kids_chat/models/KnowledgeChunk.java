package com.education.kids_chat.models;

import java.util.List;
import java.util.Vector;

public record KnowledgeChunk(String id, String title, String content, String source, String ageRange, List<String> embedding) {
}
