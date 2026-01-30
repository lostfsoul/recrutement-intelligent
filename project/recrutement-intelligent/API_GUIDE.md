# Recruitment Platform API - User Guide

Complete guide to test the API using **Postman** or **cURL**.

---

## Table of Contents

1. [Quick Start](#quick-start)
2. [Prerequisites](#prerequisites)
3. [Installation](#installation)
4. [Configuration](#configuration)
5. [Running the Application](#running-the-application)
6. [API Endpoints](#api-endpoints)
7. [Postman Collection](#postman-collection)
8. [Test Examples](#test-examples)

---

## Quick Start

```bash
# 1. Clone and navigate
cd /path/to/project/backend

# 2. Configure database (edit application.properties)
# 3. Run application
mvn spring-boot:run

# 4. Test API
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email":"user@test.com","password":"Password123","passwordConfirmation":"Password123","nom":"Doe","prenom":"John","role":"CANDIDAT"}'
```

---

## Prerequisites

| Software | Version | Required For |
|----------|---------|--------------|
| Java | 21+ | Running the application |
| Maven | 3.8+ | Building the project |
| PostgreSQL | 14+ | Database |
| PgVector | Latest | Vector similarity search |

### Check Versions

```bash
java -version
mvn -version
psql --version
```

---

## Installation

### 1. Clone the Project

```bash
git clone <repository-url>
cd recrutement-intelligent/backend
```

### 2. Database Setup

#### Create Database

```sql
-- Connect to PostgreSQL
sudo -u postgres psql

-- Run these commands
CREATE DATABASE recrutement;
CREATE USER recrutement_user WITH PASSWORD 'pswrd';
GRANT ALL PRIVILEGES ON DATABASE recrutement TO recrutement_user;
\q
```

#### Install PgVector Extension

```bash
# From your machine (if using remote DB)
PGPASSWORD=pswrd psql -h <your-host> -U recrutement_user -d recrutement

# Or from PostgreSQL server
sudo -u postgres psql -d recrutement
```

```sql
-- Install extension
CREATE EXTENSION IF NOT EXISTS vector;

-- Verify
SELECT * FROM pg_extension WHERE extname = 'vector';
```

#### Fix Table Constraints (if needed)

```sql
-- Connect to database
PGPASSWORD=pswrd psql -h <your-host> -U recrutement_user -d recrutement

-- Run these fixes
ALTER TABLE utilisateurs ALTER COLUMN niveau_acces DROP NOT NULL;
ALTER TABLE utilisateurs ALTER COLUMN verified DROP NOT NULL;
```

### 3. Configure Application

Edit `src/main/resources/application.properties`:

```properties
# Database Configuration
spring.datasource.url=jdbc:postgresql://localhost:5432/recrutement
spring.datasource.username=recrutement_user
spring.datasource.password=pswrd

# For remote database, use:
# spring.datasource.url=jdbc:postgresql://193.24.208.37:5432/recrutement?sslmode=disable

# Server Port
server.port=8080

# OpenAI API Key (required for AI features)
spring.ai.openai.api-key=your-openai-api-key-here
```

### 4. Build the Project

```bash
mvn clean install
```

---

## Running the Application

### Development Mode

```bash
mvn spring-boot:run
```

### Production Mode

```bash
# Build JAR
mvn clean package -DskipTests

# Run JAR
java -jar target/recrutement-intelligent-1.0.0.jar
```

### Verify Application

```bash
# Check if running
curl http://localhost:8080/api/v1/auth/login

# Should return error (expected) or Swagger redirect
```

---

## API Endpoints

### Base URL

```
http://localhost:8080/api/v1
```

### Complete Endpoint List

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| **Authentication** |||
| POST | `/auth/register` | Register new user | No |
| POST | `/auth/login` | User login | No |
| POST | `/auth/refresh` | Refresh access token | No |
| POST | `/auth/logout` | User logout | Yes |
| GET | `/auth/me` | Get current user profile | Yes |
| **Candidates** |||
| GET | `/candidats/me` | Get my candidate profile | Yes (CANDIDAT) |
| PUT | `/candidats/me` | Update my profile | Yes (CANDIDAT) |
| GET | `/candidats/me/cv` | Get my CV text | Yes (CANDIDAT) |
| POST | `/candidats/me/cv` | Upload CV file | Yes (CANDIDAT) |
| DELETE | `/candidats/me/cv` | Delete my CV | Yes (CANDIDAT) |
| **Recruiters** |||
| GET | `/recruteurs/me` | Get my recruiter profile | Yes (RECRUTEUR) |
| PUT | `/recruteurs/me` | Update my profile | Yes (RECRUTEUR) |
| POST | `/recruteurs/me/entreprises` | Create a company | Yes (RECRUTEUR) |
| GET | `/recruteurs/me/entreprises` | Get my companies | Yes (RECRUTEUR) |
| GET | `/recruteurs/me/offres` | Get my job offers | Yes (RECRUTEUR) |
| **Companies (Entreprises)** |||
| GET | `/entreprises` | List all companies | No |
| GET | `/entreprises/{id}` | Get company details | No |
| PUT | `/entreprises/{id}` | Update company | Yes (OWNER) |
| DELETE | `/entreprises/{id}` | Delete company | Yes (OWNER) |
| **Job Offers** |||
| GET | `/offres` | List all published offers | No |
| GET | `/offres/{id}` | Get offer details | No |
| POST | `/offres` | Create job offer | Yes (RECRUTEUR) |
| PUT | `/offres/{id}` | Update job offer | Yes (OWNER) |
| DELETE | `/offres/{id}` | Delete job offer | Yes (OWNER) |
| GET | `/offres?titre=xxx&localisation=xxx` | Search offers by title/location | No |
| GET | `/offres/recents` | Get recent offers | No |
| **Applications (Candidatures)** |||
| POST | `/offres/{offreId}/postuler` | Apply to job offer | Yes (CANDIDAT) |
| GET | `/candidatures/me` | Get my applications | Yes (CANDIDAT) |
| GET | `/offres/{offreId}/candidatures` | Get applications for offer | Yes (RECRUTEUR) |
| GET | `/candidatures/{id}` | Get application details | Yes (OWNER) |
| PUT | `/candidatures/{id}/statut` | Update application status | Yes (RECRUTEUR) |
| POST | `/candidatures/{id}/vue` | Mark application as viewed | Yes (RECRUTEUR) |
| DELETE | `/candidatures/{id}` | Cancel application | Yes (CANDIDAT) |
| GET | `/offres/{offreId}/candidatures/non-vues/count` | Count unseen applications | Yes (RECRUTEUR) |
| **AI Features** |||
| POST | `/ai/parse-cv` | Parse CV with AI | Yes |
| POST | `/ai/extract-skills` | Extract skills from text | Yes |
| **Matching** |||
| GET | `/matching/candidats/{id}/offres` | Find matching offers for candidate | Yes (CANDIDAT) |
| GET | `/matching/offres/{id}/candidats` | Find matching candidates for offer | Yes (RECRUTEUR) |
| GET | `/matching/candidats/{id}/offres/score/{offreId}` | Get match score | Yes |
| **Admin** |||
| GET | `/admin/stats` | Get platform statistics | Yes (ADMIN) |
| GET | `/admin/utilisateurs` | List all users | Yes (ADMIN) |
| PUT | `/admin/utilisateurs/{id}/valider` | Validate user | Yes (ADMIN) |
| PUT | `/admin/utilisateurs/{id}/statut` | Update user status | Yes (ADMIN) |
| PUT | `/admin/offres/{id}/moderer` | Moderate job offer | Yes (ADMIN) |

---

## Postman Collection

### Import This Collection

Copy and save as `Recruitment_API.postman_collection.json`:

```json
{
  "info": {
    "name": "Recruitment Platform API",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "variable": [
    {
      "key": "baseUrl",
      "value": "http://localhost:8080/api/v1"
    },
    {
      "key": "accessToken",
      "value": ""
    }
  ],
  "item": [
    {
      "name": "Authentication",
      "item": [
        {
          "name": "Register Candidate",
          "event": [
            {
              "listen": "test",
              "script": {
                "exec": [
                  "var jsonData = pm.response.json();",
                  "pm.collectionVariables.set(\"accessToken\", jsonData.accessToken);",
                  "pm.collectionVariables.set(\"candidateId\", jsonData.userInfo.id);"
                ],
                "type": "text/javascript"
              }
            }
          ],
          "request": {
            "method": "POST",
            "header": [
              {
                "key": "Content-Type",
                "value": "application/json"
              }
            ],
            "body": {
              "mode": "raw",
              "raw": "{\n  \"email\": \"candidate@test.com\",\n  \"password\": \"Password123\",\n  \"passwordConfirmation\": \"Password123\",\n  \"nom\": \"Doe\",\n  \"prenom\": \"John\",\n  \"role\": \"CANDIDAT\"\n}"
            },
            "url": {
              "raw": "{{baseUrl}}/auth/register",
              "host": ["{{baseUrl}}"],
              "path": ["auth", "register"]
            }
          }
        },
        {
          "name": "Register Recruiter",
          "event": [
            {
              "listen": "test",
              "script": {
                "exec": [
                  "var jsonData = pm.response.json();",
                  "pm.collectionVariables.set(\"recruiterToken\", jsonData.accessToken);"
                ],
                "type": "text/javascript"
              }
            }
          ],
          "request": {
            "method": "POST",
            "header": [
              {
                "key": "Content-Type",
                "value": "application/json"
              }
            ],
            "body": {
              "mode": "raw",
              "raw": "{\n  \"email\": \"recruiter@test.com\",\n  \"password\": \"Password123\",\n  \"passwordConfirmation\": \"Password123\",\n  \"nom\": \"Smith\",\n  \"prenom\": \"Jane\",\n  \"nomEntreprise\": \"TechCorp\",\n  \"poste\": \"HR Manager\",\n  \"role\": \"RECRUTEUR\"\n}"
            },
            "url": {
              "raw": "{{baseUrl}}/auth/register",
              "host": ["{{baseUrl}}"],
              "path": ["auth", "register"]
            }
          }
        },
        {
          "name": "Login",
          "event": [
            {
              "listen": "test",
              "script": {
                "exec": [
                  "var jsonData = pm.response.json();",
                  "pm.collectionVariables.set(\"accessToken\", jsonData.accessToken);"
                ],
                "type": "text/javascript"
              }
            }
          ],
          "request": {
            "method": "POST",
            "header": [
              {
                "key": "Content-Type",
                "value": "application/json"
              }
            ],
            "body": {
              "mode": "raw",
              "raw": "{\n  \"email\": \"candidate@test.com\",\n  \"password\": \"Password123\"\n}"
            },
            "url": {
              "raw": "{{baseUrl}}/auth/login",
              "host": ["{{baseUrl}}"],
              "path": ["auth", "login"]
            }
          }
        }
      ]
    },
    {
      "name": "Job Offers",
      "item": [
        {
          "name": "Get All Offers",
          "request": {
            "method": "GET",
            "header": [],
            "url": {
              "raw": "{{baseUrl}}/offres?page=0&size=10",
              "host": ["{{baseUrl}}"],
              "path": ["offres"],
              "query": [
                {
                  "key": "page",
                  "value": "0"
                },
                {
                  "key": "size",
                  "value": "10"
                }
              ]
            }
          }
        },
        {
          "name": "Create Offer",
          "request": {
            "method": "POST",
            "header": [
              {
                "key": "Authorization",
                "value": "Bearer {{recruiterToken}}"
              },
              {
                "key": "Content-Type",
                "value": "application/json"
              }
            ],
            "body": {
              "mode": "raw",
              "raw": "{\n  \"titre\": \"Senior Java Developer\",\n  \"description\": \"We are looking for an experienced Java developer to join our team.\",\n  \"typeContrat\": \"CDI\",\n  \"salaireMin\": 50000,\n  \"salaireMax\": 70000,\n  \"localisation\": \"Paris, France\",\n  \"ville\": \"Paris\",\n  \"teletravail\": true,\n  \"competencesRequises\": \"Java, Spring Boot, PostgreSQL, React\"\n}"
            },
            "url": {
              "raw": "{{baseUrl}}/offres",
              "host": ["{{baseUrl}}"],
              "path": ["offres"]
            }
          }
        }
      ]
    },
    {
      "name": "Candidates",
      "item": [
        {
          "name": "Get My Profile",
          "request": {
            "method": "GET",
            "header": [
              {
                "key": "Authorization",
                "value": "Bearer {{accessToken}}"
              }
            ],
            "url": {
              "raw": "{{baseUrl}}/candidats/me",
              "host": ["{{baseUrl}}"],
              "path": ["candidats", "me"]
            }
          }
        },
        {
          "name": "Update Profile",
          "request": {
            "method": "PUT",
            "header": [
              {
                "key": "Authorization",
                "value": "Bearer {{accessToken}}"
              },
              {
                "key": "Content-Type",
                "value": "application/json"
              }
            ],
            "body": {
              "mode": "raw",
              "raw": "{\n  \"telephone\": \"+33612345678\",\n  \"linkedinUrl\": \"https://linkedin.com/in/johndoe\",\n  \"githubUrl\": \"https://github.com/johndoe\",\n  \"presentation\": \"Passionate full-stack developer with 5 years experience\",\n  \"titrePosteRecherche\": \"Full Stack Developer\",\n  \"pretentionSalarialeMin\": 45000,\n  \"pretentionSalarialeMax\": 60000,\n  \"disponibiliteImmediate\": true\n}"
            },
            "url": {
              "raw": "{{baseUrl}}/candidats/me",
              "host": ["{{baseUrl}}"],
              "path": ["candidats", "me"]
            }
          }
        },
        {
          "name": "Upload CV",
          "request": {
            "method": "POST",
            "header": [
              {
                "key": "Authorization",
                "value": "Bearer {{accessToken}}"
              }
            ],
            "body": {
              "mode": "formdata",
              "formdata": [
                {
                  "key": "file",
                  "type": "file",
                  "src": "/path/to/your/cv.pdf"
                }
              ]
            },
            "url": {
              "raw": "{{baseUrl}}/candidats/me/cv",
              "host": ["{{baseUrl}}"],
              "path": ["candidats", "me", "cv"]
            }
          }
        }
      ]
    },
    {
      "name": "AI & Matching",
      "item": [
        {
          "name": "Parse CV with AI",
          "request": {
            "method": "POST",
            "header": [
              {
                "key": "Authorization",
                "value": "Bearer {{accessToken}}"
              }
            ],
            "body": {
              "mode": "formdata",
              "formdata": [
                {
                  "key": "file",
                  "type": "file",
                  "src": "/path/to/cv.pdf"
                }
              ]
            },
            "url": {
              "raw": "{{baseUrl}}/ai/parse-cv",
              "host": ["{{baseUrl}}"],
              "path": ["ai", "parse-cv"]
            }
          }
        },
        {
          "name": "Extract Skills",
          "request": {
            "method": "POST",
            "header": [
              {
              "key": "Authorization",
              "value": "Bearer {{recruiterToken}}"
            },
              {
                "key": "Content-Type",
                "value": "application/json"
              }
            ],
            "body": {
              "mode": "raw",
              "raw": "{\n  \"text\": \"Experienced Java developer with Spring Boot, PostgreSQL, and React knowledge. Looking for full-stack position.\"\n}"
            },
            "url": {
              "raw": "{{baseUrl}}/ai/extract-skills",
              "host": ["{{baseUrl}}"],
              "path": ["ai", "extract-skills"]
            }
          }
        }
      ]
    }
  ]
}
```

### How to Import

1. Open **Postman**
2. Click **Import** (top left)
3. Paste the JSON above or save to file and import
4. Set your `baseUrl` variable to: `http://localhost:8080/api/v1`
5. Run requests!

---

## Test Examples

### 1. Authentication - Register & Login

#### Register a Candidate

```http
POST http://localhost:8080/api/v1/auth/register
Content-Type: application/json

{
  "email": "candidate@test.com",
  "password": "Password123",
  "passwordConfirmation": "Password123",
  "nom": "Doe",
  "prenom": "John",
  "role": "CANDIDAT"
}
```

**Response:**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJjYW5kaWRhdGVAdGVzdC5jb20iLCJpYXQiOjE3Njk3MzI3MTIsImV4cCI6MTc2OTgxOTExMn0...",
  "refreshToken": "eyJhbGciOiJIUzI1NiJ9...",
  "tokenType": "Bearer",
  "expiresIn": 86400,
  "userInfo": {
    "id": 1,
    "email": "candidate@test.com",
    "nom": "Doe",
    "prenom": "John",
    "role": "CANDIDAT",
    "emailVerifie": false
  }
}
```

#### Login

```http
POST http://localhost:8080/api/v1/auth/login
Content-Type: application/json

{
  "email": "candidate@test.com",
  "password": "Password123"
}
```

---

### 2. Recruiter Workflow (Complete)

#### Step 1: Register as Recruiter

```http
POST http://localhost:8080/api/v1/auth/register
Content-Type: application/json

{
  "email": "recruiter@test.com",
  "password": "Password123",
  "passwordConfirmation": "Password123",
  "nom": "Smith",
  "prenom": "Jane",
  "nomEntreprise": "TechCorp",
  "poste": "HR Manager",
  "role": "RECRUTEUR"
}
```

**Response:**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "userInfo": {
    "id": 2,
    "email": "recruiter@test.com",
    "role": "RECRUTEUR"
  }
}
```

#### Step 2: Create a Company (Entreprise)

**⚠️ Required before creating job offers!**

```http
POST http://localhost:8080/api/v1/recruteurs/me/entreprises
Authorization: Bearer <recruiter-token>
Content-Type: application/json

{
  "nom": "TechCorp Inc",
  "description": "Leading technology company specializing in innovative solutions",
  "secteur": "Technology",
  "ville": "Paris",
  "pays": "France",
  "siteWeb": "https://techcorp.com",
  "taille": "50-100",
  "localisation": "15 Rue de la Paix, 75002 Paris"
}
```

**Response:**
```json
{
  "id": 1,
  "nom": "TechCorp Inc",
  "description": "Leading technology company...",
  "secteur": "Technology",
  "ville": "Paris",
  "pays": "France",
  "siteWeb": "https://techcorp.com",
  "recruteurId": 2
}
```

**⚠️ Copy the `id` (1) - you'll need it for creating job offers!**

#### Step 3: Create Job Offer

```http
POST http://localhost:8080/api/v1/offres
Authorization: Bearer <recruiter-token>
Content-Type: application/json

{
  "titre": "Senior Java Developer",
  "description": "We are looking for an experienced Java developer to join our dynamic team. You will work on cutting-edge projects using Spring Boot, PostgreSQL, and modern cloud technologies.",
  "typeContrat": "CDI",
  "salaireMin": 50000,
  "salaireMax": 70000,
  "localisation": "Paris, France (Hybrid)",
  "ville": "Paris",
  "teletravail": true,
  "competencesRequises": "Java, Spring Boot, PostgreSQL, React, Docker",
  "profilRecherche": "5+ years of experience in software development",
  "experienceMin": 5,
  "experienceMax": 10,
  "entrepriseId": 1
}
```

**⚠️ Important: `entrepriseId` is required!**

#### Step 4: View My Companies

```http
GET http://localhost:8080/api/v1/recruteurs/me/entreprises
Authorization: Bearer <recruiter-token>
```

#### Step 5: View My Job Offers

```http
GET http://localhost:8080/api/v1/recruteurs/me/offres
Authorization: Bearer <recruiter-token>
```

---

### 3. Job Offers (Public)

#### Get All Job Offers (Paginated)

```http
GET http://localhost:8080/api/v1/offres?page=0&size=10
```

**Query Parameters:**
- `page` - Page number (default: 0)
- `size` - Items per page (default: 10)
- `sort` - Sort field (e.g., `dateCreation,desc`)

**Response:**
```json
{
  "content": [...],
  "pageNumber": 0,
  "pageSize": 10,
  "totalElements": 25,
  "totalPages": 3,
  "first": true,
  "last": false
}
```

#### Get Job Offer by ID

```http
GET http://localhost:8080/api/v1/offres/1
```

#### Search Job Offers (Keyword)

```http
GET http://localhost:8080/api/v1/offres?titre=developer&localisation=Paris&page=0&size=10
```

**Query Parameters:**
- `titre` - Search in title (optional)
- `localisation` - Search in location (optional)
- `page` - Page number (default: 0)
- `size` - Page size (default: 10)

#### Get Recent Offers

```http
GET http://localhost:8080/api/v1/offres/recents
```

#### Update Job Offer

```http
PUT http://localhost:8080/api/v1/offres/1
Authorization: Bearer <recruiter-token>
Content-Type: application/json

{
  "titre": "Senior Java Developer - Remote",
  "salaireMax": 80000,
  "statut": "PUBLIEE"
}
```

#### Delete Job Offer

```http
DELETE http://localhost:8080/api/v1/offres/1
Authorization: Bearer <recruiter-token>
```

---

### 4. Candidate Profile

#### Get My Profile

```http
GET http://localhost:8080/api/v1/candidats/me
Authorization: Bearer <candidate-token>
```

**Response:**
```json
{
  "id": 1,
  "email": "candidate@test.com",
  "nom": "Doe",
  "prenom": "John",
  "telephone": "+33612345678",
  "linkedinUrl": "https://linkedin.com/in/johndoe",
  "githubUrl": "https://github.com/johndoe",
  "presentation": "Passionate developer...",
  "titrePosteRecherche": "Full Stack Developer",
  "pretentionSalarialeMin": 45000,
  "pretentionSalarialeMax": 60000,
  "disponibiliteImmediate": true,
  "cvPath": "/uploads/cv_john_doe.pdf",
  "cvText": "John Doe\nSoftware Developer...",
  "dateCreation": "2026-01-30T10:00:00"
}
```

#### Update Profile

```http
PUT http://localhost:8080/api/v1/candidats/me
Authorization: Bearer <candidate-token>
Content-Type: application/json

{
  "telephone": "+33612345678",
  "linkedinUrl": "https://linkedin.com/in/johndoe",
  "githubUrl": "https://github.com/johndoe",
  "portefolioUrl": "https://johndoe.dev",
  "presentation": "Passionate full-stack developer with 5 years experience in Java and React",
  "titrePosteRecherche": "Full Stack Developer",
  "pretentionSalarialeMin": 45000,
  "pretentionSalarialeMax": 60000,
  "disponibiliteImmediate": true,
  "dateDisponibilite": "2026-02-15",
  "mobilite": "Paris, Lyon, Marseille"
}
```

#### Upload CV

```http
POST http://localhost:8080/api/v1/candidats/me/cv
Authorization: Bearer <candidate-token>
Content-Type: multipart/form-data

file: [select PDF or DOCX file]
```

**In Postman:**
1. Select `Body` tab
2. Select `form-data`
3. Key: `file` (type: File)
4. Select your CV file

#### Get CV Text

```http
GET http://localhost:8080/api/v1/candidats/me/cv
Authorization: Bearer <candidate-token>
```

#### Delete CV

```http
DELETE http://localhost:8080/api/v1/candidats/me/cv
Authorization: Bearer <candidate-token>
```

---

### 5. Applications (Candidatures)

#### Apply to a Job Offer

```http
POST http://localhost:8080/api/v1/offres/1/postuler
Authorization: Bearer <candidate-token>
Content-Type: application/json

{
  "lettreMotivation": "I am very interested in this position because my skills in Java and Spring Boot align perfectly with your requirements..."
}
```

**Response:**
```json
{
  "id": 1,
  "statut": "EN_ATTENTE",
  "dateCandidature": "2026-01-30T10:30:00",
  "lettreMotivation": "I am very interested...",
  "candidat": {
    "id": 1,
    "nom": "Doe",
    "prenom": "John"
  },
  "offre": {
    "id": 1,
    "titre": "Senior Java Developer"
  }
}
```

#### Get My Applications

```http
GET http://localhost:8080/api/v1/candidatures/me
Authorization: Bearer <candidate-token>
```

#### Get Application Details

```http
GET http://localhost:8080/api/v1/candidatures/1
Authorization: Bearer <candidate-token>
```

#### Cancel Application

```http
DELETE http://localhost:8080/api/v1/candidatures/1
Authorization: Bearer <candidate-token>
```

---

### 6. Recruiter - Application Management

#### Get Applications for My Job Offer

```http
GET http://localhost:8080/api/v1/offres/1/candidatures
Authorization: Bearer <recruiter-token>
```

#### Update Application Status

```http
PUT http://localhost:8080/api/v1/candidatures/1/statut?statut=EN_ENTRETIEN
Authorization: Bearer <recruiter-token>
```

**Status values:**
- `EN_ATTENTE` - Pending
- `EN_ENTRETIEN` - In interview
- `RETENU` - Accepted/Hired
- `REFUSE` - Rejected

#### Mark Application as Viewed

```http
POST http://localhost:8080/api/v1/candidatures/1/vue
Authorization: Bearer <recruiter-token>
```

#### Count Unviewed Applications

```http
GET http://localhost:8080/api/v1/offres/1/candidatures/non-vues/count
Authorization: Bearer <recruiter-token>
```

---

### 7. Companies (Entreprises)

#### List All Companies

```http
GET http://localhost:8080/api/v1/entreprises
```

#### Get Company Details

```http
GET http://localhost:8080/api/v1/entreprises/1
```

**Response:**
```json
{
  "id": 1,
  "nom": "TechCorp Inc",
  "description": "Leading technology company...",
  "secteur": "Technology",
  "ville": "Paris",
  "pays": "France",
  "siteWeb": "https://techcorp.com",
  "taille": "50-100",
  "statutValidation": "VALIDEE",
  "active": true
}
```

#### Get My Companies (Recruiter)

```http
GET http://localhost:8080/api/v1/recruteurs/me/entreprises
Authorization: Bearer <recruiter-token>
```

#### Update Company

```http
PUT http://localhost:8080/api/v1/entreprises/1
Authorization: Bearer <recruiter-token>
Content-Type: application/json

{
  "description": "Updated company description",
  "siteWeb": "https://newtechcorp.com"
}
```

#### Delete Company

```http
DELETE http://localhost:8080/api/v1/entreprises/1
Authorization: Bearer <recruiter-token>
```

---

### 8. AI Features

#### Parse CV with AI

Extracts structured information from CV using GPT-4:

```http
POST http://localhost:8080/api/v1/ai/parse-cv
Authorization: Bearer <token>
Content-Type: multipart/form-data

file: [select CV PDF/DOCX]
```

**Response:**
```json
{
  "text": "Extracted CV text...",
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
      "degree": "Master in Computer Science",
      "school": "MIT",
      "year": 2019
    }
  ],
  "languages": ["English", "French"],
  "softSkills": ["Leadership", "Communication"]
}
```

#### Extract Skills from Text

```http
POST http://localhost:8080/api/v1/ai/extract-skills
Authorization: Bearer <token>
Content-Type: application/json

{
  "text": "Experienced Java developer with Spring Boot, PostgreSQL, and React knowledge. Looking for full-stack position."
}
```

**Response:**
```json
{
  "skills": ["Java", "Spring Boot", "PostgreSQL", "React"],
  "experience": "Experienced",
  "profile": "Full Stack Developer"
}
```

---

### 9. Matching

#### Find Matching Job Offers for Candidate

Uses AI to find job offers matching your CV:

```http
GET http://localhost:8080/api/v1/matching/candidats/1/offres
Authorization: Bearer <candidate-token>
```

**Response:**
```json
[
  {
    "id": 1,
    "titre": "Senior Java Developer",
    "score": 0.85,
    "semanticScore": 0.82,
    "skillsScore": 0.90,
    "experienceScore": 0.80,
    "salaryScore": 0.90,
    "entreprise": "TechCorp Inc",
    "matchingSkills": ["Java", "Spring Boot", "PostgreSQL"],
    "missingSkills": ["Kubernetes"]
  }
]
```

#### Find Matching Candidates for Job Offer

```http
GET http://localhost:8080/api/v1/matching/offres/1/candidats?limit=10
Authorization: Bearer <recruiter-token>
```

**Query Parameters:**
- `limit` - Maximum number of results (default: 10)
- `threshold` - Minimum score threshold (default: 0.5)

#### Get Specific Match Score

```http
GET http://localhost:8080/api/v1/matching/candidats/1/offres/score/1
Authorization: Bearer <token>
```

---

### 10. Admin Endpoints

#### Get Platform Statistics

```http
GET http://localhost:8080/api/v1/admin/stats
Authorization: Bearer <admin-token>
```

**Response:**
```json
{
  "totalCandidats": 150,
  "totalRecruteurs": 45,
  "totalOffres": 80,
  "totalCandidatures": 320,
  "candidatsCeMois": 12,
  "offresPubliees": 65,
  "entreprisesEnAttente": 5
}
```

#### List All Users

```http
GET http://localhost:8080/api/v1/admin/utilisateurs?page=0&size=20
Authorization: Bearer <admin-token>
```

#### Validate User

```http
PUT http://localhost:8080/api/v1/admin/utilisateurs/5/valider
Authorization: Bearer <admin-token>
```

#### Update User Status

```http
PUT http://localhost:8080/api/v1/admin/utilisateurs/5/statut?statut=ACTIF
Authorization: Bearer <admin-token>
```

**Status values:** `ACTIF`, `INACTIF`, `SUSPENDU`, `EN_ATTENTE_VALIDATION`

#### Moderate Job Offer

```http
PUT http://localhost:8080/api/v1/admin/offres/10/moderer
Authorization: Bearer <admin-token>
Content-Type: application/json

{
  "statut": "REFUSEE",
  "raison": "Inappropriate content"
}
```

#### Apply to a Job Offer

```http
POST http://localhost:8080/api/v1/offres/1/postuler
Authorization: Bearer <candidate-token>
Content-Type: application/json

{
  "lettreMotivation": "I am very interested in this position..."
}
```

#### Get My Applications

```http
GET http://localhost:8080/api/v1/candidatures/me
Authorization: Bearer <candidate-token>
```

---

### 5. AI Features

#### Parse CV with AI

```http
POST http://localhost:8080/api/v1/ai/parse-cv
Authorization: Bearer <token>
Content-Type: multipart/form-data

file: [select CV PDF]
```

**Response:**
```json
{
  "text": "Extracted CV text...",
  "skills": ["Java", "Spring Boot", "PostgreSQL"],
  "experience": 5,
  "education": "Master in Computer Science"
}
```

#### Extract Skills from Text

```http
POST http://localhost:8080/api/v1/ai/extract-skills
Authorization: Bearer <recruiter-token>
Content-Type: application/json

{
  "text": "Experienced Java developer with Spring Boot and PostgreSQL knowledge"
}
```

---

### 6. Matching

#### Find Matching Offers for Candidate

```http
GET http://localhost:8080/api/v1/matching/candidats/1/offres
Authorization: Bearer <candidate-token>
```

#### Find Matching Candidates for Offer

```http
GET http://localhost:8080/api/v1/matching/offres/1/candidats
Authorization: Bearer <recruiter-token>
```

---

## Response Codes

| Code | Meaning |
|------|---------|
| 200 | Success |
| 201 | Created |
| 400 | Bad Request - Validation error |
| 401 | Unauthorized - Invalid/missing token |
| 403 | Forbidden - Insufficient permissions |
| 404 | Not Found |
| 409 | Conflict - Duplicate resource |
| 500 | Internal Server Error |

---

## Common Errors

### 400 Validation Error

```json
{
  "timestamp": "2026-01-30T10:00:00",
  "status": 400,
  "error": "Validation Failed",
  "message": "Erreur de validation des données",
  "validationErrors": {
    "email": "Email already exists",
    "password": "Password must be at least 8 characters"
  }
}
```

### 401 Unauthorized

```json
{
  "timestamp": "2026-01-30T10:00:00",
  "status": 401,
  "error": "Unauthorized",
  "message": "Invalid or expired token"
}
```

**Solution:** Get a new token via `/auth/login` or `/auth/refresh`

---

## Tips for Testing

1. **Save tokens** after register/login for reuse
2. **Use environment variables** in Postman for tokens
3. **Check response headers** for pagination info
4. **Use the test scripts** in Postman collection to auto-extract tokens
5. **For file uploads**, use form-data body type

---

## Quick cURL Reference

```bash
# Set variables
API_BASE="http://localhost:8080/api/v1"
TOKEN="your-jwt-token-here"

# Register
curl -X POST $API_BASE/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"Password123","passwordConfirmation":"Password123","nom":"Test","prenom":"User","role":"CANDIDAT"}'

# Login
curl -X POST $API_BASE/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"Password123"}'

# Get profile (with token)
curl -X GET $API_BASE/candidats/me \
  -H "Authorization: Bearer $TOKEN"

# Upload CV
curl -X POST $API_BASE/candidats/me/cv \
  -H "Authorization: Bearer $TOKEN" \
  -F "file=@/path/to/cv.pdf"
```

---

**Need help?** Check the Technical Documentation for implementation details.
