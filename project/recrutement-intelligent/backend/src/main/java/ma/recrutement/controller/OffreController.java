package ma.recrutement.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import ma.recrutement.dto.OffreCreateDTO;
import ma.recrutement.dto.OffreEmploiDTO;
import ma.recrutement.dto.OffreUpdateDTO;
import ma.recrutement.dto.PaginationResponseDTO;
import ma.recrutement.entity.Candidature;
import ma.recrutement.service.OffreEmploiService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller pour la gestion des offres d'emploi.
 *
 * @author Recrutement Team
 * @version 1.0.0
 */
@Tag(name = "Offres d'emploi", description = "API de gestion des offres d'emploi")
@RestController
@RequestMapping("/api/v1/offres")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class OffreController {

    private final OffreEmploiService offreEmploiService;

    /**
     * Crée une nouvelle offre d'emploi.
     *
     * @param dto les données de l'offre
     * @return l'offre créée
     */
    @Operation(summary = "Créer une offre", description = "Crée une nouvelle offre d'emploi")
    @PostMapping
    public ResponseEntity<OffreEmploiDTO> createOffre(@Valid @RequestBody OffreCreateDTO dto) {
        OffreEmploiDTO created = offreEmploiService.createOffre(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Met à jour une offre d'emploi.
     *
     * @param offreId l'ID de l'offre
     * @param dto les données à mettre à jour
     * @return l'offre mise à jour
     */
    @Operation(summary = "Mettre à jour une offre", description = "Met à jour une offre d'emploi existante")
    @PutMapping("/{offreId}")
    public ResponseEntity<OffreEmploiDTO> updateOffre(
            @PathVariable Long offreId,
            @Valid @RequestBody OffreUpdateDTO dto
    ) {
        OffreEmploiDTO updated = offreEmploiService.updateOffre(offreId, dto);
        return ResponseEntity.ok(updated);
    }

    /**
     * Supprime une offre d'emploi.
     *
     * @param offreId l'ID de l'offre
     */
    @Operation(summary = "Supprimer une offre", description = "Supprime une offre d'emploi")
    @DeleteMapping("/{offreId}")
    public ResponseEntity<Void> deleteOffre(@PathVariable Long offreId) {
        offreEmploiService.deleteOffre(offreId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Obtient une offre par son ID.
     *
     * @param offreId l'ID de l'offre
     * @return l'offre
     */
    @Operation(summary = "Détails d'une offre", description = "Récupère les détails d'une offre d'emploi")
    @GetMapping("/{offreId}")
    public ResponseEntity<OffreEmploiDTO> getOffreById(@PathVariable Long offreId) {
        OffreEmploiDTO offre = offreEmploiService.getOffreById(offreId);
        return ResponseEntity.ok(offre);
    }

    /**
     * Recherche des offres avec pagination.
     *
     * @param page le numéro de page (défaut 0)
     * @param size la taille de la page (défaut 10)
     * @param titre le titre à rechercher (optionnel)
     * @param localisation la localisation (optionnelle)
     * @return les offres paginées
     */
    @Operation(summary = "Rechercher des offres", description = "Recherche des offres d'emploi avec pagination")
    @GetMapping
    public ResponseEntity<PaginationResponseDTO<OffreEmploiDTO>> searchOffres(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String titre,
            @RequestParam(required = false) String localisation
    ) {
        PaginationResponseDTO<OffreEmploiDTO> result = offreEmploiService.searchOffres(page, size, titre, localisation);
        return ResponseEntity.ok(result);
    }

    /**
     * Obtient les offres récentes.
     *
     * @param limit le nombre maximum d'offres (défaut 5)
     * @return la liste des offres récentes
     */
    @Operation(summary = "Offres récentes", description = "Récupère les offres publiées récemment")
    @GetMapping("/recentes")
    public ResponseEntity<List<OffreEmploiDTO>> getRecentOffres(
            @RequestParam(defaultValue = "5") int limit
    ) {
        List<OffreEmploiDTO> offres = offreEmploiService.getRecentOffres(limit);
        return ResponseEntity.ok(offres);
    }

    /**
     * Publie une offre d'emploi.
     *
     * @param offreId l'ID de l'offre
     * @return l'offre publiée
     */
    @Operation(summary = "Publier une offre", description = "Publie une offre d'emploi")
    @PostMapping("/{offreId}/publier")
    public ResponseEntity<OffreEmploiDTO> publishOffre(@PathVariable Long offreId) {
        OffreEmploiDTO offre = offreEmploiService.publishOffre(offreId);
        return ResponseEntity.ok(offre);
    }

    /**
     * Clôture une offre d'emploi.
     *
     * @param offreId l'ID de l'offre
     * @return l'offre clôturée
     */
    @Operation(summary = "Clôturer une offre", description = "Clôture une offre d'emploi")
    @PostMapping("/{offreId}/cloturer")
    public ResponseEntity<OffreEmploiDTO> closeOffre(@PathVariable Long offreId) {
        OffreEmploiDTO offre = offreEmploiService.closeOffre(offreId);
        return ResponseEntity.ok(offre);
    }
}
