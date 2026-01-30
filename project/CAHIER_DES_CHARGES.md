# Plateforme de Recrutement Intelligente - Cahier Des Charges

## 1. Description du Projet

### 1.1 Contexte
La **Plateforme de Recrutement Intelligente** est une application **Spring Boot (Java)** unifiée qui utilise **Spring AI** pour intégrer l'intelligence artificielle. La plateforme utilise des algorithmes d'IA pour matcher automatiquement les candidats avec les offres d'emploi les plus pertinentes.

### 1.2 Objectifs
- Faciliter la mise en relation entre candidats et recruteurs via API REST
- Automatiser le processus de recrutement grâce à Spring AI
- Centraliser les offres d'emploi et les CVs
- Permettre une gestion efficace des candidatures
- Fournir une documentation API interactive avec Springdoc OpenAPI
- Architecture unifiée Spring Boot avec intégration native de l'IA

---

## 2. Acteurs du Système

| Acteur | Description |
|--------|-------------|
| **Candidat** | Personne recherchant un emploi, peut créer un profil, postuler aux offres via API |
| **Recruteur** | Représentant d'une entreprise, publie des offres, gère les candidatures via API |
| **Administrateur** | Gère la plateforme, valide les comptes, modère le contenu via API |
| **Client API** | Application tierce consommant les endpoints REST |
| **Système IA** | Composant automatique qui analyse les CVs et matche avec les offres |

---

## 3. Technologies Utilisées

| Catégorie | Technologie |
|-----------|-------------|
| **API Layer** | Java 17+ + Spring Boot 3.x |
| **Backend** | Java 17+ + Spring Boot 3.x |
| **Persistance** | Spring Data JPA + Hibernate |
| **Base de données** | PostgreSQL + PgVector (extensions vectorielle) |
| **Communication** | REST / JSON natif Spring Boot |
| **Authentification** | JWT + Spring Security |
| **Validation** | Bean Validation (Java) |
| **Documentation** | Springdoc OpenAPI (Swagger UI) |
| **Tests** | JUnit 5, Mockito, Testcontainers |
| **IA/ML** | Spring AI + OpenAI GPT-4 |
| **Vector Store** | PgVector Store (Spring AI) |
| **Document Reader** | Spring AI PDF Document Reader |
| **Embedding Model** | OpenAI text-embedding-3-small |
| **Serveur** | Embedded Tomcat (Spring Boot) |
| **Build** | Maven / Gradle |
| **Docker** | Docker Compose (mono-container) |
| **CI/CD** | GitHub Actions |

---

## 4. Architecture Applicative

### 4.1 Architecture Spring Boot avec Spring AI

```
┌─────────────────────────────────────────────────────────────────┐
│                         Client API                              │
│           (Postman / Insomnia / curl / Frontend)                │
└─────────────────────────────────────────────────────────────────┘
                              ▲
                              │ HTTPS / JSON
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                  SPRING BOOT BACKEND                            │
│                  (Embedded Tomcat - Port 8080)                  │
│  ┌──────────────────────────────────────────────────────┐     │
│  │              REST Controllers                         │     │
│  │  UtilisateurController  CandidatController           │     │
│  │  RecruteurController  OffreController                │     │
│  │  AdminController  MatchingController  AIController   │     │
│  └──────────────────────────────────────────────────────┘     │
│                              ▲                                 │
│                              │                                 │
│  ┌───────────────────────────┴───────────────────────────┐     │
│  │                   Service Layer (@Service)             │     │
│  │  UtilisateurService  CandidatService  OffreService    │     │
│  │  MatchingService    AdminService                      │     │
│  │  ┌─────────────────────────────────────────────┐     │     │
│  │  │        Spring AI Services                    │     │     │
│  │  │  CvParsingService  SkillExtractionService   │     │     │
│  │  │  MatchingEngineService  RecommendationService│     │     │
│  │  └─────────────────────────────────────────────┘     │     │
│  └───────────────────────────────────────────────────────┘     │
│                              ▲                                 │
│                              │                                 │
│  ┌───────────────────────────┴───────────────────────────┐     │
│  │              Repository Layer (Spring Data JPA)        │     │
│  │  UtilisateurRepository  CandidatRepository            │     │
│  │  RecruteurRepository  OffreRepository                 │     │
│  └───────────────────────────────────────────────────────┘     │
│                              ▲                                 │
│                              │                                 │
│  ┌───────────────────────────┴───────────────────────────┐     │
│  │                  JPA Entities                         │     │
│  │  Utilisateur  Candidat  Recruteur  Offre  Candidature │     │
│  └───────────────────────────────────────────────────────┘     │
│  ┌──────────────────────────────────────────────────────┐     │
│  │              Spring AI Components                    │     │
│  │  ChatClient  EmbeddingModel  VectorStore            │     │
│  │  DocumentReader  PromptTemplate                     │     │
│  └──────────────────────────────────────────────────────┘     │
└─────────────────────────────────────────────────────────────────┘
                              ▲
                              │ JPA / JDBC
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                      PostgreSQL + PgVector                      │
│              (Données relationnelles + Vector embeddings)       │
└─────────────────────────────────────────────────────────────────┘
                              ▲
                              │ REST API
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                      OpenAI API                                 │
│              (GPT-4 pour NLP, Embeddings pour matching)        │
└─────────────────────────────────────────────────────────────────┘
```

