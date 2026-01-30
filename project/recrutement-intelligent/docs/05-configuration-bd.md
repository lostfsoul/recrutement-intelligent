# Configuration Base de Données - Plateforme de Recrutement Intelligente

## 1. Vue d'Ensemble

Ce document détaille la configuration complète de la base de données **PostgreSQL** avec l'extension **PgVector** pour le stockage et la recherche des embeddings vectoriels utilisés par le module Spring AI.

---

## 2. Configuration PostgreSQL

### 2.1 Versions et Compatibilité

| Composant | Version | Notes |
|-----------|---------|-------|
| **PostgreSQL** | 16.x | Version stable avec support PgVector |
| **PgVector** | 0.5.0+ | Extension vectorielle |
| **PostgreSQL Driver** | 42.7.1 | Driver JDBC pour Java |
| **Hibernate** | 6.4.x | Compatible avec PostgreSQL 16 |

### 2.2 Installation de PgVector

L'extension PgVector est pré-installée dans l'image Docker `pgvector/pgvector:pg16`.

Pour une installation manuelle sur PostgreSQL :

```bash
# Installation depuis les sources
git clone --branch v0.5.0 https://github.com/pgvector/pgvector.git
cd pgvector
make
make install #可能需要 sudo
```

---

## 3. Configuration application.properties

### 3.1 application.properties (Principal)

```properties
# =====================================================
# Application Configuration
# =====================================================
spring.application.name=recrutement-intelligent
server.port=8080

# =====================================================
# Active Profile
# =====================================================
spring.profiles.active=dev

# =====================================================
# Database Configuration
# =====================================================
spring.datasource.url=jdbc:postgresql://localhost:5432/recrutement
spring.datasource.username=recrutement
spring.datasource.password=recrutement123
spring.datasource.driver-class-name=org.postgresql.Driver

# HikariCP Configuration (Connection Pool)
spring.datasource.hikari.connection-timeout=30000
spring.datasource.hikari.maximum-pool-size=20
spring.datasource.hikari.minimum-idle=5
spring.datasource.hikari.idle-timeout=600000
spring.datasource.hikari.max-lifetime=1800000

# =====================================================
# JPA / Hibernate Configuration
# =====================================================
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect

# Statistics (dev only)
spring.jpa.properties.hibernate.generate_statistics=true

# =====================================================
# OpenAI Configuration
# =====================================================
spring.ai.openai.api-key=${OPENAI_API_KEY}
spring.ai.openai.chat.options.model=gpt-4
spring.ai.openai.chat.options.temperature=0.7
spring.ai.openai.chat.options.max_tokens=2000

# =====================================================
# Embedding Model Configuration
# =====================================================
spring.ai.openai.embedding.options.model=text-embedding-3-small
spring.ai.openai.embedding.options.dimensions=1536

# =====================================================
# Vector Store Configuration (PgVector)
# =====================================================
spring.ai.vectorstore.pgvector.index-type=IVFFLAT
spring.ai.vectorstore.pgvector.distance-type=COSINE_DISTANCE
spring.ai.vectorstore.pgvector.dimensions=1536

# PgVector Connection
spring.ai.vectorstore.pgvector.host=localhost
spring.ai.vectorstore.pgvector.port=5432
spring.ai.vectorstore.pgvector.database=recrutement
spring.ai.vectorstore.pgvector.username=recrutement
spring.ai.vectorstore.pgvector.password=recrutement123
spring.ai.vectorstore.pgvector.table-name=cv_embeddings

# PgVector Index Configuration
spring.ai.vectorstore.pgvector.create-schema=true
spring.ai.vectorstore.pgvector.initialize-schema=true

# =====================================================
# JWT Configuration
# =====================================================
jwt.secret=${JWT_SECRET:maSuperCleSecretePourJWT123456789}
jwt.expiration=86400000 # 24 heures en millisecondes
jwt.refresh-expiration=604800000 # 7 jours en millisecondes

# =====================================================
# File Upload Configuration
# =====================================================
spring.servlet.multipart.enabled=true
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=10MB

file.upload-dir=./uploads/cv

# =====================================================
# CORS Configuration
# =====================================================
cors.allowed-origins=http://localhost:3000,http://localhost:4200
cors.allowed-methods=GET,POST,PUT,DELETE,OPTIONS
cors.allowed-headers=*
cors.allow-credentials=true
cors.max-age=3600

# =====================================================
# Logging Configuration
# =====================================================
logging.level.root=INFO
logging.level.ma.recrutement=DEBUG
logging.level.org.springframework.web=INFO
logging.level.org.hibernate.SQL=DEBUG
logging.level.org.hibernate.type.descriptor.sql.BasicBinder=TRACE

# =====================================================
# SpringDoc OpenAPI (Swagger)
# =====================================================
springdoc.api-docs.path=/api-docs
springdoc.swagger-ui.path=/swagger-ui.html
springdoc.swagger-ui.operations-sorter=method
springdoc.swagger-ui.tags-sorter=alpha

springdoc.packages-to-scan=ma.recrutement.controller
springdoc.paths-to-match=/api/**
```

