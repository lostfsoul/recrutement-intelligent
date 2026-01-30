# Architecture UML - Plateforme de Recrutement Intelligente

## 1. Diagramme de Classes UML

```mermaid
classDiagram
    %% Enumérations
    class Role {
        <<enumeration>>
        CANDIDAT
        RECRUTEUR
        ADMINISTRATEUR
    }

    class StatutOffre {
        <<enumeration>>
        BROUILLON
        PUBLIEE
        COMBLEE
        ARCHIVEE
    }

    class StatutCandidature {
        <<enumeration>>
        EN_ATTENTE
        EN_ETUDE
        ENTRETIEN_PREVU
        ACCEPTEE
        REFUSEE
    }

    %% Entité de base
    class Utilisateur {
        -Long id
        -String email
        -String password
        -String nom
        -String prenom
        -Role role
        -Boolean estValide
        -LocalDateTime dateCreation
        +getEmail() String
        +getRole() Role
    }

    %% Entités spécialisées
    class Candidat {
        -String titre
        -String telephone
        -String adresse
        -String cvPath
        -String cvText
        -Float embedding[]
        -LocalDateTime dateModificationCV
        +uploadCv(File)
        +getCompetences() List~Competence~
    }

    class Recruteur {
        -String poste
        -String telephone
        -List~Entreprise~ entreprises
        +ajouterEntreprise(Entreprise)
        +getEntreprises() List~Entreprise~
    }

    class Administrateur {
        -String niveau
        +validerUtilisateur(Long)
        +getStatistiques() StatsDTO
    }

    %% Entreprise
    class Entreprise {
        -Long id
        -String nom
        -String description
        -String secteur
        -String adresse
        -String siteWeb
        -String email
        -String telephone
        -Recruteur createur
        -LocalDateTime dateCreation
        +getOffresActives() List~OffreEmploi~
    }

    %% Offre d'emploi
    class OffreEmploi {
        -Long id
        -String titre
        -String description
        -String localisation
        -String typeContrat
        -Integer salaireMin
        -Integer salaireMax
        -StatutOffre statut
        -Float embedding[]
        -LocalDateTime datePublication
        -LocalDateTime dateExpiration
        +publier()
        +archiver()
        +estExpiree() boolean
    }

    %% Candidature
    class Candidature {
        -Long id
        -LocalDateTime datePostulation
        -StatutCandidature statut
        -String lettreMotivation
        -Integer scoreMatching
        +accepter()
        +refuser()
        +planifierEntretien(LocalDateTime)
    }

    %% Compétence
    class Competence {
        -Long id
        -String nom
        -String categorie
        -String niveau
        +toString() String
    }

    %% Expérience
    class Experience {
        -Long id
        -String titre
        -String entreprise
        -LocalDate dateDebut
        -LocalDate dateFin
        -String description
        -EnCours enCours
        +getDuree() int
    }

    %% Relations
    Utilisateur <|-- Candidat
    Utilisateur <|-- Recruteur
    Utilisateur <|-- Administrateur
    Recruteur "1" -- "*" Entreprise : crée
    Entreprise "1" -- "*" OffreEmploi : publie
    Candidat "*" -- "*" Candidature : postule
    OffreEmploi "1" -- "*" Candidature : reçoit
    Candidat "1" -- "*" Competence : possède
    OffreEmploi "1" -- "*" Competence : requiert
    Candidat "1" -- "*" Experience : a
```

---

## 2. Diagramme de Séquence - Inscription / Connexion

```mermaid
sequenceDiagram
    actor U as Utilisateur
    participant API as REST API
    participant AS as AuthService
    participant UR as UserRepository
    participant JWT as JwtUtil
    participant DB as PostgreSQL

    %% Inscription
    U->>API: POST /api/auth/register
    API->>API: @Valid RegisterRequestDTO
    API->>AS: register(RegisterRequestDTO)
    AS->>UR: findByEmail(email)
    UR->>DB: SELECT * FROM utilisateur
    DB-->>UR: null
    AS->>AS: encodePassword(password)
    AS->>UR: save(utilisateur)
    UR->>DB: INSERT INTO utilisateur
    DB-->>UR: Utilisateur
    AS-->>API: Utilisateur créé
    API-->>U: 201 Created + Message

    %% Connexion
    U->>API: POST /api/auth/login
    API->>AS: login(LoginRequestDTO)
    AS->>UR: findByEmail(email)
    UR->>DB: SELECT * FROM utilisateur
    DB-->>UR: Utilisateur
    AS->>AS: checkPassword(raw, encoded)
    AS->>AS: checkEstValide()
    AS->>JWT: generateToken(utilisateur)
    JWT-->>AS: JWT token
    AS-->>API: TokenDTO
    API-->>U: 200 OK + {token, type}
```

---

