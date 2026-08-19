package com.amir35.spring_ai.config;

import java.util.List;
import java.util.Map;

import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class RagConfig {

    @Bean
    VectorStore vectorStore(EmbeddingModel embeddingModel) {
        return SimpleVectorStore.builder(embeddingModel).build();
    }

    @Bean
    ApplicationRunner loadDemoKnowledge(VectorStore vectorStore) {
        return args -> vectorStore.add(List.of(
                new Document("""
                        Course policy: The Spring AI beginner course has three modules.
                        Module 1 is chat basics, Module 2 is embeddings and vector stores,
                        and Module 3 is Retrieval Augmented Generation.
                        """, Map.of("source", "course-handbook", "topic", "course-plan")),
                new Document("""
                        Project rule: The demo project must compare two answers side by side:
                        a plain model answer and a RAG answer grounded in retrieved documents.
                        """, Map.of("source", "course-handbook", "topic", "project")),
                new Document("""
                        RAG definition: Retrieval Augmented Generation first retrieves relevant
                        document chunks from a vector store, then sends those chunks as context
                        to the language model so the final answer is grounded in your data.
                        """, Map.of("source", "course-handbook", "topic", "rag")),
                new Document("""
                        Embedding definition: An embedding is a list of numbers that represents
                        the meaning of text. Text with similar meaning should have similar vectors.
                        Vector stores use these vectors to perform similarity search.
                        """, Map.of("source", "course-handbook", "topic", "embeddings")),
                new Document("""
                        Vector store warning: SimpleVectorStore is useful for learning and tests,
                        but production systems usually use a real vector database such as PostgreSQL
                        with pgvector, Redis, Pinecone, Qdrant, MongoDB Atlas, or Elasticsearch.
                        """, Map.of("source", "course-handbook", "topic", "vector-store"))
        ));
    }
}