### 3.2 application-dev.properties (Développement)

```properties
# Development Environment
spring.profiles.group.dev=dev

# Database (local)
spring.datasource.url=jdbc:postgresql://localhost:5432/recrutement_dev
spring.datasource.username=recrutement
spring.datasource.password=recrutement123

# Show SQL queries
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true

# Debug logging
logging.level.root=DEBUG
logging.level.ma.recrutement=DEBUG

# Hot reload
spring.devtools.restart.enabled=true

# CORS (allow all origins for development)
cors.allowed-origins=*
```

### 3.3 application-prod.properties (Production)

```properties
# Production Environment
spring.profiles.group.prod=prod

# Database (remote)
spring.datasource.url=${DB_URL}
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}

# Hide SQL queries
spring.jpa.show-sql=false

# Production logging
logging.level.root=WARN
logging.level.ma.recrutement=INFO

# Disable devtools
spring.devtools.restart.enabled=false

# CORS (specific origins)
cors.allowed-origins=${CORS_ALLOWED_ORIGINS}
```

---

## 4. Configuration Docker Compose

### 4.1 docker-compose.yml

```yaml
version: '3.8'

services:
  postgres:
    image: pgvector/pgvector:pg16
    container_name: recrutement-postgres
    environment:
      POSTGRES_DB: recrutement
      POSTGRES_USER: recrutement
      POSTGRES_PASSWORD: recrutement123
      POSTGRES_INITDB_ARGS: "-E UTF8"
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data
      - ./docker/init-db.sql:/docker-entrypoint-initdb.d/01-init-db.sql
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U recrutement"]
      interval: 10s
      timeout: 5s
      retries: 5
    networks:
      - recrutement-network

  backend:
    build:
      context: ./backend
      dockerfile: Dockerfile
    container_name: recrutement-backend
    ports:
      - "8080:8080"
    environment:
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/recrutement
      SPRING_DATASOURCE_USERNAME: recrutement
      SPRING_DATASOURCE_PASSWORD: recrutement123
      SPRING_PROFILES_ACTIVE: prod
      OPENAI_API_KEY: ${OPENAI_API_KEY}
      JWT_SECRET: ${JWT_SECRET}
    depends_on:
      postgres:
        condition: service_healthy
    networks:
      - recrutement-network
    restart: unless-stopped

volumes:
  postgres_data:
    driver: local

networks:
  recrutement-network:
    driver: bridge
```

### 4.2 .env.example

```bash
# OpenAI API Key
OPENAI_API_KEY=sk-your-openai-api-key-here

# JWT Secret Key
JWT_SECRET=your-super-secret-jwt-key-minimum-256-bits

# CORS Allowed Origins (comma-separated)
CORS_ALLOWED_ORIGINS=https://recrutement.ma,https://www.recrutement.ma
```

---

## 5. Script d'Initialisation de la Base de Données

### 5.1 init-db.sql

