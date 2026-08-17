package com.amir35.spring_ai.service;

import org.springframework.ai.document.Document;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PdfIngestionService {

    private final VectorStore vectorStore;

    public PdfIngestionService(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    public int ingestPdf(Resource pdfResource) {

        // 1. Read PDF
        PagePdfDocumentReader pdfReader =
                new PagePdfDocumentReader(pdfResource);

        List<Document> documents = pdfReader.read();

        System.out.println("PDF documents/pages read: " + documents.size());

        // 2. Split into smaller chunks
        TokenTextSplitter splitter =
                new TokenTextSplitter();

        List<Document> chunks =
                splitter.apply(documents);

        System.out.println("Chunks created: " + chunks.size());

        // 3. Store chunks in PGVector
        vectorStore.add(chunks);

        System.out.println("Chunks stored in PGVector: " + chunks.size());

        return chunks.size();
    }
}