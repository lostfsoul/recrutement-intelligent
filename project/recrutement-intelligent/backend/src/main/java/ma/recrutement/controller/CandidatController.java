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

import java.util.List;

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
     * Obtient le CV du candidat.
     *
     * @return le CV
     */
    @Operation(summary = "Mon CV", description = "Récupère le CV du candidat connecté")
    @GetMapping("/me/cv")
    public ResponseEntity<String> getMyCv() {
        String cvPath = candidatService.getMyCv();
        return ResponseEntity.ok(cvPath);
    }

    /**
     * Supprime le CV du candidat.
     */
    @Operation(summary = "Supprimer mon CV", description = "Supprime le CV du candidat connecté")
    @DeleteMapping("/me/cv")
    public ResponseEntity<Void> deleteMyCv() {
        candidatService.deleteMyCv();
        return ResponseEntity.noContent().build();
    }

    /**
     * Ajoute une compétence au candidat.
     *
     * @param competenceDto la compétence à ajouter
     * @return la compétence créée
     */
    @Operation(summary = "Ajouter une compétence", description = "Ajoute une compétence au profil du candidat")
    @PostMapping("/me/competences")
    public ResponseEntity<CompetenceDTO> addCompetence(@Valid @RequestBody CompetenceDTO competenceDto) {
        CompetenceDTO created = candidatService.addCompetence(competenceDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Supprime une compétence du candidat.
     *
     * @param competenceId l'ID de la compétence
     */
    @Operation(summary = "Supprimer une compétence", description = "Supprime une compétence du profil du candidat")
    @DeleteMapping("/me/competences/{competenceId}")
    public ResponseEntity<Void> deleteCompetence(@PathVariable Long competenceId) {
        candidatService.deleteCompetence(competenceId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Ajoute une expérience au candidat.
     *
     * @param experienceDto l'expérience à ajouter
     * @return l'expérience créée
     */
    @Operation(summary = "Ajouter une expérience", description = "Ajoute une expérience professionnelle au profil du candidat")
    @PostMapping("/me/experiences")
    public ResponseEntity<ExperienceDTO> addExperience(@Valid @RequestBody ExperienceDTO experienceDto) {
        ExperienceDTO created = candidatService.addExperience(experienceDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Supprime une expérience du candidat.
     *
     * @param experienceId l'ID de l'expérience
     */
    @Operation(summary = "Supprimer une expérience", description = "Supprime une expérience du profil du candidat")
    @DeleteMapping("/me/experiences/{experienceId}")
    public ResponseEntity<Void> deleteExperience(@PathVariable Long experienceId) {
        candidatService.deleteExperience(experienceId);
        return ResponseEntity.noContent().build();
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
