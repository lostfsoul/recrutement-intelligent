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
    ('Travail d equipe', 'SOFT_SKILL', 'INTERMEDIAIRE'),
    ('Leadership', 'SOFT_SKILL', 'INTERMEDIAIRE'),
    ('Gestion de projet', 'SOFT_SKILL', 'INTERMEDIAIRE'),
    ('Resolution de problemes', 'SOFT_SKILL', 'INTERMEDIAIRE'),
    ('Adaptabilite', 'SOFT_SKILL', 'INTERMEDIAIRE'),
    ('Creativite', 'SOFT_SKILL', 'INTERMEDIAIRE'),
    ('Esprit critique', 'SOFT_SKILL', 'INTERMEDIAIRE'),

    -- Langues
    ('Francais', 'LANGUE', 'INTERMEDIAIRE'),
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
    ('Spark', 'DATA', 'INTERMEDIAIRE')

ON CONFLICT (nom) DO NOTHING;

-- =====================================================
-- Utilisateur administrateur par défaut
-- =====================================================
-- Mot de passe: admin123 (hash BCrypt)
INSERT INTO utilisateur (email, password, nom, prenom, role, est_valide)
VALUES ('admin@recrutement.ma', '$2a$10$N9qo8uLOickgx2ZMRZoMye1j/cKGYlLKC9GLHjpWZ7LjGZx.LY4iG', 'Admin', 'Systeme', 'ADMINISTRATEUR', true)
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

-- =====================================================
-- Message de fin
-- =====================================================
DO $$
BEGIN
    RAISE NOTICE '===========================================';
    RAISE NOTICE 'Base de donnees initialisee avec succes!';
    RAISE NOTICE 'Utilisateur admin: admin@recrutement.ma';
    RAISE NOTICE 'Mot de passe: admin123';
    RAISE NOTICE '===========================================';
END $$;
