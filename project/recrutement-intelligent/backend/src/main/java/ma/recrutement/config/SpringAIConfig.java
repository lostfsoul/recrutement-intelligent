package ma.recrutement.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiEmbeddingModel;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ai.autoconfigure.vectorstore.pgvector.PgVectorStoreAutoConfiguration;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Configuration de Spring AI pour l'intégration avec OpenAI.
 *
 * @author Recrutement Team
 * @version 1.0.0
 */
@Configuration
@EnableAutoConfiguration(exclude = {PgVectorStoreAutoConfiguration.class})
public class SpringAIConfig {

    @Value("${spring.ai.openai.api-key}")
    private String openaiApiKey;

    @Value("${spring.ai.openai.chat.options.model:gpt-4}")
    private String chatModel;

    private final DataSource dataSource;

    public SpringAIConfig(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * Configure le ChatModel pour les interactions avec l'IA.
     *
     * @return le ChatModel
     */
    @Bean
    public ChatModel chatModel() {
        return new OpenAiChatModel(new OpenAiApi(openaiApiKey));
    }

    /**
     * Configure le ChatClient pour les interactions avec l'IA.
     *
     * @param chatModel le modèle de chat
     * @return le ChatClient
     */
    @Bean
    public ChatClient chatClient(ChatModel chatModel) {
        return ChatClient.builder(chatModel).build();
    }

    /**
     * Configure l'EmbeddingModel pour les embeddings.
     *
     * @return l'EmbeddingModel
     */
    @Bean
    public EmbeddingModel embeddingModel() {
        return new OpenAiEmbeddingModel(new OpenAiApi(openaiApiKey));
    }

    /**
     * Configure un VectorStore transactionnel qui utilise le même datasource que l'application.
     * Cela permet de garantir que les opérations d'indexage sont commitées dans la même transaction.
     *
     * @param embeddingModel le modèle d'embedding
     * @return le VectorStore
     */
    @Bean
    public org.springframework.ai.vectorstore.VectorStore vectorStore(EmbeddingModel embeddingModel) {
        return new TransactionalPgVectorStore(dataSource, embeddingModel);
    }

    /**
     * VectorStore personnalisé qui utilise JdbcTemplate pour garantir la transaction.
     */
    @Slf4j
    public static class TransactionalPgVectorStore implements org.springframework.ai.vectorstore.VectorStore {
        private final JdbcTemplate jdbcTemplate;
        private final EmbeddingModel embeddingModel;

        public TransactionalPgVectorStore(DataSource dataSource, EmbeddingModel embeddingModel) {
            this.jdbcTemplate = new JdbcTemplate(dataSource);
            this.embeddingModel = embeddingModel;
            initializeSchema();
        }

        private void initializeSchema() {
            try {
                jdbcTemplate.execute("CREATE EXTENSION IF NOT EXISTS vector");
                jdbcTemplate.execute("""
                    CREATE TABLE IF NOT EXISTS cv_embeddings (
                        id SERIAL PRIMARY KEY,
                        content TEXT,
                        metadata JSON,
                        embedding vector(1536),
                        UNIQUE(content)
                    )
                """);
                jdbcTemplate.execute("""
                    CREATE INDEX IF NOT EXISTS cv_embeddings_embedding_idx
                    ON cv_embeddings
                    USING ivfflat (embedding vector_cosine_ops)
                    WITH (lists = 100)
                """);
            } catch (Exception e) {
                // Table might already exist
            }
        }

        @Override
        @Transactional
        public void add(List<Document> documents) {
            for (Document document : documents) {
                // Generate embedding
                float[] embedding = embeddingModel.embed(document.getContent());

                // Convert to PostgreSQL vector format
                StringBuilder vectorStr = new StringBuilder("[");
                for (int i = 0; i < embedding.length; i++) {
                    if (i > 0) vectorStr.append(",");
                    vectorStr.append(embedding[i]);
                }
                vectorStr.append("]");

                // Build metadata JSON
                StringBuilder metadataJson = new StringBuilder("{");
                boolean first = true;
                for (Map.Entry<String, Object> entry : document.getMetadata().entrySet()) {
                    if (!first) metadataJson.append(",");
                    first = false;
                    metadataJson.append("\"").append(entry.getKey()).append("\":");
                    Object value = entry.getValue();
                    if (value instanceof String) {
                        metadataJson.append("\"").append(value).append("\"");
                    } else {
                        metadataJson.append(value);
                    }
                }
                metadataJson.append("}");

                // Insert - delete existing if any (simpler than ON CONFLICT)
                int deleted = jdbcTemplate.update("DELETE FROM cv_embeddings WHERE content = ?", document.getContent());
                int inserted = jdbcTemplate.update(
                    "INSERT INTO cv_embeddings (content, metadata, embedding) VALUES (?::text, ?::json, ?::vector)",
                    document.getContent(),
                    metadataJson.toString(),
                    vectorStr.toString()
                );
                log.info("Inserted document: deleted={}, inserted=1, type={}, id={}",
                    deleted, document.getMetadata().get("type"), document.getId());
            }
        }

        @Override
        @Transactional
        public java.util.Optional<Boolean> delete(List<String> idList) {
            for (String id : idList) {
                jdbcTemplate.update("DELETE FROM cv_embeddings WHERE id = ?", Integer.parseInt(id));
            }
            return java.util.Optional.of(true);
        }

        @Override
        public List<Document> similaritySearch(SearchRequest request) {
            // Generate embedding for the query
            float[] embedding = embeddingModel.embed(request.getQuery());

            // Convert to PostgreSQL vector format
            StringBuilder vectorStr = new StringBuilder("[");
            for (int i = 0; i < embedding.length; i++) {
                if (i > 0) vectorStr.append(",");
                vectorStr.append(embedding[i]);
            }
            vectorStr.append("]");

            // Search for similar documents
            List<Document> results = jdbcTemplate.query(
                "SELECT *, embedding <=> ?::vector AS distance FROM cv_embeddings ORDER BY distance LIMIT ?",
                rs -> {
                    List<Document> docs = new java.util.ArrayList<>();
                    while (rs.next()) {
                        Map<String, Object> metadata = new java.util.HashMap<>();
                        String metadataJson = rs.getString("metadata");
                        log.info("Raw metadata JSON: {}", metadataJson);
                        if (metadataJson != null && !metadataJson.equals("null")) {
                            // Simple JSON parsing (you might want to use a proper JSON library)
                            metadata.put("raw_metadata", metadataJson);
                            // Parse type if available
                            if (metadataJson.contains("\"type\"")) {
                                String type = metadataJson.replaceAll(".*\"type\"\\s*:\\s*\"([^\"]+)\".*", "$1");
                                metadata.put("type", type);
                            }
                            if (metadataJson.contains("\"candidatId\"")) {
                                String candidatId = metadataJson.replaceAll(".*\"candidatId\"\\s*:\\s*\"([^\"]+)\".*", "$1");
                                metadata.put("candidatId", candidatId);
                            }
                            if (metadataJson.contains("\"offreId\"")) {
                                String offreId = metadataJson.replaceAll(".*\"offreId\"\\s*:\\s*\"([^\"]+)\".*", "$1");
                                metadata.put("offreId", offreId);
                            }
                        }
                        Document doc = new Document(rs.getString("id"), rs.getString("content"), metadata);
                        log.debug("Parsed document: id={}, type={}, metadata={}",
                            doc.getId(), metadata.get("type"), metadata);
                        docs.add(doc);
                    }
                    log.info("SimilaritySearch returned {} results (with types: {})",
                        docs.size(), docs.stream().map(d -> d.getMetadata().get("type")).toList());
                    return docs;
                },
                vectorStr.toString(),
                request.getTopK()
            );
            return results;
        }

        // Other required methods
        @Override
        public List<Document> similaritySearch(String query) {
            return similaritySearch(SearchRequest.query(query).withTopK(10));
        }
    }
}
