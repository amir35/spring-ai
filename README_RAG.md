**Project Setup:**
Spring Boot + Spring AI + Ollama + PostgreSQL + PGVector

**Ollama Setup**
spring.ai.ollama.base-url=http://localhost:11434
spring.ai.ollama.chat.model=llama3.2

**Chat model**
llama3.2

**Embedding Model**
ollama pull nomic-embed-text
pulling manifest
pulling 970aa74c0a90: 100% ▕████████████████████████████████████████████████████████████████████████████████████████████████▏ 274 MB
verifying sha256 digest
writing manifest
success

**ollama list**
NAME                       ID              SIZE      MODIFIED
nomic-embed-text:latest    0a109f422b47    274 MB    About a minute ago
llama3.2:latest            a80c4f17acd5    2.0 GB    2 hours ago

spring.ai.ollama.embedding.model=nomic-embed-text

nomic-embed-text
For example: "How can I reset my password?"
gets converted into a numerical vector.
Conceptually:
Text -> [0.12, -0.45, 0.83, ...]

**EmbeddingService**
This service currently does two things:

1. Generate embeddings
   embeddingModel.embed(text);
2. Manually calculate cosine similarity
   cosineSimilarity(vectorA, vectorB);

**Manual Similarity Experiment**
We created an endpoint:
POST /api/ai/similarity

Sentence A:
How can I reset my internet banking password?

Sentence B:
Customers can change their online banking password using
the Forgot Password option.

Sentence C:
You can withdraw cash from any supported ATM.

Question:
I forgot my online banking password. How can I change it?

**PostgreSQL Setup**
pgAdmin 4 and PostgreSQL 15

Our final database setup became:

Windows PostgreSQL 15 -> ragdb -> springai

**PostgreSQL springai User**
CREATE USER springai WITH PASSWORD 'springai';
GRANT ALL PRIVILEGES ON DATABASE ragdb TO springai;
GRANT ALL ON SCHEMA public TO springai;
ALTER SCHEMA public OWNER TO springai;

We verified the user from command line: psql -h localhost -U springai -d ragdb

**PGVector Installation**
PostgreSQL itself does not automatically have vector functionality.

We downloaded:
https://github.com/andreiramani/pgvector_pgsql_windows/releases/tag/0.8.6_15?
pgvector v0.8.6 for PostgreSQL 15, Microsoft Windows

We copied: vector.dll to: C:\Program Files\PostgreSQL\15\lib\
copy /Y "D:\AI\vector.v0.8.6-pg15\lib\vector.dll" "C:\Program Files\PostgreSQL\15\lib\"

and copied the extension files into: C:\Program Files\PostgreSQL\15\share\extension\
xcopy /Y "D:\AI\vector.v0.8.6-pg15\share\extension\*" "C:\Program Files\PostgreSQL\15\share\extension\"  

**Verifying PGVector**
CREATE EXTENSION IF NOT EXISTS vector;

SELECT extname, extversion FROM pg_extension WHERE extname = 'vector';
extname | extversion
--------+-----------
vector  | 0.8.6

**Spring Boot Database Configuration**
application.properties
# PostgreSQL
spring.datasource.url=jdbc:postgresql://localhost:5432/ragdb
spring.datasource.username=springai
spring.datasource.password=springai

# PGVector
spring.ai.vectorstore.pgvector.initialize-schema=true
It tells Spring AI to initialize the required PGVector schema/table.

**VectorStoreService**
It handles:
Document storage + Vector similarity search

**PGVector Table**
SELECT table_name
FROM information_schema.tables
WHERE table_schema = 'public';

**First Document Insertion**
We added an endpoint: POST /api/ai/vector-store

which calls: vectorStoreService.saveText(text);

**Verifying the Stored Document**
SELECT id, content, metadata
FROM vector_store;

Output:
id       → UUID
content  → Customers can reset their internet banking password...
metadata → {}

**Similarity Search**
public List<Document> search(String query) {

    return vectorStore.similaritySearch(query);
}

API: POST /api/ai/search
Body: I forgot my internet banking password. How can I change it?
Output: Customers can reset their internet banking password using the Forgot Password option.

**Actual Similarity Search Result**
[
{
"id": "5f33aef5-489f-4405-b607-6dfaeb379d99",
"media": null,
"metadata": {
"distance": 0.16362493
},
"score": 0.8363750725984573,
"text": "Customers can reset their internet banking password using the Forgot Password option."
}
]
Understanding distance and score

Our result:
distance = 0.16362493
score    = 0.83637507

**score ≈ 1 - distance**

