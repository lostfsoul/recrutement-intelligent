package ma.recrutement.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ma.recrutement.dto.EntrepriseDTO;
import ma.recrutement.entity.Entreprise;
import ma.recrutement.entity.OffreEmploi;
import ma.recrutement.entity.Utilisateur;
import ma.recrutement.exception.ResourceNotFoundException;
import ma.recrutement.repository.EntrepriseRepository;
import ma.recrutement.repository.OffreEmploiRepository;
import ma.recrutement.repository.UtilisateurRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service pour la gestion administrative.
 *
 * @author Recrutement Team
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminService {

    private final UtilisateurRepository utilisateurRepository;
    private final EntrepriseRepository entrepriseRepository;
    private final OffreEmploiRepository offreEmploiRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Obtient les statistiques générales de la plateforme.
     *
     * @return les statistiques
     */
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();

        // Nombre d'utilisateurs par rôle
        stats.put("totalCandidats", utilisateurRepository.findAll().stream()
            .filter(u -> u.getRole() == Utilisateur.Role.CANDIDAT).count());
        stats.put("totalRecruteurs", utilisateurRepository.findAll().stream()
            .filter(u -> u.getRole() == Utilisateur.Role.RECRUTEUR).count());
        stats.put("totalAdministrateurs", utilisateurRepository.findAll().stream()
            .filter(u -> u.getRole() == Utilisateur.Role.ADMINISTRATEUR).count());

        // Nombre d'entreprises par statut
        stats.put("entreprisesEnAttente", entrepriseRepository.findByStatutValidation(Entreprise.StatutValidation.EN_ATTENTE).size());
        stats.put("entreprisesValidees", entrepriseRepository.findByStatutValidation(Entreprise.StatutValidation.VALIDEE).size());
        stats.put("entreprisesRefusees", entrepriseRepository.findByStatutValidation(Entreprise.StatutValidation.REFUSEE).size());

        // Nombre d'offres par statut
        stats.put("offresBrouillon", offreEmploiRepository.findByStatut(OffreEmploi.StatutOffre.BROUILLON).size());
        stats.put("offresPubliees", offreEmploiRepository.findByStatut(OffreEmploi.StatutOffre.PUBLIEE).size());
        stats.put("offresCloturees", offreEmploiRepository.findByStatut(OffreEmploi.StatutOffre.CLOSE).size());

        // Utilisateurs actifs vs inactifs
        stats.put("utilisateursActifs", utilisateurRepository.findByStatut(Utilisateur.StatutUtilisateur.ACTIF).size());
        stats.put("utilisateursInactifs", utilisateurRepository.findByStatut(Utilisateur.StatutUtilisateur.INACTIF).size());

        return stats;
    }

    /**
     * Valide une entreprise.
     *
     * @param entrepriseId l'ID de l'entreprise
     * @return l'entreprise validée
     */
    @Transactional
    public EntrepriseDTO validateEntreprise(Long entrepriseId) {
        Entreprise entreprise = entrepriseRepository.findById(entrepriseId)
            .orElseThrow(() -> new ResourceNotFoundException("Entreprise", entrepriseId));

        entreprise.setStatutValidation(Entreprise.StatutValidation.VALIDEE);
        entreprise.setDateValidation(java.time.LocalDateTime.now());
        // TODO: Set valideParId from authenticated admin
        entreprise = entrepriseRepository.save(entreprise);

        log.info("Entreprise validée: ID={}", entrepriseId);

        return mapEntrepriseToDTO(entreprise);
    }

    /**
     * Refuse une entreprise.
     *
     * @param entrepriseId l'ID de l'entreprise
     * @return l'entreprise refusée
     */
    @Transactional
    public EntrepriseDTO rejectEntreprise(Long entrepriseId) {
        Entreprise entreprise = entrepriseRepository.findById(entrepriseId)
            .orElseThrow(() -> new ResourceNotFoundException("Entreprise", entrepriseId));

        entreprise.setStatutValidation(Entreprise.StatutValidation.REFUSEE);
        entreprise = entrepriseRepository.save(entreprise);

        log.info("Entreprise refusée: ID={}", entrepriseId);

        return mapEntrepriseToDTO(entreprise);
    }

    /**
     * Obtient les entreprises en attente de validation.
     *
     * @return la liste des entreprises
     */
    public List<EntrepriseDTO> getEntreprisesEnAttente() {
        return entrepriseRepository.findByStatutValidationOrderByDateCreationAsc(Entreprise.StatutValidation.EN_ATTENTE).stream()
            .map(this::mapEntrepriseToDTO)
            .collect(Collectors.toList());
    }

    /**
     * Active un utilisateur.
     *
     * @param utilisateurId l'ID de l'utilisateur
     */
    @Transactional
    public void activateUser(Long utilisateurId) {
        Utilisateur utilisateur = utilisateurRepository.findById(utilisateurId)
            .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", utilisateurId));

        utilisateur.setStatut(Utilisateur.StatutUtilisateur.ACTIF);
        utilisateurRepository.save(utilisateur);

        log.info("Utilisateur activé: ID={}", utilisateurId);
    }

    /**
     * Désactive un utilisateur.
     *
     * @param utilisateurId l'ID de l'utilisateur
     */
    @Transactional
    public void deactivateUser(Long utilisateurId) {
        Utilisateur utilisateur = utilisateurRepository.findById(utilisateurId)
            .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", utilisateurId));

        utilisateur.setStatut(Utilisateur.StatutUtilisateur.SUSPENDU);
        utilisateurRepository.save(utilisateur);

        log.info("Utilisateur désactivé: ID={}", utilisateurId);
    }

    /**
     * Réinitialise le mot de passe d'un utilisateur.
     *
     * @param utilisateurId l'ID de l'utilisateur
     * @param newPassword le nouveau mot de passe
     */
    @Transactional
    public void resetPassword(Long utilisateurId, String newPassword) {
        Utilisateur utilisateur = utilisateurRepository.findById(utilisateurId)
            .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", utilisateurId));

        utilisateur.setPassword(passwordEncoder.encode(newPassword));
        utilisateurRepository.save(utilisateur);

        log.info("Mot de passe réinitialisé pour l'utilisateur: ID={}", utilisateurId);
    }

    /**
     * Supprime une offre (admin).
     *
     * @param offreId l'ID de l'offre
     */
    @Transactional
    public void deleteOffre(Long offreId) {
        OffreEmploi offre = offreEmploiRepository.findById(offreId)
            .orElseThrow(() -> new ResourceNotFoundException("Offre", offreId));

        offre.setActif(false);
        offre.setStatut(OffreEmploi.StatutOffre.ANNULEE);
        offreEmploiRepository.save(offre);

        log.info("Offre supprimée par admin: ID={}", offreId);
    }

    private EntrepriseDTO mapEntrepriseToDTO(Entreprise entreprise) {
        return EntrepriseDTO.builder()
            .id(entreprise.getId())
            .nom(entreprise.getNom())
            .description(entreprise.getDescription())
            .secteur(entreprise.getSecteur())
            .tailleEntreprise(entreprise.getTailleEntreprise())
            .localisation(entreprise.getLocalisation())
            .ville(entreprise.getVille())
            .pays(entreprise.getPays())
            .siteWeb(entreprise.getSiteWeb())
            .emailContact(entreprise.getEmailContact())
            .telephoneContact(entreprise.getTelephoneContact())
            .statutValidation(entreprise.getStatutValidation().name())
            .active(entreprise.getActive())
            .recruteurId(entreprise.getRecruteur().getId())
            .nombreOffres(entreprise.getOffres().size())
            .dateCreation(entreprise.getDateCreation())
            .build();
    }
}
