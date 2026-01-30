package ma.recrutement.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ma.recrutement.dto.CandidatureCreateDTO;
import ma.recrutement.dto.CandidatureDTO;
import ma.recrutement.entity.Candidat;
import ma.recrutement.entity.Candidature;
import ma.recrutement.entity.OffreEmploi;
import ma.recrutement.exception.BusinessException;
import ma.recrutement.exception.ResourceNotFoundException;
import ma.recrutement.repository.CandidatRepository;
import ma.recrutement.repository.CandidatureRepository;
import ma.recrutement.repository.OffreEmploiRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service pour la gestion des candidatures.
 *
 * @author Recrutement Team
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CandidatureService {

    private final CandidatureRepository candidatureRepository;
    private final CandidatRepository candidatRepository;
    private final OffreEmploiRepository offreEmploiRepository;

    /**
     * Crée une nouvelle candidature (candidat postule à une offre).
     *
     * @param dto les données de la candidature
     * @return la candidature créée
     */
    @Transactional
    public CandidatureDTO createCandidature(CandidatureCreateDTO dto) {
        Candidat candidat = getAuthenticatedCandidat();

        OffreEmploi offre = offreEmploiRepository.findById(dto.getOffreId())
            .orElseThrow(() -> new ResourceNotFoundException("Offre", dto.getOffreId()));

        // Vérifier que l'offre est publiée et active
        if (!offre.estOuverte()) {
            throw new BusinessException("Cette offre n'est plus ouverte aux candidatures");
        }

        // Vérifier si le candidat a déjà postulé
        if (candidatureRepository.existsByCandidatIdAndOffreId(candidat.getId(), offre.getId())) {
            throw new BusinessException("Vous avez déjà postulé à cette offre");
        }

        // Créer la candidature
        Candidature candidature = Candidature.builder()
            .candidat(candidat)
            .offre(offre)
            .lettreMotivation(dto.getLettreMotivation())
            .statut(Candidature.StatutCandidature.EN_ATTENTE)
            .vuParRecruteur(false)
            .build();

        candidature = candidatureRepository.save(candidature);

        // Incrémenter le nombre de candidatures de l'offre
        offre.incrementerCandidatures();
        offreEmploiRepository.save(offre);

        log.info("Candidature créée: ID={}, Candidat={}, Offre={}", candidature.getId(), candidat.getId(), offre.getId());

        return mapToDTO(candidature);
    }

    /**
     * Obtient les candidatures du candidat connecté.
     *
     * @return la liste des candidatures
     */
    public List<CandidatureDTO> getMyCandidatures() {
        Candidat candidat = getAuthenticatedCandidat();
        return candidatureRepository.findByCandidatIdOrderByDateCandidatureDesc(candidat.getId()).stream()
            .map(this::mapToDTO)
            .collect(Collectors.toList());
    }

    /**
     * Obtient les candidatures pour une offre (recruteur).
     *
     * @param offreId l'ID de l'offre
     * @return la liste des candidatures
     */
    public List<CandidatureDTO> getCandidaturesByOffre(Long offreId) {
        // Vérifier que l'offre appartient au recruteur connecté
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        OffreEmploi offre = offreEmploiRepository.findById(offreId)
            .orElseThrow(() -> new ResourceNotFoundException("Offre", offreId));

        if (!offre.getRecruteur().getEmail().equals(email)) {
            throw new BusinessException("Vous n'avez pas les droits pour voir les candidatures de cette offre");
        }

        return candidatureRepository.findByOffreIdOrderByDateCandidatureDesc(offreId).stream()
            .map(this::mapToDTO)
            .collect(Collectors.toList());
    }

    /**
     * Met à jour le statut d'une candidature (recruteur).
     *
     * @param candidatureId l'ID de la candidature
     * @param nouveauStatut le nouveau statut
     * @return la candidature mise à jour
     */
    @Transactional
    public CandidatureDTO updateStatut(Long candidatureId, Candidature.StatutCandidature nouveauStatut) {
        Candidature candidature = candidatureRepository.findById(candidatureId)
            .orElseThrow(() -> new ResourceNotFoundException("Candidature", candidatureId));

        // Vérifier que l'offre appartient au recruteur connecté
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        if (!candidature.getOffre().getRecruteur().getEmail().equals(email)) {
            throw new BusinessException("Vous n'avez pas les droits pour modifier cette candidature");
        }

        candidature.changerStatut(nouveauStatut);
        candidature = candidatureRepository.save(candidature);

        log.info("Statut candidature mis à jour: ID={}, NouveauStatut={}", candidatureId, nouveauStatut);

        return mapToDTO(candidature);
    }

    /**
     * Marque une candidature comme vue par le recruteur.
     *
     * @param candidatureId l'ID de la candidature
     * @return la candidature mise à jour
     */
    @Transactional
    public CandidatureDTO markAsViewed(Long candidatureId) {
        Candidature candidature = candidatureRepository.findById(candidatureId)
            .orElseThrow(() -> new ResourceNotFoundException("Candidature", candidatureId));

        // Vérifier que l'offre appartient au recruteur connecté
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        if (!candidature.getOffre().getRecruteur().getEmail().equals(email)) {
            throw new BusinessException("Vous n'avez pas les droits pour modifier cette candidature");
        }

        candidature.marquerCommeVue();
        candidature = candidatureRepository.save(candidature);

        return mapToDTO(candidature);
    }

    /**
     * Annule une candidature (candidat).
     *
     * @param candidatureId l'ID de la candidature
     */
    @Transactional
    public void cancelCandidature(Long candidatureId) {
        Candidat candidat = getAuthenticatedCandidat();

        Candidature candidature = candidatureRepository.findById(candidatureId)
            .orElseThrow(() -> new ResourceNotFoundException("Candidature", candidatureId));

        // Vérifier que la candidature appartient au candidat
        if (!candidature.getCandidat().getId().equals(candidat.getId())) {
            throw new BusinessException("Vous n'avez pas les droits pour annuler cette candidature");
        }

        candidature.changerStatut(Candidature.StatutCandidature.RETIRE_PAR_CANDIDAT);

        // Décrémenter le nombre de candidatures de l'offre
        candidature.getOffre().decrementerCandidatures();
        offreEmploiRepository.save(candidature.getOffre());

        candidatureRepository.save(candidature);

        log.info("Candidature annulée: ID={}", candidatureId);
    }

    /**
     * Obtient le nombre de candidatures non vues pour une offre.
     *
     * @param offreId l'ID de l'offre
     * @return le nombre de candidatures non vues
     */
    public long countUnviewedCandidatures(Long offreId) {
        return candidatureRepository.findByOffreIdAndVuParRecruteurFalse(offreId).size();
    }

    private Candidat getAuthenticatedCandidat() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        return candidatRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("Candidat non trouvé"));
    }

    private CandidatureDTO mapToDTO(Candidature candidature) {
        return CandidatureDTO.builder()
            .id(candidature.getId())
            .candidatId(candidature.getCandidat().getId())
            .candidatNom(candidature.getCandidat().getNom())
            .candidatPrenom(candidature.getCandidat().getPrenom())
            .offreId(candidature.getOffre().getId())
            .offreTitre(candidature.getOffre().getTitre())
            .nomEntreprise(candidature.getOffre().getEntreprise().getNom())
            .lettreMotivation(candidature.getLettreMotivation())
            .statut(candidature.getStatut().name())
            .scoreMatching(candidature.getScoreMatching())
            .vuParRecruteur(candidature.getVuParRecruteur())
            .dateCandidature(candidature.getDateCandidature())
            .dateStatutChange(candidature.getDateStatutChange())
            .raisonRefus(candidature.getRaisonRefus())
            .build();
    }
}
