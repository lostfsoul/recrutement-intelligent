package ma.recrutement.controller.candidature;

import com.fasterxml.jackson.databind.ObjectMapper;
import ma.recrutement.config.BaseControllerTest;
import ma.recrutement.config.TestDataFactory;
import ma.recrutement.dto.CandidatureCreateDTO;
import ma.recrutement.dto.CandidatureDTO;
import ma.recrutement.entity.*;
import ma.recrutement.repository.CandidatureRepository;
import ma.recrutement.repository.OffreEmploiRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test class for {@link ma.recrutement.controller.CandidatureController}.
 * Tests all application endpoints: apply, view, update status, cancel.
 *
 * Endpoints tested:
 * - POST /api/v1/offres/{offreId}/postuler
 * - GET /api/v1/candidatures/me
 * - GET /api/v1/offres/{offreId}/candidatures
 * - PUT /api/v1/candidatures/{candidatureId}/statut
 * - POST /api/v1/candidatures/{candidatureId}/vue
 * - DELETE /api/v1/candidatures/{candidatureId}
 * - GET /api/v1/offres/{offreId}/candidatures/non-vues/count
 *
 * @author Recrutement Team
 * @version 1.0.0
 */
@DisplayName("Application Controller Tests")
class CandidatureControllerTest extends BaseControllerTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CandidatureRepository candidatureRepository;

    @Autowired
    private OffreEmploiRepository offreEmploiRepository;

    private Candidature testCandidature;

    @Override
    protected void createTestData() {
        super.createTestData();

        // Create a published job offer for applications
        testOffre.setStatut(OffreEmploi.StatutOffre.PUBLIEE);
        testOffre = offreEmploiRepository.save(testOffre);

        // Create a test application
        testCandidature = Candidature.builder()
            .candidat(testCandidat)
            .offre(testOffre)
            .lettreMotivation("I am very interested in this position")
            .statut(Candidature.StatutCandidature.EN_ATTENTE)
            .dateCandidature(java.time.LocalDateTime.now())
            .vue(false)
            .build();
        testCandidature = candidatureRepository.save(testCandidature);
    }

    @Nested
    @DisplayName("POST /api/v1/offres/{offreId}/postuler - Apply to Job Offer")
    class ApplyToOffreTests {

        @Test
        @DisplayName("Should apply to job offer successfully as candidate")
        void shouldApplyToJobOfferSuccessfully() throws Exception {
            // Given - create a new published offer
            var newOffre = createTestOffre("New Position", "Description");
            newOffre.setStatut(OffreEmploi.StatutOffre.PUBLIEE);
            newOffre.setRecruteur(testRecruteur);
            newOffre.setEntreprise(testEntreprise);
            newOffre = offreEmploiRepository.save(newOffre);

            CandidatureCreateDTO createDTO = CandidatureCreateDTO.builder()
                .lettreMotivation("I would like to apply for this position")
                .offreId(newOffre.getId())
                .build();

            // When/Then
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/v1/offres/" + newOffre.getId() + "/postuler")
                    .header("Authorization", "Bearer " + candidatToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.lettreMotivation").value(createDTO.getLettreMotivation()))
                .andExpect(jsonPath("$.statut").value(Candidature.StatutCandidature.EN_ATTENTE.name()));
        }

        @Test
        @DisplayName("Should return 401 without authentication")
        void shouldReturn401WithoutAuthentication() throws Exception {
            // Given
            CandidatureCreateDTO createDTO = TestDataFactory.createApplicationCreateDTO();

            // When/Then
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/v1/offres/" + testOffre.getId() + "/postuler")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Should return 400 when applying twice to same offer")
        void shouldReturn400WhenApplyingTwiceToSameOffer() throws Exception {
            // Given - already applied via createTestData

            CandidatureCreateDTO createDTO = CandidatureCreateDTO.builder()
                .lettreMotivation("Second application")
                .offreId(testOffre.getId())
                .build();

            // When/Then
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/v1/offres/" + testOffre.getId() + "/postuler")
                    .header("Authorization", "Bearer " + candidatToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 404 when applying to non-existent offer")
        void shouldReturn404WhenApplyingToNonExistentOffer() throws Exception {
            // Given
            CandidatureCreateDTO createDTO = TestDataFactory.createApplicationCreateDTO();

            // When/Then
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/v1/offres/999999/postuler")
                    .header("Authorization", "Bearer " + candidatToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should allow empty motivation letter")
        void shouldAllowEmptyMotivationLetter() throws Exception {
            // Given - create a new offer
            var newOffre = createTestOffre("Another Position", "Description");
            newOffre.setStatut(OffreEmploi.StatutOffre.PUBLIEE);
            newOffre.setRecruteur(testRecruteur);
            newOffre.setEntreprise(testEntreprise);
            newOffre = offreEmploiRepository.save(newOffre);

            CandidatureCreateDTO createDTO = CandidatureCreateDTO.builder()
                .offreId(newOffre.getId())
                .build();

            // When/Then
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/v1/offres/" + newOffre.getId() + "/postuler")
                    .header("Authorization", "Bearer " + candidatToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isCreated());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/candidatures/me - Get My Applications")
    class GetMyCandidaturesTests {

        @Test
        @DisplayName("Should get my applications successfully")
        void shouldGetMyApplicationsSuccessfully() throws Exception {
            // When/Then
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/v1/candidatures/me")
                    .header("Authorization", "Bearer " + candidatToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].id").value(testCandidature.getId()))
                .andExpect(jsonPath("$[0].statut").value(Candidature.StatutCandidature.EN_ATTENTE.name()));
        }

        @Test
        @DisplayName("Should return empty list when no applications")
        void shouldReturnEmptyListWhenNoApplications() throws Exception {
            // Given - delete all applications
            candidatureRepository.deleteAll();

            // When/Then
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/v1/candidatures/me")
                    .header("Authorization", "Bearer " + candidatToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()", is(0)));
        }

        @Test
        @DisplayName("Should return 401 without authentication")
        void shouldReturn401WithoutAuthentication() throws Exception {
            // When/Then
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/v1/candidatures/me"))
                .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/offres/{offreId}/candidatures - Get Offer Applications")
    class GetOfferCandidaturesTests {

        @Test
        @DisplayName("Should get applications for own offer as recruiter")
        void shouldGetApplicationsForOwnOfferAsRecruiter() throws Exception {
            // When/Then
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/v1/offres/" + testOffre.getId() + "/candidatures")
                    .header("Authorization", "Bearer " + recruteurToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].id").value(testCandidature.getId()));
        }

        @Test
        @DisplayName("Should return 403 when accessing another recruiter's offer applications")
        void shouldReturn403WhenAccessingAnotherRecruitersOfferApplications() throws Exception {
            // Given - create another recruiter and offer
            var otherRecruiter = createTestRecruteur("other@test.com", "Other", "Recruiter");
            otherRecruiter = recruteurRepository.save(otherRecruiter);

            var otherOffre = createTestOffre("Other Position", "Description");
            otherOffre.setStatut(OffreEmploi.StatutOffre.PUBLIEE);
            otherOffre.setRecruteur(otherRecruiter);
            otherOffre = offreEmploiRepository.save(otherOffre);

            // When/Then - try to access with different recruiter
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/v1/offres/" + otherOffre.getId() + "/candidatures")
                    .header("Authorization", "Bearer " + recruteurToken))
                .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Should return 404 for non-existent offer")
        void shouldReturn404ForNonExistentOffer() throws Exception {
            // When/Then
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/v1/offres/999999/candidatures")
                    .header("Authorization", "Bearer " + recruteurToken))
                .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should return empty list when no applications")
        void shouldReturnEmptyListWhenNoApplications() throws Exception {
            // Given - create new offer without applications
            var newOffre = createTestOffre("New Position", "Description");
            newOffre.setStatut(OffreEmploi.StatutOffre.PUBLIEE);
            newOffre.setRecruteur(testRecruteur);
            newOffre.setEntreprise(testEntreprise);
            newOffre = offreEmploiRepository.save(newOffre);

            // When/Then
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/v1/offres/" + newOffre.getId() + "/candidatures")
                    .header("Authorization", "Bearer " + recruteurToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()", is(0)));
        }
    }

    @Nested
    @DisplayName("PUT /api/v1/candidatures/{candidatureId}/statut - Update Application Status")
    class UpdateStatutTests {

        @Test
        @DisplayName("Should update application status as recruiter")
        void shouldUpdateApplicationStatusAsRecruiter() throws Exception {
            // When/Then
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put("/api/v1/candidatures/" + testCandidature.getId() + "/statut")
                    .header("Authorization", "Bearer " + recruteurToken)
                    .param("statut", "ENTRETIEN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statut").value(Candidature.StatutCandidature.ENTRETIEN.name()));

            // Verify update
            var updated = candidatureRepository.findById(testCandidature.getId()).orElseThrow();
            assertEquals(Candidature.StatutCandidature.ENTRETIEN, updated.getStatut());
        }

        @Test
        @DisplayName("Should return 404 for non-existent application")
        void shouldReturn404ForNonExistentApplication() throws Exception {
            // When/Then
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put("/api/v1/candidatures/999999/statut")
                    .header("Authorization", "Bearer " + recruteurToken)
                    .param("statut", "ACCEPTEE"))
                .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should return 403 when candidate tries to update status")
        void shouldReturn403WhenCandidateTriesToUpdateStatus() throws Exception {
            // When/Then
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put("/api/v1/candidatures/" + testCandidature.getId() + "/statut")
                    .header("Authorization", "Bearer " + candidatToken)
                    .param("statut", "ACCEPTEE"))
                .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Should validate status enum value")
        void shouldValidateStatusEnumValue() throws Exception {
            // When/Then - invalid status
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put("/api/v1/candidatures/" + testCandidature.getId() + "/statut")
                    .header("Authorization", "Bearer " + recruteurToken)
                    .param("statut", "INVALID_STATUS"))
                .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("POST /api/v1/candidatures/{candidatureId}/vue - Mark as Viewed")
    class MarkAsViewedTests {

        @Test
        @DisplayName("Should mark application as viewed as recruiter")
        void shouldMarkApplicationAsViewedAsRecruiter() throws Exception {
            // Given - application is not viewed
            assertFalse(testCandidature.isVue());

            // When/Then
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/v1/candidatures/" + testCandidature.getId() + "/vue")
                    .header("Authorization", "Bearer " + recruteurToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.vue").value(true));

            // Verify update
            var updated = candidatureRepository.findById(testCandidature.getId()).orElseThrow();
            assertTrue(updated.isVue());
        }

        @Test
        @DisplayName("Should return 404 for non-existent application")
        void shouldReturn404ForNonExistentApplication() throws Exception {
            // When/Then
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/v1/candidatures/999999/vue")
                    .header("Authorization", "Bearer " + recruteurToken))
                .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should return 403 when candidate tries to mark as viewed")
        void shouldReturn403WhenCandidateTriesToMarkAsViewed() throws Exception {
            // When/Then
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/v1/candidatures/" + testCandidature.getId() + "/vue")
                    .header("Authorization", "Bearer " + candidatToken))
                .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Should handle already viewed application")
        void shouldHandleAlreadyViewedApplication() throws Exception {
            // Given - mark as viewed first
            testCandidature.setVue(true);
            candidatureRepository.save(testCandidature);

            // When/Then - should still succeed
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/v1/candidatures/" + testCandidature.getId() + "/vue")
                    .header("Authorization", "Bearer " + recruteurToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.vue").value(true));
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/candidatures/{candidatureId} - Cancel Application")
    class CancelCandidatureTests {

        @Test
        @DisplayName("Should cancel own application successfully")
        void shouldCancelOwnApplicationSuccessfully() throws Exception {
            // Given
            Long candidatureId = testCandidature.getId();

            // When/Then
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete("/api/v1/candidatures/" + candidatureId)
                    .header("Authorization", "Bearer " + candidatToken))
                .andExpect(status().isNoContent());

            // Verify deletion
            assertFalse(candidatureRepository.existsById(candidatureId));
        }

        @Test
        @DisplayName("Should return 404 for non-existent application")
        void shouldReturn404ForNonExistentApplication() throws Exception {
            // When/Then
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete("/api/v1/candidatures/999999")
                    .header("Authorization", "Bearer " + candidatToken))
                .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should return 403 when recruiter tries to cancel candidate's application")
        void shouldReturn403WhenRecruiterTriesToCancelCandidatesApplication() throws Exception {
            // When/Then
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete("/api/v1/candidatures/" + testCandidature.getId())
                    .header("Authorization", "Bearer " + recruteurToken))
                .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/offres/{offreId}/candidatures/non-vues/count - Count Unviewed")
    class CountUnviewedTests {

        @Test
        @DisplayName("Should count unviewed applications successfully")
        void shouldCountUnviewedApplicationsSuccessfully() throws Exception {
            // When/Then
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/v1/offres/" + testOffre.getId() + "/candidatures/non-vues/count")
                    .header("Authorization", "Bearer " + recruteurToken))
                .andExpect(status().isOk())
                .andExpect(content().string(String.valueOf(1L))); // One unviewed application
        }

        @Test
        @DisplayName("Should return 0 when all applications are viewed")
        void shouldReturn0WhenAllApplicationsAreViewed() throws Exception {
            // Given - mark as viewed
            testCandidature.setVue(true);
            candidatureRepository.save(testCandidature);

            // When/Then
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/v1/offres/" + testOffre.getId() + "/candidatures/non-vues/count")
                    .header("Authorization", "Bearer " + recruteurToken))
                .andExpect(status().isOk())
                .andExpect(content().string(String.valueOf(0L)));
        }

        @Test
        @DisplayName("Should return 403 when candidate tries to count")
        void shouldReturn403WhenCandidateTriesToCount() throws Exception {
            // When/Then
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/v1/offres/" + testOffre.getId() + "/candidatures/non-vues/count")
                    .header("Authorization", "Bearer " + candidatToken))
                .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Should return 404 for non-existent offer")
        void shouldReturn404ForNonExistentOffer() throws Exception {
            // When/Then
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/v1/offres/999999/candidatures/non-vues/count")
                    .header("Authorization", "Bearer " + recruteurToken))
                .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("Integration Tests - Complete Application Flow")
    class IntegrationTests {

        @Test
        @DisplayName("Should complete full application lifecycle")
        void shouldCompleteFullApplicationLifecycle() throws Exception {
            // Step 1: Create new published offer
            var newOffre = createTestOffre("Integration Test Position", "Description");
            newOffre.setStatut(OffreEmploi.StatutOffre.PUBLIEE);
            newOffre.setRecruteur(testRecruteur);
            newOffre.setEntreprise(testEntreprise);
            newOffre = offreEmploiRepository.save(newOffre);

            // Step 2: Candidate applies
            CandidatureCreateDTO applyDTO = CandidatureCreateDTO.builder()
                .lettreMotivation("I am very interested")
                .offreId(newOffre.getId())
                .build();

            MvcResult result = mockMvc.perform(
                    org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/v1/offres/" + newOffre.getId() + "/postuler")
                        .header("Authorization", "Bearer " + candidatToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(applyDTO)))
                .andExpect(status().isCreated())
                .andReturn();

            String response = result.getResponse().getContentAsString();
            CandidatureDTO created = objectMapper.readValue(response, CandidatureDTO.class);

            // Step 3: Candidate views their applications
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/v1/candidatures/me")
                    .header("Authorization", "Bearer " + candidatToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[?(@.id == " + created.getId() + ")]").exists());

            // Step 4: Recruiter views applications for the offer
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/v1/offres/" + newOffre.getId() + "/candidatures")
                    .header("Authorization", "Bearer " + recruteurToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[?(@.id == " + created.getId() + ")]").exists());

            // Step 5: Count unviewed applications
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/v1/offres/" + newOffre.getId() + "/candidatures/non-vues/count")
                    .header("Authorization", "Bearer " + recruteurToken))
                .andExpect(status().isOk())
                .andExpect(content().string(String.valueOf(greaterThanOrEqualTo(1L))));

            // Step 6: Mark as viewed
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/v1/candidatures/" + created.getId() + "/vue")
                    .header("Authorization", "Bearer " + recruteurToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.vue").value(true));

            // Step 7: Update status
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put("/api/v1/candidatures/" + created.getId() + "/statut")
                    .header("Authorization", "Bearer " + recruteurToken)
                    .param("statut", "ENTRETIEN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statut").value(Candidature.StatutCandidature.ENTRETIEN.name()));

            // Step 8: Candidate cancels application
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete("/api/v1/candidatures/" + created.getId())
                    .header("Authorization", "Bearer " + candidatToken))
                .andExpect(status().isNoContent());

            // Verify deletion
            assertFalse(candidatureRepository.existsById(created.getId()));
        }
    }
}
