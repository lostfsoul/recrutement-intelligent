# AI & Matching - Minimal (Mermaid)

## 1. Complete Flow

```mermaid
flowchart LR
    subgraph CV["CV PROCESSING"]
        A[CV File] --> B[PDFBox Parse]
        B --> C[Raw Text]
        C --> D[GPT-4 Extract Skills]
        D --> E[CompetenceDTO]
        E --> F[(Database)]
    end

    subgraph VEC["VECTOR INDEXING"]
        F --> G[Embedding Model]
        G --> H[Vector 1536]
        H --> I[(pgvector)]
    end

    subgraph MATCH["MATCHING"]
        I --> J[Cosine Search]
        J --> K[Top Results]
        K --> L[MatchingResultDTO]
    end
```

## 2. CV Upload Sequence

```mermaid
sequenceDiagram
    User->>Controller: POST /me/cv (PDF)
    Controller->>Service: uploadCv()
    Service->>Parser: parseCv(file)
    Parser-->>Service: "Alex is a Java dev..."
    Service->>AI: extractSkills(text)
    AI->>GPT: ChatClient.call()
    GPT-->>AI: {competences: [{nom:"Java",niveau:"EXPERT"}]}
    AI-->>Service: SkillAnalysisDTO
    Service->>DB: save(candidat)
    Service-->>User: CvUploadDTO
```

## 3. Vector Indexing

```mermaid
sequenceDiagram
    User->>Controller: POST /cv/me/indexer
    Controller->>Service: indexMyCv()
    Service->>DB: getCandidat()
    DB-->>Service: cvText
    Service->>Vector: add(document)
    Vector->>OpenAI: embed(text)
    OpenAI-->>Vector: [0.23, -0.11, ...]
    Vector->>DB: INSERT embedding
    Service-->>User: 204 No Content
```

## 4. Semantic Search

```mermaid
sequenceDiagram
    User->>Controller: GET /candidats/24/offres
    Controller->>Service: findMatching(24)
    Service->>DB: getCvText()
    DB-->>Service: "Java developer..."
    Service->>Vector: similaritySearch(cvText)
    Vector->>DB: cosine distance <=>
    DB-->>Vector: [{score:0.85, offreId:7}, ...]
    Vector-->>Service: Documents
    Service->>DB: getOffre(7)
    DB-->>Service: OffreEmploi
    Service-->>User: [{score:85, recommande:true}]
```

## 5. AI Components Class Diagram

```mermaid
classDiagram
    class CvParsingService {
        +parseCv(File) String
    }

    class SkillExtractionService {
        +extractSkills(String) DTO
    }

    class MatchingEngineService {
        +indexCv(Long)
        +indexOffre(Long)
        +findMatching(Long, int) List
    }

    class VectorStore {
        +add(List)
        +similaritySearch(String) List
    }

    CvParsingService --> SkillExtractionService
    SkillExtractionService --> MatchingEngineService
    MatchingEngineService --> VectorStore
```

## 6. Data Transformations

```mermaid
flowchart LR
    subgraph A["INPUT"]
        F1[PDF File]
        F2[Job Offer]
    end

    subgraph B["PROCESS"]
        B1[Parse → Text]
        B2[AI → JSON DTO]
        B3[Map → Entity]
        B4[Embed → Vector]
    end

    subgraph C["STORAGE"]
        C1[(PostgreSQL)]
        C2[(pgvector)]
    end

    subgraph D["OUTPUT"]
        D1[MatchingResult]
    end

    F1 --> B1 --> B2 --> B3 --> C1
    F2 --> B4 --> C2
    C1 --> D1
    C2 --> D1
```
