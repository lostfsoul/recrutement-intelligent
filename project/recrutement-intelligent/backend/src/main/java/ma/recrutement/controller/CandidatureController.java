package ma.recrutement.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import ma.recrutement.dto.CandidatureCreateDTO;
import ma.recrutement.dto.CandidatureDTO;
import ma.recrutement.entity.Candidature;
import ma.recrutement.service.CandidatureService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller pour la gestion des candidatures.
 *
 * @author Recrutement Team
 * @version 1.0.0
 */
@Tag(name = "Candidatures", description = "API de gestion des candidatures")
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class CandidatureController {

    private final CandidatureService candidatureService;

    /**
     * Crée une nouvelle candidature (postule à une offre).
     *
     * @param offreId l'ID de l'offre
     * @param dto les données de la candidature
     * @return la candidature créée
     */
    @Operation(summary = "Postuler à une offre", description = "Crée une nouvelle candidature à une offre d'emploi")
    @PostMapping("/offres/{offreId}/postuler")
    public ResponseEntity<CandidatureDTO> createCandidature(
            @PathVariable Long offreId,
            @Valid @RequestBody CandidatureCreateDTO dto
    ) {
        dto.setOffreId(offreId);
        CandidatureDTO created = candidatureService.createCandidature(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Obtient les candidatures du candidat connecté.
     *
     * @return la liste des candidatures
     */
    @Operation(summary = "Mes candidatures", description = "Récupère toutes les candidatures du candidat connecté")
    @GetMapping("/candidatures/me")
    public ResponseEntity<List<CandidatureDTO>> getMyCandidatures() {
        List<CandidatureDTO> candidatures = candidatureService.getMyCandidatures();
        return ResponseEntity.ok(candidatures);
    }

    /**
     * Obtient les candidatures pour une offre (recruteur).
     *
     * @param offreId l'ID de l'offre
     * @return la liste des candidatures
     */
    @Operation(summary = "Candidatures d'une offre", description = "Récupère toutes les candidatures pour une offre")
    @GetMapping("/offres/{offreId}/candidatures")
    public ResponseEntity<List<CandidatureDTO>> getCandidaturesByOffre(@PathVariable Long offreId) {
        List<CandidatureDTO> candidatures = candidatureService.getCandidaturesByOffre(offreId);
        return ResponseEntity.ok(candidatures);
    }

    /**
     * Met à jour le statut d'une candidature (recruteur).
     *
     * @param candidatureId l'ID de la candidature
     * @param statut le nouveau statut
     * @return la candidature mise à jour
     */
    @Operation(summary = "Mettre à jour le statut", description = "Met à jour le statut d'une candidature")
    @PutMapping("/candidatures/{candidatureId}/statut")
    public ResponseEntity<CandidatureDTO> updateStatut(
            @PathVariable Long candidatureId,
            @RequestParam Candidature.StatutCandidature statut
    ) {
        CandidatureDTO updated = candidatureService.updateStatut(candidatureId, statut);
        return ResponseEntity.ok(updated);
    }
}
