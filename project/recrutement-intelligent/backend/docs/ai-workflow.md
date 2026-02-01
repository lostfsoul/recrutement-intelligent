# AI Workflow - Recrutement Intelligent

## Vue d'ensemble

Le système de recrutement intelligent utilise l'IA et les embeddings vectoriels pour matcher automatiquement les candidats avec les offres d'emploi. Basé sur OpenAI (text-embedding-3-small) et pgvector pour le stockage des vecteurs.

---

## Architecture Technique

```
┌─────────────────────────────────────────────────────────────────┐
│                    AI RECRUITMENT SYSTEM                         │
├─────────────────────────────────────────────────────────────────┤
│                                                                   │
│  ┌──────────────┐    ┌─────────────┐    ┌─────────────────┐   │
│  │   CV Upload  │───▶│  CV Parsing │───▶│  OpenAI Embed   │   │
│  │              │    │             │    │   (1536-dim)    │   │
│  └──────────────┘    └─────────────┘    └────────┬────────┘   │
│                                                 │                │
│                                                 ▼                │
│  ┌────────────────────────────────────────────────────────┐    │
│  │              pgvector (PostgreSQL)                     │    │
│  │        • cv_embeddings (vector(1536))                  │    │
│  │        • IVFFLAT index (cosine similarity)            │    │
│  └───────────────────────────────┬────────────────────────┘    │
│                                  │                              │
│                                  ▼                              │
│  ┌────────────────────────────────────────────────────────┐    │
│  │            Semantic Search (Cosine Similarity)         │    │
│  │                     OpenAI Embedding + Query             │    │
│  └───────────────────────────────┬────────────────────────┘    │
│                                  │                              │
│                                  ▼                              │
│  ┌────────────────────────────────────────────────────────┐    │
│  │              Matching Engine Service                    │    │
│  │       • Find matching jobs for candidate              │    │
│  │       • Find matching candidates for job              │    │
│  │       • Score calculation & ranking                   │    │
│  └────────────────────────────────────────────────────────┘    │
│                                                                   │
└─────────────────────────────────────────────────────────────────┘
```

---

## Workflow Complet

```
┌─────────────────────────────────────────────────────────────────────────┐
│                        CANDIDAT WORKFLOW                              │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│  1. UPLOAD CV                                                            │
│     POST /api/v1/candidats/me/cv                                       │
│     ├─ Parse PDF/DOCX                                                   │
│     ├─ Extract text                                                     │
│     └─ Store cv_path                                                   │
│                                                                         │
│  2. SET CV TEXT (optionnel)                                              │
│     POST /api/v1/candidats/me/cv/text                                 │
│     └─ Direct text input for testing                                  │
│                                                                         │
│  3. INDEX CV IN VECTOR STORE ⭐                                        │
│     POST /api/v1/matching/cv/me/indexer                                │
│     ├─ Generate embedding (OpenAI text-embedding-3-small)            │
│     ├─ Store in pgvector with metadata                                │
│     └─ 1536-dimensional vector                                         │
│                                                                         │
│  4. FIND MATCHING JOBS ⭐⭐⭐                                         │
│     GET /api/v1/matching/candidats/{id}/offres?limit=5                 │
│     ├─ Generate query embedding                                        │
│     ├─ Vector similarity search (cosine distance)                    │
│     ├─ Filter by type="offre"                                          │
│     ├─ Calculate matching scores                                       │
│     └─ Return ranked results                                          │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────┐
│                       RECRUITER WORKFLOW                               │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│  1. CREATE JOB OFFER                                                    │
│     POST /api/v1/offres                                                │
│     └─ Store job with description                                     │
│                                                                         │
│  2. PUBLISH JOB OFFER                                                   │
│     POST /api/v1/offres/{id}/publier                                   │
│     └─ Set status to PUBLIEE                                           │
│                                                                         │
│  3. INDEX JOB IN VECTOR STORE ⭐                                        │
│     POST /api/v1/matching/offres/{id}/indexer                          │
│     ├─ Generate embedding from job description                         │
│     ├─ Store in pgvector with metadata                                │
│     └─ 1536-dimensional vector                                         │
│                                                                         │
│  4. FIND MATCHING CANDIDATES ⭐⭐⭐                                   │
│     GET /api/v1/matching/offres/{id}/candidats?limit=10                │
│     ├─ Generate query embedding                                        │
│     ├─ Vector similarity search (cosine distance)                    │
│     ├─ Filter by type="cv"                                             │
│     ├─ Calculate matching scores                                       │
│     └─ Return ranked results                                          │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘
```