```sql
-- =====================================================
-- Plateforme de Recrutement Intelligente
-- Script d'Initialisation PostgreSQL + PgVector
-- =====================================================

-- Activer l'extension PgVector
CREATE EXTENSION IF NOT EXISTS vector;

-- =====================================================
-- Création des Tables
-- =====================================================

-- Table: utilisateur
CREATE TABLE IF NOT EXISTS utilisateur (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    nom VARCHAR(100) NOT NULL,
    prenom VARCHAR(100) NOT NULL,
    role VARCHAR(50) NOT NULL CHECK (role IN ('CANDIDAT', 'RECRUTEUR', 'ADMINISTRATEUR')),
    est_valide BOOLEAN DEFAULT FALSE,
    date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Indexes
CREATE INDEX IF NOT EXISTS idx_utilisateur_email ON utilisateur(email);
CREATE INDEX IF NOT EXISTS idx_utilisateur_role ON utilisateur(role);

-- Table: candidat
CREATE TABLE IF NOT EXISTS candidat (
    utilisateur_id BIGINT PRIMARY KEY REFERENCES utilisateur(id) ON DELETE CASCADE,
    titre VARCHAR(255),
    telephone VARCHAR(20),
    adresse VARCHAR(500),
    cv_path VARCHAR(500),
    cv_text TEXT,
    embedding vector(1536),
    date_modification_cv TIMESTAMP
);

-- Table: recruteur
CREATE TABLE IF NOT EXISTS recruteur (
    utilisateur_id BIGINT PRIMARY KEY REFERENCES utilisateur(id) ON DELETE CASCADE,
    poste VARCHAR(255),
    telephone VARCHAR(20)
);

-- Table: administrateur
CREATE TABLE IF NOT EXISTS administrateur (
    utilisateur_id BIGINT PRIMARY KEY REFERENCES utilisateur(id) ON DELETE CASCADE,
    niveau VARCHAR(50) DEFAULT 'STANDARD'
);

-- Table: entreprise
CREATE TABLE IF NOT EXISTS entreprise (
    id BIGSERIAL PRIMARY KEY,
    nom VARCHAR(255) NOT NULL,
    description TEXT,
    secteur VARCHAR(255),
    adresse VARCHAR(500),
    site_web VARCHAR(255),
    email VARCHAR(255),
    telephone VARCHAR(20),
    recruteur_id BIGINT REFERENCES utilisateur(id) ON DELETE SET NULL,
    date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_entreprise_secteur ON entreprise(secteur);
CREATE INDEX IF NOT EXISTS idx_entreprise_recruteur ON entreprise(recruteur_id);

-- Table: offre_emploi
CREATE TABLE IF NOT EXISTS offre_emploi (
    id BIGSERIAL PRIMARY KEY,
    titre VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    localisation VARCHAR(255),
    type_contrat VARCHAR(50) CHECK (type_contrat IN ('CDI', 'CDD', 'STAGE', 'ALTERNANCE', 'FREELANCE')),
    salaire_min INTEGER,
    salaire_max INTEGER,
    statut VARCHAR(50) DEFAULT 'BROUILLON' CHECK (statut IN ('BROUILLON', 'PUBLIEE', 'COMBLEE', 'ARCHIVEE')),
    embedding vector(1536),
    entreprise_id BIGINT NOT NULL REFERENCES entreprise(id) ON DELETE CASCADE,
    date_publication TIMESTAMP,
    date_expiration TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_offre_statut ON offre_emploi(statut);
CREATE INDEX IF NOT EXISTS idx_offre_entreprise ON offre_emploi(entreprise_id);
CREATE INDEX IF NOT EXISTS idx_offre_localisation ON offre_emploi(localisation);
CREATE INDEX IF NOT EXISTS idx_offre_type_contrat ON offre_emploi(type_contrat);

-- Table: competence
CREATE TABLE IF NOT EXISTS competence (
    id BIGSERIAL PRIMARY KEY,
    nom VARCHAR(255) UNIQUE NOT NULL,
    categorie VARCHAR(100),
    niveau VARCHAR(50) CHECK (niveau IN ('DEBUTANT', 'INTERMEDIAIRE', 'AVANCE', 'EXPERT'))
);

CREATE INDEX IF NOT EXISTS idx_competence_categorie ON competence(categorie);

-- Table: candidat_competence
CREATE TABLE IF NOT EXISTS candidat_competence (
    candidat_id BIGINT NOT NULL REFERENCES candidat(utilisateur_id) ON DELETE CASCADE,
    competence_id BIGINT NOT NULL REFERENCES competence(id) ON DELETE CASCADE,
    niveau_acquis VARCHAR(50) CHECK (niveau_acquis IN ('DEBUTANT', 'INTERMEDIAIRE', 'AVANCE', 'EXPERT')),
    PRIMARY KEY (candidat_id, competence_id)
);

-- Table: offre_competence
CREATE TABLE IF NOT EXISTS offre_competence (
    offre_id BIGINT NOT NULL REFERENCES offre_emploi(id) ON DELETE CASCADE,
    competence_id BIGINT NOT NULL REFERENCES competence(id) ON DELETE CASCADE,
    niveau_requis VARCHAR(50) CHECK (niveau_requis IN ('DEBUTANT', 'INTERMEDIAIRE', 'AVANCE', 'EXPERT')),
    PRIMARY KEY (offre_id, competence_id)
);

-- Table: experience
CREATE TABLE IF NOT EXISTS experience (
    id BIGSERIAL PRIMARY KEY,
    titre VARCHAR(255) NOT NULL,
    entreprise VARCHAR(255) NOT NULL,
    date_debut DATE NOT NULL,
    date_fin DATE,
    en_cours BOOLEAN DEFAULT FALSE,
    description TEXT,
    candidat_id BIGINT NOT NULL REFERENCES candidat(utilisateur_id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_experience_candidat ON experience(candidat_id);
CREATE INDEX IF NOT EXISTS idx_experience_date_debut ON experience(date_debut);

-- Table: candidature
CREATE TABLE IF NOT EXISTS candidature (
    id BIGSERIAL PRIMARY KEY,
    date_postulation TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    statut VARCHAR(50) DEFAULT 'EN_ATTENTE' CHECK (statut IN ('EN_ATTENTE', 'EN_ETUDE', 'ENTRETIEN_PREVU', 'ACCEPTEE', 'REFUSEE')),
    lettre_motivation TEXT,
    score_matching INTEGER CHECK (score_matching >= 0 AND score_matching <= 100),
    candidat_id BIGINT NOT NULL REFERENCES candidat(utilisateur_id) ON DELETE CASCADE,
    offre_id BIGINT NOT NULL REFERENCES offre_emploi(id) ON DELETE CASCADE,
    UNIQUE (candidat_id, offre_id)
);

CREATE INDEX IF NOT EXISTS idx_candidature_statut ON candidature(statut);
CREATE INDEX IF NOT EXISTS idx_candidature_candidat ON candidature(candidat_id);
CREATE INDEX IF NOT EXISTS idx_candidature_offre ON candidature(offre_id);
CREATE INDEX IF NOT EXISTS idx_candidature_score_matching ON candidature(score_matching);

-- =====================================================
-- Table: cv_embeddings (Vector Store pour Spring AI)
-- =====================================================
CREATE TABLE IF NOT EXISTS cv_embeddings (
    id BIGSERIAL PRIMARY KEY,
    embedding vector(1536) NOT NULL,
    metadata JSON,
    owner_id BIGINT NOT NULL,
    owner_type VARCHAR(50) NOT NULL CHECK (owner_type IN ('CANDIDAT', 'OFFRE')),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Index vectoriel pour la recherche de similarité
CREATE INDEX IF NOT EXISTS idx_cv_embeddings_vector
    ON cv_embeddings USING ivfflat (embedding vector_cosine_ops) WITH (lists = 100);

-- Index sur owner
CREATE INDEX IF NOT EXISTS idx_cv_embeddings_owner
    ON cv_embeddings(owner_id, owner_type);

-- =====================================================
-- Données initiales - Compétences communes
-- =====================================================
INSERT INTO competence (nom, categorie, niveau) VALUES
    -- Langages de programmation
    ('Java', 'LANGAGE', 'INTERMEDIAIRE'),
    ('Python', 'LANGAGE', 'INTERMEDIAIRE'),
    ('JavaScript', 'LANGAGE', 'INTERMEDIAIRE'),
    ('TypeScript', 'LANGAGE', 'INTERMEDIAIRE'),
    ('C++', 'LANGAGE', 'INTERMEDIAIRE'),
    ('C#', 'LANGAGE', 'INTERMEDIAIRE'),
    ('Go', 'LANGAGE', 'INTERMEDIAIRE'),
    ('Rust', 'LANGAGE', 'INTERMEDIAIRE'),
    ('PHP', 'LANGAGE', 'INTERMEDIAIRE'),
    ('Ruby', 'LANGAGE', 'INTERMEDIAIRE'),

    -- Frameworks Backend
    ('Spring Boot', 'FRAMEWORK', 'INTERMEDIAIRE'),
    ('Spring Framework', 'FRAMEWORK', 'INTERMEDIAIRE'),
    ('Express.js', 'FRAMEWORK', 'INTERMEDIAIRE'),
    ('Django', 'FRAMEWORK', 'INTERMEDIAIRE'),
    ('Flask', 'FRAMEWORK', 'INTERMEDIAIRE'),
    ('FastAPI', 'FRAMEWORK', 'INTERMEDIAIRE'),
    ('.NET Core', 'FRAMEWORK', 'INTERMEDIAIRE'),
    ('Laravel', 'FRAMEWORK', 'INTERMEDIAIRE'),

    -- Frameworks Frontend
    ('React', 'FRAMEWORK', 'INTERMEDIAIRE'),
    ('Angular', 'FRAMEWORK', 'INTERMEDIAIRE'),
    ('Vue.js', 'FRAMEWORK', 'INTERMEDIAIRE'),
    ('Next.js', 'FRAMEWORK', 'INTERMEDIAIRE'),
    ('Svelte', 'FRAMEWORK', 'INTERMEDIAIRE'),

    -- Bases de données
    ('PostgreSQL', 'BASE_DE_DONNEES', 'INTERMEDIAIRE'),
    ('MySQL', 'BASE_DE_DONNEES', 'INTERMEDIAIRE'),
    ('MongoDB', 'BASE_DE_DONNEES', 'INTERMEDIAIRE'),
    ('Redis', 'BASE_DE_DONNEES', 'INTERMEDIAIRE'),
    ('Elasticsearch', 'BASE_DE_DONNEES', 'INTERMEDIAIRE'),
    ('Oracle', 'BASE_DE_DONNEES', 'INTERMEDIAIRE'),
    ('SQL Server', 'BASE_DE_DONNEES', 'INTERMEDIAIRE'),

    -- DevOps & Cloud
    ('Docker', 'DEVOPS', 'INTERMEDIAIRE'),
    ('Kubernetes', 'DEVOPS', 'INTERMEDIAIRE'),
    ('AWS', 'CLOUD', 'INTERMEDIAIRE'),
    ('Azure', 'CLOUD', 'INTERMEDIAIRE'),
    ('GCP', 'CLOUD', 'INTERMEDIAIRE'),
    ('Jenkins', 'DEVOPS', 'INTERMEDIAIRE'),
    ('GitLab CI', 'DEVOPS', 'INTERMEDIAIRE'),
    ('GitHub Actions', 'DEVOPS', 'INTERMEDIAIRE'),
    ('Terraform', 'DEVOPS', 'INTERMEDIAIRE'),
    ('Ansible', 'DEVOPS', 'INTERMEDIAIRE'),

    -- Outils
    ('Git', 'OUTIL', 'INTERMEDIAIRE'),
    ('SVN', 'OUTIL', 'INTERMEDIAIRE'),
    ('Jira', 'OUTIL', 'INTERMEDIAIRE'),
    ('Confluence', 'OUTIL', 'INTERMEDIAIRE'),
    ('Maven', 'OUTIL', 'INTERMEDIAIRE'),
    ('Gradle', 'OUTIL', 'INTERMEDIAIRE'),
    ('npm', 'OUTIL', 'INTERMEDIAIRE'),
    ('yarn', 'OUTIL', 'INTERMEDIAIRE'),

    -- Méthodologies
    ('Agile', 'METHODOLOGIE', 'INTERMEDIAIRE'),
    ('Scrum', 'METHODOLOGIE', 'INTERMEDIAIRE'),
    ('Kanban', 'METHODOLOGIE', 'INTERMEDIAIRE'),
    ('SAFe', 'METHODOLOGIE', 'INTERMEDIAIRE'),

    -- Soft Skills
    ('Communication', 'SOFT_SKILL', 'INTERMEDIAIRE'),
    ('Travail d équipe', 'SOFT_SKILL', 'INTERMEDIAIRE'),
    ('Leadership', 'SOFT_SKILL', 'INTERMEDIAIRE'),
    ('Gestion de projet', 'SOFT_SKILL', 'INTERMEDIAIRE'),
    ('Résolution de problèmes', 'SOFT_SKILL', 'INTERMEDIAIRE'),
    ('Adaptabilité', 'SOFT_SKILL', 'INTERMEDIAIRE'),
    ('Créativité', 'SOFT_SKILL', 'INTERMEDIAIRE'),
    ('Esprit critique', 'SOFT_SKILL', 'INTERMEDIAIRE'),

    -- Langues
    ('Français', 'LANGUE', 'INTERMEDIAIRE'),
    ('Anglais', 'LANGUE', 'INTERMEDIAIRE'),
    ('Arabe', 'LANGUE', 'INTERMEDIAIRE'),
    ('Espagnol', 'LANGUE', 'INTERMEDIAIRE'),
    ('Allemand', 'LANGUE', 'INTERMEDIAIRE'),

    -- Architectures
    ('Microservices', 'ARCHITECTURE', 'INTERMEDIAIRE'),
    ('Monolithique', 'ARCHITECTURE', 'INTERMEDIAIRE'),
    ('Serverless', 'ARCHITECTURE', 'INTERMEDIAIRE'),
    ('Event-Driven', 'ARCHITECTURE', 'INTERMEDIAIRE'),

    -- Sécurité
    ('OWASP', 'SECURITE', 'INTERMEDIAIRE'),
    ('OAuth 2.0', 'SECURITE', 'INTERMEDIAIRE'),
    ('JWT', 'SECURITE', 'INTERMEDIAIRE'),
    ('LDAP', 'SECURITE', 'INTERMEDIAIRE'),

    -- Tests
    ('JUnit', 'TEST', 'INTERMEDIAIRE'),
    ('pytest', 'TEST', 'INTERMEDIAIRE'),
    ('Selenium', 'TEST', 'INTERMEDIAIRE'),
    ('Cypress', 'TEST', 'INTERMEDIAIRE'),
    ('Jest', 'TEST', 'INTERMEDIAIRE'),

    -- Mobile
    ('React Native', 'MOBILE', 'INTERMEDIAIRE'),
    ('Flutter', 'MOBILE', 'INTERMEDIAIRE'),
    ('Swift', 'MOBILE', 'INTERMEDIAIRE'),
    ('Kotlin', 'MOBILE', 'INTERMEDIAIRE'),

    -- Data & AI
    ('Machine Learning', 'DATA', 'INTERMEDIAIRE'),
    ('Deep Learning', 'DATA', 'INTERMEDIAIRE'),
    ('Data Science', 'DATA', 'INTERMEDIAIRE'),
    ('TensorFlow', 'DATA', 'INTERMEDIAIRE'),
    ('PyTorch', 'DATA', 'INTERMEDIAIRE'),
    ('Pandas', 'DATA', 'INTERMEDIAIRE'),
    ('NumPy', 'DATA', 'INTERMEDIAIRE'),
    ('Spark', 'DATA', 'INTERMEDIAIRE'),

ON CONFLICT (nom) DO NOTHING;

-- =====================================================
-- Utilisateur administrateur par défaut
-- =====================================================
-- Mot de passe: admin123 (à hasher avec BCrypt)
INSERT INTO utilisateur (email, password, nom, prenom, role, est_valide)
VALUES ('admin@recrutement.ma', '$2a$10$N9qo8uLOickgx2ZMRZoMye1j/cKGYlLKC9GLHjpWZ7LjGZx.LY4iG', 'Admin', 'Système', 'ADMINISTRATEUR', true)
ON CONFLICT (email) DO NOTHING;

INSERT INTO administrateur (utilisateur_id, niveau)
SELECT id, 'SUPER_ADMIN'
FROM utilisateur
WHERE email = 'admin@recrutement.ma'
ON CONFLICT DO NOTHING;

-- =====================================================
-- Vues
-- =====================================================

-- Vue: Candidats avec détails
CREATE OR REPLACE VIEW v_candidats_complets AS
SELECT
    u.id,
    u.email,
    u.nom,
    u.prenom,
    c.titre,
    c.telephone,
    c.adresse,
    c.cv_path,
    COUNT(DISTINCT cc.competence_id) as nb_competences,
    COUNT(DISTINCT exp.id) as nb_experiences,
    COUNT(DISTINCT cand.id) as nb_candidatures
FROM utilisateur u
JOIN candidat c ON u.id = c.utilisateur_id
LEFT JOIN candidat_competence cc ON c.utilisateur_id = cc.candidat_id
LEFT JOIN experience exp ON c.utilisateur_id = exp.candidat_id
LEFT JOIN candidature cand ON c.utilisateur_id = cand.candidat_id
WHERE u.role = 'CANDIDAT'
GROUP BY u.id, u.email, u.nom, u.prenom, c.titre, c.telephone, c.adresse, c.cv_path;

-- Vue: Offres avec détails
CREATE OR REPLACE VIEW v_offres_completes AS
SELECT
    o.id,
    o.titre,
    o.description,
    o.localisation,
    o.type_contrat,
    o.salaire_min,
    o.salaire_max,
    o.statut,
    o.date_publication,
    o.date_expiration,
    e.nom as entreprise_nom,
    e.secteur as entreprise_secteur,
    COUNT(DISTINCT oc.competence_id) as nb_competences,
    COUNT(DISTINCT cand.id) as nb_candidatures
FROM offre_emploi o
JOIN entreprise e ON o.entreprise_id = e.id
LEFT JOIN offre_competence oc ON o.id = oc.offre_id
LEFT JOIN candidature cand ON o.id = cand.offre_id
GROUP BY o.id, o.titre, o.description, o.localisation, o.type_contrat,
         o.salaire_min, o.salaire_max, o.statut, o.date_publication,
         o.date_expiration, e.nom, e.secteur;

-- Vue: Statistiques globales
CREATE OR REPLACE VIEW v_statistiques AS
SELECT
    (SELECT COUNT(*) FROM utilisateur WHERE role = 'CANDIDAT') as total_candidats,
    (SELECT COUNT(*) FROM utilisateur WHERE role = 'RECRUTEUR') as total_recruteurs,
    (SELECT COUNT(*) FROM utilisateur WHERE role = 'ADMINISTRATEUR') as total_admins,
    (SELECT COUNT(*) FROM entreprise) as total_entreprises,
    (SELECT COUNT(*) FROM offre_emploi WHERE statut = 'PUBLIEE') as total_offres_publiees,
    (SELECT COUNT(*) FROM offre_emploi) as total_offres,
    (SELECT COUNT(*) FROM candidature) as total_candidatures,
    (SELECT COUNT(*) FROM candidature WHERE statut = 'EN_ATTENTE') as candidatures_en_attente,
    (SELECT COUNT(*) FROM candidature WHERE statut = 'ACCEPTEE') as candidatures_acceptees,
    (SELECT COUNT(*) FROM competence) as total_competences;

-- Message de succès
DO $$
BEGIN
    RAISE NOTICE '===========================================';
    RAISE NOTICE 'Base de données initialisée avec succès!';
    RAISE NOTICE 'Utilisateur admin: admin@recrutement.ma';
    RAISE NOTICE 'Mot de passe: admin123';
    RAISE NOTICE '===========================================';
END $$;
```

