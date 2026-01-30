# Structure du Projet - Plateforme de Recrutement Intelligente

## 1. Vue d'Ensemble

Ce projet est une **Plateforme de Recrutement Intelligente** développée avec Spring Boot 3.2.0 et Spring AI, utilisant PostgreSQL avec l'extension PgVector pour le stockage et la recherche vectorielle.

## 2. Structure Globale

```
recrutement-intelligent/
├── backend/                        # Spring Boot Backend
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/ma/recrutement/
│   │   │   └── resources/
│   │   └── test/
│   ├── pom.xml
│   └── Dockerfile
├── docker/
│   ├── docker-compose.yml
│   ├── .env.example
│   └── init-db.sql
└── docs/
    ├── 01-structure-projet.md
    ├── 02-architecture-uml.md
    ├── 03-modele-donnees.md
    ├── 04-dtos-specifications.md
    └── 05-configuration-bd.md
```

## 3. Organisation des Packages Java

### 3.1 Package Principal: `ma.recrutement`

Le package principal contient l'application Spring Boot et tous les sous-packages.

### 3.2 Description des Packages

| Package | Responsabilité | Classes Principales |
|---------|----------------|---------------------|
| **entity** | Entités JPA / Modèle de données | Utilisateur, Candidat, Recruteur, Administrateur, Entreprise, OffreEmploi, Candidature, Competence, Experience |
| **repository** | Interfaces Spring Data JPA | Interfaces Repository pour chaque entité |
| **service** | Logique métier | Services métier et Services IA (CvParsingService, MatchingEngineService) |
| **controller** | API REST | Controllers pour chaque module (AuthController, CandidatController, RecruteurController, OffreController, AdminController) |
| **dto** | Data Transfer Objects | DTOs pour les requêtes et réponses API |
| **config** | Configuration Spring | Security, JWT, OpenAI, VectorStore, CORS |
| **exception** | Gestion des erreurs | Exceptions personnalisées et gestionnaires globaux |
| **util** | Utilitaires | JwtUtil, FileUtil, ValidationUtil |

## 4. Rôles et Responsabilités

### 4.1 Utilisateurs

Le système définit trois types d'utilisateurs via l'énumération `Role`:

- **CANDIDAT**: Peut créer un profil, uploader un CV, postuler aux offres
- **RECRUTEUR**: Peut créer des entreprises, publier des offres, consulter les candidatures
- **ADMINISTRATEUR**: Peut valider les comptes utilisateurs, consulter les statistiques

### 4.2 Flux Principaux

1. **Inscription**: L'utilisateur s'inscrit avec un email et un mot de passe
2. **Validation**: L'administrateur valide le compte
3. **Connexion**: L'utilisateur se connecte et reçoit un token JWT
4. **Candidat**: Uploade son CV, qui est analysé par l'IA pour extraire les compétences
5. **Recruteur**: Crée une offre d'emploi avec les compétences requises
6. **Matching**: Le système matche automatiquement les candidats aux offres via des embeddings vectoriels
7. **Candidature**: Le candidat postule à une offre

## 5. Technologies Utilisées

### 5.1 Backend

| Technologie | Version | Utilisation |
|-------------|---------|-------------|
| **Spring Boot** | 3.2.0 | Framework principal |
| **Spring Data JPA** | 3.2.0 | ORM et accès aux données |
| **Spring Security** | 6.2.0 | Authentification et autorisation |
| **Spring AI** | 1.0.0-M4 | Intégration IA (OpenAI, VectorStore) |
| **Springdoc OpenAPI** | 2.3.0 | Documentation API (Swagger) |
| **PostgreSQL Driver** | 42.7.1 | Driver JDBC PostgreSQL |
| **PgVector** | 0.1.4+ | Stockage et recherche vectorielle |
| **JJWT** | 0.12.3 | Génération et validation JWT |
| **Testcontainers** | 1.19.3 | Tests d'intégration |

### 5.2 Intelligence Artificielle

| Technologie | Utilisation |
|-------------|-------------|
| **OpenAI GPT-4** | Extraction des compétences depuis les CV |
| **OpenAI Embeddings** | Génération des embeddings pour le matching |
| **PgVector Store** | Stockage et recherche vectorielle |

### 5.3 Infrastructure

| Technologie | Utilisation |
|-------------|-------------|
| **Docker** | Conteneurisation de l'application |
| **Docker Compose** | Orchestration des services |
| **PostgreSQL 16** | Base de données principale |
| **PgVector Extension** | Recherche vectorielle |

### 5.4 Outils de Développement

| Outil | Utilisation |
|-------|-------------|
| **Maven** | Gestion des dépendances |
| **Git** | Contrôle de version |
| **Mermaid** | Diagrammes UML |

## 6. Architecture en Couches

```
┌─────────────────────────────────────────────────────────────┐
│                     REST API Layer                          │
│                    (Controllers + DTOs)                     │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                     Service Layer                           │
│              (Business Services + AI Services)              │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                      Data Layer                             │
│                    (Repositories + Entities)                │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                   Database Layer                            │
│              (PostgreSQL + PgVector)                        │
└─────────────────────────────────────────────────────────────┘
```

## 7. Conventions de Code

### 7.1 Nommage

- **Classes**: PascalCase (ex: `CandidatService`)
- **Méthodes**: camelCase (ex: `findById`)
- **Variables**: camelCase (ex: `emailAddress`)
- **Constantes**: UPPER_SNAKE_CASE (ex: `DEFAULT_PAGE_SIZE`)
- **Packages**: tout en minuscules (ex: `ma.recrutement.service`)

### 7.2 Annotations

- **@Entity**: Entités JPA
- **@Repository**: Interfaces Repository
- **@Service**: Services métier
- **@RestController**: Controllers REST
- **@RequestMapping**: Mapping des routes API
- **@Valid**: Validation Bean Validation

### 7.3 Documentation

- JavaDoc pour les classes publiques
- commentaires pour la logique complexe
- Documentation Swagger pour les API REST

## 8. Environnements

### 8.1 Environnement de Développement

- Profil: `dev`
- Base de données locale
- Logs en mode DEBUG

### 8.2 Environnement de Production

- Profil: `prod`
- Base de données hébergée
- Logs en mode INFO

## 9. Sécurité

- **Authentification**: JWT (JSON Web Token)
- **Autorisation**: Spring Security avec rôles
- **Validation des entrées**: Bean Validation
- **Protection CSRF**: Désactivé pour les API REST
- **CORS**: Configuré pour les origines autorisées

## 10. API Documentation

L'API est documentée avec Swagger UI et accessible à l'URL:
- **Dev**: `http://localhost:8080/swagger-ui.html`
- **Prod**: `https://api.recrutement.ma/swagger-ui.html`