---

## 1. Extraction des Compétences (CV Parsing)

### Parse CV from File

**Endpoint:** `POST /api/v1/ai/parse-cv`

**Content-Type:** `multipart/form-data`

**Requête:**
```
file: [fichier PDF/DOCX]
```

**Réponse:**
```json
{
  "text": "Alex Martin is a senior Java developer...",
  "fileName": "cv_alex_martin.pdf",
  "pages": 2
}
```

**Technologie:** Apache PDFBox (PDF), Apache POI (DOCX)

---

### Extract Skills using GPT-4

**Endpoint:** `POST /api/v1/ai/extract-skills`

**Content-Type:** `text/plain`

**Requête:**
```
Alex Martin is a senior Java developer with 5 years of experience. Expertise includes: Java 17, Spring Boot, Hibernate, PostgreSQL, React, Docker, Kubernetes, AWS (ECS, EKS), Jenkins CI/CD, Git. AWS Certified Solutions Architect Professional. Speaks English (C2), French (Native), German (B1). Has experience with microservices architecture and agile methodologies.
```

**Réponse:**
```json
{
  "competences": [
    {
      "nom": "Java",
      "categorie": "LANGAGE",
      "niveau": "EXPERT",
      "anneesExperience": 5
    },
    {
      "nom": "Spring Boot",
      "categorie": "FRAMEWORK",
      "niveau": "EXPERT",
      "anneesExperience": 5
    },
    {
      "nom": "PostgreSQL",
      "categorie": "DATABASE",
      "niveau": "AVANCE",
      "anneesExperience": 5
    },
    {
      "nom": "Docker",
      "categorie": "OUTIL",
      "niveau": "AVANCE",
      "anneesExperience": 3
    },
    {
      "nom": "Kubernetes",
      "categorie": "CLOUD",
      "niveau": "INTERMEDIAIRE",
      "anneesExperience": 2
    }
  ],
  "langues": [
    {"langue": "Anglais", "niveau": "C2"},
    {"langue": "Français", "niveau": "Natif"},
    {"langue": "Allemand", "niveau": "B1"}
  ],
  "certifications": [
    "AWS Certified Solutions Architect Professional"
  ]
}
```

---

### Complete CV Analysis (One-Shot)

**Endpoint:** `POST /api/v1/ai/analyze-cv`

**Content-Type:** `multipart/form-data`

**Réponse:**
```json
{
  "cvText": "...",
  "competences": [...],
  "langues": [...],
  "certifications": [...],
  "profilDetecte": {
    "titre": "Senior Java Developer",
    "anneesExperience": 5,
    "principalesCompetences": ["Java", "Spring Boot", "PostgreSQL"]
  }
}
```

---

## 2. Indexation Vectorielle

### Indexer Mon CV

**Endpoint:** `POST /api/v1/matching/cv/me/indexer`

**Description:** Convertit le texte du CV en vecteur 1536D et le stocke dans pgvector

**Headers:**
```
Authorization: Bearer {token}
```

**Réponse:** `204 No Content`

**Processus:**
1. Récupérer le CV text du candidat connecté
2. Générer l'embedding via OpenAI `text-embedding-3-small`
3. Stocker dans `cv_embeddings` avec metadata:
   ```json
   {
     "candidatId": "24",
     "type": "cv",
     "nom": "Martin Alex"
   }
   ```