### 4.2 Spring AI Integration

```
┌─────────────────────────────────────────────────────────────────┐
│                   Spring AI Components                          │
├─────────────────────────────────────────────────────────────────┤
│  ┌───────────────────────────────────────────────────────┐     │
│  │              OpenAI ChatClient                        │     │
│  │  - Skill Extraction (GPT-4 + PromptTemplate)         │     │
│  │  - CV Analysis                                        │     │
│  └───────────────────────────────────────────────────────┘     │
│  ┌───────────────────────────────────────────────────────┐     │
│  │              OpenAI EmbeddingModel                    │     │
│  │  - Vector embeddings pour CVs et Offres               │     │
│  │  - text-embedding-3-small (1536 dimensions)           │     │
│  └───────────────────────────────────────────────────────┘     │
│  ┌───────────────────────────────────────────────────────┐     │
│  │              PgVector Store                           │     │
│  │  - Stockage des embeddings dans PostgreSQL            │     │
│  │  - Recherche de similarité sémantique                │     │
│  └───────────────────────────────────────────────────────┘     │
│  ┌───────────────────────────────────────────────────────┐     │
│  │              DocumentReader                           │     │
│  │  - Parsing PDF/DOCX pour CVs                         │     │
│  └───────────────────────────────────────────────────────┘     │
└─────────────────────────────────────────────────────────────────┘
```

**Flux de données avec IA:**
1. Client uploade un CV via API REST Spring Boot
2. `CvParsingService` extrait le texte du PDF/DOCX
3. `SkillExtractionService` utilise ChatGPT pour extraire les compétences
4. `EmbeddingModel` génère un vecteur embedding du CV
5. `VectorStore` stocke l'embedding dans PgVector
6. Pour une offre d'emploi, `MatchingEngineService` trouve les candidats similaires
7. `RecommendationService` classe les résultats par score de similarité

---

## 5. Endpoints API Principaux

### 5.1 Authentication

| Méthode | Endpoint | Description | Spring Boot Controller |
|---------|----------|-------------|------------------------|
| **POST** | `/api/v1/auth/register` | Inscription (candidat/recruteur) | UtilisateurController |
| **POST** | `/api/v1/auth/login` | Connexion + Token JWT | UtilisateurController |
| **POST** | `/api/v1/auth/refresh` | Rafraîchir le token | UtilisateurController |
| **POST** | `/api/v1/auth/logout` | Déconnexion | UtilisateurController |

### 5.2 Candidat

| Méthode | Endpoint | Description | Spring Boot Controller |
|---------|----------|-------------|------------------------|
| **GET** | `/api/v1/candidats/me` | Profil du candidat connecté | CandidatController |
| **PUT** | `/api/v1/candidats/me` | Modifier le profil | CandidatController |
| **POST** | `/api/v1/candidats/me/cv` | Uploader un CV | CandidatController |
| **GET** | `/api/v1/candidats/me/cv` | Récupérer le CV parsé | CandidatController |
| **GET** | `/api/v1/offres` | Rechercher des offres | OffreController |
| **POST** | `/api/v1/offres/{id}/postuler` | Postuler à une offre | CandidatureController |
| **GET** | `/api/v1/candidats/me/candidatures` | Mes candidatures | CandidatureController |

### 5.3 Recruteur

| Méthode | Endpoint | Description | Spring Boot Controller |
|---------|----------|-------------|------------------------|
| **GET** | `/api/v1/recruteurs/me` | Profil du recruteur | RecruteurController |
| **PUT** | `/api/v1/recruteurs/me` | Modifier le profil | RecruteurController |
| **POST** | `/api/v1/recruteurs/me/entreprises` | Créer une entreprise | RecruteurController |
| **GET** | `/api/v1/recruteurs/me/entreprises` | Lister les entreprises | RecruteurController |
| **POST** | `/api/v1/offres` | Créer une offre | OffreController |
| **GET** | `/api/v1/recruteurs/me/offres` | Mes offres | OffreController |
| **PUT** | `/api/v1/offres/{id}` | Modifier une offre | OffreController |
| **DELETE** | `/api/v1/offres/{id}` | Supprimer une offre | OffreController |
| **GET** | `/api/v1/offres/{id}/candidatures` | Candidatures d'une offre | CandidatureController |
| **GET** | `/api/v1/candidats` | Rechercher des candidats | CandidatController |
| **GET** | `/api/v1/matching/offres/{id}` | Suggestions IA pour une offre | MatchingController |

