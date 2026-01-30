package ma.recrutement.controller.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import ma.recrutement.config.BaseControllerTest;
import ma.recrutement.dto.CvParseResponseDTO;
import ma.recrutement.dto.SkillExtractionDTO;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test class for {@link ma.recrutement.controller.AIController}.
 * Tests all AI endpoints: CV parsing, skill extraction, CV analysis.
 *
 * Endpoints tested:
 * - POST /api/v1/ai/parse-cv
 * - POST /api/v1/ai/extract-skills
 * - POST /api/v1/ai/analyze-cv
 *
 * Note: These tests mock the AI service responses since the actual AI
 * functionality requires external API keys and services.
 *
 * @author Recrutement Team
 * @version 1.0.0
 */
@DisplayName("AI Controller Tests")
class AIControllerTest extends BaseControllerTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Nested
    @DisplayName("POST /api/v1/ai/parse-cv - Parse CV")
    class ParseCvTests {

        @Test
        @DisplayName("Should parse PDF CV successfully")
        void shouldParsePDFCVSuccessfully() throws Exception {
            // Given
            MockMultipartFile file = new MockMultipartFile(
                "file",
                "cv.pdf",
                MediaType.APPLICATION_PDF_VALUE,
                "Sample PDF content for CV parsing".getBytes()
            );

            // When/Then
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart("/api/v1/ai/parse-cv")
                    .file(file)
                    .header("Authorization", "Bearer " + candidatToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.extractedText").isNotEmpty())
                .andExpect(jsonPath("$.fileName").value("cv.pdf"));
        }

        @Test
        @DisplayName("Should parse DOCX CV successfully")
        void shouldParseDOCXCVSuccessfully() throws Exception {
            // Given
            MockMultipartFile file = new MockMultipartFile(
                "file",
                "cv.docx",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                "Sample DOCX content".getBytes()
            );

            // When/Then
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart("/api/v1/ai/parse-cv")
                    .file(file)
                    .header("Authorization", "Bearer " + candidatToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
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
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart("/api/v1/ai/parse-cv")
                    .file(file))
                .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Should return 400 for unsupported file type")
        void shouldReturn400ForUnsupportedFileType() throws Exception {
            // Given - unsupported file type
            MockMultipartFile file = new MockMultipartFile(
                "file",
                "image.png",
                MediaType.IMAGE_PNG_VALUE,
                "fake image content".getBytes()
            );

            // When/Then
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart("/api/v1/ai/parse-cv")
                    .file(file)
                    .header("Authorization", "Bearer " + candidatToken))
                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 400 for empty file")
        void shouldReturn400ForEmptyFile() throws Exception {
            // Given - empty file
            MockMultipartFile file = new MockMultipartFile(
                "file",
                "cv.pdf",
                MediaType.APPLICATION_PDF_VALUE,
                new byte[0]
            );

            // When/Then
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart("/api/v1/ai/parse-cv")
                    .file(file)
                    .header("Authorization", "Bearer " + candidatToken))
                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should handle file size limit")
        void shouldHandleFileSizeLimit() throws Exception {
            // Given - large file (>10MB)
            byte[] largeContent = new byte[11 * 1024 * 1024]; // 11MB
            MockMultipartFile file = new MockMultipartFile(
                "file",
                "large.pdf",
                MediaType.APPLICATION_PDF_VALUE,
                largeContent
            );

            // When/Then
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart("/api/v1/ai/parse-cv")
                    .file(file)
                    .header("Authorization", "Bearer " + candidatToken))
                .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("POST /api/v1/ai/extract-skills - Extract Skills")
    class ExtractSkillsTests {

        @Test
        @DisplayName("Should extract skills from CV text successfully")
        void shouldExtractSkillsFromCVTextSuccessfully() throws Exception {
            // Given
            String cvText = """
                John Doe
                Software Engineer

                Skills:
                - Java (Advanced)
                - Spring Boot (Intermediate)
                - PostgreSQL (Advanced)
                - React (Beginner)
                - Docker (Intermediate)
                - Kubernetes (Beginner)

                Experience:
                Senior Java Developer at Tech Corp (2020-2023)
                Java Developer at Startup Inc (2018-2020)
                """;

            // When/Then
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/v1/ai/extract-skills")
                    .header("Authorization", "Bearer " + candidatToken)
                    .contentType(MediaType.TEXT_PLAIN)
                    .content(cvText))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.skills").isArray())
                .andExpect(jsonPath("$.skills", hasSize(greaterThan(0))));
        }

        @Test
        @DisplayName("Should return 401 without authentication")
        void shouldReturn401WithoutAuthentication() throws Exception {
            // Given
            String cvText = "Sample CV text";

            // When/Then
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/v1/ai/extract-skills")
                    .contentType(MediaType.TEXT_PLAIN)
                    .content(cvText))
                .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Should handle empty CV text")
        void shouldHandleEmptyCVText() throws Exception {
            // Given - empty text
            String cvText = "";

            // When/Then
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/v1/ai/extract-skills")
                    .header("Authorization", "Bearer " + candidatToken)
                    .contentType(MediaType.TEXT_PLAIN)
                    .content(cvText))
                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should handle very short CV text")
        void shouldHandleVeryShortCVText() throws Exception {
            // Given - very short text
            String cvText = "Java Developer";

            // When/Then - should still process but may return limited results
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/v1/ai/extract-skills")
                    .header("Authorization", "Bearer " + candidatToken)
                    .contentType(MediaType.TEXT_PLAIN)
                    .content(cvText))
                .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("POST /api/v1/ai/analyze-cv - Analyze CV Complete")
    class AnalyzeCvTests {

        @Test
        @DisplayName("Should analyze CV file successfully")
        void shouldAnalyzeCVFileSuccessfully() throws Exception {
            // Given
            MockMultipartFile file = new MockMultipartFile(
                "file",
                "cv.pdf",
                MediaType.APPLICATION_PDF_VALUE,
                "John Doe - Java Developer with 5 years experience in Spring Boot, PostgreSQL, and React".getBytes()
            );

            // When/Then
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart("/api/v1/ai/analyze-cv")
                    .file(file)
                    .header("Authorization", "Bearer " + candidatToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.skills").isArray());
        }

        @Test
        @DisplayName("Should return 400 when parsing fails")
        void shouldReturn400WhenParsingFails() throws Exception {
            // Given - corrupted/invalid file
            MockMultipartFile file = new MockMultipartFile(
                "file",
                "cv.txt",
                MediaType.TEXT_PLAIN_VALUE,
                "Not a valid PDF or DOCX".getBytes()
            );

            // When/Then
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart("/api/v1/ai/analyze-cv")
                    .file(file)
                    .header("Authorization", "Bearer " + candidatToken))
                .andExpect(status().isBadRequest());
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
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart("/api/v1/ai/analyze-cv")
                    .file(file))
                .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Should work for recruiter role as well")
        void shouldWorkForRecruiterRoleAsWell() throws Exception {
            // Given
            MockMultipartFile file = new MockMultipartFile(
                "file",
                "cv.pdf",
                MediaType.APPLICATION_PDF_VALUE,
                "Candidate CV content for recruiter review".getBytes()
            );

            // When/Then
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart("/api/v1/ai/analyze-cv")
                    .file(file)
                    .header("Authorization", "Bearer " + recruteurToken))
                .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("Integration Tests - Complete AI Flow")
    class IntegrationTests {

        @Test
        @DisplayName("Should complete full CV analysis workflow")
        void shouldCompleteFullCVAnalysisWorkflow() throws Exception {
            // Step 1: Parse CV
            MockMultipartFile file = new MockMultipartFile(
                "file",
                "cv.pdf",
                MediaType.APPLICATION_PDF_VALUE,
                "John Doe - Senior Java Developer - Skills: Java, Spring Boot, PostgreSQL, React, Docker".getBytes()
            );

            MvcResult parseResult = mockMvc.perform(
                    org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart("/api/v1/ai/parse-cv")
                        .file(file)
                        .header("Authorization", "Bearer " + candidatToken))
                .andExpect(status().isOk())
                .andReturn();

            String parseResponse = parseResult.getResponse().getContentAsString();
            CvParseResponseDTO parseDTO = objectMapper.readValue(parseResponse, CvParseResponseDTO.class);
            assertTrue(parseDTO.getSuccess());
            assertNotNull(parseDTO.getExtractedText());

            // Step 2: Extract skills from parsed text
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/v1/ai/extract-skills")
                    .header("Authorization", "Bearer " + candidatToken)
                    .contentType(MediaType.TEXT_PLAIN)
                    .content(parseDTO.getExtractedText()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.skills").isArray());

            // Step 3: Analyze CV in one step
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart("/api/v1/ai/analyze-cv")
                    .file(file)
                    .header("Authorization", "Bearer " + candidatToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.skills").isArray())
                .andExpect(jsonPath("$.skills", hasSize(greaterThan(0))));
        }

        @Test
        @DisplayName("Should handle various CV formats")
        void shouldHandleVariousCVFormats() throws Exception {
            // Test PDF
            MockMultipartFile pdfFile = new MockMultipartFile(
                "file",
                "cv.pdf",
                MediaType.APPLICATION_PDF_VALUE,
                "PDF CV content".getBytes()
            );

            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart("/api/v1/ai/parse-cv")
                    .file(pdfFile)
                    .header("Authorization", "Bearer " + candidatToken))
                .andExpect(status().isOk());

            // Test DOCX
            MockMultipartFile docxFile = new MockMultipartFile(
                "file",
                "cv.docx",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                "DOCX CV content".getBytes()
            );

            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart("/api/v1/ai/parse-cv")
                    .file(docxFile)
                    .header("Authorization", "Bearer " + candidatToken))
                .andExpect(status().isOk());
        }
    }
}
