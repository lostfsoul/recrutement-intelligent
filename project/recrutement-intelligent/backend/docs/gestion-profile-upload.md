# Gestion de Profil et Upload CV

## Vue d'ensemble

Cette section couvre la gestion complète du profil candidat et l'upload de CV dans notre système de recrutement intelligent.

---

## 1. Authentification

### Enregistrement Candidat

**Endpoint:** `POST /api/v1/auth/register`

**Requête:**
```json
{
  "email": "alex.candidate@demo.com",
  "password": "Demo123!",
  "passwordConfirmation": "Demo123!",
  "nom": "Martin",
  "prenom": "Alex",
  "role": "CANDIDAT"
}
```

**Réponse:**
```json
{
  "accessToken": "eyJhbGciOiJIUzUxMiJ9...",
  "refreshToken": "eyJhbGciOiJIUzUxMiJ9...",
  "tokenType": "Bearer",
  "expiresIn": 86400,
  "userInfo": {
    "id": 24,
    "email": "alex.candidate@demo.com",
    "nom": "Martin",
    "prenom": "Alex",
    "role": "CANDIDAT",
    "emailVerifie": false
  }
}
```

### Connexion

**Endpoint:** `POST /api/v1/auth/login`

**Requête:**
```json
{
  "email": "alex.candidate@demo.com",
  "password": "Demo123!"
}
```

---

## 2. Consultation du Profil

### Obtenir Mon Profil Complet

**Endpoint:** `GET /api/v1/candidats/me`

**Headers:**
```
Authorization: Bearer {token}
```

**Réponse:**
```json
{
  "candidat": {
    "id": 24,
    "email": "alex.candidate@demo.com",
    "nom": "Martin",
    "prenom": "Alex",
    "telephone": "+33612345678",
    "titrePosteRecherche": "Senior Full-Stack Developer",
    "disponibiliteImmediate": true,
    "pretentionSalarialeMin": 45000,
    "pretentionSalarialeMax": 65000,
    "linkedinUrl": "https://linkedin.com/in/alexmartin",
    "githubUrl": "https://github.com/alexmartin",
    "presentation": "Développeur passionné avec 5 ans d'expérience.",
    "cvPath": "/uploads/cv/cv_alex_martin.pdf",
    "cvText": "Alex Martin is a senior Java developer...",
    "statut": "ACTIF",
    "emailVerifie": false,
    "dateCreation": "2026-01-30T10:30:00"
  },
  "competences": [
    {
      "id": 1,
      "nom": "Java",
      "niveau": "EXPERT",
      "anneesExperience": 5,
      "certifiee": false
    },
    {
      "id": 2,
      "nom": "Spring Boot",
      "niveau": "AVANCE",
      "anneesExperience": 4,
      "certifiee": false
    }
  ],
  "experiences": [
    {
      "id": 1,
      "titre": "Senior Java Developer",
      "entreprise": "Tech Solutions",
      "typeEntreprise": "SSII",
      "dateDebut": "2022-01-01",
      "dateFin": "2024-01-01",
      "description": "Développement d'applications enterprise Spring Boot."
    }
  ],
  "nombreCompetences": 2,
  "nombreExperiences": 1,
  "nombreCandidatures": 3
}
```

---

## 3. Mise à Jour du Profil

### Modifier Mon Profil

**Endpoint:** `PUT /api/v1/candidats/me`

**Requête:**
```json
{
  "telephone": "+33612345678",
  "titrePosteRecherche": "Senior Full-Stack Developer",
  "disponibiliteImmediate": true,
  "pretentionSalarialeMin": 45000,
  "pretentionSalarialeMax": 65000,
  "linkedinUrl": "https://linkedin.com/in/alexmartin",
  "githubUrl": "https://github.com/alexmartin",
  "presentation": "Développeur passionné avec 5 ans d'expérience en Java et Spring Boot."
}
```

**Champs modifiables:**
- `telephone` - Numéro de téléphone
- `titrePosteRecherche` - Poste recherché
- `disponibiliteImmediate` - Disponibilité immédiate
- `dateDisponibilite` - Date de disponibilité
- `pretentionSalarialeMin/Max` - Prétentions salariales
- `mobilite` - Mobilité géographique
- `linkedinUrl` - Profil LinkedIn
- `githubUrl` - Profil GitHub
- `portefolioUrl` - Lien vers portfolio
- `presentation` - Texte de présentation

---

## 4. Gestion des Compétences

### Ajouter une Compétence

**Endpoint:** `POST /api/v1/candidats/me/competences`

**Requête:**
```json
{
  "nom": "Java",
  "categorie": "LANGAGE",
  "niveau": "EXPERT",
  "anneesExperience": 5,
  "certifiee": false
}
```

**Niveaux disponibles:**
- `DEBUTANT` - 0-2 ans
- `INTERMEDIAIRE` - 2-4 ans
- `AVANCE` - 4-7 ans
- `EXPERT` - 7+ ans