**Logs:**
```
INFO pringAIConfig$TransactionalPgVectorStore : Inserted document: deleted=0, inserted=1, type=cv, id=...
INFO m.r.service.ai.MatchingEngineService     : CV indexé pour le candidat: 24
```

---

### Indexer Offre d'Emploi

**Endpoint:** `POST /api/v1/matching/offres/{offreId}/indexer`

**Processus:**
1. Récupérer l'offre + description
2. Construire le texte à indexer:
   ```
   {titre} - {description} - {competences} - {profilRecherche}
   ```
3. Générer l'embedding via OpenAI
4. Stocker dans `cv_embeddings` avec metadata:
   ```json
   {
     "offreId": "7",
     "type": "offre",
     "titre": "Java Developer",
     "entreprise": "Tech Corp"
   }
   ```

---

## 3. Matching Intelligent

### Trouver des Offres pour un Candidat ⭐

**Endpoint:** `GET /api/v1/matching/candidats/{candidatId}/offres?limit=5`

**Processus:**
```
1. Récupérer le CV du candidat
2. Générer l'embedding de la requête
3. Recherche vectorielle (cosine similarity) dans pgvector
4. Filtrer les résultats où type="offre"
5. Calculer le score de matching
6. Retourner les résultats classés
```

**Réponse:**
```json
[
  {
    "offreId": 7,
    "offreTitre": "Java Developer",
    "nomEntreprise": "Tech Corp",
    "candidatId": 24,
    "candidatNom": "Developer",
    "candidatPrenom": "Java",
    "scoreMatching": 7500,
    "scoreCompetences": 7500,
    "scoreExperience": 0,
    "scoreFormation": 0,
    "competencesMatch": [],
    "competencesManquantes": [],
    "recommendation": "Excellent match! Le profil correspond très bien aux exigences du poste.",
    "recommande": true,
    "reason": "Basé sur l'analyse sémantique du CV et de l'offre"
  }
]
```

**Scores:**
- `7500-10000` = Excellent match (recommandé)
- `5000-7499` = Bon match
- `2500-4999` = Match partiel
- `0-2499` = Faible correspondance

---

### Trouver des Candidats pour une Offre ⭐

**Endpoint:** `GET /api/v1/matching/offres/{offreId}/candidats?limit=10`

**Rôle requis:** `RECRUTEUR` ou `ADMINISTRATEUR`

**Processus similaire** mais filtre `type="cv"`

---

## 4. Algorithmes de Matching

### Recherche Sémantique

```sql
SELECT *, embedding <=> ?::vector AS distance
FROM cv_embeddings
ORDER BY distance
LIMIT ?
```

- `embedding <=> ?::vector` = Cosine distance
- Distance = 0 = Parfait
- Distance = 1 = Opposé

### Filtrage par Type

```java
results.stream()
    .filter(doc -> "offre".equals(doc.getMetadata().get("type")))
    .filter(doc -> "cv".equals(doc.getMetadata().get("type")))
```

### Calcul du Score

```java
int scoreMatching = (int) (vectorScore * 100);
```

Avec:
- `vectorScore` = Similarité cosinus (0 à 1)
- `scoreMatching` = 0 à 100

---

## 5. Configuration OpenAI

### Modèle d'Embedding

```properties
spring.ai.openai.embedding.options.model=text-embedding-3-small
spring.ai.openai.embedding.options.dimensions=1536
```

**Caractéristiques:**
- Modèle: `text-embedding-3-small`
- Dimensions: 1536
- Prix: ~$0.00002 / 1K tokens
- Max tokens: 8191

### Vector Store

```properties
spring.ai.vectorstore.pgvector.distance-type=COSINE_DISTANCE
spring.ai.vectorstore.pgvector.dimensions=1536
spring.ai.vectorstore.pgvector.index-type=IVFFLAT
```

---

## 6. Exemples d'Utilisation

### Scénario 1: Candidat cherche un emploi