### 5.4 Admin

| Méthode | Endpoint | Description | Spring Boot Controller |
|---------|----------|-------------|------------------------|
| **GET** | `/api/v1/admin/stats` | Statistiques globales | AdminController |
| **GET** | `/api/v1/admin/utilisateurs` | Lister tous les utilisateurs | AdminController |
| **PUT** | `/api/v1/admin/utilisateurs/{id}/valider` | Valider un compte | AdminController |
| **DELETE** | `/api/v1/admin/utilisateurs/{id}` | Supprimer un utilisateur | AdminController |
| **GET** | `/api/v1/admin/offres` | Toutes les offres | AdminController |
| **PUT** | `/api/v1/admin/offres/{id}/moderer` | Modérer une offre | AdminController |

### 5.5 IA / Matching

| Méthode | Endpoint | Description | Spring Boot Controller |
|---------|----------|-------------|------------------------|
| **POST** | `/api/v1/ai/parse-cv` | Parser un CV et extraire les compétences | MatchingController |
| **GET** | `/api/v1/matching/candidats/{id}/offres` | Offres recommandées pour un candidat | MatchingController |
| **GET** | `/api/v1/matching/offres/{id}/candidats` | Candidats recommandés pour une offre | MatchingController |
| **GET** | `/api/v1/ai/skills/suggest` | Autocomplete des compétences | MatchingController |

---

## 6. Structure du Projet

### 6.1 Structure Globale

```
recrutement-intelligent/
│
├── 📁 backend/                        # Spring Boot Backend (Application unifiée)
│   ├── src/
│   │   └── main/
│       ├── java/
│       │   └── ma/
│       │       └── recrutement/
│       │           ├── RecrutementApplication.java  # @SpringBootApplication
│       │           │
│       │           ├── entity/    # JPA Entities
│       │           │   ├── Utilisateur.java
│       │           │   ├── Candidat.java
│       │           │   ├── Recruteur.java
│       │           │   ├── Administrateur.java
│       │           │   ├── Entreprise.java
│       │           │   ├── OffreEmploi.java
│       │           │   ├── Candidature.java
│       │           │   ├── Competence.java
│       │           │   └── Experience.java
│       │           │
│       │           ├── repository/ # Spring Data JPA Repositories
│       │           │   ├── UtilisateurRepository.java
│       │           │   ├── CandidatRepository.java
│       │           │   ├── RecruteurRepository.java
│       │           │   ├── OffreEmploiRepository.java
│       │           │   └── CandidatureRepository.java
│       │           │
│       │           ├── service/   # Service Layer (@Service)
│       │           │   ├── UtilisateurService.java
│       │           │   ├── CandidatService.java
│       │           │   ├── RecruteurService.java
│       │           │   ├── OffreEmploiService.java
│       │           │   ├── CandidatureService.java
│       │           │   ├── AdminService.java
│       │           │   ├── MatchingService.java
│       │           │   └── ai/                        # Spring AI Services
│       │           │       ├── CvParsingService.java           # DocumentReader PDF/DOCX
│       │           │       ├── SkillExtractionService.java     # ChatGPT + PromptTemplate
│       │           │       ├── MatchingEngineService.java      # EmbeddingModel + VectorStore
│       │           │       └── RecommendationService.java      # Similarity calculator
│       │           │
│       │           ├── controller/ # REST Controllers
│       │           │   ├── UtilisateurController.java
│       │           │   ├── CandidatController.java
│       │           │   ├── RecruteurController.java
│       │           │   ├── OffreController.java
│       │           │   ├── CandidatureController.java
│       │           │   ├── AdminController.java
│       │           │   ├── MatchingController.java
│       │           │   └── AIController.java                 # REST endpoints AI Spring
│       │           │
│       │           ├── dto/       # Data Transfer Objects
│       │           │   ├── CandidatDTO.java
│       │           │   ├── OffreDTO.java
│       │           │   ├── CandidatureDTO.java
│       │           │   └── MatchingResultDTO.java
│       │           │
│       │           ├── config/    # Spring Configuration
│       │           │   ├── SecurityConfig.java
│       │           │   ├── JpaConfig.java
│       │           │   ├── CorsConfig.java
│       │           │   └── SpringAIConfig.java               # OpenAI, VectorStore config
│       │           │
│       │           ├── exception/ # Exception Handling
│       │           │   ├── BusinessException.java
│       │           │   ├── ResourceNotFoundException.java
│       │           │   └── GlobalExceptionHandler.java
│       │           │
│       │           └── util/      # Utilitaires
│       │               ├── PasswordEncoder.java
│       │               ├── EmailSender.java
│       │               └── FileStorage.java
│       │           │
│       └── resources/
│           ├── application.properties           # Spring Boot config
│           ├── application-dev.properties       # Dev profile
│           ├── application-prod.properties      # Prod profile
│           └── messages.properties              # Messages i18n
│   │
│   ├── src/test/                      # Tests Java
│   │   └── java/
│   │       └── ma/
│   │           └── recrutement/
│   │               ├── repository/
│   │               ├── service/
│   │               └── controller/
│   │
│   ├── pom.xml                           # Maven configuration
│   └── Dockerfile                         # Docker Spring Boot
│
├── 📁 docker/                             # Docker Compose
│   ├── docker-compose.yml                 # Mono-container setup (Spring Boot)
│   ├── .env.example                       # Environment variables
│   └── init-db.sql                        # Database init script (PgVector)
│
├── 📁 docs/                               # Documentation projet
│   ├── architecture-diagram.md
│   ├── api-specification.md
│   ├── spring-integration-guide.md        # Spring AI configuration
│   └── deployment-guide.md
│
├── README.md                              # Documentation principale
└── .gitignore
```

