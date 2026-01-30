package ma.recrutement.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import ma.recrutement.dto.EntrepriseDTO;
import ma.recrutement.entity.Entreprise;
import ma.recrutement.service.AdminService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controller pour la gestion administrative.
 *
 * @author Recrutement Team
 * @version 1.0.0
 */
@Tag(name = "Administration", description = "API d'administration de la plateforme")
@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AdminController {

    private final AdminService adminService;

    /**
     * Obtient les statistiques générales de la plateforme.
     *
     * @return les statistiques
     */
    @Operation(summary = "Statistiques", description = "Récupère les statistiques générales de la plateforme")
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStatistics() {
        Map<String, Object> stats = adminService.getStatistics();
        return ResponseEntity.ok(stats);
    }

    /**
     * Obtient les entreprises en attente de validation.
     *
     * @return la liste des entreprises
     */
    @Operation(summary = "Entreprises en attente", description = "Récupère les entreprises en attente de validation")
    @GetMapping("/entreprises/en-attente")
    public ResponseEntity<List<EntrepriseDTO>> getEntreprisesEnAttente() {
        List<EntrepriseDTO> entreprises = adminService.getEntreprisesEnAttente();
        return ResponseEntity.ok(entreprises);
    }

    /**
     * Valide une entreprise.
     *
     * @param entrepriseId l'ID de l'entreprise
     * @return l'entreprise validée
     */
    @Operation(summary = "Valider une entreprise", description = "Valide une entreprise en attente")
    @PutMapping("/entreprises/{entrepriseId}/valider")
    public ResponseEntity<EntrepriseDTO> validateEntreprise(@PathVariable Long entrepriseId) {
        EntrepriseDTO entreprise = adminService.validateEntreprise(entrepriseId);
        return ResponseEntity.ok(entreprise);
    }

    /**
     * Refuse une entreprise.
     *
     * @param entrepriseId l'ID de l'entreprise
     * @return l'entreprise refusée
     */
    @Operation(summary = "Refuser une entreprise", description = "Refuse une entreprise en attente")
    @PutMapping("/entreprises/{entrepriseId}/refuser")
    public ResponseEntity<EntrepriseDTO> rejectEntreprise(@PathVariable Long entrepriseId) {
        EntrepriseDTO entreprise = adminService.rejectEntreprise(entrepriseId);
        return ResponseEntity.ok(entreprise);
    }

    /**
     * Active un utilisateur.
     *
     * @param utilisateurId l'ID de l'utilisateur
     */
    @Operation(summary = "Activer un utilisateur", description = "Active un utilisateur désactivé")
    @PutMapping("/utilisateurs/{utilisateurId}/activer")
    public ResponseEntity<Void> activateUser(@PathVariable Long utilisateurId) {
        adminService.activateUser(utilisateurId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Désactive un utilisateur.
     *
     * @param utilisateurId l'ID de l'utilisateur
     */
    @Operation(summary = "Désactiver un utilisateur", description = "Désactive un utilisateur")
    @PutMapping("/utilisateurs/{utilisateurId}/desactiver")
    public ResponseEntity<Void> deactivateUser(@PathVariable Long utilisateurId) {
        adminService.deactivateUser(utilisateurId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Réinitialise le mot de passe d'un utilisateur.
     *
     * @param utilisateurId l'ID de l'utilisateur
     * @param newPassword le nouveau mot de passe
     */
    @Operation(summary = "Réinitialiser le mot de passe", description = "Réinitialise le mot de passe d'un utilisateur")
    @PutMapping("/utilisateurs/{utilisateurId}/password")
    public ResponseEntity<Void> resetPassword(
            @PathVariable Long utilisateurId,
            @RequestParam String newPassword
    ) {
        adminService.resetPassword(utilisateurId, newPassword);
        return ResponseEntity.noContent().build();
    }

    /**
     * Supprime une offre.
     *
     * @param offreId l'ID de l'offre
     */
    @Operation(summary = "Supprimer une offre", description = "Supprime une offre d'emploi")
    @DeleteMapping("/offres/{offreId}")
    public ResponseEntity<Void> deleteOffre(@PathVariable Long offreId) {
        adminService.deleteOffre(offreId);
        return ResponseEntity.noContent().build();
    }
}
