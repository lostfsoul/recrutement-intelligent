package ma.recrutement.controller.offre;

import com.fasterxml.jackson.databind.ObjectMapper;
import ma.recrutement.config.BaseControllerTest;
import ma.recrutement.config.TestDataFactory;
import ma.recrutement.dto.OffreCreateDTO;
import ma.recrutement.dto.OffreEmploiDTO;
import ma.recrutement.dto.OffreUpdateDTO;
import ma.recrutement.dto.PaginationResponseDTO;
import ma.recrutement.entity.OffreEmploi;
import ma.recrutement.repository.OffreEmploiRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.util.HashSet;
import java.util.Set;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test class for {@link ma.recrutement.controller.OffreController}.
 * Tests all job offer endpoints: CRUD operations, search, publish, close.
 *
 * Endpoints tested:
 * - POST /api/v1/offres
 * - PUT /api/v1/offres/{offreId}
 * - DELETE /api/v1/offres/{offreId}
 * - GET /api/v1/offres/{offreId}
 * - GET /api/v1/offres (search)
 * - GET /api/v1/offres/recentes
 * - POST /api/v1/offres/{offreId}/publier
 * - POST /api/v1/offres/{offreId}/cloturer
 *
 * @author Recrutement Team
 * @version 1.0.0
 */
