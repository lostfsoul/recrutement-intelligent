package ma.recrutement.controller.recruteur;

import com.fasterxml.jackson.databind.ObjectMapper;
import ma.recrutement.config.BaseControllerTest;
import ma.recrutement.config.TestDataFactory;
import ma.recrutement.dto.EntrepriseCreateDTO;
import ma.recrutement.dto.EntrepriseDTO;
import ma.recrutement.dto.RecruteurDTO;
import ma.recrutement.entity.*;
import ma.recrutement.repository.EntrepriseRepository;
import ma.recrutement.repository.RecruteurRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test class for {@link ma.recrutement.controller.RecruteurController}.
 * Tests all recruiter endpoints: profile management, company management.
 *
 * Endpoints tested:
 * - GET /api/v1/recruteurs/me
 * - PUT /api/v1/recruteurs/me
 * - POST /api/v1/recruteurs/me/entreprises
 * - GET /api/v1/recruteurs/me/entreprises
 * - GET /api/v1/recruteurs/me/entreprises/{entrepriseId}
 * - PUT /api/v1/recruteurs/me/entreprises/{entrepriseId}
 * - POST /api/v1/recruteurs/me/entreprises/{entrepriseId}/logo
 * - GET /api/v1/recruteurs/me/offres
 *
 * @author Recrutement Team
 * @version 1.0.0
 */
