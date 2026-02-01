package ma.recrutement.service.ai;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ma.recrutement.dto.MatchingResultDTO;
import ma.recrutement.entity.Candidat;
import ma.recrutement.entity.Competence;
import ma.recrutement.entity.Experience;
import ma.recrutement.entity.OffreEmploi;
import ma.recrutement.repository.CandidatRepository;
import ma.recrutement.repository.OffreEmploiRepository;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service pour le matching intelligent CV/Offres utilisant Spring AI et Vector Store.
 * Utilise la recherche sémantique vectorielle et l'analyse GPT-4 pour des matchs précis.
 *
 * @author Recrutement Team
 * @version 2.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MatchingEngineService {

    private final VectorStore vectorStore;
    private final ChatClient chatClient;
    private final CandidatRepository candidatRepository;
    private final OffreEmploiRepository offreEmploiRepository;
    private final ObjectMapper objectMapper;

    // Scoring weights
    private static final double SKILLS_WEIGHT = 0.40;
    private static final double EXPERIENCE_WEIGHT = 0.30;
    private static final double EDUCATION_WEIGHT = 0.20;
    private static final double LOCATION_WEIGHT = 0.10;

    /**
     * Indexe un CV dans le vector store.
     *
     * @param candidatId l'ID du candidat
     */
    @org.springframework.transaction.annotation.Transactional
    public void indexCv(Long candidatId) {
        Candidat candidat = candidatRepository.findById(candidatId)
            .orElseThrow(() -> new RuntimeException("Candidat non trouvé"));

        if (candidat.getCvText() == null || candidat.getCvText().isBlank()) {
            log.warn("Impossible d'indexer le CV du candidat {}: pas de texte", candidatId);
            return;
        }

        // Generate UUID for document ID (required by PgVectorStore)
        String documentId = UUID.randomUUID().toString();
        Document document = new Document(
            documentId,
            candidat.getCvText(),
            Map.of(
                "candidatId", candidatId.toString(),
                "type", "cv",
                "nom", candidat.getNom() + " " + candidat.getPrenom()
            )
        );

        // Ajouter au vector store
        List<Document> documents = List.of(document);
        vectorStore.add(documents);

        // Mettre à jour le candidat avec l'ID du vector
        candidat.setCvVectorId(documentId);
        candidatRepository.save(candidat);

        log.info("CV indexé pour le candidat: {}", candidatId);
    }

    /**
     * Indexe le CV du candidat connecté.
     */
    public void indexMyCv() {
        // Get current candidate from security context
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        Candidat candidat = candidatRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("Candidat non trouvé"));
        indexCv(candidat.getId());
    }

    /**
     * Indexe une offre dans le vector store.
     *
     * @param offreId l'ID de l'offre
     */
    @org.springframework.transaction.annotation.Transactional
    public void indexOffre(Long offreId) {
        OffreEmploi offre = offreEmploiRepository.findByIdWithEntreprise(offreId);

        if (offre == null) {
            throw new RuntimeException("Offre non trouvée avec ID: " + offreId);
        }

        // Construire le texte à indexer
        String texte = buildOffreText(offre);

        // Generate UUID for document ID (required by PgVectorStore)
        String documentId = UUID.randomUUID().toString();
        String nomEntreprise = offre.getEntreprise() != null ? offre.getEntreprise().getNom() : "Non spécifiée";
        Document document = new Document(
            documentId,
            texte,
            Map.of(
                "offreId", offreId.toString(),
                "type", "offre",
                "titre", offre.getTitre(),
                "entreprise", nomEntreprise
            )
        );

        // Ajouter au vector store
        List<Document> documents = List.of(document);
        vectorStore.add(documents);

        // Mettre à jour l'offre avec l'ID du vector
        offre.setVectorId(documentId);
        offreEmploiRepository.save(offre);

        log.info("Offre indexée: {}", offreId);
    }

    /**
     * Trouve les candidats correspondants pour une offre en utilisant:
     * 1. Recherche sémantique vectorielle pour récupérer les candidats les plus similaires
     * 2. Analyse GPT-4 pour un scoring détaillé et multi-dimensionnel
     *
     * @param offreId l'ID de l'offre
     * @param limit le nombre maximum de résultats
     * @return la liste des candidats correspondants avec scores détaillés
     */
    public List<MatchingResultDTO> findMatchingCandidates(Long offreId, int limit) {
        OffreEmploi offre = offreEmploiRepository.findById(offreId)
            .orElseThrow(() -> new RuntimeException("Offre non trouvée"));

        // Build offer text for semantic search
        String offreText = buildOffreText(offre);
        if (offreText == null || offreText.isBlank()) {
            log.warn("Impossible de trouver des candidats pour l'offre {}: pas de description", offreId);
            return new ArrayList<>();
        }

        log.info("Matching offre {}: {} ({} chars)", offreId, offre.getTitre(), offreText.length());

        // Step 1: Use vector store for semantic similarity search (get more candidates initially)
        SearchRequest searchRequest = SearchRequest.query(offreText)
            .withTopK(50); // Get more candidates for better filtering

        List<Document> similarDocuments = vectorStore.similaritySearch(searchRequest);

        // Step 2: Filter documents by type="cv" and extract candidat IDs
        List<Long> candidatIds = similarDocuments.stream()
            .filter(doc -> {
                String type = (String) doc.getMetadata().get("type");
                return "cv".equalsIgnoreCase(type);
            })
            .map(doc -> {
                String candidatIdStr = (String) doc.getMetadata().get("candidatId");
                if (candidatIdStr != null) {
                    try {
                        return Long.parseLong(candidatIdStr);
                    } catch (NumberFormatException e) {
                        log.warn("Invalid candidatId in metadata: {}", candidatIdStr);
                        return null;
                    }
                }
                return null;
            })
            .filter(id -> id != null)
            .distinct()
            .collect(Collectors.toList());

        log.info("Found {} distinct CV candidates from vector search", candidatIds.size());

        // Step 3: Load candidates and analyze each with AI
        List<MatchingResultDTO> results = new ArrayList<>();
        for (Long candidatId : candidatIds) {
            candidatRepository.findById(candidatId).ifPresent(candidat -> {
                if (candidat.getCvText() != null && !candidat.getCvText().isBlank()) {
                    try {
                        MatchingResultDTO result = analyzeMatchWithAI(offre, candidat);
                        results.add(result);
                        log.debug("AI analysis for candidat {}: score={}",
                            candidatId, result.getScoreMatching());
                    } catch (Exception e) {
                        log.error("Error analyzing match for candidat {}: {}", candidatId, e.getMessage());
                    }
                }
            });
        }

        // Step 4: Sort by final score and return top results
        results.sort((a, b) -> Integer.compare(
            b.getScoreMatching() != null ? b.getScoreMatching() : 0,
            a.getScoreMatching() != null ? a.getScoreMatching() : 0
        ));

        List<MatchingResultDTO> finalResults = results.stream()
            .limit(limit)
            .collect(Collectors.toList());

        log.info("Returning {} top matching candidates for offre {}", finalResults.size(), offreId);
        return finalResults;
    }

    /**
     * Analyse un match candidat-offre en utilisant GPT-4 pour un scoring détaillé.
     *
     * @param offre l'offre d'emploi
     * @param candidat le candidat
     * @return le résultat du matching avec scores détaillés
     */
    private MatchingResultDTO analyzeMatchWithAI(OffreEmploi offre, Candidat candidat) {
        String nomEntreprise = offre.getEntreprise() != null ? offre.getEntreprise().getNom() : "Non spécifiée";

        // Build the prompt for AI analysis
        String prompt = buildAnalysisPrompt(offre, candidat);

        try {
            // Call GPT-4 for analysis
            String aiResponse = chatClient.prompt()
                .user(prompt)
                .call()
                .content();

            log.debug("AI response for candidat {}: {}", candidat.getId(), aiResponse);

            // Parse the JSON response
            Map<String, Object> aiAnalysis = parseAIResponse(aiResponse);

            // Extract scores from AI response
            @SuppressWarnings("unchecked")
            List<String> matchedSkills = (List<String>) aiAnalysis.getOrDefault("matchedSkills", new ArrayList<>());
            @SuppressWarnings("unchecked")
            List<String> missingSkills = (List<String>) aiAnalysis.getOrDefault("missingSkills", new ArrayList<>());

            int skillScore = extractIntValue(aiAnalysis, "skillScore");
            int experienceScore = extractIntValue(aiAnalysis, "experienceScore");
            int educationScore = extractIntValue(aiAnalysis, "educationScore");
            String recommendation = (String) aiAnalysis.getOrDefault("recommendation",
                "Analyse automatique basée sur le profil du candidat");

            // Calculate location score
            int locationScore = calculateLocationScore(offre, candidat);

            // Calculate final weighted score
            int finalScore = (int) Math.round(
                (skillScore * SKILLS_WEIGHT) +
                (experienceScore * EXPERIENCE_WEIGHT) +
                (educationScore * EDUCATION_WEIGHT) +
                (locationScore * LOCATION_WEIGHT)
            );

            // Determine if recommended
            Boolean recommande = finalScore >= 60;

            return MatchingResultDTO.builder()
                .offreId(offre.getId())
                .offreTitre(offre.getTitre())
                .nomEntreprise(nomEntreprise)
                .candidatId(candidat.getId())
                .candidatNom(candidat.getNom())
                .candidatPrenom(candidat.getPrenom())
                .scoreMatching(finalScore)
                .scoreCompetences(skillScore)
                .scoreExperience(experienceScore)
                .scoreFormation(educationScore)
                .competencesMatch(matchedSkills)
                .competencesManquantes(missingSkills)
                .recommendation(recommendation)
                .recommande(recommande)
                .reason("Analyse IA multi-dimensionnelles: compétences, expérience, formation, localisation")
                .build();

        } catch (Exception e) {
            log.error("Error in AI analysis for candidat {} and offre {}: {}",
                candidat.getId(), offre.getId(), e.getMessage(), e);

            // Return a basic result on error
            return MatchingResultDTO.builder()
                .offreId(offre.getId())
                .offreTitre(offre.getTitre())
                .nomEntreprise(nomEntreprise)
                .candidatId(candidat.getId())
                .candidatNom(candidat.getNom())
                .candidatPrenom(candidat.getPrenom())
                .scoreMatching(0)
                .scoreCompetences(0)
                .scoreExperience(0)
                .scoreFormation(0)
                .competencesMatch(new ArrayList<>())
                .competencesManquantes(new ArrayList<>())
                .recommendation("Erreur lors de l'analyse IA")
                .recommande(false)
                .reason("Erreur technique: " + e.getMessage())
                .build();
        }
    }

    /**
     * Construit le prompt d'analyse pour GPT-4.
     */
    private String buildAnalysisPrompt(OffreEmploi offre, Candidat candidat) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("You are an expert HR analyst. Analyze the match between a job offer and a candidate.\n\n");
        prompt.append("JOB OFFER:\n");
        prompt.append("- Title: ").append(offre.getTitre()).append("\n");
        prompt.append("- Description: ").append(offre.getDescription() != null ? offre.getDescription() : "N/A").append("\n");
        prompt.append("- Required Skills: ").append(offre.getCompetencesRequises() != null ? offre.getCompetencesRequises() : "N/A").append("\n");
        prompt.append("- Profile: ").append(offre.getProfilRecherche() != null ? offre.getProfilRecherche() : "N/A").append("\n");
        prompt.append("- Minimum Experience (years): ").append(offre.getExperienceMinAnnees() != null ? offre.getExperienceMinAnnees() : "N/A").append("\n");
        prompt.append("- Minimum Education Level: ").append(offre.getNiveauEtudesMin() != null ? offre.getNiveauEtudesMin() : "N/A").append("\n");
        prompt.append("- Location: ").append(offre.getLocalisation() != null ? offre.getLocalisation() : "N/A").append("\n");
        prompt.append("- Remote Work: ").append(offre.getTeletravail() != null ? offre.getTeletravail() : false).append("\n\n");

        prompt.append("CANDIDATE:\n");
        prompt.append("- CV Text: ").append(candidat.getCvText() != null ? candidat.getCvText() : "N/A").append("\n");
        prompt.append("- Desired Position: ").append(candidat.getTitrePosteRecherche() != null ? candidat.getTitrePosteRecherche() : "N/A").append("\n");

        prompt.append("- Experiences:\n");
        if (candidat.getExperiences() != null && !candidat.getExperiences().isEmpty()) {
            for (Experience exp : candidat.getExperiences()) {
                prompt.append("  * ").append(exp.getTitre())
                    .append(" at ").append(exp.getEntreprise())
                    .append(" (").append(exp.getDateDebut()).append(" - ")
                    .append(exp.getEmploiActuel() != null && exp.getEmploiActuel() ? "Present" : exp.getDateFin()).append(")\n");
            }
        } else {
            prompt.append("  None specified\n");
        }

        prompt.append("- Skills:\n");
        if (candidat.getCompetences() != null && !candidat.getCompetences().isEmpty()) {
            for (Competence comp : candidat.getCompetences()) {
                prompt.append("  * ").append(comp.getNom())
                    .append(" (Level: ").append(comp.getNiveau() != null ? comp.getNiveau() : "N/A")
                    .append(", Years: ").append(comp.getAnneesExperience() != null ? comp.getAnneesExperience() : "N/A").append(")\n");
            }
        } else {
            prompt.append("  None specified\n");
        }

        prompt.append("- Mobility: ").append(candidat.getMobilite() != null ? candidat.getMobilite() : "N/A").append("\n");
        prompt.append("- Immediately Available: ").append(candidat.getDisponibiliteImmediate() != null ? candidat.getDisponibiliteImmediate() : true).append("\n\n");

        prompt.append("Provide your analysis as a JSON object with this exact structure:\n");
        prompt.append("{\n");
        prompt.append("  \"matchedSkills\": [\"skill1\", \"skill2\", ...],\n");
        prompt.append("  \"missingSkills\": [\"skill1\", \"skill2\", ...],\n");
        prompt.append("  \"skillScore\": <0-100>,\n");
        prompt.append("  \"experienceScore\": <0-100>,\n");
        prompt.append("  \"educationScore\": <0-100>,\n");
        prompt.append("  \"recommendation\": \"Detailed explanation of why this candidate is a good match or not...\"\n");
        prompt.append("}\n\n");
        prompt.append("SCORING GUIDELINES:\n");
        prompt.append("- skillScore: Based on the percentage of required skills the candidate has (0-100)\n");
        prompt.append("- experienceScore: Based on whether candidate's years of experience meets or exceeds the requirement (0-100)\n");
        prompt.append("- educationScore: Based on whether candidate's education level meets or exceeds the minimum requirement (0-100)\n");
        prompt.append("- recommendation: A 2-3 sentence explanation in FRENCH highlighting strengths and any gaps\n\n");
        prompt.append("Return ONLY valid JSON, no markdown formatting, no additional text.");

        return prompt.toString();
    }

    /**
     * Parse la réponse de l'IA en gérant différents formats (JSON brut, markdown avec code blocks).
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> parseAIResponse(String response) {
        try {
            // Clean the response - remove markdown code blocks if present
            String cleanedResponse = response.trim();

            // Remove ```json and ``` markers
            if (cleanedResponse.startsWith("```json")) {
                cleanedResponse = cleanedResponse.substring(7);
            } else if (cleanedResponse.startsWith("```")) {
                cleanedResponse = cleanedResponse.substring(3);
            }

            if (cleanedResponse.endsWith("```")) {
                cleanedResponse = cleanedResponse.substring(0, cleanedResponse.length() - 3);
            }

            cleanedResponse = cleanedResponse.trim();

            // Try to parse as JSON
            return objectMapper.readValue(cleanedResponse, new TypeReference<Map<String, Object>>() {});

        } catch (JsonProcessingException e) {
            log.warn("Failed to parse AI response as JSON, returning default values. Response: {}", response, e);
            return new HashMap<>();
        }
    }

    /**
     * Extrait une valeur entière de la map d'analyse IA.
     */
    private int extractIntValue(Map<String, Object> analysis, String key) {
        Object value = analysis.get(key);
        if (value == null) {
            return 0;
        }
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        try {
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /**
     * Calcule le score de localisation (0-100).
     */
    private int calculateLocationScore(OffreEmploi offre, Candidat candidat) {
        // If remote work is allowed, give full score
        if (offre.getTeletravail() != null && offre.getTeletravail()) {
            return 100;
        }

        // Check candidate mobility
        String mobilite = candidat.getMobilite();
        if (mobilite != null) {
            mobilite = mobilite.toLowerCase();
            if (mobilite.contains("nationale") || mobilite.contains("international") || mobilite.contains("mondial")) {
                return 100;
            }
            if (mobilite.contains("regionale") || mobilite.contains("locale")) {
                return 80;
            }
        }

        // Check if locations match (basic string comparison)
        String offreLoc = offre.getLocalisation();
        if (offreLoc != null && candidat.getExperiences() != null) {
            for (Experience exp : candidat.getExperiences()) {
                if (exp.getLocalisation() != null &&
                    exp.getLocalisation().toLowerCase().contains(offreLoc.toLowerCase())) {
                    return 90;
                }
            }
        }

        return 50; // Neutral score if no information
    }

    /**
     * Trouve les offres correspondantes pour un candidat.
     *
     * @param candidatId l'ID du candidat
     * @param limit le nombre maximum de résultats
     * @return la liste des offres correspondantes
     */
    public List<MatchingResultDTO> findMatchingOffres(Long candidatId, int limit) {
        Candidat candidat = candidatRepository.findById(candidatId)
            .orElseThrow(() -> new RuntimeException("Candidat non trouvé"));

        if (candidat.getCvText() == null || candidat.getCvText().isBlank()) {
            log.warn("Impossible de trouver des offres pour le candidat {}: pas de CV", candidatId);
            return new ArrayList<>();
        }

        log.info("Finding matching offres for candidat {}", candidatId);

        // Step 1: Use vector store for semantic similarity search
        SearchRequest searchRequest = SearchRequest.query(candidat.getCvText())
            .withTopK(50);

        List<Document> similarDocuments = vectorStore.similaritySearch(searchRequest);

        // Step 2: Filter documents by type="offre" and extract offre IDs
        List<Long> offreIds = similarDocuments.stream()
            .filter(doc -> {
                String type = (String) doc.getMetadata().get("type");
                return "offre".equalsIgnoreCase(type);
            })
            .map(doc -> {
                String offreIdStr = (String) doc.getMetadata().get("offreId");
                if (offreIdStr != null) {
                    try {
                        return Long.parseLong(offreIdStr);
                    } catch (NumberFormatException e) {
                        log.warn("Invalid offreId in metadata: {}", offreIdStr);
                        return null;
                    }
                }
                return null;
            })
            .filter(id -> id != null)
            .distinct()
            .collect(Collectors.toList());

        log.info("Found {} distinct offre documents from vector search", offreIds.size());

        // Step 3: Load offers and analyze each with AI
        List<MatchingResultDTO> results = new ArrayList<>();
        for (Long offreId : offreIds) {
            offreEmploiRepository.findById(offreId).ifPresent(offre -> {
                if (offre.getStatut() == OffreEmploi.StatutOffre.PUBLIEE) {
                    try {
                        MatchingResultDTO result = analyzeMatchWithAI(offre, candidat);
                        results.add(result);
                    } catch (Exception e) {
                        log.error("Error analyzing match for offre {}: {}", offreId, e.getMessage());
                    }
                }
            });
        }

        // Step 4: Sort by final score and return top results
        results.sort((a, b) -> Integer.compare(
            b.getScoreMatching() != null ? b.getScoreMatching() : 0,
            a.getScoreMatching() != null ? a.getScoreMatching() : 0
        ));

        List<MatchingResultDTO> finalResults = results.stream()
            .limit(limit)
            .collect(Collectors.toList());

        log.info("Returning {} top matching offres for candidat {}", finalResults.size(), candidatId);
        return finalResults;
    }

    /**
     * Construit le texte représentatif d'une offre pour l'indexation.
     *
     * @param offre l'offre
     * @return le texte
     */
    private String buildOffreText(OffreEmploi offre) {
        StringBuilder sb = new StringBuilder();
        sb.append(offre.getTitre()).append("\n");

        // Description could be null
        if (offre.getDescription() != null) {
            sb.append(offre.getDescription()).append("\n");
        }

        if (offre.getCompetencesRequises() != null) {
            sb.append("Compétences requises: ").append(offre.getCompetencesRequises()).append("\n");
        }
        if (offre.getProfilRecherche() != null) {
            sb.append("Profil recherché: ").append(offre.getProfilRecherche()).append("\n");
        }
        sb.append("Type de contrat: ").append(offre.getTypeContrat()).append("\n");

        // Localisation could be null
        if (offre.getLocalisation() != null) {
            sb.append("Localisation: ").append(offre.getLocalisation()).append("\n");
        }
        if (offre.getNiveauEtudesMin() != null) {
            sb.append("Niveau d'études: ").append(offre.getNiveauEtudesMin()).append("\n");
        }
        if (offre.getExperienceMinAnnees() != null) {
            sb.append("Expérience minimum: ").append(offre.getExperienceMinAnnees()).append(" ans\n");
        }
        if (offre.getTeletravail() != null && offre.getTeletravail()) {
            sb.append("Télétravail possible\n");
        }
        return sb.toString();
    }
}
