# psql Commands Guide - Database Exploration

## Connection

### Connect to Database
```bash
# Connect to specific database
psql -h 193.24.208.37 -p 5432 -U recrutement_user -d recrutement

# Or connect and then switch database
psql -h 193.24.208.37 -p 5432 -U recrutement_user
\c recrutement

# Connection string format
postgresql://recrutement_user:pswrd@193.24.208.37:5432/recrutement
```

---

## 1. Database Overview

### List All Databases
```sql
\l
-- OR
SELECT datname FROM pg_database WHERE datistemplate = false ORDER BY datname;
```

### Current Database Info
```sql
SELECT current_database();
SELECT current_user;
SELECT version();
```

### Database Size
```sql
SELECT pg_database.datname,
       pg_size_pretty(pg_database_size(pg_database.datname)) AS size
FROM pg_database
WHERE datname = 'recrutement'
ORDER BY pg_database.datname;
```

---

## 2. Tables Overview

### List All Tables
```sql
\dt
-- OR
SELECT tablename FROM pg_tables WHERE schemaname = 'public' ORDER BY tablename;
```

### List All Tables with Sizes
```sql
SELECT schemaname,
       tablename,
       pg_size_pretty(pg_total_relation_size(schemaname::text||'.'||tablename::text)) AS size
FROM pg_tables
WHERE schemaname = 'public'
ORDER BY pg_total_relation_size(schemaname::text||'.'||tablename::text) DESC;
```

### List All Sequences
```sql
\ds
```

### List All Views
```sql
\dv
```

---

## 3. Table Structures

### Describe a Table
```sql
\d candidats
\d offres_emploi
\d candidatures
\d utilisateurs
```

### Get Column Details
```sql
SELECT column_name,
       data_type,
       is_nullable,
       column_default
FROM information_schema.columns
WHERE table_name = 'candidats'
ORDER BY ordinal_position;
```

### Get All Constraints
```sql
SELECT conname AS constraint_name,
       contype AS constraint_type
FROM pg_constraint
WHERE conrelid = 'candidats'::regclass
ORDER BY conname;
```

### Get All Indexes
```sql
\di candidats
-- OR
SELECT indexname,
       indexdef
FROM pg_indexes
WHERE tablename = 'candidats';
```

---

## 4. Count Records

### Count All Records per Table
```sql
SELECT
    'utilisateurs' AS table_name, COUNT(*) AS count FROM utilisateurs
UNION ALL SELECT 'candidats', COUNT(*) FROM candidats
UNION ALL SELECT 'recruteurs', COUNT(*) FROM recruteurs
UNION ALL SELECT 'administrateurs', COUNT(*) FROM administrateurs
UNION ALL SELECT 'entreprises', COUNT(*) FROM entreprises
UNION ALL SELECT 'offres_emploi', COUNT(*) FROM offres_emploi
UNIONION ALL SELECT 'candidatures', COUNT(*) FROM candidatures
UNIONION ALL SELECT 'competences', COUNT(*) FROM competences
UNIONION ALL SELECT 'experiences', COUNT(*) FROM experiences
ORDER BY count DESC;
```

### Quick Row Count
```sql
SELECT COUNT(*) FROM candidats;
SELECT COUNT(*) FROM offres_emploi;
SELECT COUNT(*) FROM candidatures;
SELECT COUNT(*) FROM cv_embeddings;
```

---

## 5. Users Data

### All Users by Role
```sql
SELECT u.role,
       COUNT(*) AS count,
       COUNT(*) FILTER (WHERE u.email_verifie = true) AS verified
FROM utilisateurs u
GROUP BY u.role
ORDER BY count DESC;
```

### All Candidates
```sql
SELECT c.id,
       c.email,
       c.nom,
       c.prenom,
       c.statut,
       c.cv_path IS NOT NULL AS has_cv,
       c.cv_text IS NOT NULL AS has_cv_text,
       c.date_creation
FROM candidats c
ORDER BY c.date_creation DESC;
```

### All Recruiters with Companies
```sql
SELECT r.id,
       r.email,
       r.nom,
       r.prenom,
       COUNT(e.id) AS nb_entreprises,
       COUNT(o.id) AS nb_offres
FROM recruteurs r
LEFT JOIN entreprises e ON e.recruteur_id = r.id
LEFT JOIN offres_emploi o ON o.entreprise_id = e.id
GROUP BY r.id, r.email, r.nom, r.prenom
ORDER BY nb_offres DESC;
```

