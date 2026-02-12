package com.education.kids_chat.services;

import com.education.kids_chat.contracts.KnowledgeRetrieval;
import com.education.kids_chat.models.KnowledgeChunk;
import com.education.kids_chat.models.Request;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class InMemoryKnowledgeRetrievalService implements KnowledgeRetrieval {


    public final List<KnowledgeChunk> knowledgeBase = List.of(
            new KnowledgeChunk("1", "Plants needs sunlight", "Most plants need sunlight to grow. Some plants can grow with less light.", null),
            new KnowledgeChunk("2", "Plants needs water", "Plants need water to stay healthy and move food inside them.", null)
    );


    @Override
    public List<KnowledgeChunk> retrieveKnowledgeBlock(Request request) {
        System.out.println("question: " + request.question().toLowerCase());
        return knowledgeBase.stream()
                .filter(chunk -> chunk.content().toLowerCase().contains("plants"))
                .limit(2)
                .toList();
    }

}
