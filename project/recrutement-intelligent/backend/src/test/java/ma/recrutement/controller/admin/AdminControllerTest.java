package ma.recrutement.controller.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import ma.recrutement.config.BaseControllerTest;
import ma.recrutement.dto.EntrepriseDTO;
import ma.recrutement.entity.Administrateur;
import ma.recrutement.entity.Candidat;
import ma.recrutement.entity.Entreprise;
import ma.recrutement.entity.Utilisateur;
import ma.recrutement.repository.AdministrateurRepository;
import ma.recrutement.repository.EntrepriseRepository;
import ma.recrutement.repository.UtilisateurRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test class for {@link ma.recrutement.controller.AdminController}.
 * Tests all admin endpoints: stats, company validation, user management.
 *
 * Endpoints tested:
 * - GET /api/v1/admin/stats
 * - GET /api/v1/admin/entreprises/en-attente
 * - PUT /api/v1/admin/entreprises/{entrepriseId}/valider
 * - PUT /api/v1/admin/entreprises/{entrepriseId}/refuser
 * - PUT /api/v1/admin/utilisateurs/{utilisateurId}/activer
 * - PUT /api/v1/admin/utilisateurs/{utilisateurId}/desactiver
 * - PUT /api/v1/admin/utilisateurs/{utilisateurId}/password
 * - DELETE /api/v1/admin/offres/{offreId}
 *
 * @author Recrutement Team
 * @version 1.0.0
 */
