# Spécifications des DTOs - Plateforme de Recrutement Intelligente

## 1. Vue d'Ensemble

Ce document définit tous les **Data Transfer Objects (DTOs)** utilisés dans l'API REST de la plateforme de recrutement. Les DTOs permettent de séparer les entités JPA de la structure des données exposées via l'API.

### 1.1 Conventions de Nommage

| Suffixe | Description |
|---------|-------------|
| `DTO` | DTO standard pour les réponses |
| `CreateDTO` | DTO pour la création d'une ressource |
| `UpdateDTO` | DTO pour la mise à jour d'une ressource |
| `RequestDTO` | DTO pour les requêtes spéciales (auth, recherche) |
| `ResponseDTO` | DTO pour les réponses spéciales (IA, stats) |

---

## 2. DTOs d'Authentification

### 2.1 RegisterRequestDTO

```java
package ma.recrutement.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class RegisterRequestDTO {

    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "L'email doit être valide")
    @Size(max = 255, message = "L'email ne peut pas dépasser 255 caractères")
    private String email;

    @NotBlank(message = "Le mot de passe est obligatoire")
    @Size(min = 8, max = 100, message = "Le mot de passe doit contenir entre 8 et 100 caractères")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$",
             message = "Le mot de passe doit contenir au moins une majuscule, une minuscule et un chiffre")
    private String password;

    @NotBlank(message = "Le nom est obligatoire")
    @Size(min = 2, max = 100, message = "Le nom doit contenir entre 2 et 100 caractères")
    private String nom;

    @NotBlank(message = "Le prénom est obligatoire")
    @Size(min = 2, max = 100, message = "Le prénom doit contenir entre 2 et 100 caractères")
    private String prenom;

    @NotNull(message = "Le rôle est obligatoire")
    private Role role; // CANDIDAT, RECRUTEUR, ADMINISTRATEUR
}
```

### 2.2 LoginRequestDTO

```java
package ma.recrutement.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class LoginRequestDTO {

    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "L'email doit être valide")
    private String email;

    @NotBlank(message = "Le mot de passe est obligatoire")
    private String password;
}
```

### 2.3 TokenDTO

```java
package ma.recrutement.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TokenDTO {
    private String token;
    private String type = "Bearer";
    private Long expiresIn = 86400L; // 24 heures en secondes
}
```

---

## 3. DTOs de Candidat

### 3.1 CandidatDTO

```java
package ma.recrutement.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CandidatDTO {

    private Long id;

    @NotBlank
    @Email
    private String email;

    @NotBlank
    private String nom;

    @NotBlank
    private String prenom;

    private String titre;

    private String telephone;

    private String adresse;

    private String cvPath;

    private String cvText;

    private LocalDateTime dateModificationCV;

    private Boolean estValide;

    @NotNull
    private List<CompetenceDTO> competences;

    @NotNull
    private List<ExperienceDTO> experiences;

    private Integer nombreCandidatures;
}
```

### 3.2 CandidatCreateDTO

```java
package ma.recrutement.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class CandidatCreateDTO {

    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "L'email doit être valide")
    private String email;

    @NotBlank(message = "Le mot de passe est obligatoire")
    @Size(min = 8, message = "Le mot de passe doit contenir au moins 8 caractères")
    private String password;

    @NotBlank(message = "Le nom est obligatoire")
    @Size(min = 2, max = 100)
    private String nom;

    @NotBlank(message = "Le prénom est obligatoire")
    @Size(min = 2, max = 100)
    private String prenom;

    private String titre;

    @Pattern(regexp = "^[+]?[0-9]{10,15}$", message = "Le numéro de téléphone n'est pas valide")
    private String telephone;

    @Size(max = 500, message = "L'adresse ne peut pas dépasser 500 caractères")
    private String adresse;
}
```

### 3.3 CandidatUpdateDTO

```java
package ma.recrutement.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class CandidatUpdateDTO {

    @Size(max = 255, message = "Le titre ne peut pas dépasser 255 caractères")
    private String titre;

    @Pattern(regexp = "^[+]?[0-9]{10,15}$", message = "Le numéro de téléphone n'est pas valide")
    private String telephone;

    @Size(max = 500, message = "L'adresse ne peut pas dépasser 500 caractères")
    private String adresse;
}
```