@DisplayName("Job Offer Controller Tests")
class OffreControllerTest extends BaseControllerTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private OffreEmploiRepository offreEmploiRepository;

    @Nested
    @DisplayName("POST /api/v1/offres - Create Job Offer")
    class CreateOffreTests {

        @Test
        @DisplayName("Should create job offer successfully as recruiter")
        void shouldCreateJobOfferSuccessfully() throws Exception {
            // Given
            OffreCreateDTO createDTO = TestDataFactory.createJobOfferCreateDTO();

            // When/Then
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/v1/offres")
                    .header("Authorization", "Bearer " + recruteurToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.titre").value(createDTO.getTitre()))
                .andExpect(jsonPath("$.description").value(createDTO.getDescription()))
                .andExpect(jsonPath("$.typeContrat").value(createDTO.getTypeContrat().name()))
                .andExpect(jsonPath("$.salaireMin").value(createDTO.getSalaireMin()))
                .andExpect(jsonPath("$.salaireMax").value(createDTO.getSalaireMax()))
                .andExpect(jsonPath("$.statut").value(OffreEmploi.StatutOffre.BROUILLON.name()));
        }

        @Test
        @DisplayName("Should return 401 without authentication")
        void shouldReturn401WithoutAuthentication() throws Exception {
            // Given
            OffreCreateDTO createDTO = TestDataFactory.createJobOfferCreateDTO();

            // When/Then
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/v1/offres")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Should validate required fields")
        void shouldValidateRequiredFields() throws Exception {
            // Given - empty DTO
            OffreCreateDTO createDTO = OffreCreateDTO.builder().build();

            // When/Then
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/v1/offres")
                    .header("Authorization", "Bearer " + recruteurToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should validate titre length")
        void shouldValidateTitreLength() throws Exception {
            // Given - titre too short
            OffreCreateDTO createDTO = OffreCreateDTO.builder()
                .titre("AB")
                .description("Description")
                .typeContrat(OffreEmploi.TypeContrat.CDI)
                .typePoste(OffreEmploi.TypePoste.HYBRIDE)
                .salaireMin(40000L)
                .salaireMax(60000L)
                .localisation("Paris")
                .build();

            // When/Then
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/v1/offres")
                    .header("Authorization", "Bearer " + recruteurToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should validate salary range")
        void shouldValidateSalaryRange() throws Exception {
            // Given - min > max
            Set<String> competences = new HashSet<>();
            competences.add("Java");

            OffreCreateDTO createDTO = OffreCreateDTO.builder()
                .titre("Senior Developer")
                .description("Description")
                .typeContrat(OffreEmploi.TypeContrat.CDI)
                .typePoste(OffreEmploi.TypePoste.HYBRIDE)
                .salaireMin(100000L)
                .salaireMax(50000L)
                .competencesRequises(competences)
                .localisation("Paris")
                .build();

            // When/Then
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/v1/offres")
                    .header("Authorization", "Bearer " + recruteurToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("PUT /api/v1/offres/{offreId} - Update Job Offer")
    class UpdateOffreTests {

        @Test
        @DisplayName("Should update own job offer successfully")
        void shouldUpdateOwnJobOfferSuccessfully() throws Exception {
            // Given
            OffreUpdateDTO updateDTO = TestDataFactory.createJobOfferUpdateDTO();

            // When/Then
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put("/api/v1/offres/" + testOffre.getId())
                    .header("Authorization", "Bearer " + recruteurToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titre").value(updateDTO.getTitre()))
                .andExpect(jsonPath("$.description").value(updateDTO.getDescription()))
                .andExpect(jsonPath("$.typeContrat").value(updateDTO.getTypeContrat().name()))
                .andExpect(jsonPath("$.salaireMin").value(updateDTO.getSalaireMin()))
                .andExpect(jsonPath("$.salaireMax").value(updateDTO.getSalaireMax()));
        }

        @Test
        @DisplayName("Should return 404 for non-existent offer")
        void shouldReturn404ForNonExistentOffer() throws Exception {
            // Given
            OffreUpdateDTO updateDTO = TestDataFactory.createJobOfferUpdateDTO();

            // When/Then
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put("/api/v1/offres/999999")
                    .header("Authorization", "Bearer " + recruteurToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should return 403 when updating another recruiter's offer")
        void shouldReturn403WhenUpdatingAnotherRecruitersOffer() throws Exception {
            // Given - create another recruiter and offer
            var otherRecruiter = createTestRecruteur("other@test.com", "Other", "Recruiter");
            otherRecruiter = recruteurRepository.save(otherRecruiter);

            var otherEntreprise = createTestEntreprise("Other Company", "Description");
            otherEntreprise.setRecruteur(otherRecruiter);
            otherEntreprise = entrepriseRepository.save(otherEntreprise);

            var otherOffre = createTestOffre("Other Offer", "Description");
            otherOffre.setRecruteur(otherRecruiter);
            otherOffre.setEntreprise(otherEntreprise);
            otherOffre = offreEmploiRepository.save(otherOffre);

            OffreUpdateDTO updateDTO = TestDataFactory.createJobOfferUpdateDTO();

            // When/Then - try to update with different recruiter
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put("/api/v1/offres/" + otherOffre.getId())
                    .header("Authorization", "Bearer " + recruteurToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/offres/{offreId} - Delete Job Offer")
    class DeleteOffreTests {

        @Test
        @DisplayName("Should delete own job offer successfully")
        void shouldDeleteOwnJobOfferSuccessfully() throws Exception {
            // Given
            Long offreId = testOffre.getId();

            // When/Then
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete("/api/v1/offres/" + offreId)
                    .header("Authorization", "Bearer " + recruteurToken))
                .andExpect(status().isNoContent());

            // Verify deletion
            assertFalse(offreEmploiRepository.existsById(offreId));
        }

        @Test
        @DisplayName("Should return 404 for non-existent offer")
        void shouldReturn404ForNonExistentOffer() throws Exception {
            // When/Then
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete("/api/v1/offres/999999")
                    .header("Authorization", "Bearer " + recruteurToken))
                .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should return 403 when deleting another recruiter's offer")
        void shouldReturn403WhenDeletingAnotherRecruitersOffer() throws Exception {
            // Given - create another recruiter's offer
            var otherRecruiter = createTestRecruteur("other@test.com", "Other", "Recruiter");
            otherRecruiter = recruteurRepository.save(otherRecruiter);

            var otherOffre = createTestOffre("Other Offer", "Description");
            otherOffre.setRecruteur(otherRecruiter);
            otherOffre = offreEmploiRepository.save(otherOffre);

            // When/Then
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete("/api/v1/offres/" + otherOffre.getId())
                    .header("Authorization", "Bearer " + recruteurToken))
                .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/offres/{offreId} - Get Job Offer Details")
    class GetOffreTests {

        @Test
        @DisplayName("Should get job offer details (public)")
        void shouldGetJobOfferDetails() throws Exception {
            // When/Then - public endpoint, no auth required
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/v1/offres/" + testOffre.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testOffre.getId()))
                .andExpect(jsonPath("$.titre").value(testOffre.getTitre()))
                .andExpect(jsonPath("$.description").value(testOffre.getDescription()))
                .andExpect(jsonPath("$.typeContrat").value(testOffre.getTypeContrat().name()))
                .andExpect(jsonPath("$.statut").value(testOffre.getStatut().name()));
        }

        @Test
        @DisplayName("Should return 404 for non-existent offer")
        void shouldReturn404ForNonExistentOffer() throws Exception {
            // When/Then
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/v1/offres/999999"))
                .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/offres - Search Job Offers")
    class SearchOffresTests {

        @Test
        @DisplayName("Should search job offers without filters")
        void shouldSearchJobOffersWithoutFilters() throws Exception {
            // When/Then
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/v1/offres"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.pageable.pageNumber").value(0))
                .andExpect(jsonPath("$.pageable.pageSize").value(10));
        }

        @Test
        @DisplayName("Should search job offers with pagination")
        void shouldSearchJobOffersWithPagination() throws Exception {
            // When/Then
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/v1/offres")
                    .param("page", "0")
                    .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pageable.pageSize").value(5));
        }

        @Test
        @DisplayName("Should search job offers by titre")
        void shouldSearchJobOffersByTitre() throws Exception {
            // When/Then
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/v1/offres")
                    .param("titre", "Test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].titre").value(containsString("Test")));
        }

        @Test
        @DisplayName("Should search job offers by localisation")
        void shouldSearchJobOffersByLocalisation() throws Exception {
            // When/Then
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/v1/offres")
                    .param("localisation", "Paris"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
        }

        @Test
        @DisplayName("Should return empty list when no matches")
        void shouldReturnEmptyListWhenNoMatches() throws Exception {
            // When/Then
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/v1/offres")
                    .param("titre", "NonExistentJobTitleXYZ123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(0));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/offres/recentes - Get Recent Offers")
    class GetRecentOffresTests {

        @Test
        @DisplayName("Should get recent offers with default limit")
        void shouldGetRecentOffersWithDefaultLimit() throws Exception {
            // When/Then
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/v1/offres/recentes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()", lessThanOrEqualTo(5)));
        }

        @Test
        @DisplayName("Should get recent offers with custom limit")
        void shouldGetRecentOffersWithCustomLimit() throws Exception {
            // When/Then
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/v1/offres/recentes")
                    .param("limit", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()", lessThanOrEqualTo(3)));
        }

        @Test
        @DisplayName("Should respect limit parameter")
        void shouldRespectLimitParameter() throws Exception {
            // Given - create multiple offers
            for (int i = 0; i < 10; i++) {
                var offre = createTestOffre("Offer " + i, "Description " + i);
                offre.setRecruteur(testRecruteur);
                offre.setEntreprise(testEntreprise);
                offreEmploiRepository.save(offre);
            }

            // When/Then
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/v1/offres/recentes")
                    .param("limit", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()", lessThanOrEqualTo(5)));
        }
    }

    @Nested
    @DisplayName("POST /api/v1/offres/{offreId}/publier - Publish Offer")
    class PublishOffreTests {

        @Test
        @DisplayName("Should publish own job offer successfully")
        void shouldPublishOwnJobOfferSuccessfully() throws Exception {
            // Given - offer is in BROUILLON status
            assertEquals(OffreEmploi.StatutOffre.BROUILLON, testOffre.getStatut());

            // When/Then
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/v1/offres/" + testOffre.getId() + "/publier")
                    .header("Authorization", "Bearer " + recruteurToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statut").value(OffreEmploi.StatutOffre.PUBLIEE.name()));

            // Verify status changed
            var updated = offreEmploiRepository.findById(testOffre.getId()).orElseThrow();
            assertEquals(OffreEmploi.StatutOffre.PUBLIEE, updated.getStatut());
        }

        @Test
        @DisplayName("Should return 404 for non-existent offer")
        void shouldReturn404ForNonExistentOffer() throws Exception {
            // When/Then
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/v1/offres/999999/publier")
                    .header("Authorization", "Bearer " + recruteurToken))
                .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should return 403 when publishing another recruiter's offer")
        void shouldReturn403WhenPublishingAnotherRecruitersOffer() throws Exception {
            // Given - another recruiter's offer
            var otherRecruiter = createTestRecruteur("other@test.com", "Other", "Recruiter");
            otherRecruiter = recruteurRepository.save(otherRecruiter);

            var otherOffre = createTestOffre("Other Offer", "Description");
            otherOffre.setRecruteur(otherRecruiter);
            otherOffre = offreEmploiRepository.save(otherOffre);

            // When/Then
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/v1/offres/" + otherOffre.getId() + "/publier")
                    .header("Authorization", "Bearer " + recruteurToken))
                .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("POST /api/v1/offres/{offreId}/cloturer - Close Offer")
    class CloseOffreTests {

        @Test
        @DisplayName("Should close own job offer successfully")
        void shouldCloseOwnJobOfferSuccessfully() throws Exception {
            // Given - publish offer first
            testOffre.setStatut(OffreEmploi.StatutOffre.PUBLIEE);
            offreEmploiRepository.save(testOffre);

            // When/Then
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/v1/offres/" + testOffre.getId() + "/cloturer")
                    .header("Authorization", "Bearer " + recruteurToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statut").value(OffreEmploi.StatutOffre.CLOTUREE.name()));

            // Verify status changed
            var updated = offreEmploiRepository.findById(testOffre.getId()).orElseThrow();
            assertEquals(OffreEmploi.StatutOffre.CLOTUREE, updated.getStatut());
        }

        @Test
        @DisplayName("Should return 404 for non-existent offer")
        void shouldReturn404ForNonExistentOffer() throws Exception {
            // When/Then
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/v1/offres/999999/cloturer")
                    .header("Authorization", "Bearer " + recruteurToken))
                .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should return 403 when closing another recruiter's offer")
        void shouldReturn403WhenClosingAnotherRecruitersOffer() throws Exception {
            // Given - another recruiter's offer
            var otherRecruiter = createTestRecruteur("other@test.com", "Other", "Recruiter");
            otherRecruiter = recruteurRepository.save(otherRecruiter);

            var otherOffre = createTestOffre("Other Offer", "Description");
            otherOffre.setRecruteur(otherRecruiter);
            otherOffre = offreEmploiRepository.save(otherOffre);

            // When/Then
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/v1/offres/" + otherOffre.getId() + "/cloturer")
                    .header("Authorization", "Bearer " + recruteurToken))
                .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("Integration Tests - Complete Offer Flow")
    class IntegrationTests {

        @Test
        @DisplayName("Should complete full offer lifecycle")
        void shouldCompleteFullOfferLifecycle() throws Exception {
            // Step 1: Create offer
            OffreCreateDTO createDTO = TestDataFactory.createJobOfferCreateDTO();
            createDTO.setTitre("Integration Test Offer");

            MvcResult result = mockMvc.perform(
                    org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/v1/offres")
                        .header("Authorization", "Bearer " + recruteurToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isCreated())
                .andReturn();

            String response = result.getResponse().getContentAsString();
            OffreEmploiDTO created = objectMapper.readValue(response, OffreEmploiDTO.class);

            // Step 2: Get offer details
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/v1/offres/" + created.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titre").value("Integration Test Offer"));

            // Step 3: Update offer
            OffreUpdateDTO updateDTO = OffreUpdateDTO.builder()
                .titre("Updated Integration Test Offer")
                .description("Updated description")
                .build();

            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put("/api/v1/offres/" + created.getId())
                    .header("Authorization", "Bearer " + recruteurToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titre").value("Updated Integration Test Offer"));

            // Step 4: Publish offer
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/v1/offres/" + created.getId() + "/publier")
                    .header("Authorization", "Bearer " + recruteurToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statut").value(OffreEmploi.StatutOffre.PUBLIEE.name()));

            // Step 5: Close offer
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/v1/offres/" + created.getId() + "/cloturer")
                    .header("Authorization", "Bearer " + recruteurToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statut").value(OffreEmploi.StatutOffre.CLOTUREE.name()));
        }
    }
}