---

## 7. Répartition des Rôles (7 Étudiants)

### 📋 RÉCAPITULATIF DES RÔLES

| # | Étudiant | Rôle Principal | Label Pro | Responsabilités |
|---|-----------|----------------|----------|-----------------|
| **1** | **Ayoub Imourig** | Chef de Projet & Architecte | **Gestion de Projet & Coordination** 📋 | Architecture Spring Boot unifiée, coordination, documentation |
| **2** | **ILYASSE Abdenacer** | Backend Java Data Layer | **Gestion des Données & Persistance** 🗄️ | Entités JPA, Spring Data JPA, config |
| **3** | **Mahdi EL METYOUY** | Backend Java Service Layer | **Gestion des Services & Logique Métier** ⚙️ | Services métier (@Service), logique business |
| **4** | **Younes Hamouddo** | Backend Java REST Controllers | **Gestion des APIs & Contrôleurs** 🔗 | Controllers Spring, DTOs, exception handling |
| **5** | **Samir Ouaaziz** | API Documentation & Quality | **Gestion de la Documentation API & Qualité** 📚 | Springdoc OpenAPI, Postman, Qualité API |
| **6** | **El Hassan CHADER** | Spring AI Developer | **Gestion du Matching IA** 🤖 | Spring AI, OpenAI, VectorStore, Embeddings |
| **7** | **Mohammed EL AMRANI** | DevOps & QA | **Gestion de l'Infrastructure & Tests** 🔧 | Tests, Docker Compose, Postman, CI/CD |

---

### 👤 AYOUB IMOURIG: Chef de Projet & Architecte du Système

**Label Pro:** **Gestion de Projet & Coordination** 📋

**Mission:** Garantir la cohérence architecturale hybride et la bonne collaboration.

| Responsabilité | Tâches |
|----------------|--------|
| **Architecture Hybride** | Définir l'architecture FastAPI + Spring Boot, communication REST |
| **Coordination** | Organiser réunions, suivre l'avancement, gérer les conflits |
| **Intégration** | Fusionner le code, résoudre conflits Git |
| **Documentation** | Cahier des charges, diagrammes d'architecture |
| **Qualité** | Revue de code, respect des standards |

**Livrables:**
- Diagramme d'architecture complète (FastAPI ↔ Spring Boot)
- Spécification OpenAPI complète
- Documentation technique
- Postman Collection organisée
- Rapports d'avancement

---

### 👤 ILYASSE ABDENACER: Backend Java (Data Layer)

**Label Pro:** **Gestion des Données & Persistance** 🗄️

**Mission:** Développer la couche de données Spring Boot.

| Responsabilité | Tâches |
|----------------|--------|
| **Entités JPA** | Créer toutes les entités avec annotations JPA |
| **Repositories** | Créer les interfaces Spring Data JPA |
| **Configuration** | application.properties, JPA config |
| **Validation** | Bean Validation annotations |

**Livrables:**
- `backend/src/main/java/ma/recrutement/entity/*.java` (toutes les entités)
- `backend/src/main/java/ma/recrutement/repository/*.java` (tous les repositories)
- `backend/src/main/resources/application.properties`
- Tests unitaires repository

---

### 👤 MAHDI EL METYOUY: Backend Java (Service Layer)

**Label Pro:** **Gestion des Services & Logique Métier** ⚙️

**Mission:** Développer la logique métier Spring Boot.

