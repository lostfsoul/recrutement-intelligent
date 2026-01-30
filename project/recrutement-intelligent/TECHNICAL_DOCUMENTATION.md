# Technical Documentation - AI Recruitment Platform

How the platform handles CV parsing, skill extraction, scoring, and OpenAI integration.

---

## Table of Contents

1. [Architecture Overview](#architecture-overview)
2. [CV Parsing Pipeline](#cv-parsing-pipeline)
3. [Skill Extraction](#skill-extraction)
4. [Vector Embeddings & Storage](#vector-embeddings--storage)
5. [Matching & Scoring Algorithm](#matching--scoring-algorithm)
6. [OpenAI Integration](#openai-integration)
7. [Database Schema](#database-schema)
8. [Flow Diagrams](#flow-diagrams)

---

## Architecture Overview

```
┌─────────────────────────────────────────────────────────────────┐
│                        Frontend (React)                         │
└────────────────────────────┬───────────────────────────────────┘
                             │ HTTP/REST
                             ▼
┌─────────────────────────────────────────────────────────────────┐
│                     Spring Boot API Layer                       │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐          │
│  │ Controllers  │─▶│   Services   │─▶│ Repositories │          │
│  └──────────────┘  └──────┬───────┘  └──────────────┘          │
│                           │
│  ┌─────────────────────────┴─────────────────────────┐          │
│  │              AI Services Layer                    │          │
│  │  ┌──────────────┐  ┌──────────────┐              │          │
│  │  │ CV Parsing   │  │  Skill       │              │          │
│  │  │   Service    │  │ Extraction   │              │          │
│  │  └──────┬───────┘  └──────┬───────┘              │          │
│  │         │                 │                          │          │
│  │         ▼                 ▼                          │          │
│  │  ┌─────────────────────────────────┐              │          │
│  │  │     Matching Engine Service     │              │          │
│  │  └───────────────┬─────────────────┘              │          │
│  └──────────────────┼─────────────────────────────────┘          │
└───────────────────────┼─────────────────────────────────────────┘
                        │
        ┌───────────────┼───────────────┐
        ▼               ▼               ▼
┌───────────────┐ ┌──────────┐ ┌──────────────┐
│   OpenAI      │ │ PgVector │ │  PostgreSQL  │
│   API         │ │  Store   │ │  Database    │
└───────────────┘ └──────────┘ └──────────────┘
```

---

## CV Parsing Pipeline

### 1. Upload & Store

When a candidate uploads their CV:

```
POST /api/v1/candidats/me/cv
Content-Type: multipart/form-data
```

**Backend Process:**

```java
// CandidatController.java
@PostMapping("/me/cv")
public ResponseEntity<CandidatDTO> uploadCv(
    @RequestParam("file") MultipartFile file
) {
    // 1. Validate file (PDF/DOCX, max 5MB)
    cvService.validateCvFile(file);

    // 2. Extract text from document
    String cvText = cvParsingService.parseCv(file);

    // 3. Extract structured information
    CvParseResponseDTO parsedData = cvParsingService.extractCvData(cvText);

    // 4. Update candidate profile
    candidatService.updateCvData(file.getOriginalFilename(), cvText, parsedData);

    // 5. Generate embedding and index in vector store
    matchingEngineService.indexCv(candidatId, cvText);

    return ResponseEntity.ok(updatedProfile);
}
```

### 2. Text Extraction

**CvParsingService.java**

```java
@Service
@RequiredArgsConstructor
public class CvParsingService {

    private final PdfParserService pdfParserService;
    private final DocxParserService docxParserService;

    /**
     * Parse CV file and extract raw text
     */
    public String parseCv(MultipartFile file) {
        String filename = file.getOriginalFilename();
        byte[] content = file.getBytes();

        if (filename.endsWith(".pdf")) {
            return pdfParserService.extractText(content);
        } else if (filename.endsWith(".docx")) {
            return docxParserService.extractText(content);
        }
        throw new IllegalArgumentException("Unsupported file format");
    }
}
```

**PDF Parsing (Apache PDFBox):**

```java
@Service
public class PdfParserService {

    public String extractText(byte[] pdfContent) {
        try (PDDocument document = PDDocument.load(pdfContent)) {
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setSortByPosition(true);

            String text = stripper.getText(document);

            // Clean up text
            return cleanupText(text);
        } catch (IOException e) {
            throw new CvParsingException("Failed to parse PDF", e);
        }
    }

    private String cleanupText(String text) {
        // Remove excessive whitespace
        text = text.replaceAll("\\s+", " ");
        // Remove non-printable characters
        text = text.replaceAll("[^\\x20-\\x7E\\n\\r\\t]", "");
        return text.trim();
    }
}
```

---

## Skill Extraction

### Using OpenAI GPT-4

**SkillExtractionService.java**

```java
@Service
@RequiredArgsConstructor
public class SkillExtractionService {

    private final ChatClient chatClient;

    /**
     * Extract skills and experience from CV text using OpenAI
     */
    public SkillExtractionDTO extractSkills(String cvText) {
        String prompt = buildExtractionPrompt(cvText);

        String response = chatClient.prompt()
            .user(prompt)
            .call()
            .content();

        return parseResponse(response);
    }

    private String buildExtractionPrompt(String cvText) {
        return """
            You are an expert recruiter and HR analyst. Extract structured information from this CV:

            %s

            Return a JSON object with this exact structure:
            {
                "skills": ["skill1", "skill2", ...],
                "experience": {
                    "totalYears": 5,
                    "positions": [
                        {
                            "title": "Job Title",
                            "company": "Company Name",
                            "duration": "2 years",
                            "description": "Key responsibilities..."
                        }
                    ]
                },
                "education": [
                    {
                        "degree": "Master's degree",
                        "field": "Computer Science",
                        "school": "University Name",
                        "year": 2020
                    }
                ],
                "languages": ["English", "French"],
                "softSkills": ["Leadership", "Communication"],
                "certifications": ["AWS Certified", "Scrum Master"]
            }
            """.formatted(cvText);
    }
}
```

**Response Parsing:**

```java
private SkillExtractionDTO parseResponse(String response) {
    try {
        // Extract JSON from markdown code block if present
        String json = extractJson(response);

        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(json, SkillExtractionDTO.class);
    } catch (Exception e) {
        // Fallback: use regex to extract skills
        return extractSkillsManually(response);
    }
}
```

### Data Structure

**SkillExtractionDTO.java**

```java
@Data
public class SkillExtractionDTO {
    private List<String> skills;
    private ExperienceSummary experience;
    private List<Education> education;
    private List<String> languages;
    private List<String> softSkills;
    private List<String> certifications;

    @Data
    public static class ExperienceSummary {
        private Integer totalYears;
        private List<Position> positions;
    }

    @Data
    public static class Position {
        private String title;
        private String company;
        private String duration;
        private String description;
    }

    @Data
    public static class Education {
        private String degree;
        private String field;
        private String school;
        private Integer year;
    }
}
```

---

## Vector Embeddings & Storage

### 1. Generating Embeddings

**EmbeddingService.java**

```java
@Service
@RequiredArgsConstructor
public class EmbeddingService {

    private final EmbeddingModel embeddingModel;

    /**
     * Convert text to vector embedding using OpenAI
     * Returns 1536-dimensional vector (text-embedding-3-small)
     */
    public float[] generateEmbedding(String text) {
        try {
            EmbeddingResponse response = embeddingModel.embed(
                List.of(text)
            );

            return response.getResults().get(0).getOutput();
        } catch (Exception e) {
            throw new EmbeddingException("Failed to generate embedding", e);
        }
    }

    /**
     * Generate embedding for job offer
     */
    public float[] embedJobOffer(OffreEmploi offre) {
        String text = String.format("""
            %s %s %s %s
            Required skills: %s
            """,
            offre.getTitre(),
            offre.getDescription(),
            offre.getProfilRecherche(),
            offre.getCompetencesRequises()
        );

        return generateEmbedding(text);
    }

    /**
     * Generate embedding for CV
     */
    public float[] embedCv(String cvText, List<String> skills) {
        // Weight skills more heavily
        String skillsText = " ".join(skills);
        String text = cvText + " " + skillsText.repeat(3);

        return generateEmbedding(text);
    }
}
```

### 2. Storing in PgVector

**MatchingEngineService.java**

```java
@Service
@RequiredArgsConstructor
public class MatchingEngineService {

    private final EmbeddingService embeddingService;
    private final VectorStore vectorStore;
    private final CandidatRepository candidatRepository;
    private final OffreEmploiRepository offreRepository;

    /**
     * Index CV in vector store
     */
    public void indexCv(Long candidatId, String cvText) {
        Candidat candidat = candidatRepository.findById(candidatId)
            .orElseThrow(() -> new ResourceNotFoundException("Candidat not found"));

        // Generate embedding
        float[] embedding = embeddingService.generateEmbedding(cvText);

        // Create document with metadata
        Document document = Document.builder()
            .id("cv-" + candidatId)
            .text(cvText)
            .metadata(Map.of(
                "type", "cv",
                "candidatId", candidatId.toString(),
                "nom", candidat.getNom(),
                "prenom", candidat.getPrenom(),
                "email", candidat.getEmail()
            ))
            .embedding(embedding)
            .build();

        // Store in PgVector
        vectorStore.add(List.of(document));

        // Update candidate with vector ID
        candidat.setCvVectorId("cv-" + candidatId);
        candidatRepository.save(candidat);
    }

    /**
     * Index job offer in vector store
     */
    public void indexOffre(Long offreId) {
        OffreEmploi offre = offreRepository.findById(offreId)
            .orElseThrow(() -> new ResourceNotFoundException("Offre not found"));

        // Generate embedding
        float[] embedding = embeddingService.embedJobOffer(offre);

        // Create document
        Document document = Document.builder()
            .id("offre-" + offreId)
            .text(offre.getDescription())
            .metadata(Map.of(
                "type", "offre",
                "offreId", offreId.toString(),
                "titre", offre.getTitre(),
                "entrepriseId", offre.getEntreprise().getId().toString()
            ))
            .embedding(embedding)
            .build();

        vectorStore.add(List.of(document));

        offre.setVectorId("offre-" + offreId);
        offreRepository.save(offre);
    }
}
```

### 3. PgVector Configuration

**application.properties**

```properties
# PgVector Configuration
spring.ai.vectorstore.pgvector.index-type=IVFFLAT
spring.ai.vectorstore.pgvector.dimension=1536
spring.ai.vectorstore.pgvector.distance-type=COSINE_DISTANCE
spring.ai.vectorstore.pgvector.table-name=cv_embeddings
```

**Database Schema:**

```sql
-- Vector embeddings table
CREATE TABLE cv_embeddings (
    id SERIAL PRIMARY KEY,
    content TEXT,
    metadata JSON,
    embedding vector(1536),
    UNIQUE(content)
);

-- IVFFLAT index for fast similarity search
CREATE INDEX cv_embeddings_embedding_idx
ON cv_embeddings
USING ivfflat (embedding vector_cosine_ops)
WITH (lists = 100);
```

---

## Matching & Scoring Algorithm

### Similarity Search

**MatchingEngineService.java**

```java
/**
 * Find matching job offers for a candidate
 */
public List<MatchingResultDTO> findMatchingOffres(Long candidatId, int limit) {
    Candidat candidat = candidatRepository.findById(candidatId)
        .orElseThrow(() -> new ResourceNotFoundException("Candidat not found"));

    // Get candidate's CV embedding
    float[] cvEmbedding = embeddingService.generateEmbedding(
        candidat.getCvText()
    );

    // Search for similar job offers
    List<Document> similarDocuments = vectorStore.similaritySearch(
        SearchRequest.query(candidat.getCvText())
            .withTopK(limit)
            .withSimilarityThreshold(0.6)
    );

    // Filter only job offers
    return similarDocuments.stream()
        .filter(doc -> "offre".equals(doc.getMetadata().get("type")))
        .map(this::convertToMatchingResult)
        .collect(Collectors.toList());
}

/**
 * Find matching candidates for a job offer
 */
public List<MatchingResultDTO> findMatchingCandidates(Long offreId, int limit) {
    OffreEmploi offre = offreRepository.findById(offreId)
        .orElseThrow(() -> new ResourceNotFoundException("Offre not found"));

    // Search for similar CVs
    List<Document> similarDocuments = vectorStore.similaritySearch(
        SearchRequest.query(offre.getDescription())
            .withTopK(limit)
            .withSimilarityThreshold(0.6)
    );

    // Calculate additional scores
    return similarDocuments.stream()
        .filter(doc -> "cv".equals(doc.getMetadata().get("type")))
        .map(doc -> calculateMatchScore(doc, offre))
        .sorted(Comparator.comparing(MatchingResultDTO::getScore).reversed())
        .collect(Collectors.toList());
}
```

### Score Calculation

**Matching Scoring Formula:**

```
Total Score = (0.4 × Semantic Score) +
              (0.3 × Skills Match) +
              (0.2 × Experience Fit) +
              (0.1 × Salary Match)
```

```java
private MatchingResultDTO calculateMatchScore(Document doc, OffreEmploi offre) {
    MatchingResultDTO result = new MatchingResultDTO();

    // 1. Semantic similarity (from vector search)
    double semanticScore = calculateCosineSimilarity(doc, offre);
    result.setSemanticScore(semanticScore);

    // 2. Skills matching
    double skillsScore = calculateSkillsMatch(doc, offre);
    result.setSkillsScore(skillsScore);

    // 3. Experience fit
    double experienceScore = calculateExperienceFit(doc, offre);
    result.setExperienceScore(experienceScore);

    // 4. Salary alignment
    double salaryScore = calculateSalaryMatch(doc, offre);
    result.setSalaryScore(salaryScore);

    // Weighted total
    double totalScore = (semanticScore * 0.4) +
                       (skillsScore * 0.3) +
                       (experienceScore * 0.2) +
                       (salaryScore * 0.1);
    result.setScore(totalScore);

    return result;
}

private double calculateSkillsMatch(Document doc, OffreEmploi offre) {
    // Extract skills from candidate
    String candidateSkills = (String) doc.getMetadata().get("skills");

    // Get required skills from offer
    String requiredSkills = offre.getCompetencesRequises();

    // Calculate overlap
    Set<String> candidateSet = extractSkills(candidateSkills);
    Set<String> requiredSet = extractSkills(requiredSkills);

    Set<String> intersection = new HashSet<>(candidateSet);
    intersection.retainAll(requiredSet);

    // Score = matching skills / required skills
    return requiredSet.isEmpty() ? 0.0 :
           (double) intersection.size() / requiredSet.size();
}

private double calculateExperienceFit(Document doc, OffreEmploi offre) {
    Integer candidateYears = (Integer) doc.getMetadata().get("experienceYears");
    Integer requiredYears = offre.getExperienceMin();

    if (candidateYears == null || requiredYears == null) {
        return 0.5; // Neutral score if data missing
    }

    if (candidateYears >= requiredYears) {
        return 1.0;
    } else if (candidateYears >= requiredYears - 1) {
        return 0.7; // Close enough
    } else {
        return 0.3; // Under-qualified
    }
}

private double calculateSalaryMatch(Document doc, OffreEmploi offre) {
    Integer candidateMin = (Integer) doc.getMetadata().get("salaryMin");
    Integer candidateMax = (Integer) doc.getMetadata().get("salaryMax");
    Integer offreMin = offre.getSalaireMin();
    Integer offreMax = offre.getSalaireMax();

    // Check overlap between ranges
    if (candidateMax != null && offreMin != null && candidateMax < offreMin) {
        return 0.2; // Below range
    }

    if (candidateMin != null && offreMax != null && candidateMin > offreMax) {
        return 0.5; // Above range (might be flexible)
    }

    return 1.0; // Ranges overlap
}
```

### Matching Result DTO

```java
@Data
public class MatchingResultDTO {
    private Long id;
    private String type; // "candidat" or "offre"
    private String nom;
    private String titre;
    private Double score;        // Total score (0-1)
    private Double semanticScore;
    private Double skillsScore;
    private Double experienceScore;
    private Double salaryScore;
    private List<String> matchingSkills;
    private List<String> missingSkills;
}
```

---

## OpenAI Integration

### Configuration

**SpringAIConfig.java**

```java
@Configuration
public class SpringAIConfig {

    @Value("${spring.ai.openai.api-key}")
    private String openaiApiKey;

    @Bean
    public ChatModel chatModel() {
        return new OpenAiChatModel(
            new OpenAiApi(openaiApiKey)
        );
    }

    @Bean
    public ChatClient chatClient(ChatModel chatModel) {
        return ChatClient.builder(chatModel).build();
    }

    @Bean
    public EmbeddingModel embeddingModel() {
        return new OpenAiEmbeddingModel(
            new OpenAiApi(openaiApiKey),
            OpenAiEmbeddingOptions.builder()
                .withModel("text-embedding-3-small")
                .withDimensions(1536)
                .build()
        );
    }
}
```

### Models Used

| Model | Purpose | Dimensions | Cost |
|-------|---------|------------|------|
| **gpt-4** | Chat, skill extraction | - | $0.03/1K tokens |
| **text-embedding-3-small** | Vector embeddings | 1536 | $0.00002/1K tokens |

### Usage Examples

**1. Chat-based Skill Extraction**

```java
String response = chatClient.prompt()
    .user("Extract skills from this CV: " + cvText)
    .call()
    .content();
```

**2. Generating Embeddings**

```java
float[] embedding = embeddingModel.embed(text);
```

---

## Database Schema

### Key Tables

```sql
-- Users table (single-table inheritance)
CREATE TABLE utilisateurs (
    id BIGSERIAL PRIMARY KEY,
    role VARCHAR(20) NOT NULL,  -- 'CANDIDAT', 'RECRUTEUR', 'ADMIN'
    email VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    nom VARCHAR(50) NOT NULL,
    prenom VARCHAR(50) NOT NULL,
    statut VARCHAR(20) DEFAULT 'ACTIF',

    -- Candidate-specific
    cv_path VARCHAR(255),
    cv_text TEXT,
    cv_vector_id VARCHAR(255),
    competences_requises TEXT,

    -- Recruiter-specific
    nom_entreprise VARCHAR(100),
    poste VARCHAR(100),
    verified BOOLEAN DEFAULT false,

    date_creation TIMESTAMP NOT NULL,
    date_modification TIMESTAMP
);

-- Job offers
CREATE TABLE offres_emploi (
    id BIGSERIAL PRIMARY KEY,
    titre VARCHAR(200) NOT NULL,
    description TEXT,
    competences_requises TEXT,
    profil_recherche TEXT,

    type_contrat VARCHAR(50),
    salaire_min INTEGER,
    salaire_max INTEGER,
    localisation VARCHAR(255),
    ville VARCHAR(100),

    experience_min_annees INTEGER,
    teletravail BOOLEAN DEFAULT false,

    statut VARCHAR(20) DEFAULT 'BROUILLON',
    actif BOOLEAN DEFAULT true,

    vector_id VARCHAR(255),

    recruteur_id BIGINT REFERENCES utilisateurs(id),
    entreprise_id BIGINT REFERENCES entreprises(id),
    date_creation TIMESTAMP NOT NULL
);

-- Applications
CREATE TABLE candidatures (
    id BIGSERIAL PRIMARY KEY,
    statut VARCHAR(20) DEFAULT 'EN_ATTENTE',
    lettre_motivation TEXT,
    date_candidature TIMESTAMP NOT NULL,
    vue BOOLEAN DEFAULT false,

    candidat_id BIGINT REFERENCES utilisateurs(id),
    offre_id BIGINT REFERENCES offres_emploi(id),

    UNIQUE(candidat_id, offre_id)
);

-- Skills
CREATE TABLE competences (
    id BIGSERIAL PRIMARY KEY,
    nom VARCHAR(100) NOT NULL,
    niveau VARCHAR(20),
    candidat_id BIGINT REFERENCES utilisateurs(id)
);

-- Experiences
CREATE TABLE experiences (
    id BIGSERIAL PRIMARY KEY,
    titre VARCHAR(200),
    entreprise VARCHAR(200),
    date_debut DATE,
    date_fin DATE,
    description TEXT,
    candidat_id BIGINT REFERENCES utilisateurs(id)
);

-- Vector embeddings (managed by PgVector)
CREATE TABLE cv_embeddings (
    id SERIAL PRIMARY KEY,
    content TEXT,
    metadata JSON,
    embedding vector(1536)
);
```

---

## Flow Diagrams

### CV Upload Flow

```
┌──────────┐
│ Candidate│
│ uploads  │
│   CV     │
└────┬─────┘
     │
     ▼
┌──────────────────┐
│ Validate File    │──▶ Error: Invalid format
│ (PDF/DOCX, <5MB) │
└────┬─────────────┘
     │
     ▼
┌──────────────────┐
│ Extract Text     │
│ (PDFBox/POI)     │
└────┬─────────────┘
     │
     ├───────────────────────────────┐
     │                               │
     ▼                               ▼
┌──────────────────┐        ┌──────────────────┐
│ Store Raw Text   │        │ Call OpenAI API  │
│ in Database      │        │ Extract Skills   │
└────┬─────────────┘        └────┬─────────────┘
     │                            │
     └────────────┬───────────────┘
                  ▼
         ┌──────────────────┐
         │ Generate Vector  │
         │ Embedding        │
         └────┬─────────────┘
              │
              ▼
    ┌─────────────────┐
    │ Store in PgVector│
    │ (cv_embeddings) │
    └─────────────────┘
```

### Matching Flow

```
┌──────────────────┐
│ Job Offer Posted │
│ or CV Uploaded   │
└────┬─────────────┘
     │
     ▼
┌──────────────────┐
│ Generate         │
│ Embedding (1536) │
└────┬─────────────┘
     │
     ▼
┌──────────────────┐
│ Store in PgVector│
│ with Metadata    │
└────┬─────────────┘
     │
     ▼
┌──────────────────────┐
│ Vector Search        │
│ (Cosine Similarity)  │
└────┬─────────────────┘
     │
     ▼
┌──────────────────────┐
│ Calculate Scores:    │
│ - Semantic (40%)     │
│ - Skills (30%)       │
│ - Experience (20%)   │
│ - Salary (10%)       │
└────┬─────────────────┘
     │
     ▼
┌──────────────────┐
│ Return Ranked    │
│ Matches (Top K)  │
└──────────────────┘
```

---

## API Response Examples

### CV Parse Response

```json
POST /api/v1/ai/parse-cv
Content-Type: multipart/form-data

Response:
{
  "text": "John Doe\nSoftware Developer with 5 years experience...",
  "skills": ["Java", "Spring Boot", "PostgreSQL", "React"],
  "experience": {
    "totalYears": 5,
    "positions": [
      {
        "title": "Senior Developer",
        "company": "TechCorp",
        "duration": "3 years",
        "description": "Led team of 5 developers..."
      }
    ]
  },
  "education": [
    {
      "degree": "Master's in Computer Science",
      "school": "MIT",
      "year": 2019
    }
  ],
  "languages": ["English", "French"],
  "softSkills": ["Leadership", "Communication"]
}
```

### Matching Response

```json
GET /api/v1/matching/offres/123/candidats

Response:
[
  {
    "id": 1,
    "type": "candidat",
    "nom": "Doe",
    "prenom": "John",
    "score": 0.85,
    "semanticScore": 0.82,
    "skillsScore": 0.90,
    "experienceScore": 0.80,
    "salaryScore": 0.90,
    "matchingSkills": ["Java", "Spring Boot", "PostgreSQL"],
    "missingSkills": ["Kubernetes"]
  },
  {
    "id": 2,
    "type": "candidat",
    "nom": "Smith",
    "prenom": "Jane",
    "score": 0.72,
    "semanticScore": 0.70,
    "skillsScore": 0.75,
    "experienceScore": 0.65,
    "salaryScore": 0.85,
    "matchingSkills": ["Java", "React"],
    "missingSkills": ["Spring Boot", "PostgreSQL"]
  }
]
```

---

## Cost Optimization

### OpenAI API Costs

| Operation | Model | Tokens (avg) | Cost per 1K | Est. Cost |
|-----------|-------|--------------|-------------|-----------|
| Parse CV (text extraction) | None | - | $0 | $0 |
| Extract skills | gpt-4 | 2000 | $0.06 | ~$0.12/CV |
| Generate embedding | text-embedding-3-small | 1000 | $0.00002 | ~$0.00002/CV |

**Per-candidate cost:** ~$0.12 (one-time)
**Per-job-offer cost:** ~$0.02 (one-time)

---

## Security & Privacy

### Data Protection

1. **CV Storage** - Files stored securely, access restricted
2. **PII Handling** - Personal data encrypted at rest
3. **API Keys** - OpenAI keys stored in environment variables
4. **Data Retention** - CVs deleted upon account deletion
5. **GDPR Compliance** - Right to export/delete personal data

### Rate Limiting

```java
@RateLimiter(name = "openai-api", fallbackMethod = "rateLimitFallback")
public SkillExtractionDTO extractSkills(String cvText) {
    // OpenAI API call
}
```

---

## Troubleshooting

### Common Issues

| Issue | Cause | Solution |
|-------|-------|----------|
| CV parsing fails | Corrupted PDF | Validate file before processing |
| Empty embeddings | Text too short | Ensure CV has sufficient content |
| Low match scores | Poor CV quality | Guide users to improve CVs |
| OpenAI timeout | API rate limit | Implement retry with exponential backoff |
| PgVector errors | Extension not installed | `CREATE EXTENSION vector;` |

---

**Version:** 1.0.0
**Last Updated:** 2026-01-30