## 3. Diagramme de Séquence - Création d'une Offre

```mermaid
sequenceDiagram
    actor R as Recruteur
    participant API as REST API
    participant OS as OffreService
    participant ES as EntrepriseService
    participant OR as OffreRepository
    participant ER as EntrepriseRepository
    participant AI as AIParsingService
    participant VS as VectorStore
    participant DB as PostgreSQL

    R->>API: POST /api/recruteurs/offres
    API->>API: @Valid OffreCreateDTO
    API->>OS: createOffre(OffreCreateDTO)
    OS->>ES: findById(entrepriseId)
    ES->>ER: findById(entrepriseId)
    ER->>DB: SELECT * FROM entreprise
    DB-->>ER: Entreprise
    ER-->>ES: Entreprise
    ES-->>OS: Entreprise
    OS->>OS: map DTO to Entity
    OS->>OR: save(offre)
    OR->>DB: INSERT INTO offre_emploi
    DB-->>OR: OffreEmploi
    OR-->>OS: OffreEmploi

    %% Génération de l'embedding pour le matching
    OS->>AI: generateEmbedding(offre)
    AI->>AI: extract text from offre
    AI->>VS: store(embedding, metadata)
    VS-->>AI: stored
    AI-->>OS: embeddingId

    OS-->>API: OffreDTO
    API-->>R: 201 Created + OffreDTO
```

---

## 4. Diagramme de Séquence - Upload CV & Parsing IA

```mermaid
sequenceDiagram
    actor C as Candidat
    participant API as REST API
    participant FS as FileStorageService
    participant CS as CandidatService
    participant PS as CvParsingService
    participant GPT as ChatGPT API
    participant ES as EmbeddingService
    participant VS as VectorStore
    participant DB as PostgreSQL

    C->>API: POST /api/candidats/me/cv
    API->>FS: storeFile(multipartFile)
    FS->>FS: validateFile()
    FS->>FS: generateUniqueFileName()
    FS->>FS: saveToDisk(file)
    FS-->>API: filePath

    API->>CS: updateCv(filePath)
    CS->>PS: parseCv(filePath)
    PS->>PS: extractTextFromPDF(file)
    PS-->>PS: cvText

    %% Extraction des compétences via GPT
    PS->>GPT: extractSkills(cvText)
    Note over GPT: Prompt: "Extract skills from CV..."
    GPT-->>PS: List<Competence>

    %% Extraction des expériences via GPT
    PS->>GPT: extractExperiences(cvText)
    Note over GPT: Prompt: "Extract experiences from CV..."
    GPT-->>PS: List<Experience>

    PS-->>CS: CvParseResponseDTO

    %% Génération de l'embedding
    CS->>ES: generateEmbedding(candidat)
    ES->>VS: store(embedding, metadata)
    VS-->>ES: vectorId
    ES-->>CS: vectorId

    CS->>DB: UPDATE candidat SET cv_text, embedding_id
    DB-->>CS: Candidat

    CS-->>API: CandidatDTO
    API-->>C: 200 OK + CandidatDTO
```

---

## 5. Diagramme de Séquence - Matching IA Candidat ↔ Offre

```mermaid
sequenceDiagram
    actor R as Recruteur
    participant API as REST API
    participant MS as MatchingService
    participant VS as VectorStore
    participant OS as OffreRepository
    participant CS as CandidatRepository
    participant DB as PostgreSQL
    participant AI as OpenAI Embeddings

    R->>API: GET /api/recruteurs/offres/{id}/candidats-matches
    API->>MS: findMatchingCandidats(offreId, limit)

    MS->>OS: findById(offreId)
    OS->>DB: SELECT * FROM offre_emploi
    DB-->>OS: OffreEmploi
    OS-->>MS: offre

    MS->>AI: generateEmbedding(offre.description + competences)
    AI-->>MS: offreEmbedding

    %% Recherche vectorielle
    MS->>VS: similaritySearch(offreEmbedding, topK=20)
    Note over VS: Recherche cosine similarity\nsur les embeddings CV
    VS-->>MS: List~VectorDocument~

    %% Récupération des candidats correspondants
    loop Pour chaque document similaire
        VS->>MS: document (owner_id, score)
        MS->>CS: findById(owner_id)
        CS->>DB: SELECT * FROM candidat
        DB-->>CS: Candidat
        CS-->>MS: Candidat
        MS->>MS: calculerScoreMatching()
    end

    MS-->>API: List~MatchingResultDTO~
    API-->>R: 200 OK + List~MatchingResultDTO~

    Note over R: Liste des candidats triés\npar score de matching
```

---

## 6. Diagramme de Séquence - Postulation à une Offre

