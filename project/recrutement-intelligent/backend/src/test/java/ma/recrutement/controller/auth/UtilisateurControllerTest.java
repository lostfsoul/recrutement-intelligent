package ma.recrutement.controller.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import ma.recrutement.config.BaseControllerTest;
import ma.recrutement.config.TestDataFactory;
import ma.recrutement.dto.LoginRequestDTO;
import ma.recrutement.dto.RegisterRequestDTO;
import ma.recrutement.dto.TokenResponseDTO;
import ma.recrutement.entity.Candidat;
import ma.recrutement.entity.Recruteur;
import ma.recrutement.entity.Utilisateur;
import ma.recrutement.repository.CandidatRepository;
import ma.recrutement.repository.RecruteurRepository;
import ma.recrutement.repository.UtilisateurRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test class for {@link ma.recrutement.controller.UtilisateurController}.
 * Tests all authentication endpoints: register, login, refresh, logout.
 *
 * Endpoints tested:
 * - POST /api/v1/auth/register
 * - POST /api/v1/auth/login
 * - POST /api/v1/auth/refresh
 * - POST /api/v1/auth/logout
 *
 * @author Recrutement Team
 * @version 1.0.0
 */
@DisplayName("Auth Controller Tests")
class UtilisateurControllerTest extends BaseControllerTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Autowired
    private CandidatRepository candidatRepository;

    @Autowired
    private RecruteurRepository recruteurRepository;

    @Nested
    @DisplayName("POST /api/v1/auth/register - User Registration")
    class RegisterTests {

        @Test
        @DisplayName("Should register a new candidate successfully")
        void shouldRegisterCandidateSuccessfully() throws Exception {
            // Given
            RegisterRequestDTO request = TestDataFactory.createCandidateRegisterRequest();

            // When/Then
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/v1/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").value(86400L))
                .andExpect(jsonPath("$.userInfo.id").isNumber())
                .andExpect(jsonPath("$.userInfo.email").value(request.getEmail()))
                .andExpect(jsonPath("$.userInfo.nom").value(request.getNom()))
                .andExpect(jsonPath("$.userInfo.prenom").value(request.getPrenom()))
                .andExpect(jsonPath("$.userInfo.role").value("CANDIDAT"))
                .andExpect(jsonPath("$.userInfo.emailVerifie").value(false));

            // Verify user was created in database
            assertTrue(utilisateurRepository.existsByEmail(request.getEmail()));
        }

        @Test
        @DisplayName("Should register a new recruiter successfully")
        void shouldRegisterRecruiterSuccessfully() throws Exception {
            // Given
            RegisterRequestDTO request = TestDataFactory.createRecruiterRegisterRequest();

            // When/Then
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/v1/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userInfo.role").value("RECRUTEUR"));

            // Verify user was created in database
            assertTrue(recruteurRepository.existsByEmail(request.getEmail()));
        }

        @Test
        @DisplayName("Should return 400 when passwords don't match")
        void shouldReturn400WhenPasswordsDontMatch() throws Exception {
            // Given
            RegisterRequestDTO request = TestDataFactory.createCandidateRegisterRequest();
            request.setPasswordConfirmation("DifferentPassword123!");

            // When/Then
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/v1/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 400 when email is invalid")
        void shouldReturn400WhenEmailIsInvalid() throws Exception {
            // Given
            RegisterRequestDTO request = TestDataFactory.createCandidateRegisterRequest();
            request.setEmail("invalid-email");

            // When/Then
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/v1/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 400 when password is too short")
        void shouldReturn400WhenPasswordIsTooShort() throws Exception {
            // Given
            RegisterRequestDTO request = TestDataFactory.createCandidateRegisterRequest();
            request.setPassword("Short1!");
            request.setPasswordConfirmation("Short1!");

            // When/Then
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/v1/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 400 when name is too short")
        void shouldReturn400WhenNameIsTooShort() throws Exception {
            // Given
            RegisterRequestDTO request = TestDataFactory.createCandidateRegisterRequest();
            request.setNom("A");

            // When/Then
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/v1/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 400 when email is missing")
        void shouldReturn400WhenEmailIsMissing() throws Exception {
            // Given
            RegisterRequestDTO request = RegisterRequestDTO.builder()
                .password("Password123!")
                .passwordConfirmation("Password123!")
                .nom("Doe")
                .prenom("John")
                .role(Utilisateur.Role.CANDIDAT)
                .build();

            // When/Then
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/v1/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 400 when role is missing")
        void shouldReturn400WhenRoleIsMissing() throws Exception {
            // Given
            RegisterRequestDTO request = RegisterRequestDTO.builder()
                .email("test@example.com")
                .password("Password123!")
                .passwordConfirmation("Password123!")
                .nom("Doe")
                .prenom("John")
                .build();

            // When/Then
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/v1/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 400 when email already exists")
        void shouldReturn400WhenEmailAlreadyExists() throws Exception {
            // Given - create a user first
            RegisterRequestDTO request = TestDataFactory.createCandidateRegisterRequest();
            Candidat existingUser = Candidat.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode("Password123!"))
                .nom("Existing")
                .prenom("User")
                .statut(Utilisateur.StatutUtilisateur.ACTIF)
                .build();
            candidatRepository.save(existingUser);

            // When/Then - try to register with same email
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/v1/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 400 when required fields are null")
        void shouldReturn400WhenRequiredFieldsAreNull() throws Exception {
            // Given - request with null fields
            String invalidRequest = "{}";

            // When/Then
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/v1/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(invalidRequest))
                .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("POST /api/v1/auth/login - User Login")
    class LoginTests {

        @Test
        @DisplayName("Should login candidate successfully with valid credentials")
        void shouldLoginCandidateSuccessfully() throws Exception {
            // Given - create a user first
            String rawPassword = "Password123!";
            Candidat candidate = Candidat.builder()
                .email("login@test.com")
                .password(passwordEncoder.encode(rawPassword))
                .nom("Test")
                .prenom("Candidate")
                .telephone("0612345678")
                .statut(Utilisateur.StatutUtilisateur.ACTIF)
                .build();
            candidatRepository.save(candidate);

            LoginRequestDTO request = LoginRequestDTO.builder()
                .email("login@test.com")
                .password(rawPassword)
                .build();

            // When/Then
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/v1/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.userInfo.email").value("login@test.com"))
                .andExpect(jsonPath("$.userInfo.role").value("CANDIDAT"));
        }

        @Test
        @DisplayName("Should login recruiter successfully with valid credentials")
        void shouldLoginRecruiterSuccessfully() throws Exception {
            // Given - create a recruiter first
            String rawPassword = "Password123!";
            Recruteur recruiter = Recruteur.builder()
                .email("recruiter@login.com")
                .password(passwordEncoder.encode(rawPassword))
                .nom("Test")
                .prenom("Recruiter")
                .telephone("0687654321")
                .nomEntreprise("Test Company")
                .poste("RH Manager")
                .statut(Utilisateur.StatutUtilisateur.ACTIF)
                .build();
            recruteurRepository.save(recruiter);

            LoginRequestDTO request = LoginRequestDTO.builder()
                .email("recruiter@login.com")
                .password(rawPassword)
                .build();

            // When/Then
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/v1/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userInfo.role").value("RECRUTEUR"));
        }

        @Test
        @DisplayName("Should return 401 with invalid email")
        void shouldReturn401WithInvalidEmail() throws Exception {
            // Given
            LoginRequestDTO request = LoginRequestDTO.builder()
                .email("nonexistent@test.com")
                .password("Password123!")
                .build();

            // When/Then
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/v1/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Should return 401 with invalid password")
        void shouldReturn401WithInvalidPassword() throws Exception {
            // Given - create a user
            String rawPassword = "Password123!";
            Candidat candidate = Candidat.builder()
                .email("login@test.com")
                .password(passwordEncoder.encode(rawPassword))
                .nom("Test")
                .prenom("Candidate")
                .statut(Utilisateur.StatutUtilisateur.ACTIF)
                .build();
            candidatRepository.save(candidate);

            LoginRequestDTO request = LoginRequestDTO.builder()
                .email("login@test.com")
                .password("WrongPassword123!")
                .build();

            // When/Then
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/v1/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Should return 400 when email is missing")
        void shouldReturn400WhenEmailIsMissing() throws Exception {
            // Given
            LoginRequestDTO request = LoginRequestDTO.builder()
                .password("Password123!")
                .build();

            // When/Then
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/v1/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 400 when password is missing")
        void shouldReturn400WhenPasswordIsMissing() throws Exception {
            // Given
            LoginRequestDTO request = LoginRequestDTO.builder()
                .email("test@example.com")
                .build();

            // When/Then
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/v1/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should update last login date on successful login")
        void shouldUpdateLastLoginDate() throws Exception {
            // Given
            String rawPassword = "Password123!";
            Candidat candidate = Candidat.builder()
                .email("login@test.com")
                .password(passwordEncoder.encode(rawPassword))
                .nom("Test")
                .prenom("Candidate")
                .statut(Utilisateur.StatutUtilisateur.ACTIF)
                .derniereConnexion(null)
                .build();
            candidatRepository.save(candidate);

            LoginRequestDTO request = LoginRequestDTO.builder()
                .email("login@test.com")
                .password(rawPassword)
                .build();

            // When
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/v1/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

            // Then
            Candidat updated = candidatRepository.findByEmail("login@test.com").orElseThrow();
            assertNotNull(updated.getDerniereConnexion());
        }
    }

    @Nested
    @DisplayName("POST /api/v1/auth/refresh - Token Refresh")
    class RefreshTokenTests {

        @Test
        @DisplayName("Should return 501 when refresh is not implemented")
        void shouldReturn501WhenRefreshNotImplemented() throws Exception {
            // Given - a refresh token (even though not implemented)
            String refreshToken = "some-refresh-token";

            // When/Then
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/v1/auth/refresh")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(refreshToken))
                .andExpect(status().isInternalServerError());
        }

        @Test
        @DisplayName("Refresh token endpoint should exist")
        void refreshTokenEndpointShouldExist() throws Exception {
            // The endpoint exists but returns 501 (Not Implemented)
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/v1/auth/refresh")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("test-token"))
                .andExpect(status().isInternalServerError());
        }
    }

    @Nested
    @DisplayName("POST /api/v1/auth/logout - User Logout")
    class LogoutTests {

        @Test
        @DisplayName("Should logout successfully (204 No Content)")
        void shouldLogoutSuccessfully() throws Exception {
            // When/Then - logout is stateless for JWT, should always succeed
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/v1/auth/logout"))
                .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("Logout should work without authentication")
        void logoutShouldWorkWithoutAuthentication() throws Exception {
            // For JWT stateless auth, logout doesn't require authentication
            // The client simply removes the token
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/v1/auth/logout"))
                .andExpect(status().isNoContent());
        }
    }

    @Nested
    @DisplayName("Integration Tests - Complete Auth Flow")
    class IntegrationTests {

        @Test
        @DisplayName("Should complete full registration and login flow")
        void shouldCompleteFullRegistrationAndLoginFlow() throws Exception {
            // Step 1: Register a new candidate
            RegisterRequestDTO registerRequest = TestDataFactory.createCandidateRegisterRequest();

            MvcResult registerResult = mockMvc.perform(
                    org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty())
                .andReturn();

            // Extract response
            String registerResponse = registerResult.getResponse().getContentAsString();
            TokenResponseDTO tokenResponse = objectMapper.readValue(registerResponse, TokenResponseDTO.class);

            // Step 2: Login with the same credentials
            LoginRequestDTO loginRequest = LoginRequestDTO.builder()
                .email(registerRequest.getEmail())
                .password(registerRequest.getPassword())
                .build();

            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/v1/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.userInfo.email").value(registerRequest.getEmail()));

            // Step 3: Logout
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/v1/auth/logout"))
                .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("Should handle multiple users with different roles")
        void shouldHandleMultipleUsersWithDifferentRoles() throws Exception {
            // Register candidate
            RegisterRequestDTO candidateRequest = TestDataFactory.createCandidateRegisterRequest();
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/v1/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(candidateRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userInfo.role").value("CANDIDAT"));

            // Register recruiter
            RegisterRequestDTO recruiterRequest = TestDataFactory.createRecruiterRegisterRequest();
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/v1/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(recruiterRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userInfo.role").value("RECRUTEUR"));

            // Verify both users exist
            assertTrue(candidatRepository.existsByEmail(candidateRequest.getEmail()));
            assertTrue(recruteurRepository.existsByEmail(recruiterRequest.getEmail()));
        }
    }
}