| Responsabilité | Tâches |
|----------------|--------|
| **Services** | Créer les services métier (@Service) |
| **Logique métier** | Implémenter les règles business |
| **Transactions** | Gérer les transactions @Transactional |
| **Integration** | Intégrer avec la couche repository |

**Livrables:**
- `backend/src/main/java/ma/recrutement/service/*.java` (tous les services)
- Tests unitaires services
- Documentation des services

---

### 👤 YOUNES HAMOUDDO: Backend Java (REST Controllers)

**Label Pro:** **Gestion des APIs & Contrôleurs** 🔗

**Mission:** Développer les contrôleurs REST Spring Boot.

| Responsabilité | Tâches |
|----------------|--------|
| **Controllers** | Créer les @RestController |
| **DTOs** | Créer les Data Transfer Objects |
| **Exception Handling** | GlobalExceptionHandler |
| **CORS** | Configurer CORS pour FastAPI |
| **Validation** | @Valid, BindingResult |

**Livrables:**
- `backend/src/main/java/ma/recrutement/controller/*.java` (tous les controllers)
- `backend/src/main/java/ma/recrutement/dto/*.java` (tous les DTOs)
- `backend/src/main/java/ma/recrutement/exception/*.java`
- Tests d'intégration REST (MockMvc)

---

### 👤 SAMIR OUAZIZ: API Documentation & Quality Assurance

**Label Pro:** **Gestion de la Documentation API & Qualité** 📚

**Mission:** Assurer la qualité de l'API et sa documentation.

| Responsabilité | Tâches |
|----------------|--------|
| **API Documentation** | Configuration Springdoc OpenAPI, Swagger UI |
| **API Quality** | Validation des endpoints, cohérence des réponses |
| **Postman Collection** | Collection complète et documentée |
| **API Testing** | Tests manuels via Postman/Insomnia |
| **Documentation** | Guides d'utilisation des endpoints |

**Livrables:**
- `backend/src/main/java/ma/recrutement/config/OpenApiConfig.java`
- Postman Collection complète avec exemples
- Documentation API (Swagger UI configuré)
- Guides de test API

---

### 👤 EL HASSAN CHADER: Spring AI Developer

**Label Pro:** **Gestion du Matching IA** 🤖

**Mission:** Développer les algorithmes intelligents avec Spring AI.

| Responsabilité | Tâches |
|----------------|--------|
| **Spring AI Setup** | Configuration OpenAI, VectorStore (PgVector) |
| **CV Parsing** | AiDocumentReader pour PDF/DOCX |
| **Skill Extraction** | ChatGPT + PromptTemplate pour NLP |
| **Matching Engine** | EmbeddingModel + VectorStore pour similarité sémantique |
| **Recommendations** | Algorithme de recommandation basé sur les embeddings |
| **Integration** | Services REST AI intégrés à Spring Boot |

**Livrables:**
- `backend/src/main/java/ma/recrutement/service/ai/CvParsingService.java`
- `backend/src/main/java/ma/recrutement/service/ai/SkillExtractionService.java`
- `backend/src/main/java/ma/recrutement/service/ai/MatchingEngineService.java`
- `backend/src/main/java/ma/recrutement/service/ai/RecommendationService.java`
- `backend/src/main/java/ma/recrutement/controller/AIController.java`
- `backend/src/main/java/ma/recrutement/config/SpringAIConfig.java`
- `backend/src/main/resources/application.properties` avec configuration OpenAI
- Tests unitaires des services AI

---

### 👤 MOHAMMED EL AMRANI: DevOps & QA Engineer

**Label Pro:** **Gestion de l'Infrastructure & Tests** 🔧

**Mission:** Gérer les tests, l'infrastructure et la qualité.

| Responsabilité | Tâches |
|----------------|--------|
| **Docker** | Dockerfile (FastAPI + Spring Boot), docker-compose.yml |
| **Tests** | Tests API (pytest) + Tests Java (JUnit, Testcontainers) |
| **Postman** | Collection Postman complète |
| **CI/CD** | GitHub Actions workflow |
| **Déploiement** | Configuration production |
| **Monitoring** | Logs et métriques |

**Livrables:**
- `docker-compose.yml` complet
- Dockerfiles (FastAPI + Spring Boot)
- Tests complets (Python + Java)
- Postman Collection JSON
- CI/CD pipeline
- Guide de déploiement

---

## 8. Exemples de Code

### 8.1 Spring AI Service - CvParsingService

```java
// backend/src/main/java/ma/recrutement/service/ai/CvParsingService.java
@Service
public class CvParsingService {

    private final DocumentReader documentReader;

    public CvParsingService() {
        this.documentReader = new PdfDocumentReader();
    }

    public String parseCv(MultipartFile file) {
        try {
            // Extract text from PDF/DOCX using Spring AI DocumentReader
            List<Document> documents = documentReader.get();
            return documents.stream()
                .map(Document::getText)
                .collect(Collectors.joining("\n"));
        } catch (Exception e) {
            throw new BusinessException("Error parsing CV: " + e.getMessage());
        }
    }
}
```