### Candidates with Complete Profiles
```sql
SELECT c.id,
       c.nom,
       c.prenom,
       c.email,
       COUNT(comp.id) AS nb_competences,
       COUNT(exp.id) AS nb_experiences,
       c.cv_path IS NOT NULL AS has_cv
FROM candidats c
LEFT JOIN competences comp ON comp.candidat_id = c.id
LEFT JOIN experiences exp ON exp.candidat_id = c.id
GROUP BY c.id, c.nom, c.prenom, c.email, c.cv_path
HAVING COUNT(comp.id) > 0 OR c.cv_path IS NOT NULL
ORDER BY nb_competences DESC, nb_experiences DESC;
```

---

## 6. Job Offers Data

### All Published Job Offers
```sql
SELECT o.id,
       o.reference,
       o.titre,
       o.type_contrat,
       o.salaire_min,
       o.salaire_max,
       o.statut,
       e.nom AS entreprise,
       o.ville,
       o.pays,
       COUNT(ca.id) AS nb_candidatures,
       o.date_creation
FROM offres_emploi o
JOIN entreprises e ON e.id = o.entreprise_id
LEFT JOIN candidatures ca ON ca.offre_id = o.id
WHERE o.statut = 'PUBLIEE' AND o.actif = true
GROUP BY o.id, o.reference, o.titre, o.type_contrat, o.salaire_min, o.salaire_max, o.statut, e.nom, o.ville, o.pays, o.date_creation
ORDER BY o.date_creation DESC;
```

### Job Offers by Salary Range
```sql
SELECT
    CASE
        WHEN salaire_min < 30000 THEN '< 30k'
        WHEN salaire_min < 50000 THEN '30k - 50k'
        WHEN salaire_min < 70000 THEN '50k - 70k'
        WHEN salaire_min < 100000 THEN '70k - 100k'
        ELSE '> 100k'
    END AS salary_range,
    COUNT(*) AS count
FROM offres_emploi
WHERE statut = 'PUBLIEE' AND actif = true
GROUP BY salary_range
ORDER BY MIN(salaire_min);
```

### Job Offers by Type
```sql
SELECT type_contrat,
       COUNT(*) AS count,
       ROUND(AVG(salaire_min)) AS avg_min_salary,
       ROUND(AVG(salaire_max)) AS avg_max_salary
FROM offres_emploi
WHERE statut = 'PUBLIEE' AND actif = true
GROUP BY type_contrat
ORDER BY count DESC;
```

---

## 7. Applications Data

### All Applications with Details
```sql
SELECT ca.id,
       ca.statut,
       ca.date_candidature,
       ca.lettre_motivation,
       c.nom AS candidat_nom,
       c.prenom AS candidat_prenom,
       o.titre AS offre_titre,
       e.nom AS entreprise,
       o.salaire_min,
       o.salaire_max
FROM candidatures ca
JOIN candidats c ON c.candidat_id = c.id
JOIN offres_emploi o ON o.id = ca.offre_id
JOIN entreprises e ON e.id = o.entreprise_id
ORDER BY ca.date_candidature DESC;
```

### Applications by Status
```sql
SELECT statut,
       COUNT(*) AS count
FROM candidatures
GROUP BY statut
ORDER BY count DESC;
```

### Most Popular Job Offers
```sql
SELECT o.id,
       o.titre,
       e.nom AS entreprise,
       COUNT(ca.id) AS nb_candidatures
FROM offres_emploi o
JOIN entreprises e ON e.id = o.entreprise_id
LEFT JOIN candidatures ca ON ca.offre_id = o.id
WHERE o.statut = 'PUBLIEE'
GROUP BY o.id, o.titre, e.nom
ORDER BY nb_candidatures DESC
LIMIT 10;
```

---

## 8. Skills & Competences

### All Competences by Category
```sql
SELECT categorie,
       nom,
       COUNT(*) AS count
FROM competences
GROUP BY categorie, nom
ORDER BY categorie, count DESC;
```

### Most Common Skills
```sql
SELECT nom,
       COUNT(*) AS count
FROM competences
GROUP BY nom
ORDER BY count DESC
LIMIT 20;
```

### Candidates by Skill Level
```sql
SELECT c.nom AS competence_nom,
       comp.niveau,
       COUNT(DISTINCT comp.candidat_id) AS nb_candidats
FROM competences comp
JOIN candidats c ON c.id = comp.candidat_id
GROUP BY c.nom, comp.niveau
ORDER BY c.nom, comp.niveau;
```

### Skills Match Analysis
```sql
SELECT comp.nom AS skill,
       COUNT(DISTINCT comp.candidat_id) AS candidates_with_skill
FROM competences comp
GROUP BY comp.nom
ORDER BY candidates_with_skill DESC
LIMIT 10;
```