```bash
# 1. S'inscrire et se connecter
TOKEN=$(curl -s -X POST http://localhost:8088/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"candidate.java@test.com","password":"password123"}' \
  | jq -r '.accessToken')

# 2. Définir le CV text
curl -X POST http://localhost:8088/api/v1/candidats/me/cv/text \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '"Java Developer with 5 years experience in Spring Boot, Hibernate, PostgreSQL, REST APIs, microservices."'

# 3. Indexer le CV
curl -X POST http://localhost:8088/api/v1/matching/cv/me/indexer \
  -H "Authorization: Bearer $TOKEN"

# 4. Trouver des offres matching
curl -X GET "http://localhost:8088/api/v1/matching/candidats/24/offres?limit=5" \
  -H "Authorization: Bearer $TOKEN" | jq '.'
```

### Scénario 2: Recruteur cherche des candidats

```bash
# 1. Créer et publier une offre (complet plus bas)
OFFRE_ID=$(curl -s -X POST http://localhost:8088/api/v1/offres \
  -H "Authorization: Bearer $RECRUTEUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"titre":"Senior Java Developer","description":"Looking for experienced Java developer...","typeContrat":"CDI"}' \
  | jq -r '.id')

# 2. Publier l'offre
curl -X POST http://localhost:8088/api/v1/offres/$OFFRE_ID/publier \
  -H "Authorization: Bearer $RECRUTEUR_TOKEN"

# 3. Indexer l'offre
curl -X POST http://localhost:8088/api/v1/matching/offres/$OFFRE_ID/indexer \
  -H "Authorization: Bearer $RECRUTEUR_TOKEN"

# 4. Trouver les candidats matching
curl -X GET "http://localhost:8088/api/v1/matching/offres/$OFFRE_ID/candidats?limit=10" \
  -H "Authorization: Bearer $RECRUTEUR_TOKEN" | jq '.'
```

---

## 7. Réponses Matching Détailées

### Format de Réponse Complet

```json
[
  {
    "offreId": 7,
    "offreTitre": "Java Developer",
    "offreReference": "OFF-FD1CC0A5",
    "offreDescription": "Looking for an experienced Java developer...",
    "nomEntreprise": "Tech Corp",
    "entrepriseVille": "Paris",
    "salaireMin": 45000,
    "salaireMax": 65000,
    "typeContrat": "CDI",

    "candidatId": 24,
    "candidatNom": "Developer",
    "candidatPrenom": "Java",
    "candidatEmail": "candidate.java@test.com",

    "scoreMatching": 7500,
    "scoreCompetences": 7500,
    "scoreExperience": 0,
    "scoreFormation": 0,

    "competencesMatch": [
      {"nom": "Java", "niveau": "EXPERT", "match": "EXACT"}
    ],

    "competencesManquantes": [
      {"nom": "Maven", "requis": "INTERMEDIAIRE"}
    ],

    "recommendation": "Excellent match! Le profil correspond très bien aux exigences du poste.",
    "recommande": true,
    "reason": "Basé sur l'analyse sémantique du CV et de l'offre"
  }
]
```

### Interprétation des Scores

| Score | Recommandé | Signification |
|-------|-----------|---------------|
| 75-100+ | ✅ Oui | Match excellent |
| 50-74 | ⚠️ Parfois | Bon match |
| 25-49 | ❌ Non | Match limité |
| 0-24 | ❌ Non | Faible correspondance |

---

## 8. Performance et Scalabilité

### Vector Index (IVFFLAT)

```
CREATE INDEX cv_embeddings_embedding_idx
ON cv_embeddings
USING ivfflat (embedding vector_cosine_ops)
WITH (lists = 100)
```

- **lists = 100** : Optimal pour ~10k-100k vectors
- **Cosine distance** : Mesure de similarité sémantique
- **Dimension 1536** : Espace vectoriel dense

### Temps de Recherche

- Indexation: ~1-3 secondes (API OpenAI + INSERT)
- Recherche: ~200-500ms (embedding + requête SQL)

---

## 9. Bonnes Pratiques

### Pour les Candidats

