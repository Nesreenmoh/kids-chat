package com.education.kids_chat.contracts;


import com.education.kids_chat.models.KnowledgeChunk;
import com.education.kids_chat.models.Request;

import java.util.List;

public interface KnowledgeRetrieval {

    List<KnowledgeChunk> retrieveKnowledgeBlock(Request request);
}