### 3.4 CompetenceDTO

```java
package ma.recrutement.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompetenceDTO {
    private Long id;
    private String nom;
    private String categorie;
    private String niveau; // DEBUTANT, INTERMEDIAIRE, AVANCE, EXPERT
}
```

### 3.5 ExperienceDTO

```java
package ma.recrutement.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExperienceDTO {
    private Long id;
    private String titre;
    private String entreprise;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private Boolean enCours;
    private String description;
}
```

### 3.6 ExperienceCreateDTO

```java
package ma.recrutement.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

@Data
public class ExperienceCreateDTO {

    @NotBlank(message = "Le titre est obligatoire")
    @Size(max = 255)
    private String titre;

    @NotBlank(message = "L'entreprise est obligatoire")
    @Size(max = 255)
    private String entreprise;

    @NotNull(message = "La date de début est obligatoire")
    @PastOrPresent(message = "La date de début doit être dans le passé ou aujourd'hui")
    private LocalDate dateDebut;

    @Future(message = "La date de fin doit être dans le futur")
    private LocalDate dateFin;

    private Boolean enCours = false;

    @Size(max = 2000, message = "La description ne peut pas dépasser 2000 caractères")
    private String description;
}
```

---

## 4. DTOs de Recruteur

### 4.1 RecruteurDTO

```java
package ma.recrutement.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecruteurDTO {
    private Long id;

    @NotBlank
    @Email
    private String email;

    @NotBlank
    private String nom;

    @NotBlank
    private String prenom;

    private String poste;

    private String telephone;

    @NotNull
    private List<EntrepriseDTO> entreprises;

    private Integer totalOffres;
}
```

### 4.2 EntrepriseDTO

```java
package ma.recrutement.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EntrepriseDTO {
    private Long id;

    @NotBlank
    private String nom;

    private String description;

    private String secteur;

    private String adresse;

    private String siteWeb;

    @Email
    private String email;

    private String telephone;

    private LocalDateTime dateCreation;

    private List<OffreDTO> offres;

    private Integer totalOffresActives;
}
```

### 4.3 EntrepriseCreateDTO

```java
package ma.recrutement.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class EntrepriseCreateDTO {

    @NotBlank(message = "Le nom de l'entreprise est obligatoire")
    @Size(min = 2, max = 255)
    private String nom;

    @Size(max = 2000, message = "La description ne peut pas dépasser 2000 caractères")
    private String description;

    @Size(max = 255)
    private String secteur;

    @Size(max = 500, message = "L'adresse ne peut pas dépasser 500 caractères")
    private String adresse;

    @Pattern(regexp = "^(https?|ftp)://[^\\s/$.?#].[^\\s]*$", message = "L'URL du site web n'est pas valide")
    private String siteWeb;

    @Email(message = "L'email de l'entreprise n'est pas valide")
    private String email;

    @Pattern(regexp = "^[+]?[0-9]{10,15}$", message = "Le numéro de téléphone n'est pas valide")
    private String telephone;
}
```

### 4.4 EntrepriseUpdateDTO

```java
package ma.recrutement.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class EntrepriseUpdateDTO {

    @Size(min = 2, max = 255)
    private String nom;

    @Size(max = 2000)
    private String description;

    @Size(max = 255)
    private String secteur;

    @Size(max = 500)
    private String adresse;

    @Pattern(regexp = "^(https?|ftp)://[^\\s/$.?#].[^\\s]*$")
    private String siteWeb;

    @Email
    private String email;

    @Pattern(regexp = "^[+]?[0-9]{10,15}$")
    private String telephone;
}
```

---

## 5. DTOs d'Offre d'Emploi

### 5.1 OffreDTO