### 8.2 Spring AI Service - SkillExtractionService

```java
// backend/src/main/java/ma/recrutement/service/ai/SkillExtractionService.java
@Service
public class SkillExtractionService {

    private final ChatClient chatClient;

    @Autowired
    public SkillExtractionService(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    public List<String> extractSkills(String cvText) {
        String prompt = """
            Extract technical skills from the following CV text.
            Return only the skills as a comma-separated list.

            CV Text:
            {cvText}
            """;

        String response = chatClient.prompt()
            .user(prompt)
            .call()
            .content();

        return Arrays.stream(response.split(","))
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .collect(Collectors.toList());
    }
}
```

### 8.3 Spring AI Service - MatchingEngineService

```java
// backend/src/main/java/ma/recrutement/service/ai/MatchingEngineService.java
@Service
public class MatchingEngineService {

    private final EmbeddingModel embeddingModel;
    private final VectorStore vectorStore;
    private final CandidatRepository candidatRepository;
    private final OffreEmploiRepository offreRepository;

    // Index CV embeddings in vector database
    public void indexCv(Long cvId) {
        Candidat candidat = candidatRepository.findById(cvId)
            .orElseThrow(() -> new ResourceNotFoundException("CV not found"));

        Document document = new Document(candidat.getCvText(),
            Map.of("cvId", cvId, "email", candidat.getEmail()));

        vectorStore.add(List.of(document));
    }

    // Find matching candidates for a job offer
    public List<MatchResult> findMatchingCandidates(Long offreId, int topK) {
        Offre offre = offreRepository.findById(offreId)
            .orElseThrow(() -> new ResourceNotFoundException("Offre not found"));

        List<Document> matches = vectorStore.similaritySearch(
            SearchRequest.query(offre.getDescription()).withTopK(topK)
        );

        return matches.stream()
            .map(doc -> new MatchResult(
                (Long) doc.getMetadata().get("cvId"),
                doc.getScore()
            ))
            .collect(Collectors.toList());
    }
}
```

### 8.4 AIController (REST Endpoints)

```java
// backend/src/main/java/ma/recrutement/controller/AIController.java
@RestController
@RequestMapping("/api/ai")
@Tag(name = "AI Services", description = "Spring AI powered endpoints")
public class AIController {

    @Autowired
    private CvParsingService cvParsingService;

    @Autowired
    private SkillExtractionService skillExtractionService;

    @Autowired
    private MatchingEngineService matchingEngineService;

    @PostMapping("/parse-cv")
    @Operation(summary = "Parse a CV file")
    public ResponseEntity<Map<String, String>> parseCv(
            @RequestParam MultipartFile file) {
        String cvText = cvParsingService.parseCv(file);
        return ResponseEntity.ok(Map.of("text", cvText));
    }

    @PostMapping("/extract-skills")
    @Operation(summary = "Extract skills from CV text")
    public ResponseEntity<List<String>> extractSkills(
            @RequestBody Map<String, String> request) {
        List<String> skills = skillExtractionService.extractSkills(
            request.get("cvText")
        );
        return ResponseEntity.ok(skills);
    }

    @GetMapping("/matching/candidates/{cvId}/offres")
    @Operation(summary = "Get matching job offers for a candidate")
    public ResponseEntity<List<OffreDTO>> getMatchingOffres(
            @PathVariable Long cvId) {
        List<Offre> offres = matchingEngineService.findMatchingOffres(cvId, 10);
        return ResponseEntity.ok(offres.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList()));
    }

    @GetMapping("/matching/offres/{offreId}/candidates")
    @Operation(summary = "Get matching candidates for a job offer")
    public ResponseEntity<List<CandidatDTO>> getMatchingCandidates(
            @PathVariable Long offreId) {
        List<MatchResult> results = matchingEngineService.findMatchingCandidates(offreId, 10);
        return ResponseEntity.ok(results.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList()));
    }
}
```

### 8.2 Spring Boot Controller (Java)

```java
// backend/src/main/java/ma/recrutement/controller/UtilisateurController.java
@RestController
@RequestMapping("/api/utilisateurs")
@CrossOrigin(origins = "http://localhost:8000")
public class UtilisateurController {

    @Autowired
    private UtilisateurService utilisateurService;

    @PostMapping("/login")
    public ResponseEntity<TokenDTO> login(@RequestBody @Valid CredentialsDTO credentials) {
        try {
            TokenDTO token = utilisateurService.authenticate(
                credentials.getEmail(), credentials.getPassword()
            );
            return ResponseEntity.ok(token);
        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }
}
```