---

## 9. Experiences Data

### Experience by Type of Company
```sql
SELECT type_entreprise,
       COUNT(*) AS count,
       ROUND(AVG(EXTRACT(YEAR FROM date_fin) - EXTRACT(YEAR FROM date_debut)), 1) AS avg_years
FROM experiences
WHERE date_debut IS NOT NULL
GROUP BY type_entreprise
ORDER BY count DESC;
```

### Candidates with Experience Duration
```sql
SELECT c.id,
       c.nom,
       c.prenom,
       COUNT(e.id) AS nb_experiences,
       SUM(CASE
           WHEN e.date_fin IS NULL THEN EXTRACT(YEAR FROM AGE(CURRENT_DATE, e.date_debut))
           ELSE EXTRACT(YEAR FROM AGE(e.date_fin, e.date_debut))
       END) AS total_years
FROM candidats c
LEFT JOIN experiences e ON e.candidat_id = c.id
GROUP BY c.id, c.nom, c.prenom
HAVING COUNT(e.id) > 0
ORDER BY total_years DESC;
```

---

## 10. AI Vector Store Data

### Check pgvector Extension
```sql
SELECT extname, extversion
FROM pg_extension
WHERE extname = 'vector';
```

### Vector Store Table Structure
```sql
\d cv_embeddings
```

### Count Embeddings by Type
```sql
SELECT
    metadata->>'type' AS type,
    COUNT(*) AS count
FROM cv_embeddings
GROUP BY type;
```

### All CV Embeddings
```sql
SELECT
    id,
    LEFT(content, 50) AS content_preview,
    metadata->>'candidatId' AS candidat_id,
    metadata->>'type' AS type,
    metadata->>'nom' AS nom
FROM cv_embeddings
WHERE metadata->>'type' = 'cv'
ORDER BY id DESC;
```

### All Job Embeddings
```sql
SELECT
    id,
    LEFT(content, 50) AS content_preview,
    metadata->>'offreId' AS offre_id,
    metadata->>'type' AS type,
    metadata->>'titre' AS titre
FROM cv_embeddings
WHERE metadata->>'type' = 'offre'
ORDER BY id DESC;
```

### Embedding Dimensions
```sql
SELECT
    vector_dims(embedding) AS dimensions
FROM cv_embeddings
LIMIT 1;
```

---

## 11. Performance & Indexes

### All Indexes in Database
```sql
SELECT
    schemaname,
    tablename,
    indexname,
    indexdef
FROM pg_indexes
WHERE schemaname = 'public'
ORDER BY tablename, indexname;
```

### Index Sizes
```sql
SELECT
    schemaname,
    tablename,
    indexname,
    pg_size_pretty(pg_relation_size('public.'||tablename||'.'||indexname)) AS size
FROM pg_indexes
WHERE schemaname = 'public'
ORDER BY pg_relation_size('public.'||tablename||'.'||indexname) DESC;
```

### Vector Index Details
```sql
SELECT indexname,
       indexdef
FROM pg_indexes
WHERE tablename = 'cv_embeddings';
```

---

## 12. Recent Activity

### Recent Registrations
```sql
SELECT role,
       email,
       nom,
       prenom,
       date_creation
FROM utilisateurs
ORDER BY date_creation DESC
LIMIT 20;
```

### Recent Applications
```sql
SELECT
    ca.date_candidature,
    c.nom || ' ' || c.prenom AS candidat,
    o.titre AS offre,
    e.nom AS entreprise,
    ca.statut
FROM candidatures ca
JOIN candidats c ON c.candidat_id = c.id
JOIN offres_emploi o ON o.id = ca.offre_id
JOIN entreprises e ON e.id = o.entreprise_id
ORDER BY ca.date_candidature DESC
LIMIT 20;
```

### Recent Job Offers
```sql
SELECT o.reference,
       o.titre,
       e.nom AS entreprise,
       o.type_contrat,
       o.statut,
       o.date_creation
FROM offres_emploi o
JOIN entreprises e ON e.id = o.entreprise_id
ORDER BY o.date_creation DESC
LIMIT 20;
```

---

## 13. Data Quality Checks

### Candidates Without CV
```sql
SELECT id, email, nom, prenom
FROM candidats
WHERE cv_path IS NULL
ORDER BY date_creation;
```

### Candidates Without Skills
```sql
SELECT c.id, c.email, c.nom, c.prenom
FROM candidats c
LEFT JOIN competences comp ON comp.candidat_id = c.id
WHERE comp.id IS NULL
ORDER BY c.date_creation;
```