```java
package ma.recrutement.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OffreDTO {
    private Long id;

    @NotBlank
    private String titre;

    @NotBlank
    private String description;

    private String localisation;

    private String typeContrat; // CDI, CDD, STAGE, ALTERNANCE, FREELANCE

    private Integer salaireMin;

    private Integer salaireMax;

    private String statut; // BROUILLON, PUBLIEE, COMBLEE, ARCHIVEE

    @NotNull
    private List<CompetenceDTO> competencesRequises;

    @NotNull
    private EntrepriseResumeDTO entreprise;

    private LocalDateTime datePublication;

    private LocalDateTime dateExpiration;

    private Integer nombreCandidatures;

    private Boolean estExpiree;
}
```

### 5.2 OffreCreateDTO

```java
package ma.recrutement.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.List;

@Data
public class OffreCreateDTO {

    @NotBlank(message = "Le titre est obligatoire")
    @Size(min = 5, max = 255, message = "Le titre doit contenir entre 5 et 255 caractères")
    private String titre;

    @NotBlank(message = "La description est obligatoire")
    @Size(min = 50, max = 5000, message = "La description doit contenir entre 50 et 5000 caractères")
    private String description;

    @Size(max = 255, message = "La localisation ne peut pas dépasser 255 caractères")
    private String localisation;

    @NotBlank(message = "Le type de contrat est obligatoire")
    @Pattern(regexp = "^(CDI|CDD|STAGE|ALTERNANCE|FREELANCE)$",
             message = "Le type de contrat doit être: CDI, CDD, STAGE, ALTERNANCE ou FREELANCE")
    private String typeContrat;

    @NotNull(message = "Le salaire minimum est obligatoire")
    @Min(value = 0, message = "Le salaire minimum ne peut pas être négatif")
    private Integer salaireMin;

    @NotNull(message = "Le salaire maximum est obligatoire")
    @Min(value = 0, message = "Le salaire maximum ne peut pas être négatif")
    private Integer salaireMax;

    @NotNull(message = "L'entreprise est obligatoire")
    private Long entrepriseId;

    @NotEmpty(message = "Au moins une compétence est requise")
    private List<Long> competencesRequisesIds;

    @NotNull(message = "La date d'expiration est obligatoire")
    @Future(message = "La date d'expiration doit être dans le futur")
    private String dateExpiration; // Format ISO date
}
```

### 5.3 OffreUpdateDTO

```java
package ma.recrutement.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.List;

@Data
public class OffreUpdateDTO {

    @Size(min = 5, max = 255)
    private String titre;

    @Size(min = 50, max = 5000)
    private String description;

    @Size(max = 255)
    private String localisation;

    @Pattern(regexp = "^(CDI|CDD|STAGE|ALTERNANCE|FREELANCE)$")
    private String typeContrat;

    @Min(0)
    private Integer salaireMin;

    @Min(0)
    private Integer salaireMax;

    @Pattern(regexp = "^(BROUILLON|PUBLIEE|COMBLEE|ARCHIVEE)$")
    private String statut;

    private List<Long> competencesRequisesIds;

    private String dateExpiration;
}
```

### 5.4 EntrepriseResumeDTO

```java
package ma.recrutement.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EntrepriseResumeDTO {
    private Long id;
    private String nom;
    private String secteur;
    private String localisation;
}
```

---

## 6. DTOs de Candidature

### 6.1 CandidatureDTO

```java
package ma.recrutement.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CandidatureDTO {
    private Long id;

    @NotNull
    private LocalDateTime datePostulation;

    @NotBlank
    private String statut; // EN_ATTENTE, EN_ETUDE, ENTRETIEN_PREVU, ACCEPTEE, REFUSEE

    private String lettreMotivation;

    private Integer scoreMatching;

    @NotNull
    private CandidatResumeDTO candidat;

    @NotNull
    private OffreResumeDTO offre;
}
```

### 6.2 CandidatureCreateDTO

```java
package ma.recrutement.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class CandidatureCreateDTO {

    @NotNull(message = "L'offre est obligatoire")
    private Long offreId;

    @Size(max = 2000, message = "La lettre de motivation ne peut pas dépasser 2000 caractères")
    private String lettreMotivation;
}
```

### 6.3 CandidatResumeDTO