### 8.3 Spring Boot Service (Java)

```java
// backend/src/main/java/ma/recrutement/service/UtilisateurService.java
@Service
@Transactional
public class UtilisateurService {

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public TokenDTO authenticate(String email, String password) {
        Utilisateur utilisateur = utilisateurRepository.findByEmail(email)
            .orElseThrow(() -> new AuthenticationException("User not found"));

        if (!passwordEncoder.matches(password, utilisateur.getPassword())) {
            throw new AuthenticationException("Invalid password");
        }

        String token = generateJWTToken(utilisateur);
        return new TokenDTO(token);
    }
}
```

### 8.4 Spring Data JPA Repository (Java)

```java
// backend/src/main/java/ma/recrutement/repository/UtilisateurRepository.java
@Repository
public interface UtilisateurRepository extends JpaRepository<Utilisateur, Long> {

    Optional<Utilisateur> findByEmail(String email);

    @Query("SELECT u FROM Utilisateur u WHERE u.role = :role")
    List<Utilisateur> findByRole(@Param("role") Role role);

    boolean existsByEmail(String email);
}
```

### 8.5 Java DTO (Data Transfer Object)

```java
// backend/src/main/java/ma/recrutement/dto/OffreDTO.java
public class OffreDTO {

    @NotNull
    @Size(min = 5, max = 200)
    private String titre;

    @NotNull
    @Size(min = 50)
    private String description;

    @NotEmpty
    private List<String> competencesRequises;

    private Integer salaireMin;
    private Integer salaireMax;

    @NotNull
    private String localisation;

    @NotNull
    private String typeContrat;

    private Long entrepriseId;

    // Getters and Setters
}
```

### 8.6 application.properties (Spring Boot)

```properties
# application.properties
spring.application.name=recrutement-backend
server.port=8080

# Database Configuration (PostgreSQL + PgVector)
spring.datasource.url=jdbc:postgresql://localhost:5432/recrutement
spring.datasource.username=recrutement
spring.datasource.password=recrutement123
spring.datasource.driver-class-name=org.postgresql.Driver

# JPA / Hibernate
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect

# OpenAI Configuration
spring.ai.openai.api-key=${OPENAI_API_KEY}
spring.ai.openai.chat.options.model=gpt-4
spring.ai.openai.chat.options.temperature=0.7

# Vector Store Configuration (PgVector)
spring.ai.vectorstore.pgvector.host=localhost
spring.ai.vectorstore.pgvector.port=5432
spring.ai.vectorstore.pgvector.database=recrutement
spring.ai.vectorstore.pgvector.username=recrutement
spring.ai.vectorstore.pgvector.password=recrutement123
spring.ai.vectorstore.pgvector.table-name=cv_embeddings
spring.ai.vectorstore.pgvector.dimension=1536

# Embedding Model
spring.ai.openai.embedding.options.model=text-embedding-3-small
spring.ai.openai.embedding.options.dimensions=1536

# CORS Configuration
cors.allowed-origins=*

# JWT Configuration
jwt.secret=your-secret-key
jwt.expiration=86400000

# File Upload
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=10MB

# Logging
logging.level.ma.recrutement=DEBUG
logging.level.org.springframework.ai=DEBUG
```

---

## 9. Planning de Développement

### Phase 1: Initialisation (Semaine 1-2)
- Setup projet (Spring Boot Initializr)
- Docker Compose (PostgreSQL + PgVector)
- Structure des répertoires
- Configuration base de données

### Phase 2: Backend Data Layer (Semaine 3-5)
- Entités JPA
- Spring Data JPA Repositories
- Configuration application.properties
- Tests unitaires repository

### Phase 3: Backend Services (Semaine 6-8)
- Services (@Service)
- Logique métier
- Tests unitaires services

### Phase 4: Backend REST (Semaine 9-10)
- @RestController
- DTOs
- Exception handling
- Tests MockMvc

### Phase 5: Spring AI Module (Semaine 11-14)
- Configuration Spring AI (OpenAI, VectorStore)
- CvParsingService (DocumentReader)
- SkillExtractionService (ChatGPT + PromptTemplate)
- MatchingEngineService (EmbeddingModel + VectorStore)
- RecommendationService
- AIController (REST endpoints AI)

### Phase 6: Tests & Déploiement (Semaine 15-17)
- Tests end-to-end
- Postman Collection
- Dockerisation (mono-container)
- Déploiement

---

## 10. Validation du Plan