**Catégories disponibles:**
- `LANGAGE` - Langages de programmation
- `FRAMEWORK` - Frameworks et bibliothèques
- `DATABASE` - Bases de données
- `OUTIL` - Outils et méthodologies
- `CLOUD` - Services cloud
- `SOFT_SKILL` - Compétences douces
- `LANGUE` - Langues étrangères

### Supprimer une Compétence

**Endpoint:** `DELETE /api/v1/candidats/me/competences/{competenceId}`

---

## 5. Gestion des Expériences

### Ajouter une Expérience

**Endpoint:** `POST /api/v1/candidats/me/experiences`

**Requête:**
```json
{
  "titre": "Senior Java Developer",
  "entreprise": "Tech Solutions",
  "typeEntreprise": "SSII",
  "dateDebut": "2022-01-01",
  "dateFin": null,
  "description": "Développement d'applications enterprise Spring Boot avec architecture microservices.",
  "poste": "Lead Developer",
  "ville": "Paris",
  "pays": "France"
}
```

**Types d'entreprise:**
- `ESN` - Grande entreprise du numérique
- `SSII` - Société de services informatique
- `STARTUP` - Startup
- `GRPE_GROUPE` - Groupe industriel
- `ADMINISTRATION` - Administration publique
- `AUTRE` - Autre

### Supprimer une Expérience

**Endpoint:** `DELETE /api/v1/candidats/me/experiences/{experienceId}`

---

## 6. Upload de CV

### Upload de Fichier CV

**Endpoint:** `POST /api/v1/candidats/me/cv`

**Content-Type:** `multipart/form-data`

**Requête:**
```
file: [fichier PDF/DOCX]
```

**Réponse:**
```json
{
  "cvPath": "/uploads/cv/cv_24_1706543200000.pdf",
  "cvText": "Alex Martin is a senior Java developer with 5 years of experience...",
  "extractionSuccessful": true,
  "message": "CV uploadé avec succès",
  "nombreCompetences": 2,
  "nombreExperiences": 1
}
```

### Définition Texte CV Direct (Test)

**Endpoint:** `POST /api/v1/candidats/me/cv/text`

**Content-Type:** `application/json`

**Requête:**
```
"Experienced Java Developer with 5 years in Spring Boot, Hibernate, PostgreSQL, REST APIs, microservices, Docker, Kubernetes, and agile development."
```

**Réponse:**
```
CV text updated
```

### Obtenir le CV

**Endpoint:** `GET /api/v1/candidats/me/cv`

**Réponse:** Le chemin du fichier CV stocké

### Supprimer le CV

**Endpoint:** `DELETE /api/v1/candidats/me/cv`

---

## 7. Mes Candidatures

### Voir Mes Candidatures

**Endpoint:** `GET /api/v1/candidats/me/candidatures`

**Réponse:**
```json
[
  {
    "id": 1,
    "statut": "EN_ATTENTE",
    "dateCandidature": "2026-01-30T10:30:00",
    "lettreMotivation": "Votre offre correspond parfaitement...",
    "offre": {
      "id": 7,
      "titre": "Java Developer",
      "reference": "OFF-123456",
      "entreprise": "Tech Corp"
    }
  }
]
```

**Statuts possibles:**
- `EN_ATTENTE` - En attente de traitement
- `EN_COURS` - En cours d'étude
- `ENTRETIEN_PLANIFIE` - Entretien planifié
- `ACCEPTEE` - Candidature acceptée
- `REFUSEE` - Candidature refusée
- `ANNULEE` - Candidature annulée

---

## 8. Bonnes Pratiques

### Structure de CV Recommandée

Pour une extraction optimale des compétences, le CV devrait contenir:

1. **Informations personnelles** clairement identifiées
2. **Compétences techniques** listées avec niveau d'expérience
3. **Certifications** explicitement mentionnées
4. **Projets** avec technologies utilisées
5. **Expériences** avec descriptions détaillées

### Exemple de CV Texte Bien Structuré

```
Alex Martin
Senior Java Developer | 5 years experience
alex.martin@email.com | +33612345678 | Paris, France

PROFESSIONAL SUMMARY
Senior Java developer with 5 years of experience in enterprise applications.
Expertise in Spring Boot, Hibernate, PostgreSQL, and microservices architecture.
AWS Certified Solutions Architect Professional.

TECHNICAL SKILLS
- Languages: Java (Expert), Python (Intermediate), JavaScript (Advanced)
- Frameworks: Spring Boot, Hibernate, Spring Cloud, Angular
- Databases: PostgreSQL, MySQL, MongoDB
- Cloud & DevOps: AWS (ECS, EKS), Docker, Kubernetes, Jenkins, Git
- Methodologies: Agile, Scrum, TDD, CI/CD

EXPERIENCE
Senior Java Developer | Tech Solutions | 2022 - Present
- Developed microservices architecture using Spring Boot and Spring Cloud
- Implemented CI/CD pipelines with Jenkins and Docker
- Managed PostgreSQL database optimization

Java Developer | Digital Innovation | 2020 - 2022
- Built RESTful APIs using Spring Boot and Hibernate
- Participated in code reviews and agile ceremonies

CERTIFICATIONS
- AWS Certified Solutions Architect - Professional (2023)
- Oracle Certified Professional: Java SE 11 Developer (2021)

LANGUAGES
- French: Native
- English: Professional (C1)
- Spanish: Intermediate (B1)
```