```java
package ma.recrutement.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CandidatResumeDTO {
    private Long id;
    private String nom;
    private String prenom;
    private String titre;
    private String email;
}
```

### 6.4 OffreResumeDTO

```java
package ma.recrutement.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OffreResumeDTO {
    private Long id;
    private String titre;
    private String nomEntreprise;
    private String localisation;
    private String typeContrat;
}
```

### 6.5 CandidatureUpdateStatutDTO

```java
package ma.recrutement.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class CandidatureUpdateStatutDTO {

    @NotBlank(message = "Le statut est obligatoire")
    @Pattern(regexp = "^(EN_ATTENTE|EN_ETUDE|ENTRETIEN_PREVU|ACCEPTEE|REFUSEE)$",
             message = "Le statut doit être: EN_ATTENTE, EN_ETUDE, ENTRETIEN_PREVU, ACCEPTEE ou REFUSEE")
    private String statut;
}
```

---

## 7. DTOs d'Administration

### 7.1 StatsDTO

```java
package ma.recrutement.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StatsDTO {
    private Long totalUsers;
    private Long totalCandidats;
    private Long totalRecruteurs;
    private Long totalEntreprises;
    private Long totalOffres;
    private Long totalOffresPubliees;
    private Long totalCandidatures;
    private Long totalCandidaturesEnAttente;
    private Double tauxConversion; // Candidatures acceptées / Total
}
```

### 7.2 UserValidationDTO

```java
package ma.recrutement.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class UserValidationDTO {

    @NotNull(message = "L'ID utilisateur est obligatoire")
    private Long userId;

    @NotNull(message = "Le statut de validation est obligatoire")
    private Boolean estValide;
}
```

### 7.3 UserListDTO

```java
package ma.recrutement.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserListDTO {
    private Long id;
    private String email;
    private String nom;
    private String prenom;
    private String role;
    private Boolean estValide;
    private LocalDateTime dateCreation;
}
```

---

## 8. DTOs d'Intelligence Artificielle

### 8.1 CvParseResponseDTO

```java
package ma.recrutement.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CvParseResponseDTO {
    private String text;
    private String fileName;
    private CvDataDTO extractedData;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
class CvDataDTO {
    private String titre;
    private List<CompetenceDTO> competences;
    private List<ExperienceDTO> experiences;
    private String email;
    private String telephone;
}
```

### 8.2 SkillsExtractionDTO

```java
package ma.recrutement.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SkillsExtractionDTO {
    private List<String> hardSkills;
    private List<String> softSkills;
    private List<String> languages;
    private String titre;
}
```

### 8.3 CvUploadResponseDTO

```java
package ma.recrutement.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CvUploadResponseDTO {
    private String cvPath;
    private String cvText;
    private Integer competencesExtracted;
    private Integer experiencesExtracted;
    private String message;
}
```

### 8.4 MatchingResultDTO

```java
package ma.recrutement.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MatchingResultDTO {
    private Long candidatId;
    private String candidatNom;
    private String candidatPrenom;
    private String titre;
    private Integer score; // 0-100
    private List<String> matchedSkills;
    private List<String> missingSkills;
    private String cvPath;
}
```

### 8.5 MatchingRequestDTO

```java
package ma.recrutement.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class MatchingRequestDTO {

    @NotNull(message = "L'ID de l'offre est obligatoire")
    private Long offreId;

    @Min(value = 1, message = "La limite doit être d'au moins 1")
    @Max(value = 100, message = "La limite ne peut pas dépasser 100")
    private Integer limit = 20;

    @Min(value = 0, message = "Le score minimum ne peut pas être négatif")
    @Max(value = 100, message = "Le score maximum est 100")
    private Integer scoreMin = 50;
}
```

---

## 9. DTOs de Recherche

### 9.1 OffreSearchDTO

```java
package ma.recrutement.dto;

import lombok.Data;

@Data
public class OffreSearchDTO {
    private String titre;
    private String localisation;
    private String typeContrat;
    private Integer salaireMin;
    private Integer secteur;
    private String competence;
    private Integer page = 0;
    private Integer size = 20;
    private String sortBy = "datePublication";
    private String sortDirection = "DESC";
}
```