@DisplayName("Admin Controller Tests")
class AdminControllerTest extends BaseControllerTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EntrepriseRepository entrepriseRepository;

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Autowired
    private AdministrateurRepository administrateurRepository;

    private Administrateur testAdmin;

    @Override
    protected void createTestData() {
        super.createTestData();

        // Create admin user
        testAdmin = Administrateur.builder()
            .email("admin@test.com")
            .password("$2a$10$encodedPassword")
            .nom("Admin")
            .prenom("Super")
            .statut(Utilisateur.StatutUtilisateur.ACTIF)
            .build();
        testAdmin = administrateurRepository.save(testAdmin);

        // Generate admin token
        adminToken = generateTokenForUser(testAdmin.getEmail(), "ADMINISTRATEUR");
    }

    @Nested
    @DisplayName("GET /api/v1/admin/stats - Get Platform Statistics")
    class GetStatsTests {

        @Test
        @DisplayName("Should get statistics successfully as admin")
        void shouldGetStatisticsSuccessfullyAsAdmin() throws Exception {
            // When/Then
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/v1/admin/stats")
                    .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalCandidats").isNumber())
                .andExpect(jsonPath("$.totalRecruteurs").isNumber())
                .andExpect(jsonPath("$.totalEntreprises").isNumber())
                .andExpect(jsonPath("$.totalOffres").isNumber())
                .andExpect(jsonPath("$.totalCandidatures").isNumber());
        }

        @Test
        @DisplayName("Should return accurate statistics")
        void shouldReturnAccurateStatistics() throws Exception {
            // When/Then
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/v1/admin/stats")
                    .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalCandidats").value(greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.totalRecruteurs").value(greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.totalEntreprises").value(greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.totalOffres").value(greaterThanOrEqualTo(1)));
        }

        @Test
        @DisplayName("Should return 403 when non-admin tries to access")
        void shouldReturn403WhenNonAdminTriesToAccess() throws Exception {
            // When/Then - candidate tries to access
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/v1/admin/stats")
                    .header("Authorization", "Bearer " + candidatToken))
                .andExpect(status().isForbidden());

            // Recruiter tries to access
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/v1/admin/stats")
                    .header("Authorization", "Bearer " + recruteurToken))
                .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Should return 401 without authentication")
        void shouldReturn401WithoutAuthentication() throws Exception {
            // When/Then
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/v1/admin/stats"))
                .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/admin/entreprises/en-attente - Get Pending Companies")
    class GetPendingEntreprisesTests {

        @Test
        @DisplayName("Should get pending companies successfully")
        void shouldGetPendingCompaniesSuccessfully() throws Exception {
            // When/Then
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/v1/admin/entreprises/en-attente")
                    .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].statut").value(Entreprise.StatutEntreprise.EN_ATTENTE.name()));
        }

        @Test
        @DisplayName("Should return empty list when no pending companies")
        void shouldReturnEmptyListWhenNoPendingCompanies() throws Exception {
            // Given - approve or reject all companies
            testEntreprise.setStatut(Entreprise.StatutEntreprise.VALIDEE);
            entrepriseRepository.save(testEntreprise);

            // When/Then
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/v1/admin/entreprises/en-attente")
                    .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()", is(0)));
        }

        @Test
        @DisplayName("Should return 403 when non-admin tries to access")
        void shouldReturn403WhenNonAdminTriesToAccess() throws Exception {
            // When/Then
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/v1/admin/entreprises/en-attente")
                    .header("Authorization", "Bearer " + recruteurToken))
                .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("PUT /api/v1/admin/entreprises/{entrepriseId}/valider - Validate Company")
    class ValidateEntrepriseTests {

        @Test
        @DisplayName("Should validate company successfully")
        void shouldValidateCompanySuccessfully() throws Exception {
            // Given - company is in EN_ATTENTE status
            assertEquals(Entreprise.StatutEntreprise.EN_ATTENTE, testEntreprise.getStatut());

            // When/Then
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put("/api/v1/admin/entreprises/" + testEntreprise.getId() + "/valider")
                    .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statut").value(Entreprise.StatutEntreprise.VALIDEE.name()));

            // Verify update
            var updated = entrepriseRepository.findById(testEntreprise.getId()).orElseThrow();
            assertEquals(Entreprise.StatutEntreprise.VALIDEE, updated.getStatut());
        }

        @Test
        @DisplayName("Should return 404 for non-existent company")
        void shouldReturn404ForNonExistentCompany() throws Exception {
            // When/Then
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put("/api/v1/admin/entreprises/999999/valider")
                    .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should return 403 when non-admin tries to validate")
        void shouldReturn403WhenNonAdminTriesToValidate() throws Exception {
            // When/Then
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put("/api/v1/admin/entreprises/" + testEntreprise.getId() + "/valider")
                    .header("Authorization", "Bearer " + recruteurToken))
                .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("PUT /api/v1/admin/entreprises/{entrepriseId}/refuser - Reject Company")
    class RejectEntrepriseTests {

        @Test
        @DisplayName("Should reject company successfully")
        void shouldRejectCompanySuccessfully() throws Exception {
            // Given - create a new pending company
            var pendingEntreprise = createTestEntreprise("Pending Company", "Description");
            pendingEntreprise.setStatut(Entreprise.StatutEntreprise.EN_ATTENTE);
            pendingEntreprise.setRecruteur(testRecruteur);
            pendingEntreprise = entrepriseRepository.save(pendingEntreprise);

            // When/Then
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put("/api/v1/admin/entreprises/" + pendingEntreprise.getId() + "/refuser")
                    .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statut").value(Entreprise.StatutEntreprise.REFUSEE.name()));

            // Verify update
            var updated = entrepriseRepository.findById(pendingEntreprise.getId()).orElseThrow();
            assertEquals(Entreprise.StatutEntreprise.REFUSEE, updated.getStatut());
        }

        @Test
        @DisplayName("Should return 404 for non-existent company")
        void shouldReturn404ForNonExistentCompany() throws Exception {
            // When/Then
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put("/api/v1/admin/entreprises/999999/refuser")
                    .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should return 403 when non-admin tries to reject")
        void shouldReturn403WhenNonAdminTriesToReject() throws Exception {
            // When/Then
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put("/api/v1/admin/entreprises/" + testEntreprise.getId() + "/refuser")
                    .header("Authorization", "Bearer " + candidatToken))
                .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("PUT /api/v1/admin/utilisateurs/{utilisateurId}/activer - Activate User")
    class ActivateUserTests {

        @Test
        @DisplayName("Should activate user successfully")
        void shouldActivateUserSuccessfully() throws Exception {
            // Given - inactive user
            testCandidat.setStatut(Utilisateur.StatutUtilisateur.INACTIF);
            testCandidat = candidatRepository.save(testCandidat);

            // When/Then
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put("/api/v1/admin/utilisateurs/" + testCandidat.getId() + "/activer")
                    .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());

            // Verify update
            var updated = candidatRepository.findById(testCandidat.getId()).orElseThrow();
            assertEquals(Utilisateur.StatutUtilisateur.ACTIF, updated.getStatut());
        }

        @Test
        @DisplayName("Should return 404 for non-existent user")
        void shouldReturn404ForNonExistentUser() throws Exception {
            // When/Then
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put("/api/v1/admin/utilisateurs/999999/activer")
                    .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should return 403 when non-admin tries to activate")
        void shouldReturn403WhenNonAdminTriesToActivate() throws Exception {
            // When/Then
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put("/api/v1/admin/utilisateurs/" + testCandidat.getId() + "/activer")
                    .header("Authorization", "Bearer " + recruteurToken))
                .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("PUT /api/v1/admin/utilisateurs/{utilisateurId}/desactiver - Deactivate User")
    class DeactivateUserTests {

        @Test
        @DisplayName("Should deactivate user successfully")
        void shouldDeactivateUserSuccessfully() throws Exception {
            // Given - active user
            assertEquals(Utilisateur.StatutUtilisateur.ACTIF, testCandidat.getStatut());

            // When/Then
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put("/api/v1/admin/utilisateurs/" + testCandidat.getId() + "/desactiver")
                    .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());

            // Verify update
            var updated = candidatRepository.findById(testCandidat.getId()).orElseThrow();
            assertEquals(Utilisateur.StatutUtilisateur.INACTIF, updated.getStatut());
        }

        @Test
        @DisplayName("Should return 404 for non-existent user")
        void shouldReturn404ForNonExistentUser() throws Exception {
            // When/Then
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put("/api/v1/admin/utilisateurs/999999/desactiver")
                    .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should return 403 when non-admin tries to deactivate")
        void shouldReturn403WhenNonAdminTriesToDeactivate() throws Exception {
            // When/Then
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put("/api/v1/admin/utilisateurs/" + testRecruteur.getId() + "/desactiver")
                    .header("Authorization", "Bearer " + candidatToken))
                .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("PUT /api/v1/admin/utilisateurs/{utilisateurId}/password - Reset Password")
    class ResetPasswordTests {

        @Test
        @DisplayName("Should reset password successfully")
        void shouldResetPasswordSuccessfully() throws Exception {
            // Given
            String newPassword = "NewPassword123!";

            // When/Then
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put("/api/v1/admin/utilisateurs/" + testCandidat.getId() + "/password")
                    .header("Authorization", "Bearer " + adminToken)
                    .param("newPassword", newPassword))
                .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("Should return 404 for non-existent user")
        void shouldReturn404ForNonExistentUser() throws Exception {
            // When/Then
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put("/api/v1/admin/utilisateurs/999999/password")
                    .header("Authorization", "Bearer " + adminToken)
                    .param("newPassword", "NewPassword123!"))
                .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should validate password strength")
        void shouldValidatePasswordStrength() throws Exception {
            // When/Then - weak password
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put("/api/v1/admin/utilisateurs/" + testCandidat.getId() + "/password")
                    .header("Authorization", "Bearer " + adminToken)
                    .param("newPassword", "weak"))
                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 403 when non-admin tries to reset password")
        void shouldReturn403WhenNonAdminTriesToResetPassword() throws Exception {
            // When/Then
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put("/api/v1/admin/utilisateurs/" + testRecruteur.getId() + "/password")
                    .header("Authorization", "Bearer " + recruteurToken)
                    .param("newPassword", "NewPassword123!"))
                .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/admin/offres/{offreId} - Delete Job Offer")
    class DeleteOffreTests {

        @Test
        @DisplayName("Should delete any job offer successfully")
        void shouldDeleteAnyJobOfferSuccessfully() throws Exception {
            // Given
            Long offreId = testOffre.getId();

            // When/Then
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete("/api/v1/admin/offres/" + offreId)
                    .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());

            // Verify deletion
            assertFalse(offreEmploiRepository.existsById(offreId));
        }

        @Test
        @DisplayName("Should return 404 for non-existent offer")
        void shouldReturn404ForNonExistentOffer() throws Exception {
            // When/Then
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete("/api/v1/admin/offres/999999")
                    .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should return 403 when non-admin tries to delete")
        void shouldReturn403WhenNonAdminTriesToDelete() throws Exception {
            // When/Then
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete("/api/v1/admin/offres/" + testOffre.getId())
                    .header("Authorization", "Bearer " + recruteurToken))
                .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("Integration Tests - Complete Admin Flow")
    class IntegrationTests {

        @Test
        @DisplayName("Should complete full admin workflow")
        void shouldCompleteFullAdminWorkflow() throws Exception {
            // Step 1: Get statistics
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/v1/admin/stats")
                    .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());

            // Step 2: Get pending companies
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/v1/admin/entreprises/en-attente")
                    .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(0))));

            // Step 3: Validate company
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put("/api/v1/admin/entreprises/" + testEntreprise.getId() + "/valider")
                    .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statut").value(Entreprise.StatutEntreprise.VALIDEE.name()));

            // Step 4: Deactivate user
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put("/api/v1/admin/utilisateurs/" + testCandidat.getId() + "/desactiver")
                    .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());

            // Step 5: Reactivate user
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put("/api/v1/admin/utilisateurs/" + testCandidat.getId() + "/activer")
                    .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());

            // Step 6: Reset password
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put("/api/v1/admin/utilisateurs/" + testCandidat.getId() + "/password")
                    .header("Authorization", "Bearer " + adminToken)
                    .param("newPassword", "NewSecurePassword123!"))
                .andExpect(status().isNoContent());
        }
    }
}
