package ma.recrutement.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import ma.recrutement.dto.CvParseResponseDTO;
import ma.recrutement.dto.SkillExtractionDTO;
import ma.recrutement.service.ai.CvParsingService;
import ma.recrutement.service.ai.SkillExtractionService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * Controller pour les fonctionnalités d'IA.
 *
 * @author Recrutement Team
 * @version 1.0.0
 */
@Tag(name = "Intelligence Artificielle", description = "API des fonctionnalités d'IA")
@RestController
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AIController {

    private final CvParsingService cvParsingService;
    private final SkillExtractionService skillExtractionService;

    /**
     * Parse un fichier CV pour en extraire le texte.
     *
     * @param file le fichier CV
     * @return la réponse avec le texte extrait
     */
    @Operation(summary = "Parser un CV", description = "Extrait le texte d'un fichier CV (PDF, DOCX)")
    @PostMapping(value = "/parse-cv", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CvParseResponseDTO> parseCv(@RequestParam("file") MultipartFile file) {
        CvParseResponseDTO response = cvParsingService.parseCv(file);
        return ResponseEntity.ok(response);
    }

    /**
     * Extrait les compétences depuis le texte d'un CV.
     *
     * @param cvText le texte du CV
     * @return les compétences extraites
     */
    @Operation(summary = "Extraire les compétences", description = "Extrait les compétences depuis le texte d'un CV utilisant l'IA")
    @PostMapping("/extract-skills")
    public ResponseEntity<SkillExtractionDTO> extractSkills(@RequestBody String cvText) {
        SkillExtractionDTO response = skillExtractionService.extractSkills(cvText);
        return ResponseEntity.ok(response);
    }

    /**
     * Analyse un fichier CV complet (parsing + extraction de compétences).
     *
     * @param file le fichier CV
     * @return les compétences extraites
     */
    @Operation(summary = "Analyser un CV complet", description = "Parse le CV et extrait les compétences en une seule requête")
    @PostMapping(value = "/analyze-cv", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<SkillExtractionDTO> analyzeCv(@RequestParam("file") MultipartFile file) {
        // D'abord extraire le texte
        CvParseResponseDTO parseResponse = cvParsingService.parseCv(file);

        if (!parseResponse.getSuccess() || parseResponse.getExtractedText() == null) {
            return ResponseEntity.badRequest().body(
                SkillExtractionDTO.builder()
                    .success(false)
                    .message("Impossible d'extraire le texte du CV")
                    .build()
            );
        }

        // Puis extraire les compétences
        SkillExtractionDTO response = skillExtractionService.extractSkills(parseResponse.getExtractedText());
        return ResponseEntity.ok(response);
    }
}