---

## 6. Configuration Hibernate pour PgVector

### 6.1 Type Personnalisé Vector

```java
package ma.recrutement.config;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.usertype.UserType;

import java.io.Serializable;
import java.sql.Array;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Arrays;

public class VectorType implements UserType {

    @Override
    public int[] sqlTypes() {
        return new int[]{Types.ARRAY};
    }

    @Override
    public Class returnedClass() {
        return float[].class;
    }

    @Override
    public boolean equals(Object x, Object y) throws HibernateException {
        return Arrays.equals((float[]) x, (float[]) y);
    }

    @Override
    public int hashCode(Object x) throws HibernateException {
        return Arrays.hashCode((float[]) x);
    }

    @Override
    public Object nullSafeGet(ResultSet rs, String[] names, SharedSessionContractImplementor session, Object owner)
            throws SQLException {
        Array array = rs.getArray(names[0]);
        if (array == null) {
            return null;
        }
        return (float[]) array.getArray();
    }

    @Override
    public void nullSafeSet(PreparedStatement st, Object value, int index, SharedSessionContractImplementor session)
            throws SQLException {
        if (value == null) {
            st.setNull(index, Types.ARRAY);
        } else {
            float[] vector = (float[]) value;
            Float[] floatObjects = new Float[vector.length];
            for (int i = 0; i < vector.length; i++) {
                floatObjects[i] = vector[i];
            }
            st.setArray(index, st.getConnection().createArrayOf("REAL", floatObjects));
        }
    }

    @Override
    public Object deepCopy(Object value) throws HibernateException {
        if (value == null) {
            return null;
        }
        float[] original = (float[]) value;
        return Arrays.copyOf(original, original.length);
    }

    @Override
    public boolean isMutable() {
        return true;
    }

    @Override
    public Serializable disassemble(Object value) throws HibernateException {
        return (float[]) deepCopy(value);
    }

    @Override
    public Object assemble(Serializable cached, Object owner) throws HibernateException {
        return deepCopy(cached);
    }
}
```

