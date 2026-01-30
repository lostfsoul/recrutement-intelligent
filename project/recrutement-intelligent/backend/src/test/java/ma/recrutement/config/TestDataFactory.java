package ma.recrutement.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import ma.recrutement.dto.*;
import ma.recrutement.entity.*;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

/**
 * Factory class for creating test data and DTOs.
 * Provides fluent builders for test objects to keep tests clean.
 *
 * @author Recrutement Team
 * @version 1.0.0
 */
public class TestDataFactory {

    private static final ObjectMapper objectMapper = new ObjectMapper()
        .registerModule(new JavaTimeModule());

    /**
     * Creates a default register request DTO for a candidate.
     */
    public static RegisterRequestDTO createCandidateRegisterRequest() {
        return RegisterRequestDTO.builder()
            .email("test.candidat@example.com")
            .password("Password123!")
            .passwordConfirmation("Password123!")
            .nom("Doe")
            .prenom("John")
            .role(Utilisateur.Role.CANDIDAT)
            .telephone("0612345678")
            .build();
    }

    /**
     * Creates a default register request DTO for a recruiter.
     */
    public static RegisterRequestDTO createRecruiterRegisterRequest() {
        return RegisterRequestDTO.builder()
            .email("test.recruteur@example.com")
            .password("Password123!")
            .passwordConfirmation("Password123!")
            .nom("Smith")
            .prenom("Jane")
            .role(Utilisateur.Role.RECRUTEUR)
            .nomEntreprise("Tech Corp")
            .poste("RH Manager")
            .telephone("0687654321")
            .build();
    }

    /**
     * Creates a default register request DTO for an admin.
     */
    public static RegisterRequestDTO createAdminRegisterRequest() {
        return RegisterRequestDTO.builder()
            .email("test.admin@example.com")
            .password("AdminPassword123!")
            .passwordConfirmation("AdminPassword123!")
            .nom("Admin")
            .prenom("Super")
            .role(Utilisateur.Role.ADMINISTRATEUR)
            .build();
    }

    /**
     * Creates a login request DTO.
     */
    public static LoginRequestDTO createLoginRequest(String email, String password) {
        return LoginRequestDTO.builder()
            .email(email)
            .password(password)
            .build();
    }

    /**
     * Creates a candidate DTO.
     */
    public static CandidatDTO createCandidateDTO() {
        return CandidatDTO.builder()
            .nom("Doe")
            .prenom("John")
            .telephone("0612345678")
            .build();
    }

    /**
     * Creates a skill DTO.
     */
    public static CompetenceDTO createSkillDTO() {
        return CompetenceDTO.builder()
            .nom("Java")
            .niveau(Competence.NiveauCompetence.AVANCE)
            .categorie("Technique")
            .build();
    }

    /**
     * Creates an experience DTO.
     */
    public static ExperienceDTO createExperienceDTO() {
        return ExperienceDTO.builder()
            .titre("Senior Developer")
            .entreprise("Tech Company")
            .description("Developed amazing features")
            .dateDebut(LocalDate.of(2020, 1, 1))
            .dateFin(LocalDate.of(2023, 1, 1))
            .emploiActuel(false)
            .localisation("Paris")
            .build();
    }

    /**
     * Creates a company creation DTO.
     */
    public static EntrepriseCreateDTO createCompanyCreateDTO() {
        return EntrepriseCreateDTO.builder()
            .nom("Tech Startup")
            .description("Innovative tech company")
            .secteur("Technology")
            .tailleEntreprise("PME")
            .siteWeb("https://techstartup.com")
            .localisation("Paris, France")
            .build();
    }

    /**
     * Creates a company DTO.
     */
    public static EntrepriseDTO createCompanyDTO() {
        return EntrepriseDTO.builder()
            .nom("Tech Startup")
            .description("Innovative tech company")
            .secteur("Technology")
            .tailleEntreprise("PME")
            .siteWeb("https://techstartup.com")
            .localisation("Paris, France")
            .build();
    }

    /**
     * Creates a job offer creation DTO.
     */
    public static OffreCreateDTO createJobOfferCreateDTO() {
        return OffreCreateDTO.builder()
            .titre("Senior Java Developer")
            .description("We are looking for a senior Java developer to join our team.")
            .typeContrat(OffreEmploi.TypeContrat.CDI)
            .salaireMin(50000)
            .salaireMax(70000)
            .competencesRequises("Java, Spring Boot, PostgreSQL")
            .localisation("Paris, France")
            .entrepriseId(1L)  // Will be set in tests
            .build();
    }

    /**
     * Creates a job offer update DTO.
     */
    public static OffreUpdateDTO createJobOfferUpdateDTO() {
        return OffreUpdateDTO.builder()
            .titre("Senior Java Developer - Updated")
            .description("Updated description")
            .typeContrat(OffreEmploi.TypeContrat.CDD)
            .salaireMin(55000)
            .salaireMax(75000)
            .localisation("Remote")
            .build();
    }

    /**
     * Creates an application creation DTO.
     */
    public static CandidatureCreateDTO createApplicationCreateDTO() {
        return CandidatureCreateDTO.builder()
            .lettreMotivation("I am very interested in this position...")
            .build();
    }

    /**
     * Gets the object mapper for JSON serialization.
     */
    public static ObjectMapper objectMapper() {
        return objectMapper;
    }

    /**
     * Converts an object to JSON string.
     */
    public static String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert object to JSON", e);
        }
    }

    /**
     * Helper class for building custom test data.
     */
    public static class Builder {
        private String email = "test@example.com";
        private String password = "Password123!";
        private String nom = "Test";
        private String prenom = "User";

        public Builder email(String email) {
            this.email = email;
            return this;
        }

        public Builder password(String password) {
            this.password = password;
            return this;
        }

        public Builder nom(String nom) {
            this.nom = nom;
            return this;
        }

        public Builder prenom(String prenom) {
            this.prenom = prenom;
            return this;
        }

        public RegisterRequestDTO buildCandidateRegister() {
            return RegisterRequestDTO.builder()
                .email(email)
                .password(password)
                .passwordConfirmation(password)
                .nom(nom)
                .prenom(prenom)
                .role(Utilisateur.Role.CANDIDAT)
                .telephone("0612345678")
                .build();
        }

        public RegisterRequestDTO buildRecruiterRegister() {
            return RegisterRequestDTO.builder()
                .email(email)
                .password(password)
                .passwordConfirmation(password)
                .nom(nom)
                .prenom(prenom)
                .role(Utilisateur.Role.RECRUTEUR)
                .nomEntreprise("Test Company")
                .poste("RH Manager")
                .telephone("0687654321")
                .build();
        }
    }

    /**
     * Creates a new builder instance.
     */
    public static Builder builder() {
        return new Builder();
    }
}
