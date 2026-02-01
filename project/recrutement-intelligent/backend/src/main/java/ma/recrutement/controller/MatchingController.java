package ma.recrutement.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import ma.recrutement.dto.MatchingResultDTO;
import ma.recrutement.service.ai.MatchingEngineService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller pour le matching intelligent CV/Offres.
 *
 * @author Recrutement Team
 * @version 1.0.0
 */
@Tag(name = "Matching Intelligent", description = "API de matching intelligent CV/Offres")
@RestController
@RequestMapping("/api/v1/matching")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class MatchingController {

    private final MatchingEngineService matchingEngineService;

    /**
     * Trouve les candidats correspondants pour une offre.
     * Accessible uniquement par les recruteurs et admins (pas les candidats).
     *
     * @param offreId l'ID de l'offre
     * @param limit le nombre maximum de résultats (défaut 10)
     * @return la liste des candidats correspondants
     */
    @Operation(summary = "Candidats pour une offre", description = "Trouve les candidats correspondants pour une offre (Recruteurs/Admins uniquement)")
    @GetMapping("/offres/{offreId}/candidats")
    @PreAuthorize("hasAnyRole('RECRUTEUR', 'ADMINISTRATEUR')")
    public ResponseEntity<List<MatchingResultDTO>> findMatchingCandidates(
            @PathVariable Long offreId,
            @RequestParam(defaultValue = "10") int limit
    ) {
        List<MatchingResultDTO> results = matchingEngineService.findMatchingCandidates(offreId, limit);
        return ResponseEntity.ok(results);
    }

    /**
     * Indexe le CV du candidat connecté pour le matching AI.
     *
     * @return confirmation de l'indexation
     */
    @Operation(summary = "Indexer mon CV", description = "Indexe le CV du candidat dans le vector store pour le matching AI")
    @PostMapping("/cv/indexer")
    public ResponseEntity<Map<String, String>> indexMyCv() {
        matchingEngineService.indexMyCv();
        Map<String, String> response = new HashMap<>();
        response.put("message", "CV indexé avec succès pour le matching AI");
        return ResponseEntity.ok(response);
    }

    /**
     * Indexe une offre d'emploi pour le matching AI.
     *
     * @param offreId l'ID de l'offre
     * @return confirmation de l'indexation
     */
    @Operation(summary = "Indexer une offre", description = "Indexe une offre dans le vector store pour le matching AI")
    @PostMapping("/offres/{offreId}/indexer")
    @PreAuthorize("hasAnyRole('RECRUTEUR', 'ADMINISTRATEUR')")
    public ResponseEntity<Map<String, String>> indexOffre(@PathVariable Long offreId) {
        matchingEngineService.indexOffre(offreId);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Offre indexée avec succès pour le matching AI");
        return ResponseEntity.ok(response);
    }
}