---

## 7. Connection Pool Configuration

### 7.1 HikariCP Settings

```properties
# Maximum pool size
spring.datasource.hikari.maximum-pool-size=20

# Minimum idle connections
spring.datasource.hikari.minimum-idle=5

# Connection timeout (ms)
spring.datasource.hikari.connection-timeout=30000

# Idle timeout (ms)
spring.datasource.hikari.idle-timeout=600000

# Max lifetime (ms)
spring.datasource.hikari.max-lifetime=1800000

# Connection test query
spring.datasource.hikari.connection-test-query=SELECT 1

# Pool name
spring.datasource.hikari.pool-name=RecrutementHikariPool
```

---

## 8. Backup et Restauration

### 8.1 Backup

```bash
# Backup complet
docker exec recrutement-postgres pg_dump -U recrutement recrutement > backup.sql

# Backup avec données compressées
docker exec recrutement-postgres pg_dump -U recrutement -F c -f /tmp/backup.dump recrutement
docker cp recrutement-postgres:/tmp/backup.dump ./backup.dump
```

### 8.2 Restauration

```bash
# Depuis un fichier SQL
docker exec -i recrutement-postgres psql -U recrutement recrutement < backup.sql

# Depuis un dump compressé
docker cp ./backup.dump recrutement-postgres:/tmp/restore.dump
docker exec recrutement-postgres pg_restore -U recrutement -d recrutement /tmp/restore.dump
```

