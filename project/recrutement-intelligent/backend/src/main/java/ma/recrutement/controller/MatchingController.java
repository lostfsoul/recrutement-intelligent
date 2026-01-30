package ma.recrutement.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import ma.recrutement.dto.MatchingResultDTO;
import ma.recrutement.service.ai.MatchingEngineService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
     * Trouve les offres correspondantes pour un candidat.
     *
     * @param candidatId l'ID du candidat
     * @param limit le nombre maximum de résultats (défaut 10)
     * @return la liste des offres correspondantes
     */
    @Operation(summary = "Offres pour un candidat", description = "Trouve les offres correspondantes pour un candidat")
    @GetMapping("/candidats/{candidatId}/offres")
    public ResponseEntity<List<MatchingResultDTO>> findMatchingOffres(
            @PathVariable Long candidatId,
            @RequestParam(defaultValue = "10") int limit
    ) {
        List<MatchingResultDTO> results = matchingEngineService.findMatchingOffres(candidatId, limit);
        return ResponseEntity.ok(results);
    }

    /**
     * Trouve les candidats correspondants pour une offre.
     *
     * @param offreId l'ID de l'offre
     * @param limit le nombre maximum de résultats (défaut 10)
     * @return la liste des candidats correspondants
     */
    @Operation(summary = "Candidats pour une offre", description = "Trouve les candidats correspondants pour une offre")
    @GetMapping("/offres/{offreId}/candidats")
    public ResponseEntity<List<MatchingResultDTO>> findMatchingCandidates(
            @PathVariable Long offreId,
            @RequestParam(defaultValue = "10") int limit
    ) {
        List<MatchingResultDTO> results = matchingEngineService.findMatchingCandidates(offreId, limit);
        return ResponseEntity.ok(results);
    }

    /**
     * Indexe un CV dans le vector store.
     *
     * @param candidatId l'ID du candidat
     */
    @Operation(summary = "Indexer un CV", description = "Indexe un CV dans le vector store pour le matching")
    @PostMapping("/cv/{candidatId}/indexer")
    public ResponseEntity<Void> indexCv(@PathVariable Long candidatId) {
        matchingEngineService.indexCv(candidatId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Indexe une offre dans le vector store.
     *
     * @param offreId l'ID de l'offre
     */
    @Operation(summary = "Indexer une offre", description = "Indexe une offre dans le vector store pour le matching")
    @PostMapping("/offres/{offreId}/indexer")
    public ResponseEntity<Void> indexOffre(@PathVariable Long offreId) {
        matchingEngineService.indexOffre(offreId);
        return ResponseEntity.noContent().build();
    }
}
