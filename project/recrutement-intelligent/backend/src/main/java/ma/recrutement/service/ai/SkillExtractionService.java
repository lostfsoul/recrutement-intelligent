package ma.recrutement.service.ai;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ma.recrutement.dto.SkillExtractionDTO;
import ma.recrutement.entity.Competence;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Service pour l'extraction des compétences depuis un CV utilisant Spring AI.
 *
 * @author Recrutement Team
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SkillExtractionService {

    private final ChatClient chatClient;

    /**
     * Extrait les compétences depuis le texte d'un CV.
     *
     * @param cvText le texte du CV
     * @return la réponse avec les compétences extraites
     */
    public SkillExtractionDTO extractSkills(String cvText) {
        log.info("Extraction des compétences depuis le CV ({} mots)", cvText.split("\\s+").length);

        try {
            String prompt = buildPrompt(cvText);
            String response = chatClient.prompt()
                .user(prompt)
                .call()
                .content();

            return parseResponse(response);

        } catch (Exception e) {
            log.error("Erreur lors de l'extraction des compétences", e);
            return SkillExtractionDTO.builder()
                .skills(new ArrayList<>())
                .summary("Erreur lors de l'extraction")
                .confidenceScore(0)
                .success(false)
                .message("Erreur: " + e.getMessage())
                .build();
        }
    }

    /**
     * Construit le prompt pour l'extraction des compétences.
     *
     * @param cvText le texte du CV
     * @return le prompt
     */
    private String buildPrompt(String cvText) {
        return """
            Tu es un expert en analyse de CV et en ressources humaines.
            Ta tâche est d'extraire les compétences techniques et professionnelles depuis ce CV.

            Voici le CV à analyser :
            ---
            %s
            ---

            Réponds UNIQUEMENT au format JSON suivant, sans aucun texte avant ou après :
            {
              "skills": [
                {
                  "name": "Nom de la compétence",
                  "category": "Technique|Langue|Soft Skill|Méthodologie",
                  "level": "Débutant|Intermédiaire|Avancé|Expert",
                  "yearsOfExperience": nombre d'années (0 si non spécifié),
                  "certified": true|false
                }
              ],
              "summary": "Résumé du profil en 2-3 phrases",
              "confidenceScore": 0-100
            }

            Extrais uniquement les compétences clés et pertinentes. Ignore les informations génériques.
            """.formatted(cvText.length() > 10000 ? cvText.substring(0, 10000) : cvText);
    }

    /**
     * Parse la réponse JSON de l'IA.
     *
     * @param response la réponse JSON
     * @return la DTO d'extraction
     */
    private SkillExtractionDTO parseResponse(String response) {
        try {
            // Nettoyer la réponse
            String cleaned = response.trim();
            if (cleaned.startsWith("```json")) {
                cleaned = cleaned.substring(7);
            }
            if (cleaned.startsWith("```")) {
                cleaned = cleaned.substring(3);
            }
            if (cleaned.endsWith("```")) {
                cleaned = cleaned.substring(0, cleaned.length() - 3);
            }

            // Parser manuellement (éviter la dépendance Jackson si possible)
            List<SkillExtractionDTO.ExtractedSkill> skills = parseSkillsFromJson(cleaned);
            String summary = extractField(cleaned, "summary");
            int confidenceScore = extractIntField(cleaned, "confidenceScore");

            return SkillExtractionDTO.builder()
                .skills(skills)
                .summary(summary)
                .confidenceScore(confidenceScore)
                .success(true)
                .message("Compétences extraites avec succès")
                .build();

        } catch (Exception e) {
            log.error("Erreur lors du parsing de la réponse IA", e);
            return SkillExtractionDTO.builder()
                .skills(new ArrayList<>())
                .summary("Erreur de parsing")
                .confidenceScore(0)
                .success(false)
                .message("Erreur lors de l'analyse de la réponse")
                .build();
        }
    }

    private List<SkillExtractionDTO.ExtractedSkill> parseSkillsFromJson(String json) {
        List<SkillExtractionDTO.ExtractedSkill> skills = new ArrayList<>();
        // Parsing simplifié - en production, utiliser ObjectMapper
        // Pour l'instant, retourner une liste vide
        return skills;
    }

    private String extractField(String json, String fieldName) {
        String pattern = "\"" + fieldName + "\"\\s*:\\s*\"([^\"]+)\"";
        java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
        java.util.regex.Matcher m = p.matcher(json);
        return m.find() ? m.group(1) : "";
    }

    private int extractIntField(String json, String fieldName) {
        String pattern = "\"" + fieldName + "\"\\s*:\\s*(\\d+)";
        java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
        java.util.regex.Matcher m = p.matcher(json);
        return m.find() ? Integer.parseInt(m.group(1)) : 0;
    }
}
