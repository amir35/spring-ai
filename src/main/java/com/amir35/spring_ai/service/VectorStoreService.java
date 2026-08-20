package com.amir35.spring_ai.service;

import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VectorStoreService {

    private final VectorStore vectorStore;

    public VectorStoreService(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    public void saveText(String text) {

        Document document = new Document(text);

        vectorStore.add(List.of(document));
    }

    public List<Document> search(String query) {

        return vectorStore.similaritySearch(query);
    }
}