### Tests à effectuer:
1. [ ] `POST /api/auth/register` - Créer un compte
2. [ ] `POST /api/auth/login` - Se connecter
3. [ ] `POST /api/offres` - Créer une offre
4. [ ] `POST /api/candidats/me/cv` - Uploader un CV
5. [ ] `POST /api/ai/parse-cv` - Parser un CV avec Spring AI
6. [ ] `GET /api/matching/offres/{id}/candidates` - Suggestions IA (Vector Store)
7. [ ] `GET /api/admin/stats` - Statistiques globales

### Outils de test:
- **Swagger UI (Springdoc)**: `http://localhost:8080/swagger-ui.html`
- **Postman**: Collection importable
- **JUnit + MockMvc**: Tests Spring Boot
- **Testcontainers**: Tests d'intégration avec PostgreSQL
- **Docker Compose**: Test mono-container

---

## 11. Outils de Collaboration

| Outil | Usage |
|-------|-------|
| **Git** | Gestion de version (monorepo ou multi-repos) |
| **GitHub** | Hébergement du code, Issues, Actions |
| **Discord** | Communication équipe |
| **Trello** | Gestion des tâches |
| **Postman** | Tests API manuels |
| **Draw.io** | Diagrammes d'architecture |
| **Docker Desktop** | Développement local |

---

## 12. Commandes Utiles

### Spring Boot Application
```bash
# Compiler et lancer
cd backend
mvn clean spring-boot:run

# Ou avec Gradle
./gradlew bootRun

# Lancer les tests
mvn test

# Builder le JAR
mvn clean package
java -jar target/recrutement-backend-1.0.0.jar
```

### Docker Compose
```bash
# Lancer l'application avec la base de données
docker-compose up -d

# Voir les logs
docker-compose logs -f backend

# Arrêter tous les services
docker-compose down
```

### Voir la documentation API
```
Swagger UI:   http://localhost:8080/swagger-ui.html
OpenAPI JSON: http://localhost:8080/v3/api-docs
```

---

## 13. Configuration Spring AI

### SpringAIConfig.java

```java
// backend/src/main/java/ma/recrutement/config/SpringAIConfig.java
@Configuration
public class SpringAIConfig {

    @Bean
    public ChatClient chatClient(ChatClient.Builder builder) {
        return builder.build();
    }

    @Bean
    public VectorStore vectorStore(JdbcTemplate jdbcTemplate,
                                    EmbeddingModel embeddingModel) {
        return new PgVectorStore(jdbcTemplate, embeddingModel);
    }
}
```

### OpenApiConfig.java (Springdoc)

```java
// backend/src/main/java/ma/recrutement/config/OpenApiConfig.java
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Plateforme de Recrutement API")
                .version("1.0")
                .description("API REST pour la plateforme de recrutement intelligente avec Spring AI"));
    }
}
```

---

## 14. Dépendances Maven (Spring Boot)

```xml
<!-- pom.xml -->
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>3.2.0</version>
</parent>

<properties>
    <spring-ai.version>1.0.0-M4</spring-ai.version>
</properties>

<dependencies>
    <!-- Spring Web -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>

    <!-- Spring Data JPA -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>

    <!-- Spring Security -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-security</artifactId>
    </dependency>

    <!-- Validation -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-validation</artifactId>
    </dependency>

    <!-- PostgreSQL -->
    <dependency>
        <groupId>org.postgresql</groupId>
        <artifactId>postgresql</artifactId>
    </dependency>

    <!-- JWT -->
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt-api</artifactId>
        <version>0.11.5</version>
    </dependency>

    <!-- Spring AI Core -->
    <dependency>
        <groupId>org.springframework.ai</groupId>
        <artifactId>spring-ai-openai-spring-boot-starter</artifactId>
    </dependency>

    <!-- Spring AI Vector Store (PgVector) -->
    <dependency>
        <groupId>org.springframework.ai</groupId>
        <artifactId>spring-ai-pgvector-store-spring-boot-starter</artifactId>
    </dependency>

    <!-- Spring AI PDF Document Reader -->
    <dependency>
        <groupId>org.springframework.ai</groupId>
        <artifactId>spring-ai-pdf-document-reader</artifactId>
    </dependency>

    <!-- Springdoc OpenAPI (Swagger UI) -->
    <dependency>
        <groupId>org.springdoc</groupId>
        <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
        <version>2.3.0</version>
    </dependency>

    <!-- Test -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
        <scope>test</scope>
    </dependency>

    <!-- Testcontainers -->
    <dependency>
        <groupId>org.testcontainers</groupId>
        <artifactId>postgresql</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>

<repositories>
    <repository>
        <id>spring-milestones</id>
        <name>Spring Milestones</name>
        <url>https://repo.spring.io/milestone</url>
        <snapshots>
            <enabled>false</enabled>
        </snapshots>
    </repository>
</repositories>
```

---

*Document généré pour le projet Plateforme de Recrutement Intelligente - Spring Boot + Spring AI 2026*