### 9.2 PageResponseDTO

```java
package ma.recrutement.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PageResponseDTO {
    private List<?> content;
    private Integer currentPage;
    private Integer totalPages;
    private Long totalElements;
    private Integer pageSize;
    private Boolean isFirst;
    private Boolean isLast;
}
```

---

## 10. DTOs de Réponse Générique

### 10.1 ApiResponseDTO

```java
package ma.recrutement.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiResponseDTO {
    private Boolean success;
    private String message;
    private Object data;
    private String timestamp;

    public static ApiResponseDTO success(String message, Object data) {
        return ApiResponseDTO.builder()
                .success(true)
                .message(message)
                .data(data)
                .timestamp(java.time.LocalDateTime.now().toString())
                .build();
    }

    public static ApiResponseDTO error(String message) {
        return ApiResponseDTO.builder()
                .success(false)
                .message(message)
                .timestamp(java.time.LocalDateTime.now().toString())
                .build();
    }
}
```

### 10.2 ErrorResponseDTO

```java
package ma.recrutement.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ErrorResponseDTO {
    private Integer status;
    private String error;
    private String message;
    private List<String> details;
    private String path;
    private String timestamp;
}
```

---

## 11. Récapitulatif des DTOs

| Catégorie | DTOs | Nombre |
|-----------|------|--------|
| **Authentification** | RegisterRequestDTO, LoginRequestDTO, TokenDTO | 3 |
| **Candidat** | CandidatDTO, CandidatCreateDTO, CandidatUpdateDTO, CompetenceDTO, ExperienceDTO, ExperienceCreateDTO | 6 |
| **Recruteur** | RecruteurDTO, EntrepriseDTO, EntrepriseCreateDTO, EntrepriseUpdateDTO | 4 |
| **Offre** | OffreDTO, OffreCreateDTO, OffreUpdateDTO, EntrepriseResumeDTO | 4 |
| **Candidature** | CandidatureDTO, CandidatureCreateDTO, CandidatResumeDTO, OffreResumeDTO, CandidatureUpdateStatutDTO | 5 |
| **Administration** | StatsDTO, UserValidationDTO, UserListDTO | 3 |
| **IA** | CvParseResponseDTO, SkillsExtractionDTO, CvUploadResponseDTO, MatchingResultDTO, MatchingRequestDTO | 5 |
| **Recherche** | OffreSearchDTO, PageResponseDTO | 2 |
| **Générique** | ApiResponseDTO, ErrorResponseDTO | 2 |
| **Total** | | **34 DTOs** |

---

## 12. Validation Bean Validation

### 12.1 Annotations Courantes

| Annotation | Description | Exemple |
|------------|-------------|---------|
| `@NotNull` | Ne peut pas être null | `private Long id;` |
| `@NotBlank` | Ne peut pas être null ou vide | `private String nom;` |
| `@NotEmpty` | Collection non vide | `private List items;` |
| `@Size` | Taille de chaîne/collection | `@Size(min=2, max=100)` |
| `@Min` | Valeur minimale | `@Min(18)` |
| `@Max` | Valeur maximale | `@Max(100)` |
| `@Email` | Format email | `@Email` |
| `@Pattern` | Expression régulière | `@Pattern(regex="...")` |
| `@Past` | Date dans le passé | `@Past` |
| `@Future` | Date dans le futur | `@Future` |
| `@Positive` | Nombre positif | `@Positive` |
| `@Negative` | Nombre négatif | `@Negative` |

### 12.2 Messages d'Erreur Personnalisés

```java
@NotBlank(message = "Le nom est obligatoire")
@Size(min = 2, max = 100, message = "Le nom doit contenir entre 2 et 100 caractères")
private String nom;
```

### 12.3 Validation de Groupe

```java
public interface CreateValidation {}
public interface UpdateValidation {}

@NotNull(groups = UpdateValidation.class)
private Long id;

@NotNull(groups = CreateValidation.class)
private String email;
```