### Job Offers Without Candidates
```sql
SELECT o.id, o.titre, e.nom, o.date_creation
FROM offres_emploi o
JOIN entreprises e ON e.id = o.entreprise_id
LEFT JOIN candidatures ca ON ca.offre_id = o.id
WHERE o.statut = 'PUBLIEE' AND ca.id IS NULL
ORDER BY o.date_creation;
```

---

## 14. Statistics Queries

### Complete Dashboard
```sql
-- Users by role
SELECT 'Candidats' AS type, COUNT(*) FROM candidats
UNION ALL
SELECT 'Recruteurs' AS type, COUNT(*) FROM recruteurs
UNION ALL
SELECT 'Entreprises' AS type, COUNT(*) FROM entreprises
UNION ALL
SELECT 'Offres' AS type, COUNT(*) FROM offres_emploi
UNIONION ALL
SELECT 'Candidatures' AS type, COUNT(*) FROM candidatures;

-- Skills distribution
SELECT
    CASE
        WHEN categorie = 'LANGAGE' THEN 'Languages'
        WHEN categorie = 'FRAMEWORK' THEN 'Frameworks'
        WHEN categorie = 'DATABASE' THEN 'Databases'
        WHEN categorie = 'CLOUD' THEN 'Cloud/DevOps'
        WHEN categorie = 'OUTIL' THEN 'Tools'
        WHEN categorie = 'SOFT_SKILL' THEN 'Soft Skills'
        WHEN categorie = 'LANGUE' THEN 'Languages'
        ELSE 'Other'
    END AS category_type,
    COUNT(*) AS count
FROM competences
GROUP BY category_type
ORDER BY count DESC;
```

---

## 15. Search Queries

### Find Candidates by Name
```sql
SELECT * FROM candidats
WHERE nom ILIKE '%martin%' OR prenom ILIKE '%martin%';
```

### Find Jobs by Title
```sql
SELECT o.*, e.nom AS entreprise
FROM offres_emploi o
JOIN entreprises e ON e.id = o.entreprise_id
WHERE o.titre ILIKE '%java%' OR o.description ILIKE '%java%';
```

### Find by Date Range
```sql
-- Job offers created in last 7 days
SELECT * FROM offres_emploi
WHERE date_creation >= NOW() - INTERVAL '7 days';
```

---

## 16. Cleanup Commands

### Clear Test Data
```sql
-- Delete test candidates (email contains "test")
DELETE FROM candidats WHERE email LIKE '%test%';

-- Delete test applications
DELETE FROM candidatures WHERE offre_id IN (
    SELECT id FROM offres_emploi WHERE reference LIKE '%TEST%'
);

-- Delete test job offers
DELETE FROM offres_emploi WHERE reference LIKE '%TEST%';
```

### Reset Vector Store
```sql
-- Clear all embeddings
TRUNCATE TABLE cv_embeddings CASCADE;

-- Reset sequences
ALTER SEQUENCE cv_embeddings_id_seq RESTART WITH 1;
```

---

## 17. System Information

### Database Connections
```sql
SELECT
    state,
    COUNT(*) AS connections,
    COUNT(*) FILTER (WHERE state = 'active') AS active
FROM pg_stat_activity
GROUP BY state;
```

### Table & Index Sizes
```sql
SELECT
    schemaname,
    tablename,
    pg_size_pretty(pg_total_relation_size(schemaname||'.'||tablename)) AS total_size,
    pg_size_pretty(pg_relation_size(schemaname||'.'||tablename)) AS table_size,
    pg_size_pretty(pg_indexes_size(schemaname||'.'||tablename)) AS indexes_size
FROM pg_tables
WHERE schemaname = 'public'
ORDER BY pg_total_relation_size(schemaname||'.'||tablename) DESC;
```

---

## 18. Quick Copy-Paste Queries

### Full Database Snapshot
```sql
-- Users summary
SELECT 'USERS' AS section, role, COUNT(*) FROM utilisateurs GROUP BY role
UNION ALL
SELECT 'CANDIDATES' AS section, COUNT(*) FROM candidats
UNION ALL
SELECT 'WITH_CV' AS section, COUNT(*) FROM candidats WHERE cv_path IS NOT NULL
UNION ALL
SELECT 'WITH_SKILLS' AS section, COUNT(DISTINCT candidat_id) FROM competences
UNION ALL
SELECT 'JOB_OFFERS' AS section, COUNT(*) FROM offres_emploi
UNIONION ALL
SELECT 'PUBLISHED_OFFERS' AS section, COUNT(*) FROM offres_emploi WHERE statut = 'PUBLIEE'
UNION ALL
UNION ALL
SELECT 'APPLICATIONS' AS section, COUNT(*) FROM candidatures
UNIONION ALL
SELECT 'VECTORS' AS section, COUNT(*) FROM cv_embeddings
ORDER BY section;
```