1. **CV détaillé** pour de meilleurs embeddings
2. **Mots-clés techniques** explicites
3. **Projets concrets** avec technologies
4. **Mettre à jour** le CV régulièrement

### Pour les Recruteurs

1. **Descriptions détaillées** des offres
2. **Compétences requises** listées
3. **Profil recherché** spécifié
4. **Indexer chaque nouvelle offre**

---

## 10. Monitoring

### Logs de Debug

```bash
# Indexation CV
INFO pringAIConfig$TransactionalPgVectorStore : Inserted document: deleted=1, inserted=1, type=cv
INFO m.r.service.ai.MatchingEngineService     : CV indexé pour le candidat: 24

# Indexation Offre
INFO pringAIConfig$TransactionalPgVectorStore : Inserted document: deleted=1, inserted=1, type=offre
INFO m.r.service.ai.MatchingEngineService     : Offre indexée: 7

# Recherche
INFO pringAIConfig$TransactionalPgVectorStore : SimilaritySearch returned 5 results
```

### Métriques Clés

- **Taux de matching** > 50% = Excellent
- **Score moyen** > 6000 = Bon matching
- **Temps de réponse** < 1s = Acceptable

---

## 11. Dépannage

### Problème: Aucun résultat de matching

**Causes possibles:**
1. CV non indexé → Vérifier avec `GET /api/v1/candidats/me`
2. Offres non indexées → Indexer les offres
3. cvText vide → Définir le texte avec `/api/v1/candidats/me/cv/text`
4. TopK trop petit → Augmenter `limit` paramètre

### Problème: Scores faibles

**Solutions:**
1. Enrichir le CV avec plus de détails
2. Améliorer la description de l'offre
3. Vérifier que les compétences sont bien nommées

---

## 12. Évolutions Futures

### Améliorations Possibles

- [ ] Reranking avec filtrage explicite de compétences
- [ ] Apprentissage automatique des poids de matching
- [ ] Explication des matchings (XAI)
- [ ] Recommandations de formations manquantes
- [ ] Matching multi-critères avancé
- [ ] Indexation automatique après upload CV

---

## 13. Résumé des Endpoints AI

| Endpoint | Méthode | Description |
|----------|---------|-------------|
| `/api/v1/ai/parse-cv` | POST | Extraire texte d'un fichier CV |
| `/api/v1/ai/extract-skills` | POST | Extraire compétences via GPT-4 |
| `/api/v1/ai/analyze-cv` | POST | Analyse complète CV (parse + AI) |
| `/api/v1/matching/cv/me/indexer` | POST | Indexer mon CV dans pgvector |
| `/api/v1/matching/cv/{id}/indexer` | POST | Indexer un CV spécifique |
| `/api/v1/matching/offres/{id}/indexer` | POST | Indexer une offre |
| `/api/v1/matching/candidats/{id}/offres` | GET | Trouver offres pour candidat |
| `/api/v1/matching/offres/{id}/candidats` | GET | Trouver candidats pour offre |
| `/api/v1/candidats/me/cv/text` | POST | Définir CV text (test) |

---

## 14. Glossaire

| Terme | Définition |
|-------|-----------|
| **Embedding** | Représentation vectorielle d'un texte |
| **pgvector** | Extension PostgreSQL pour les vecteurs |
| **Cosine Similarity** | Mesure de similarité entre vecteurs (-1 à 1) |
| **IVFFLAT** | Algorithme d'indexation vectorielle |
| **OpenAI text-embedding-3-small** | Modèle d'embedding 1536D |
| **Vector Store** | Base de données vectorielle |
| **Semantic Search** | Recherche sémantique par similarité de meaning |

---

## 15. Ressources

- [OpenAI Embeddings](https://platform.openai.com/docs/guides/embeddings)
- [pgvector Documentation](https://github.com/pgvector/pgvector)
- [Spring AI Documentation](https://docs.spring.io/spring-ai/reference/)
- [Cosine Similarity](https://en.wikipedia.org/wiki/Cosine_similarity)
