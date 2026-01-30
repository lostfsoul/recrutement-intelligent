package ma.recrutement.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import ma.recrutement.dto.*;
import ma.recrutement.service.RecruteurService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Controller pour la gestion des recruteurs.
 *
 * @author Recrutement Team
 * @version 1.0.0
 */
@Tag(name = "Recruteurs", description = "API de gestion des recruteurs")
@RestController
@RequestMapping("/api/v1/recruteurs")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class RecruteurController {

    private final RecruteurService recruteurService;

    /**
     * Obtient le profil du recruteur connecté.
     *
     * @return le profil du recruteur
     */
    @Operation(summary = "Mon profil", description = "Récupère le profil du recruteur connecté")
    @GetMapping("/me")
    public ResponseEntity<RecruteurDTO> getMyProfile() {
        RecruteurDTO profile = recruteurService.getMyProfile();
        return ResponseEntity.ok(profile);
    }

    /**
     * Met à jour le profil du recruteur connecté.
     *
     * @param dto les données à mettre à jour
     * @return le profil mis à jour
     */
    @Operation(summary = "Mettre à jour mon profil", description = "Met à jour les informations du recruteur connecté")
    @PutMapping("/me")
    public ResponseEntity<RecruteurDTO> updateMyProfile(@Valid @RequestBody RecruteurDTO dto) {
        RecruteurDTO updated = recruteurService.updateMyProfile(dto);
        return ResponseEntity.ok(updated);
    }

    /**
     * Crée une nouvelle entreprise pour le recruteur.
     *
     * @param dto les données de l'entreprise
     * @return l'entreprise créée
     */
    @Operation(summary = "Créer une entreprise", description = "Crée une nouvelle entreprise pour le recruteur connecté")
    @PostMapping("/me/entreprises")
    public ResponseEntity<EntrepriseDTO> createEntreprise(@Valid @RequestBody EntrepriseCreateDTO dto) {
        EntrepriseDTO created = recruteurService.createEntreprise(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Obtient la liste des entreprises du recruteur.
     *
     * @return la liste des entreprises
     */
    @Operation(summary = "Mes entreprises", description = "Récupère toutes les entreprises du recruteur connecté")
    @GetMapping("/me/entreprises")
    public ResponseEntity<List<EntrepriseDTO>> getMyEntreprises() {
        List<EntrepriseDTO> entreprises = recruteurService.getMyEntreprises();
        return ResponseEntity.ok(entreprises);
    }

    /**
     * Obtient une entreprise par son ID.
     *
     * @param entrepriseId l'ID de l'entreprise
     * @return l'entreprise
     */
    @Operation(summary = "Détails entreprise", description = "Récupère les détails d'une entreprise")
    @GetMapping("/me/entreprises/{entrepriseId}")
    public ResponseEntity<EntrepriseDTO> getEntreprise(@PathVariable Long entrepriseId) {
        EntrepriseDTO entreprise = recruteurService.getEntreprise(entrepriseId);
        return ResponseEntity.ok(entreprise);
    }

    /**
     * Met à jour une entreprise.
     *
     * @param entrepriseId l'ID de l'entreprise
     * @param dto les données à mettre à jour
     * @return l'entreprise mise à jour
     */
    @Operation(summary = "Mettre à jour une entreprise", description = "Met à jour les informations d'une entreprise")
    @PutMapping("/me/entreprises/{entrepriseId}")
    public ResponseEntity<EntrepriseDTO> updateEntreprise(
            @PathVariable Long entrepriseId,
            @Valid @RequestBody EntrepriseCreateDTO dto
    ) {
        EntrepriseDTO updated = recruteurService.updateEntreprise(entrepriseId, dto);
        return ResponseEntity.ok(updated);
    }

    /**
     * Upload le logo d'une entreprise.
     *
     * @param entrepriseId l'ID de l'entreprise
     * @param file le fichier logo
     * @return l'entreprise mise à jour
     */
    @Operation(summary = "Uploader le logo", description = "Upload le logo d'une entreprise")
    @PostMapping(value = "/me/entreprises/{entrepriseId}/logo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<EntrepriseDTO> uploadLogo(
            @PathVariable Long entrepriseId,
            @RequestParam("file") MultipartFile file
    ) {
        EntrepriseDTO updated = recruteurService.uploadLogo(entrepriseId, file);
        return ResponseEntity.ok(updated);
    }

    /**
     * Obtient les offres du recruteur.
     *
     * @return la liste des offres
     */
    @Operation(summary = "Mes offres", description = "Récupère toutes les offres du recruteur connecté")
    @GetMapping("/me/offres")
    public ResponseEntity<List<OffreEmploiDTO>> getMyOffres() {
        List<OffreEmploiDTO> offres = recruteurService.getMyOffres();
        return ResponseEntity.ok(offres);
    }
}