---

## 9. Codes d'Erreur

### Erreurs Courantes

| Code | Message | Description |
|------|---------|-------------|
| 400 | VALIDATION_ERROR | Données invalides |
| 401 | INVALID_CREDENTIALS | Email ou mot de passe incorrect |
| 403 | ACCESS_DENIED | Accès refusé |
| 404 | RESOURCE_NOT_FOUND | Ressource non trouvée |
| 409 | EMAIL_ALREADY_EXISTS | Email déjà utilisé |
| 422 | UNPROCESSABLE_ENTITY | Entity non traitable |

---

## 10. Workflow Complet

```
1. INSCRIPTION
   ↓
2. CONNEXION → Récupérer le token JWT
   ↓
3. CONSULTATION PROFIL → GET /api/v1/candidats/me
   ↓
4. MISE À JOUR → PUT /api/v1/candidats/me
   ↓
5. AJOUT COMPÉTENCES → POST /api/v1/candidats/me/competences
   ↓
6. AJOUT EXPÉRIENCES → POST /api/v1/candidats/me/experiences
   ↓
7. UPLOAD CV → POST /api/v1/candidats/me/cv
   ↓
8. CONSULTATION → Vérifier le profil complet
   ↓
9. CANDIDATURE → POST /api/v1/offres/{id}/postuler
   ↓
10. SUIVI → GET /api/v1/candidats/me/candidatures
```

---

## 11. Exemples cURL

### Inscription et connexion complètes

```bash
# 1. S'inscrire
curl -X POST http://localhost:8088/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "alex.candidate@demo.com",
    "password": "Demo123!",
    "passwordConfirmation": "Demo123!",
    "nom": "Martin",
    "prenom": "Alex",
    "role": "CANDIDAT"
  }'

# 2. Se connecter
TOKEN=$(curl -s -X POST http://localhost:8088/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "alex.candidate@demo.com",
    "password": "Demo123!"
  }' | jq -r '.accessToken')

# 3. Obtenir le profil
curl -X GET http://localhost:8088/api/v1/candidats/me \
  -H "Authorization: Bearer $TOKEN"

# 4. Mettre à jour le profil
curl -X PUT http://localhost:8088/api/v1/candidats/me \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "telephone": "+33612345678",
    "titrePosteRecherche": "Senior Full-Stack Developer",
    "presentation": "Développeur passionné avec 5 ans d'\expérience en Java et Spring Boot."
  }'

# 5. Ajouter une compétence
curl -X POST http://localhost:8088/api/v1/candidats/me/competences \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "nom": "Java",
    "niveau": "EXPERT",
    "anneesExperience": 5,
    "certifiee": false
  }'

# 6. Définir le texte du CV (test)
curl -X POST http://localhost:8088/api/v1/candidats/me/cv/text \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '"Experienced Java Developer with 5 years in Spring Boot, Hibernate, PostgreSQL, REST APIs, microservices, Docker, Kubernetes, and agile development."'
```

---

## 12. Validation des Données

### Règles de Validation

**Email:**
- Format valide requis
- Unique dans le système

**Mot de passe:**
- Minimum 8 caractères
- Doit contenir: majuscule, minuscule, chiffre, caractère spécial

**Compétence:**
- Nom: 2 à 100 caractères
- Niveau: valeurs autorisées uniquement
- Années expérience: 0 à 50

**Expérience:**
- Titre: 2 à 100 caractères
- Entreprise: 2 à 100 caractères
- Date fin doit être après date début (si spécifiée)

---

## 13. Sécurité

### Protection des Données

- Tous les endpoints nécessitent une authentification JWT
- Les candidats ne peuvent accéder qu'à leurs propres données
- Le mot de passe est hashé avant stockage (BCrypt)

### Tokens

- Access token: Valide 24 heures
- Refresh token: Valide 7 jours
- Stockage sécurisé recommandé côté client

---

## 14. Notes Techniques

### Transaction Management

Les opérations d'écriture (PUT, POST, DELETE) sont transactionnelles pour garantir la cohérence des données.

### Audit Trail

Chaque modification du profil est datée via `dateModification` pour assurer la traçabilité.

### Upload de CV

- Taille maximale: 10 MB
- Formats supportés: PDF, DOCX, DOC
- Stockage: Système de fichiers local avec organisation par ID candidat
- Extraction automatique du texte pour l'indexation vectorielle