### Matching Status Check
```sql
-- Candidates indexed
SELECT
    COUNT(DISTINCT c.id) AS total_candidates,
    COUNT(DISTINCT CASE WHEN c.cv_vector_id IS NOT NULL THEN c.id END AS indexed_cv
FROM candidats c;

-- Jobs indexed
SELECT
    COUNT(DISTINCT o.id) AS total_jobs,
    COUNT(DISTINCT CASE WHEN o.vector_id IS NOT NULL THEN o.id END AS indexed_jobs
FROM offres_emploi o;

-- Vector store breakdown
SELECT
    metadata->>'type' AS type,
    COUNT(*) AS count
FROM cv_embeddings
GROUP BY type;
```

---

## 19. psql Meta-Commands

### Useful psql Commands
```bash
\?          -- Show help
\l          -- List databases
\dt         -- List tables
\di         -- List indexes
\ds         -- List sequences
\dv         -- List views
\du         -- List users
\dn         -- List schemas
\z          -- List all objects (with sizes)
\conninfo   -- Show connection info
```

### Table Operations
```sql
\d table_name        -- Describe table
\dt+                -- List tables with sizes
\dt+ schema.*     -- List tables in schema
```

### Output Formatting
```sql
\x                  -- Toggle expanded output
\a                  -- Toggle align output
\t                  -- Toggle timed output
\T                  -- Show timing
```

---

## 20. Export & Import

### Export Query to CSV
```sql
\o candidates.csv
COPY (SELECT id, email, nom, prenom, telephone FROM candidats) TO stdout WITH CSV HEADER;
\o
```

### Import from CSV
```sql
COPY candidats(email, nom, prenom)
FROM '/path/to/candidates.csv'
DELIMITER ',' CSV HEADER;
```

### Backup Database
```bash
pg_dump -h 193.24.208.37 -U recrutement_user -d recrutement > backup.sql
```

### Restore Database
```bash
psql -h 193.24.208.37 -U recrutement_user -d recrutement < backup.sql
```

---

## 21. Monitoring Queries

### Active Queries
```sql
SELECT
    pid,
    now() - query_start AS duration,
    state,
    query
FROM pg_stat_activity
WHERE state != 'idle'
ORDER BY duration DESC;
```

### Long-Running Queries
```sql
SELECT
    pid,
    now() - query_start AS duration,
    state,
    query
FROM pg_stat_activity
WHERE now() - query_start > INTERVAL '5 minutes'
ORDER BY duration DESC;
```

### Table Bloat
```sql
SELECT
    schemaname,
    tablename,
    pg_size_pretty(pg_total_relation_size(schemaname||'.'||tablename)) AS total_size,
    pg_size_pretty(pg_relation_size(schemaname||'.'||tablename)) AS table_size,
    ROUND(100 * (pg_total_relation_size(schemaname||'.'||tablename) - pg_relation_size(schemaname||'.'||tablename))::numeric / pg_total_relation_size(schemaname||'.'||tablename), 2) AS bloat_percentage
FROM pg_tables
WHERE schemaname = 'public'
ORDER BY bloat_percentage DESC;
```

---

## 22. Performance Analysis

### Slow Queries (if pg_stat_statements available)
```sql
SELECT
    query,
    calls,
    total_time,
    mean_time,
    max_time
FROM pg_stat_statements
ORDER BY mean_time DESC
LIMIT 20;
```

### Table Access Statistics
```sql
SELECT
    schemaname,
    tablename,
    seq_scan,
    seq_tup_read,
    idx_scan,
    idx_tup_fetch,
    n_tup_ins,
    n_tup_upd,
    n_tup_del
FROM pg_stat_user_tables
ORDER BY schemaname, tablename;
```

---

## Quick Reference Card

```bash
# Connect
psql -h 193.24.208.37 -p 5432 -U recrutement_user -d recrutement

# Essential queries
\dt                    # List tables
\d candidats            # Describe table
SELECT COUNT(*) FROM candidats;  # Count rows

# Search
SELECT * FROM candidats WHERE email LIKE '%@%';

# Copy to CSV
\o output.csv
COPY (SELECT * FROM candidats) TO STDOUT WITH CSV HEADER;
```
