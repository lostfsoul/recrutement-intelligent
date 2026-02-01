package ma.recrutement.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import ma.recrutement.dto.*;
import ma.recrutement.service.CandidatService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller pour la gestion des candidats.
 *
 * @author Recrutement Team
 * @version 1.0.0
 */
@Tag(name = "Candidats", description = "API de gestion des candidats")
@RestController
@RequestMapping("/api/v1/candidats")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class CandidatController {

    private final CandidatService candidatService;

    /**
     * Obtient le profil du candidat connecté.
     *
     * @return le profil du candidat
     */
    @Operation(summary = "Mon profil", description = "Récupère le profil complet du candidat connecté")
    @GetMapping("/me")
    public ResponseEntity<CandidatProfileDTO> getMyProfile() {
        CandidatProfileDTO profile = candidatService.getMyProfile();
        return ResponseEntity.ok(profile);
    }

    /**
     * Met à jour le profil du candidat connecté.
     *
     * @param dto les données à mettre à jour
     * @return le profil mis à jour
     */
    @Operation(summary = "Mettre à jour mon profil", description = "Met à jour les informations du candidat connecté")
    @PutMapping("/me")
    public ResponseEntity<CandidatDTO> updateMyProfile(@Valid @RequestBody CandidatDTO dto) {
        CandidatDTO updated = candidatService.updateMyProfile(dto);
        return ResponseEntity.ok(updated);
    }

    /**
     * Upload le CV du candidat.
     *
     * @param file le fichier CV
     * @return la réponse de l'upload
     */
    @Operation(summary = "Uploader mon CV", description = "Upload et analyse le CV du candidat")
    @PostMapping(value = "/me/cv", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CvUploadDTO> uploadCv(@RequestParam("file") MultipartFile file) {
        CvUploadDTO response = candidatService.uploadCv(file);
        return ResponseEntity.ok(response);
    }

    /**
     * Définit le texte du CV directement (pour tests).
     *
     * @param cvText le texte du CV
     * @return confirmation
     */
    @Operation(summary = "Définir texte CV (test)", description = "Définit directement le texte du CV pour le matching AI")
    @PostMapping("/me/cv/text")
    public ResponseEntity<Map<String, String>> setCvText(@RequestBody String cvText) {
        candidatService.setCvText(cvText);
        Map<String, String> response = new java.util.HashMap<>();
        response.put("message", "CV text défini avec succès");
        return ResponseEntity.ok(response);
    }

    /**
     * Obtient les candidatures du candidat.
     *
     * @return la liste des candidatures
     */
    @Operation(summary = "Mes candidatures", description = "Récupère toutes les candidatures du candidat connecté")
    @GetMapping("/me/candidatures")
    public ResponseEntity<List<CandidatureDTO>> getMyCandidatures() {
        List<CandidatureDTO> candidatures = candidatService.getMyCandidatures();
        return ResponseEntity.ok(candidatures);
    }
}