@DisplayName("Recruiter Controller Tests")
class RecruteurControllerTest extends BaseControllerTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RecruteurRepository recruteurRepository;

    @Autowired
    private EntrepriseRepository entrepriseRepository;

    @Nested
    @DisplayName("GET /api/v1/recruteurs/me - Get Recruiter Profile")
    class GetProfileTests {

        @Test
        @DisplayName("Should get recruiter profile successfully")
        void shouldGetRecruiterProfileSuccessfully() throws Exception {
            // When/Then
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/v1/recruteurs/me")
                    .header("Authorization", "Bearer " + recruteurToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testRecruteur.getId()))
                .andExpect(jsonPath("$.email").value(testRecruteur.getEmail()))
                .andExpect(jsonPath("$.nom").value(testRecruteur.getNom()))
                .andExpect(jsonPath("$.prenom").value(testRecruteur.getPrenom()));
        }

        @Test
        @DisplayName("Should return 401 without authentication")
        void shouldReturn401WithoutAuthentication() throws Exception {
            // When/Then
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/v1/recruteurs/me"))
                .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Should return 401 with invalid token")
        void shouldReturn401WithInvalidToken() throws Exception {
            // When/Then
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/v1/recruteurs/me")
                    .header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("PUT /api/v1/recruteurs/me - Update Recruiter Profile")
    class UpdateProfileTests {

        @Test
        @DisplayName("Should update recruiter profile successfully")
        void shouldUpdateRecruiterProfileSuccessfully() throws Exception {
            // Given
            RecruteurDTO updateDTO = RecruteurDTO.builder()
                .nom("Updated")
                .prenom("Name")
                .telephone("0699999999")
                .poste("Senior RH Manager")
                .build();

            // When/Then
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put("/api/v1/recruteurs/me")
                    .header("Authorization", "Bearer " + recruteurToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nom").value("Updated"))
                .andExpect(jsonPath("$.prenom").value("Name"))
                .andExpect(jsonPath("$.poste").value("Senior RH Manager"));
        }

        @Test
        @DisplayName("Should return 401 without authentication")
        void shouldReturn401WithoutAuthentication() throws Exception {
            // Given
            RecruteurDTO updateDTO = RecruteurDTO.builder()
                .nom("Updated")
                .build();

            // When/Then
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put("/api/v1/recruteurs/me")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Should validate input data")
        void shouldValidateInputData() throws Exception {
            // Given - invalid data
            RecruteurDTO updateDTO = RecruteurDTO.builder()
                .telephone("invalid-phone")
                .build();

            // When/Then
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put("/api/v1/recruteurs/me")
                    .header("Authorization", "Bearer " + recruteurToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("POST /api/v1/recruteurs/me/entreprises - Create Company")
    class CreateCompanyTests {

        @Test
        @DisplayName("Should create company successfully")
        void shouldCreateCompanySuccessfully() throws Exception {
            // Given
            EntrepriseCreateDTO createDTO = TestDataFactory.createCompanyCreateDTO();

            // When/Then
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/v1/recruteurs/me/entreprises")
                    .header("Authorization", "Bearer " + recruteurToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.nom").value(createDTO.getNom()))
                .andExpect(jsonPath("$.description").value(createDTO.getDescription()))
                .andExpect(jsonPath("$.secteur").value(createDTO.getSecteur()))
                .andExpect(jsonPath("$.statut").value(Entreprise.StatutEntreprise.EN_ATTENTE.name()));
        }

        @Test
        @DisplayName("Should return 401 without authentication")
        void shouldReturn401WithoutAuthentication() throws Exception {
            // Given
            EntrepriseCreateDTO createDTO = TestDataFactory.createCompanyCreateDTO();

            // When/Then
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/v1/recruteurs/me/entreprises")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Should validate required fields")
        void shouldValidateRequiredFields() throws Exception {
            // Given - missing required fields
            EntrepriseCreateDTO createDTO = EntrepriseCreateDTO.builder()
                .build();

            // When/Then
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/v1/recruteurs/me/entreprises")
                    .header("Authorization", "Bearer " + recruteurToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should validate company name length")
        void shouldValidateCompanyNameLength() throws Exception {
            // Given - name too short
            EntrepriseCreateDTO createDTO = EntrepriseCreateDTO.builder()
                .nom("A")
                .description("Description")
                .secteur("Technology")
                .taille(Entreprise.TailleEntreprise.PME)
                .build();

            // When/Then
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/v1/recruteurs/me/entreprises")
                    .header("Authorization", "Bearer " + recruteurToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/recruteurs/me/entreprises - Get Recruiter Companies")
    class GetCompaniesTests {

        @Test
        @DisplayName("Should get companies list successfully")
        void shouldGetCompaniesListSuccessfully() throws Exception {
            // Given - test company already exists
            testEntreprise.setRecruteur(testRecruteur);
            entrepriseRepository.save(testEntreprise);

            // When/Then
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/v1/recruteurs/me/entreprises")
                    .header("Authorization", "Bearer " + recruteurToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].id").value(testEntreprise.getId()))
                .andExpect(jsonPath("$[0].nom").value(testEntreprise.getNom()));
        }

        @Test
        @DisplayName("Should return empty list when no companies")
        void shouldReturnEmptyListWhenNoCompanies() throws Exception {
            // Given - remove test company
            entrepriseRepository.deleteAll();

            // When/Then
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/v1/recruteurs/me/entreprises")
                    .header("Authorization", "Bearer " + recruteurToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
        }

        @Test
        @DisplayName("Should return 401 without authentication")
        void shouldReturn401WithoutAuthentication() throws Exception {
            // When/Then
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/v1/recruteurs/me/entreprises"))
                .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/recruteurs/me/entreprises/{entrepriseId} - Get Company Details")
    class GetCompanyDetailsTests {

        @Test
        @DisplayName("Should get company details successfully")
        void shouldGetCompanyDetailsSuccessfully() throws Exception {
            // Given - test company exists

            // When/Then
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/v1/recruteurs/me/entreprises/" + testEntreprise.getId())
                    .header("Authorization", "Bearer " + recruteurToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testEntreprise.getId()))
                .andExpect(jsonPath("$.nom").value(testEntreprise.getNom()))
                .andExpect(jsonPath("$.description").value(testEntreprise.getDescription()))
                .andExpect(jsonPath("$.secteur").value(testEntreprise.getSecteur()));
        }

        @Test
        @DisplayName("Should return 404 for non-existent company")
        void shouldReturn404ForNonExistentCompany() throws Exception {
            // When/Then
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/v1/recruteurs/me/entreprises/999999")
                    .header("Authorization", "Bearer " + recruteurToken))
                .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should return 403 for company owned by another recruiter")
        void shouldReturn403ForCompanyOwnedByAnotherRecruiter() throws Exception {
            // Given - create another recruiter and company
            Recruteur otherRecruiter = createTestRecruteur("other@test.com", "Other", "Recruiter");
            otherRecruiter = recruteurRepository.save(otherRecruiter);

            Entreprise otherEntreprise = createTestEntreprise("Other Company", "Other Description");
            otherEntreprise.setRecruteur(otherRecruiter);
            otherEntreprise = entrepriseRepository.save(otherEntreprise);

            // When/Then - try to access other's company
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/v1/recruteurs/me/entreprises/" + otherEntreprise.getId())
                    .header("Authorization", "Bearer " + recruteurToken))
                .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("PUT /api/v1/recruteurs/me/entreprises/{entrepriseId} - Update Company")
    class UpdateCompanyTests {

        @Test
        @DisplayName("Should update company successfully")
        void shouldUpdateCompanySuccessfully() throws Exception {
            // Given
            EntrepriseCreateDTO updateDTO = EntrepriseCreateDTO.builder()
                .nom("Updated Company Name")
                .description("Updated description")
                .secteur("Finance")
                .taille(Entreprise.TailleEntreprise.GRANDE)
                .siteWeb("https://updated.com")
                .localisation("London")
                .build();

            // When/Then
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put("/api/v1/recruteurs/me/entreprises/" + testEntreprise.getId())
                    .header("Authorization", "Bearer " + recruteurToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nom").value("Updated Company Name"))
                .andExpect(jsonPath("$.description").value("Updated description"))
                .andExpect(jsonPath("$.secteur").value("Finance"));
        }

        @Test
        @DisplayName("Should return 404 for non-existent company")
        void shouldReturn404ForNonExistentCompany() throws Exception {
            // Given
            EntrepriseCreateDTO updateDTO = TestDataFactory.createCompanyCreateDTO();

            // When/Then
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put("/api/v1/recruteurs/me/entreprises/999999")
                    .header("Authorization", "Bearer " + recruteurToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should validate required fields")
        void shouldValidateRequiredFields() throws Exception {
            // Given - empty update
            EntrepriseCreateDTO updateDTO = EntrepriseCreateDTO.builder().build();

            // When/Then
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put("/api/v1/recruteurs/me/entreprises/" + testEntreprise.getId())
                    .header("Authorization", "Bearer " + recruteurToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("POST /api/v1/recruteurs/me/entreprises/{entrepriseId}/logo - Upload Logo")
    class UploadLogoTests {

        @Test
        @DisplayName("Should upload logo successfully")
        void shouldUploadLogoSuccessfully() throws Exception {
            // Given
            MockMultipartFile file = new MockMultipartFile(
                "file",
                "logo.png",
                MediaType.IMAGE_PNG_VALUE,
                "fake-image-content".getBytes()
            );

            // When/Then
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart("/api/v1/recruteurs/me/entreprises/" + testEntreprise.getId() + "/logo")
                    .file(file)
                    .header("Authorization", "Bearer " + recruteurToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.logoPath").exists());
        }

        @Test
        @DisplayName("Should return 404 for non-existent company")
        void shouldReturn404ForNonExistentCompany() throws Exception {
            // Given
            MockMultipartFile file = new MockMultipartFile(
                "file",
                "logo.png",
                MediaType.IMAGE_PNG_VALUE,
                "fake-image-content".getBytes()
            );

            // When/Then
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart("/api/v1/recruteurs/me/entreprises/999999/logo")
                    .file(file)
                    .header("Authorization", "Bearer " + recruteurToken))
                .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should validate file type")
        void shouldValidateFileType() throws Exception {
            // Given - invalid file type (text file)
            MockMultipartFile file = new MockMultipartFile(
                "file",
                "document.txt",
                MediaType.TEXT_PLAIN_VALUE,
                "text content".getBytes()
            );

            // When/Then
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart("/api/v1/recruteurs/me/entreprises/" + testEntreprise.getId() + "/logo")
                    .file(file)
                    .header("Authorization", "Bearer " + recruteurToken))
                .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/recruteurs/me/offres - Get Recruiter Job Offers")
    class GetJobOffersTests {

        @Test
        @DisplayName("Should get job offers list successfully")
        void shouldGetJobOffersListSuccessfully() throws Exception {
            // Given - test offer exists

            // When/Then
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/v1/recruteurs/me/offres")
                    .header("Authorization", "Bearer " + recruteurToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].id").value(testOffre.getId()))
                .andExpect(jsonPath("$[0].titre").value(testOffre.getTitre()));
        }

        @Test
        @DisplayName("Should return empty list when no offers")
        void shouldReturnEmptyListWhenNoOffers() throws Exception {
            // Given - delete all offers
            offreEmploiRepository.deleteAll();

            // When/Then
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/v1/recruteurs/me/offres")
                    .header("Authorization", "Bearer " + recruteurToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
        }

        @Test
        @DisplayName("Should return 401 without authentication")
        void shouldReturn401WithoutAuthentication() throws Exception {
            // When/Then
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/v1/recruteurs/me/offres"))
                .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("Integration Tests - Complete Recruiter Flow")
    class IntegrationTests {

        @Test
        @DisplayName("Should complete full recruiter workflow")
        void shouldCompleteFullRecruiterWorkflow() throws Exception {
            // Step 1: Update recruiter profile
            RecruteurDTO profileDTO = RecruteurDTO.builder()
                .nom("Updated")
                .prenom("Recruiter")
                .telephone("0699999999")
                .poste("Senior RH Manager")
                .build();

            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put("/api/v1/recruteurs/me")
                    .header("Authorization", "Bearer " + recruteurToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(profileDTO)))
                .andExpect(status().isOk());

            // Step 2: Create a company
            EntrepriseCreateDTO companyDTO = TestDataFactory.createCompanyCreateDTO();
            companyDTO.setNom("New Company");

            MvcResult result = mockMvc.perform(
                    org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/v1/recruteurs/me/entreprises")
                        .header("Authorization", "Bearer " + recruteurToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(companyDTO)))
                .andExpect(status().isCreated())
                .andReturn();

            String response = result.getResponse().getContentAsString();
            EntrepriseDTO createdCompany = objectMapper.readValue(response, EntrepriseDTO.class);

            // Step 3: Get the company
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/v1/recruteurs/me/entreprises/" + createdCompany.getId())
                    .header("Authorization", "Bearer " + recruteurToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nom").value("New Company"));

            // Step 4: List all companies
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/v1/recruteurs/me/entreprises")
                    .header("Authorization", "Bearer " + recruteurToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))));
        }
    }
}
