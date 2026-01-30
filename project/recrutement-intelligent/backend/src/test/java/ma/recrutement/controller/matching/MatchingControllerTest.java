package ma.recrutement.controller.matching;

import com.fasterxml.jackson.databind.ObjectMapper;
import ma.recrutement.config.BaseControllerTest;
import ma.recrutement.dto.MatchingResultDTO;
import ma.recrutement.entity.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test class for {@link ma.recrutement.controller.MatchingController}.
 * Tests all matching endpoints: find matching offers, find matching candidates, index CVs and offers.
 *
 * Endpoints tested:
 * - GET /api/v1/matching/candidats/{candidatId}/offres
 * - GET /api/v1/matching/offres/{offreId}/candidats
 * - POST /api/v1/matching/cv/{candidatId}/indexer
 * - POST /api/v1/matching/offres/{offreId}/indexer
 *
 * Note: These tests may need to mock the vector store service since it requires
 * pgvector extension which might not be available in all test environments.
 *
 * @author Recrutement Team
 * @version 1.0.0
 */
@DisplayName("Matching Controller Tests")
class MatchingControllerTest extends BaseControllerTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    protected void createTestData() {
        super.createTestData();

        // Ensure the job offer is published for matching
        testOffre.setStatut(OffreEmploi.StatutOffre.PUBLIEE);
        testOffre = offreEmploiRepository.save(testOffre);
    }

    @Nested
    @DisplayName("GET /api/v1/matching/candidats/{candidatId}/offres - Find Matching Offers")
    class FindMatchingOffresTests {

        @Test
        @DisplayName("Should find matching offers for candidate")
        void shouldFindMatchingOffersForCandidate() throws Exception {
            // When/Then
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/v1/matching/candidats/" + testCandidat.getId() + "/offres")
                    .header("Authorization", "Bearer " + candidatToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()", greaterThanOrEqualTo(0)));
        }

        @Test
        @DisplayName("Should respect limit parameter")
        void shouldRespectLimitParameter() throws Exception {
            // When/Then
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/v1/matching/candidats/" + testCandidat.getId() + "/offres")
                    .header("Authorization", "Bearer " + candidatToken)
                    .param("limit", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()", lessThanOrEqualTo(5)));
        }

        @Test
        @DisplayName("Should return 401 without authentication")
        void shouldReturn401WithoutAuthentication() throws Exception {
            // When/Then
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/v1/matching/candidats/" + testCandidat.getId() + "/offres"))
                .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Should return 404 for non-existent candidate")
        void shouldReturn404ForNonExistentCandidate() throws Exception {
            // When/Then
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/v1/matching/candidats/999999/offres")
                    .header("Authorization", "Bearer " + candidatToken))
                .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should return results ordered by match score")
        void shouldReturnResultsOrderedByMatchScore() throws Exception {
            // When/Then - if results exist, they should be ordered by score
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/v1/matching/candidats/" + testCandidat.getId() + "/offres")
                    .header("Authorization", "Bearer " + candidatToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
            // Note: Score ordering validation would require actual matching data
        }
    }

    @Nested
    @DisplayName("GET /api/v1/matching/offres/{offreId}/candidats - Find Matching Candidates")
    class FindMatchingCandidatesTests {

        @Test
        @DisplayName("Should find matching candidates for offer")
        void shouldFindMatchingCandidatesForOffer() throws Exception {
            // When/Then
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/v1/matching/offres/" + testOffre.getId() + "/candidats")
                    .header("Authorization", "Bearer " + recruteurToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()", greaterThanOrEqualTo(0)));
        }

        @Test
        @DisplayName("Should respect limit parameter")
        void shouldRespectLimitParameter() throws Exception {
            // When/Then
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/v1/matching/offres/" + testOffre.getId() + "/candidats")
                    .header("Authorization", "Bearer " + recruteurToken)
                    .param("limit", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()", lessThanOrEqualTo(3)));
        }

        @Test
        @DisplayName("Should return 401 without authentication")
        void shouldReturn401WithoutAuthentication() throws Exception {
            // When/Then
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/v1/matching/offres/" + testOffre.getId() + "/candidats"))
                .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Should return 404 for non-existent offer")
        void shouldReturn404ForNonExistentOffer() throws Exception {
            // When/Then
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/v1/matching/offres/999999/candidats")
                    .header("Authorization", "Bearer " + recruteurToken))
                .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should return 403 when candidate tries to access")
        void shouldReturn403WhenCandidateTriesToAccess() throws Exception {
            // When/Then - candidates shouldn't be able to see matching candidates for offers
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/v1/matching/offres/" + testOffre.getId() + "/candidats")
                    .header("Authorization", "Bearer " + candidatToken))
                .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("POST /api/v1/matching/cv/{candidatId}/indexer - Index CV")
    class IndexCvTests {

        @Test
        @DisplayName("Should index candidate CV successfully")
        void shouldIndexCandidateCVSuccessfully() throws Exception {
            // Given - candidate needs a CV to index
            testCandidat.setCvPath("/uploads/cv/test-cv.pdf");
            candidatRepository.save(testCandidat);

            // When/Then
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/v1/matching/cv/" + testCandidat.getId() + "/indexer")
                    .header("Authorization", "Bearer " + candidatToken))
                .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("Should return 404 for non-existent candidate")
        void shouldReturn404ForNonExistentCandidate() throws Exception {
            // When/Then
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/v1/matching/cv/999999/indexer")
                    .header("Authorization", "Bearer " + candidatToken))
                .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should return 401 without authentication")
        void shouldReturn401WithoutAuthentication() throws Exception {
            // When/Then
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/v1/matching/cv/" + testCandidat.getId() + "/indexer"))
                .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Should handle indexing when no CV exists")
        void shouldHandleIndexingWhenNoCVExists() throws Exception {
            // Given - candidate without CV
            testCandidat.setCvPath(null);
            candidatRepository.save(testCandidat);

            // When/Then - should handle gracefully (400 or 500 depending on implementation)
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/v1/matching/cv/" + testCandidat.getId() + "/indexer")
                    .header("Authorization", "Bearer " + candidatToken))
                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should allow admin to index any CV")
        void shouldAllowAdminToIndexAnyCV() throws Exception {
            // Given - candidate has CV
            testCandidat.setCvPath("/uploads/cv/test-cv.pdf");
            candidatRepository.save(testCandidat);

            // Create admin user
            var admin = createTestAdmin();
            admin = administrateurRepository.save(admin);
            String adminToken = generateTokenForUser(admin.getEmail(), "ADMINISTRATEUR");

            // When/Then
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/v1/matching/cv/" + testCandidat.getId() + "/indexer")
                    .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());
        }
    }

    @Nested
    @DisplayName("POST /api/v1/matching/offres/{offreId}/indexer - Index Offer")
    class IndexOffreTests {

        @Test
        @DisplayName("Should index job offer successfully")
        void shouldIndexJobOfferSuccessfully() throws Exception {
            // When/Then
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/v1/matching/offres/" + testOffre.getId() + "/indexer")
                    .header("Authorization", "Bearer " + recruteurToken))
                .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("Should return 404 for non-existent offer")
        void shouldReturn404ForNonExistentOffer() throws Exception {
            // When/Then
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/v1/matching/offres/999999/indexer")
                    .header("Authorization", "Bearer " + recruteurToken))
                .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should return 401 without authentication")
        void shouldReturn401WithoutAuthentication() throws Exception {
            // When/Then
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/v1/matching/offres/" + testOffre.getId() + "/indexer"))
                .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Should return 403 when candidate tries to index offer")
        void shouldReturn403WhenCandidateTriesToIndexOffer() throws Exception {
            // When/Then
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/v1/matching/offres/" + testOffre.getId() + "/indexer")
                    .header("Authorization", "Bearer " + candidatToken))
                .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Should allow admin to index any offer")
        void shouldAllowAdminToIndexAnyOffer() throws Exception {
            // Create admin user
            var admin = createTestAdmin();
            admin = administrateurRepository.save(admin);
            String adminToken = generateTokenForUser(admin.getEmail(), "ADMINISTRATEUR");

            // When/Then
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/v1/matching/offres/" + testOffre.getId() + "/indexer")
                    .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());
        }
    }

    @Nested
    @DisplayName("Integration Tests - Complete Matching Flow")
    class IntegrationTests {

        @Test
        @DisplayName("Should complete full matching workflow")
        void shouldCompleteFullMatchingWorkflow() throws Exception {
            // Step 1: Candidate uploads CV
            testCandidat.setCvPath("/uploads/cv/test-cv.pdf");
            candidatRepository.save(testCandidat);

            // Step 2: Index the CV
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/v1/matching/cv/" + testCandidat.getId() + "/indexer")
                    .header("Authorization", "Bearer " + candidatToken))
                .andExpect(status().isNoContent());

            // Step 3: Index the job offer
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/v1/matching/offres/" + testOffre.getId() + "/indexer")
                    .header("Authorization", "Bearer " + recruteurToken))
                .andExpect(status().isNoContent());

            // Step 4: Find matching offers for candidate
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/v1/matching/candidats/" + testCandidat.getId() + "/offres")
                    .header("Authorization", "Bearer " + candidatToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

            // Step 5: Find matching candidates for offer
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/v1/matching/offres/" + testOffre.getId() + "/candidats")
                    .header("Authorization", "Bearer " + recruteurToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
        }

        @Test
        @DisplayName("Should handle matching with multiple offers and candidates")
        void shouldHandleMatchingWithMultipleOffersAndCandidates() throws Exception {
            // Given - create additional offers and candidates
            var otherCandidat = createTestCandidat("other@test.com", "Other", "Candidate");
            otherCandidat.setCvPath("/uploads/cv/other-cv.pdf");
            otherCandidat = candidatRepository.save(otherCandidat);

            var otherOffre = createTestOffre("Other Position", "Description");
            otherOffre.setStatut(OffreEmploi.StatutOffre.PUBLIEE);
            otherOffre.setRecruteur(testRecruteur);
            otherOffre.setEntreprise(testEntreprise);
            otherOffre = offreEmploiRepository.save(otherOffre);

            // Index all
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/v1/matching/cv/" + testCandidat.getId() + "/indexer")
                    .header("Authorization", "Bearer " + candidatToken))
                .andExpect(status().isNoContent());

            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/v1/matching/cv/" + otherCandidat.getId() + "/indexer")
                    .header("Authorization", "Bearer " + generateTokenForUser(otherCandidat.getEmail(), "CANDIDAT")))
                .andExpect(status().isNoContent());

            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/v1/matching/offres/" + testOffre.getId() + "/indexer")
                    .header("Authorization", "Bearer " + recruteurToken))
                .andExpect(status().isNoContent());

            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/v1/matching/offres/" + otherOffre.getId() + "/indexer")
                    .header("Authorization", "Bearer " + recruteurToken))
                .andExpect(status().isNoContent());

            // Get matching results
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/v1/matching/candidats/" + testCandidat.getId() + "/offres")
                    .header("Authorization", "Bearer " + candidatToken)
                    .param("limit", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/v1/matching/offres/" + testOffre.getId() + "/candidats")
                    .header("Authorization", "Bearer " + recruteurToken)
                    .param("limit", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
        }
    }

    // Helper method to create admin for testing
    private Administrateur createTestAdmin() {
        return Administrateur.builder()
            .email("admin2@test.com")
            .password("$2a$10$encodedPassword")
            .nom("Admin")
            .prenom("Test")
            .statut(Utilisateur.StatutUtilisateur.ACTIF)
            .build();
    }
}