```mermaid
sequenceDiagram
    actor C as Candidat
    participant API as REST API
    participant CS as CandidatService
    participant OS as OffreService
    participant CAS as CandidatureService
    participant CR as CandidatureRepository
    participant MS as MatchingService
    participant DB as PostgreSQL

    C->>API: POST /api/candidats/postulations
    API->>API: @Valid CandidatureCreateDTO
    API->>CAS: createCandidature(CandidatureCreateDTO)

    CAS->>CS: getCurrentCandidat()
    CS->>DB: SELECT * FROM utilisateur WHERE id = ?
    DB-->>CS: Candidat
    CS-->>CAS: candidat

    CAS->>OS: findById(offreId)
    OS->>DB: SELECT * FROM offre_emploi WHERE id = ?
    DB-->>OS: OffreEmploi
    OS-->>CAS: offre

    CAS->>CAS: vérifierOffreExpiree()
    CAS->>CR: findByCandidatAndOffre(candidat, offre)
    CR->>DB: SELECT * FROM candidature WHERE...
    DB-->>CR: null (pas déjà postulé)
    CR-->>CAS: null

    %% Calcul du score de matching
    CAS->>MS: calculerScoreMatching(candidat, offre)
    MS->>MS: similaritySearch(candidat, offre)
    MS-->>CAS: score (0-100)

    CAS->>CR: save(candidature)
    CR->>DB: INSERT INTO candidature
    DB-->>CR: Candidature
    CR-->>CAS: Candidature

    CAS-->>API: CandidatureDTO
    API-->>C: 201 Created + CandidatureDTO
```

---

## 7. Diagramme de Composants Spring Boot + Spring AI

```mermaid
graph TB
    subgraph "Spring Boot Application"
        subgraph "REST Layer"
            C[Controllers]
            D[DTOs]
            V[Validators]
        end

        subgraph "Service Layer"
            BS[Business Services]
            AIS[AI Services]
            SS[Security Service]
        end

        subgraph "Data Layer"
            R[Repositories]
            E[Entities]
        end

        subgraph "Spring AI Components"
            CC[ChatClient]
            EM[EmbeddingModel]
            VS[VectorStore]
            DR[DocumentReader]
            TT[TokenTextSplitter]
        end

        subgraph "Security Layer"
            SC[SecurityConfig]
            JF[JwtFilter]
            AU[AuthenticationManager]
        end
    end

    subgraph "External Services"
        PG[PostgreSQL + PgVector]
        OAI[OpenAI API]
    end

    subgraph "Clients"
        WC[Web Client]
        MC[Mobile Client]
    end

    %% REST Layer
    WC --> C
    MC --> C
    C --> D
    C --> V
    C --> BS

    %% Security
    C --> JF
    JF --> AU
    AU --> SC

    %% Service Layer
    BS --> R
    BS --> SS
    BS --> AIS

    %% AI Services
    AIS --> CC
    AIS --> EM
    AIS --> VS
    AIS --> DR
    AIS --> TT

    %% Data Layer
    R --> E
    R --> PG

    %% External Services
    CC --> OAI
    EM --> OAI
    VS --> PG

    %% Styling
    classDef controller fill:#e1f5fe
    classDef service fill:#f3e5f5
    classDef data fill:#e8f5e9
    classDef ai fill:#fff3e0
    classDef security fill:#ffebee
    classDef external fill:#f5f5f5

    class C controller
    class D controller
    class V controller
    class BS service
    class AIS service
    class SS service
    class R data
    class E data
    class CC ai
    class EM ai
    class VS ai
    class DR ai
    class TT ai
    class SC security
    class JF security
    class AU security
    class PG external
    class OAI external
```

---

## 8. Diagramme de Déploiement

```mermaid
graph TB
    subgraph "Client Side"
        WEB[Web Application]
        MOBILE[Mobile Application]
    end

    subgraph "Docker Compose"
        subgraph "Backend Container"
            SB[Spring Boot App]
        end

        subgraph "Database Container"
            PG[PostgreSQL + PgVector]
        end
    end

    subgraph "External Services"
        OAI[OpenAI API]
    end

    WEB --> SB
    MOBILE --> SB
    SB --> PG
    SB --> OAI
```

---

## 9. Résumé des Diagrammes

| Diagramme | Description |
|-----------|-------------|
| **Diagramme de Classes** | Modèle de données complet avec toutes les entités et leurs relations |
| **Séquence - Inscription/Connexion** | Flux d'authentification JWT |
| **Séquence - Création Offre** | Publication d'une offre avec génération d'embedding |
| **Séquence - Upload CV** | Parsing IA du CV avec extraction de compétences |
| **Séquence - Matching** | Recherche vectorielle pour matching candidat/offre |
| **Séquence - Postulation** | Création d'une candidature avec score de matching |
| **Composants** | Architecture Spring Boot + Spring AI |
| **Déploiement** | Infrastructure Docker Compose |