---

## 9. Surveillance et Maintenance

### 9.1 Requêtes de Diagnostic

```sql
-- Taille de la base de données
SELECT pg_size_pretty(pg_database_size('recrutement'));

-- Taille des tables
SELECT
    schemaname,
    tablename,
    pg_size_pretty(pg_total_relation_size(schemaname||'.'||tablename)) AS size
FROM pg_tables
WHERE schemaname = 'public'
ORDER BY pg_total_relation_size(schemaname||'.'||tablename) DESC;

-- Connexions actives
SELECT count(*) FROM pg_stat_activity WHERE datname = 'recrutement';

-- Verrous actifs
SELECT * FROM pg_locks WHERE pid IN (SELECT pid FROM pg_stat_activity WHERE datname = 'recrutement');

-- Statistiques PgVector
SELECT
    schemaname,
    tablename,
    indexname,
    indexdef
FROM pg_indexes
WHERE indexname LIKE '%vector%';
```

### 9.2 Maintenance Régulière

```sql
-- VACUUM ANALYZE (à exécuter régulièrement)
VACUUM ANALYZE;

-- Réindexation
REINDEX DATABASE recrutement;

-- Nettoyage des tables orphelines
DELETE FROM candidat_competence WHERE candidat_id NOT IN (SELECT utilisateur_id FROM candidat);
DELETE FROM offre_competence WHERE offre_id NOT IN (SELECT id FROM offre_emploi);
DELETE FROM experience WHERE candidat_id NOT IN (SELECT utilisateur_id FROM candidat);
```

---

## 10. Sécurité

### 10.1 Bonnes Pratiques

1. **Mots de passe forts**: Utiliser des mots de passe complexes pour les utilisateurs PostgreSQL
2. **Restriction d'accès**: Configurer `pg_hba.conf` pour restreindre les connexions
3. **SSL**: Activer SSL pour les connexions distantes
4. **Backups réguliers**: Automatiser les backups quotidiens
5. **Monitoring**: Surveiller les performances et les logs

### 10.2 Configuration pg_hba.conf

```conf
# TYPE  DATABASE        USER            ADDRESS                 METHOD

# Local connections
local   all             recrutement                              md5

# IPv4 local connections
host    recrutement     recrutement      127.0.0.1/32            md5

# Docker network
host    recrutement     recrutement      172.16.0.0/12          md5

# Reject all other connections
host    all             all              0.0.0.0/0               reject
```
