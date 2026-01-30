package ma.recrutement.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

/**
 * Initialise l'extension PgVector et les tables nécessaires pour le vector store.
 *
 * @author Recrutement Team
 * @version 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PgVectorInitializer {

    private final DataSource dataSource;

    @Value("${spring.ai.vectorstore.pgvector.dimension:1536}")
    private int dimension;

    @Value("${spring.ai.vectorstore.pgvector.table-name:vector_store}")
    private String tableName;

    /**
     * Initialise l'extension pgvector et crée les tables nécessaires
     * une fois l'application démarrée.
     */
    @EventListener(ApplicationReadyEvent.class)
    public void initializePgVector() {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

        // Créer l'extension pgvector si elle n'existe pas
        try {
            jdbcTemplate.execute("CREATE EXTENSION IF NOT EXISTS vector");
            log.info("Extension PgVector créée ou déjà existante");
        } catch (Exception e) {
            log.warn("Impossible de créer l'extension PgVector: {}", e.getMessage());
        }

        // Créer la table vector_store si elle n'existe pas
        try {
            jdbcTemplate.execute(String.format("""
                CREATE TABLE IF NOT EXISTS %s (
                    id SERIAL PRIMARY KEY,
                    content TEXT,
                    metadata JSON,
                    embedding vector(%d),
                    UNIQUE(content)
                )
                """, tableName, dimension));
            log.info("Table {} créée ou déjà existante", tableName);
        } catch (Exception e) {
            log.warn("Impossible de créer la table {}: {}", tableName, e.getMessage());
        }

        // Créer l'index IVFFLAT pour optimiser les recherches
        try {
            jdbcTemplate.execute(String.format("""
                CREATE INDEX IF NOT EXISTS %s_embedding_idx
                ON %s
                USING ivfflat (embedding vector_cosine_ops)
                WITH (lists = 100)
                """, tableName, tableName));
            log.info("Index IVFFLAT créé ou déjà existant");
        } catch (Exception e) {
            log.warn("Impossible de créer l'index: {}", e.getMessage());
        }
    }
}
