package ma.recrutement.controller.candidat;

import com.fasterxml.jackson.databind.ObjectMapper;
import ma.recrutement.config.BaseControllerTest;
import ma.recrutement.config.TestDataFactory;
import ma.recrutement.dto.*;
import ma.recrutement.entity.*;
import ma.recrutement.repository.CandidatRepository;
import ma.recrutement.repository.CompetenceRepository;
import ma.recrutement.repository.ExperienceRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test class for {@link ma.recrutement.controller.CandidatController}.
 * Tests all candidate endpoints: profile, CV, skills, experiences, applications.
 *
 * Endpoints tested:
 * - GET /api/v1/candidats/me
 * - PUT /api/v1/candidats/me
 * - POST /api/v1/candidats/me/cv
 * - GET /api/v1/candidats/me/cv
 * - DELETE /api/v1/candidats/me/cv
 * - POST /api/v1/candidats/me/competences
 * - DELETE /api/v1/candidats/me/competences/{competenceId}
 * - POST /api/v1/candidats/me/experiences
 * - DELETE /api/v1/candidats/me/experiences/{experienceId}
 * - GET /api/v1/candidats/me/candidatures
 *
 * @author Recrutement Team
 * @version 1.0.0
 */
@DisplayName("Candidate Controller Tests")
class CandidatControllerTest extends BaseControllerTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CandidatRepository candidatRepository;

    @Autowired
    private CompetenceRepository competenceRepository;

    @Autowired
    private ExperienceRepository experienceRepository;

    @Nested
    @DisplayName("GET /api/v1/candidats/me - Get Candidate Profile")
    class GetProfileTests {

        @Test
        @DisplayName("Should get candidate profile successfully")
        void shouldGetCandidateProfileSuccessfully() throws Exception {
            // When/Then
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/v1/candidats/me")
                    .header("Authorization", "Bearer " + candidatToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testCandidat.getId()))
                .andExpect(jsonPath("$.email").value(testCandidat.getEmail()))
                .andExpect(jsonPath("$.nom").value(testCandidat.getNom()))
                .andExpect(jsonPath("$.prenom").value(testCandidat.getPrenom()))
                .andExpect(jsonPath("$.telephone").value(testCandidat.getTelephone()));
        }

        @Test
        @DisplayName("Should return 401 without authentication")
        void shouldReturn401WithoutAuthentication() throws Exception {
            // When/Then
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/v1/candidats/me"))
                .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Should return 401 with invalid token")
        void shouldReturn401WithInvalidToken() throws Exception {
            // When/Then
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/v1/candidats/me")
                    .header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("PUT /api/v1/candidats/me - Update Candidate Profile")
    class UpdateProfileTests {

        @Test
        @DisplayName("Should update candidate profile successfully")
        void shouldUpdateCandidateProfileSuccessfully() throws Exception {
            // Given
            CandidatDTO updateDTO = CandidatDTO.builder()
                .nom("Updated")
                .prenom("Candidate")
                .telephone("0699999999")
                .titreProfil("Senior Developer")
                .disponibilite(Candidat.Disponibilite.IMMEDIATE)
                .anneesExperience(5)
                .salaireSouhaite(60000L)
                .build();

            // When/Then
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put("/api/v1/candidats/me")
                    .header("Authorization", "Bearer " + candidatToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nom").value("Updated"))
                .andExpect(jsonPath("$.prenom").value("Candidate"))
                .andExpect(jsonPath("$.titreProfil").value("Senior Developer"))
                .andExpect(jsonPath("$.anneesExperience").value(5))
                .andExpect(jsonPath("$.salaireSouhaite").value(60000L));
        }

        @Test
        @DisplayName("Should return 401 without authentication")
        void shouldReturn401WithoutAuthentication() throws Exception {
            // Given
            CandidatDTO updateDTO = TestDataFactory.createCandidateDTO();

            // When/Then
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put("/api/v1/candidats/me")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Should validate phone number format")
        void shouldValidatePhoneNumberFormat() throws Exception {
            // Given - invalid phone
            CandidatDTO updateDTO = CandidatDTO.builder()
                .telephone("invalid-phone")
                .build();

            // When/Then
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put("/api/v1/candidats/me")
                    .header("Authorization", "Bearer " + candidatToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should validate salary is positive")
        void shouldValidateSalaryIsPositive() throws Exception {
            // Given - negative salary
            CandidatDTO updateDTO = CandidatDTO.builder()
                .salaireSouhaite(-1000L)
                .build();

            // When/Then
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put("/api/v1/candidats/me")
                    .header("Authorization", "Bearer " + candidatToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("POST /api/v1/candidats/me/cv - Upload CV")
    class UploadCvTests {

        @Test
        @DisplayName("Should upload CV successfully")
        void shouldUploadCVSuccessfully() throws Exception {
            // Given
            MockMultipartFile file = new MockMultipartFile(
                "file",
                "cv.pdf",
                MediaType.APPLICATION_PDF_VALUE,
                "Fake PDF content for testing".getBytes()
            );

            // When/Then
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart("/api/v1/candidats/me/cv")
                    .file(file)
                    .header("Authorization", "Bearer " + candidatToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cvPath").isNotEmpty())
                .andExpect(jsonPath("$.message").value("CV uploaded successfully"));
        }

        @Test
        @DisplayName("Should return 401 without authentication")
        void shouldReturn401WithoutAuthentication() throws Exception {
            // Given
            MockMultipartFile file = new MockMultipartFile(
                "file",
                "cv.pdf",
                MediaType.APPLICATION_PDF_VALUE,
                "content".getBytes()
            );

            // When/Then
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart("/api/v1/candidats/me/cv")
                    .file(file))
                .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Should validate file type")
        void shouldValidateFileType() throws Exception {
            // Given - invalid file type
            MockMultipartFile file = new MockMultipartFile(
                "file",
                "document.txt",
                MediaType.TEXT_PLAIN_VALUE,
                "text content".getBytes()
            );

            // When/Then
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart("/api/v1/candidats/me/cv")
                    .file(file)
                    .header("Authorization", "Bearer " + candidatToken))
                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should validate file size")
        void shouldValidateFileSize() throws Exception {
            // Given - large file (>10MB)
            byte[] largeContent = new byte[11 * 1024 * 1024]; // 11MB
            MockMultipartFile file = new MockMultipartFile(
                "file",
                "large.pdf",
                MediaType.APPLICATION_PDF_VALUE,
                largeContent
            );

            // When/Then
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart("/api/v1/candidats/me/cv")
                    .file(file)
                    .header("Authorization", "Bearer " + candidatToken))
                .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/candidats/me/cv - Get CV")
    class GetCvTests {

        @Test
        @DisplayName("Should get CV path successfully when CV exists")
        void shouldGetCVPathSuccessfully() throws Exception {
            // Given - upload a CV first
            testCandidat.setCvPath("/uploads/cv/test-candidat.pdf");
            candidatRepository.save(testCandidat);

            // When/Then
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/v1/candidats/me/cv")
                    .header("Authorization", "Bearer " + candidatToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value("/uploads/cv/test-candidat.pdf"));
        }

        @Test
        @DisplayName("Should return empty when no CV uploaded")
        void shouldReturnEmptyWhenNoCVUploaded() throws Exception {
            // Given - no CV
            testCandidat.setCvPath(null);
            candidatRepository.save(testCandidat);

            // When/Then
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/v1/candidats/me/cv")
                    .header("Authorization", "Bearer " + candidatToken))
                .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("Should return 401 without authentication")
        void shouldReturn401WithoutAuthentication() throws Exception {
            // When/Then
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/v1/candidats/me/cv"))
                .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/candidats/me/cv - Delete CV")
    class DeleteCvTests {

        @Test
        @DisplayName("Should delete CV successfully")
        void shouldDeleteCVSuccessfully() throws Exception {
            // Given - upload a CV first
            testCandidat.setCvPath("/uploads/cv/test-candidat.pdf");
            candidatRepository.save(testCandidat);

            // When/Then
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete("/api/v1/candidats/me/cv")
                    .header("Authorization", "Bearer " + candidatToken))
                .andExpect(status().isNoContent());

            // Verify deletion
            var updated = candidatRepository.findById(testCandidat.getId()).orElseThrow();
            assertNull(updated.getCvPath());
        }

        @Test
        @DisplayName("Should return 401 without authentication")
        void shouldReturn401WithoutAuthentication() throws Exception {
            // When/Then
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete("/api/v1/candidats/me/cv"))
                .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("POST /api/v1/candidats/me/competences - Add Skill")
    class AddCompetenceTests {

        @Test
        @DisplayName("Should add skill successfully")
        void shouldAddSkillSuccessfully() throws Exception {
            // Given
            CompetenceDTO skillDTO = TestDataFactory.createSkillDTO();
            skillDTO.setNom("Python");

            // When/Then
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/v1/candidats/me/competences")
                    .header("Authorization", "Bearer " + candidatToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(skillDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.nom").value("Python"))
                .andExpect(jsonPath("$.niveau").value(Competence.NiveauCompetence.AVANCE.name()))
                .andExpect(jsonPath("$.categorie").value(Competence.CategorieCompetence.TECHNIQUE.name()));
        }

        @Test
        @DisplayName("Should return 401 without authentication")
        void shouldReturn401WithoutAuthentication() throws Exception {
            // Given
            CompetenceDTO skillDTO = TestDataFactory.createSkillDTO();

            // When/Then
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/v1/candidats/me/competences")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(skillDTO)))
                .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Should validate skill name is not empty")
        void shouldValidateSkillNameIsNotEmpty() throws Exception {
            // Given - empty name
            CompetenceDTO skillDTO = CompetenceDTO.builder()
                .nom("")
                .niveau(Competence.NiveauCompetence.INTERMEDIAIRE)
                .categorie(Competence.CategorieCompetence.TECHNIQUE)
                .build();

            // When/Then
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/v1/candidats/me/competences")
                    .header("Authorization", "Bearer " + candidatToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(skillDTO)))
                .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/candidats/me/competences/{competenceId} - Delete Skill")
    class DeleteCompetenceTests {

        @Test
        @DisplayName("Should delete skill successfully")
        void shouldDeleteSkillSuccessfully() throws Exception {
            // Given - create a skill
            Competence skill = Competence.builder()
                .nom("Testing Skill")
                .niveau(Competence.NiveauCompetence.DEBUTANT)
                .categorie(Competence.CategorieCompetence.TECHNIQUE)
                .candidat(testCandidat)
                .build();
            skill = competenceRepository.save(skill);

            // When/Then
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete("/api/v1/candidats/me/competences/" + skill.getId())
                    .header("Authorization", "Bearer " + candidatToken))
                .andExpect(status().isNoContent());

            // Verify deletion
            assertFalse(competenceRepository.existsById(skill.getId()));
        }

        @Test
        @DisplayName("Should return 404 for non-existent skill")
        void shouldReturn404ForNonExistentSkill() throws Exception {
            // When/Then
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete("/api/v1/candidats/me/competences/999999")
                    .header("Authorization", "Bearer " + candidatToken))
                .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should return 403 when deleting another candidate's skill")
        void shouldReturn403WhenDeletingAnotherCandidatesSkill() throws Exception {
            // Given - another candidate's skill
            var otherCandidat = createTestCandidat("other@test.com", "Other", "Candidate");
            otherCandidat = candidatRepository.save(otherCandidat);

            Competence otherSkill = Competence.builder()
                .nom("Other Skill")
                .niveau(Competence.NiveauCompetence.INTERMEDIAIRE)
                .categorie(Competence.CategorieCompetence.TECHNIQUE)
                .candidat(otherCandidat)
                .build();
            otherSkill = competenceRepository.save(otherSkill);

            // When/Then
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete("/api/v1/candidats/me/competences/" + otherSkill.getId())
                    .header("Authorization", "Bearer " + candidatToken))
                .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("POST /api/v1/candidats/me/experiences - Add Experience")
    class AddExperienceTests {

        @Test
        @DisplayName("Should add experience successfully")
        void shouldAddExperienceSuccessfully() throws Exception {
            // Given
            ExperienceDTO expDTO = TestDataFactory.createExperienceDTO();

            // When/Then
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/v1/candidats/me/experiences")
                    .header("Authorization", "Bearer " + candidatToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(expDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.titre").value(expDTO.getTitre()))
                .andExpect(jsonPath("$.entreprise").value(expDTO.getEntreprise()))
                .andExpect(jsonPath("$.localisation").value(expDTO.getLocalisation()));
        }

        @Test
        @DisplayName("Should return 401 without authentication")
        void shouldReturn401WithoutAuthentication() throws Exception {
            // Given
            ExperienceDTO expDTO = TestDataFactory.createExperienceDTO();

            // When/Then
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/v1/candidats/me/experiences")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(expDTO)))
                .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Should validate date range")
        void shouldValidateDateRange() throws Exception {
            // Given - end date before start date
            ExperienceDTO expDTO = ExperienceDTO.builder()
                .titre("Developer")
                .entreprise("Tech Corp")
                .dateDebut(LocalDate.of(2023, 1, 1))
                .dateFin(LocalDate.of(2020, 1, 1))
                .posteActuel(false)
                .build();

            // When/Then
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/v1/candidats/me/experiences")
                    .header("Authorization", "Bearer " + candidatToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(expDTO)))
                .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/candidats/me/experiences/{experienceId} - Delete Experience")
    class DeleteExperienceTests {

        @Test
        @DisplayName("Should delete experience successfully")
        void shouldDeleteExperienceSuccessfully() throws Exception {
            // Given - create an experience
            Experience exp = Experience.builder()
                .titre("Test Experience")
                .entreprise("Test Company")
                .dateDebut(LocalDate.of(2020, 1, 1))
                .dateFin(LocalDate.of(2023, 1, 1))
                .posteActuel(false)
                .candidat(testCandidat)
                .build();
            exp = experienceRepository.save(exp);

            // When/Then
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete("/api/v1/candidats/me/experiences/" + exp.getId())
                    .header("Authorization", "Bearer " + candidatToken))
                .andExpect(status().isNoContent());

            // Verify deletion
            assertFalse(experienceRepository.existsById(exp.getId()));
        }

        @Test
        @DisplayName("Should return 404 for non-existent experience")
        void shouldReturn404ForNonExistentExperience() throws Exception {
            // When/Then
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete("/api/v1/candidats/me/experiences/999999")
                    .header("Authorization", "Bearer " + candidatToken))
                .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should return 403 when deleting another candidate's experience")
        void shouldReturn403WhenDeletingAnotherCandidatesExperience() throws Exception {
            // Given - another candidate's experience
            var otherCandidat = createTestCandidat("other@test.com", "Other", "Candidate");
            otherCandidat = candidatRepository.save(otherCandidat);

            Experience otherExp = Experience.builder()
                .titre("Other Experience")
                .entreprise("Other Company")
                .dateDebut(LocalDate.of(2020, 1, 1))
                .posteActuel(true)
                .candidat(otherCandidat)
                .build();
            otherExp = experienceRepository.save(otherExp);

            // When/Then
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete("/api/v1/candidats/me/experiences/" + otherExp.getId())
                    .header("Authorization", "Bearer " + candidatToken))
                .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/candidats/me/candidatures - Get Candidate Applications")
    class GetCandidaturesTests {

        @Test
        @DisplayName("Should get applications list successfully")
        void shouldGetApplicationsListSuccessfully() throws Exception {
            // When/Then
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/v1/candidats/me/candidatures")
                    .header("Authorization", "Bearer " + candidatToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
        }

        @Test
        @DisplayName("Should return empty list when no applications")
        void shouldReturnEmptyListWhenNoApplications() throws Exception {
            // Given - no applications exist

            // When/Then
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/v1/candidats/me/candidatures")
                    .header("Authorization", "Bearer " + candidatToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()", is(0)));
        }

        @Test
        @DisplayName("Should return 401 without authentication")
        void shouldReturn401WithoutAuthentication() throws Exception {
            // When/Then
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/v1/candidats/me/candidatures"))
                .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("Integration Tests - Complete Candidate Flow")
    class IntegrationTests {

        @Test
        @DisplayName("Should complete full candidate profile setup")
        void shouldCompleteFullCandidateProfileSetup() throws Exception {
            // Step 1: Update profile
            CandidatDTO profileDTO = CandidatDTO.builder()
                .titreProfil("Full Stack Developer")
                .disponibilite(Candidat.Disponibilite.IMMEDIATE)
                .anneesExperience(3)
                .salaireSouhaite(50000L)
                .build();

            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put("/api/v1/candidats/me")
                    .header("Authorization", "Bearer " + candidatToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(profileDTO)))
                .andExpect(status().isOk());

            // Step 2: Add skills
            CompetenceDTO skillDTO = TestDataFactory.createSkillDTO();
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/v1/candidats/me/competences")
                    .header("Authorization", "Bearer " + candidatToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(skillDTO)))
                .andExpect(status().isCreated());

            // Step 3: Add experience
            ExperienceDTO expDTO = TestDataFactory.createExperienceDTO();
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/v1/candidats/me/experiences")
                    .header("Authorization", "Bearer " + candidatToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(expDTO)))
                .andExpect(status().isCreated());

            // Step 4: Verify profile
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/v1/candidats/me")
                    .header("Authorization", "Bearer " + candidatToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titreProfil").value("Full Stack Developer"))
                .andExpect(jsonPath("$.anneesExperience").value(3));
        }
    }
}
