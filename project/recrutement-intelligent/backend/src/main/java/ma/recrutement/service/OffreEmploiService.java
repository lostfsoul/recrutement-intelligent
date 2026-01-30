package ma.recrutement.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ma.recrutement.dto.OffreCreateDTO;
import ma.recrutement.dto.OffreEmploiDTO;
import ma.recrutement.dto.OffreUpdateDTO;
import ma.recrutement.dto.PaginationResponseDTO;
import ma.recrutement.entity.OffreEmploi;
import ma.recrutement.entity.Recruteur;
import ma.recrutement.entity.Utilisateur;
import ma.recrutement.exception.BusinessException;
import ma.recrutement.exception.ResourceNotFoundException;
import ma.recrutement.repository.EntrepriseRepository;
import ma.recrutement.repository.OffreEmploiRepository;
import ma.recrutement.repository.RecruteurRepository;
import ma.recrutement.repository.UtilisateurRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Service pour la gestion des offres d'emploi.
 *
 * @author Recrutement Team
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OffreEmploiService {

    private final OffreEmploiRepository offreEmploiRepository;
    private final EntrepriseRepository entrepriseRepository;
    private final RecruteurRepository recruteurRepository;
    private final UtilisateurRepository utilisateurRepository;

    /**
     * Crée une nouvelle offre d'emploi.
     *
     * @param dto les données de l'offre
     * @return l'offre créée
     */
    @Transactional
    public OffreEmploiDTO createOffre(OffreCreateDTO dto) {
        Recruteur recruteur = getAuthenticatedRecruteur();

        // Vérifier que l'entreprise appartient au recruteur
        var entreprise = recruteur.getEntreprises().stream()
            .filter(e -> e.getId().equals(dto.getEntrepriseId()))
            .findFirst()
            .orElseThrow(() -> new ResourceNotFoundException("Entreprise", dto.getEntrepriseId()));

        // Générer une référence unique
        String reference = generateReference();

        OffreEmploi offre = OffreEmploi.builder()
            .reference(reference)
            .titre(dto.getTitre())
            .description(dto.getDescription())
            .competencesRequises(dto.getCompetencesRequises())
            .profilRecherche(dto.getProfilRecherche())
            .salaireMin(dto.getSalaireMin())
            .salaireMax(dto.getSalaireMax())
            .devise(dto.getDevise() != null ? dto.getDevise() : "MAD")
            .localisation(dto.getLocalisation())
            .ville(dto.getVille())
            .pays(dto.getPays() != null ? dto.getPays() : "Maroc")
            .teletravail(dto.getTeletravail() != null ? dto.getTeletravail() : false)
            .typeContrat(dto.getTypeContrat())
            .statut(OffreEmploi.StatutOffre.BROUILLON)
            .experienceMinAnnees(dto.getExperienceMinAnnees())
            .niveauEtudesMin(dto.getNiveauEtudesMin())
            .languesRequises(dto.getLanguesRequises())
            .dateLimiteCandidature(dto.getDateLimiteCandidature())
            .nombrePostes(dto.getNombrePostes() != null ? dto.getNombrePostes() : 1)
            .nombreCandidatures(0)
            .actif(true)
            .entreprise(entreprise)
            .recruteur(recruteur)
            .build();

        offre = offreEmploiRepository.save(offre);
        log.info("Offre créée: ID={}, Reference={}", offre.getId(), offre.getReference());

        return mapToDTO(offre);
    }

    /**
     * Met à jour une offre d'emploi.
     *
     * @param offreId l'ID de l'offre
     * @param dto les données à mettre à jour
     * @return l'offre mise à jour
     */
    @Transactional
    public OffreEmploiDTO updateOffre(Long offreId, OffreUpdateDTO dto) {
        Recruteur recruteur = getAuthenticatedRecruteur();

        OffreEmploi offre = offreEmploiRepository.findById(offreId)
            .orElseThrow(() -> new ResourceNotFoundException("Offre", offreId));

        // Vérifier que l'offre appartient au recruteur
        if (!offre.getRecruteur().getId().equals(recruteur.getId())) {
            throw new BusinessException("Vous n'avez pas les droits pour modifier cette offre");
        }

        // Mettre à jour les champs
        if (dto.getTitre() != null) offre.setTitre(dto.getTitre());
        if (dto.getDescription() != null) offre.setDescription(dto.getDescription());
        if (dto.getCompetencesRequises() != null) offre.setCompetencesRequises(dto.getCompetencesRequises());
        if (dto.getProfilRecherche() != null) offre.setProfilRecherche(dto.getProfilRecherche());
        if (dto.getSalaireMin() != null) offre.setSalaireMin(dto.getSalaireMin());
        if (dto.getSalaireMax() != null) offre.setSalaireMax(dto.getSalaireMax());
        if (dto.getLocalisation() != null) offre.setLocalisation(dto.getLocalisation());
        if (dto.getVille() != null) offre.setVille(dto.getVille());
        if (dto.getTeletravail() != null) offre.setTeletravail(dto.getTeletravail());
        if (dto.getTypeContrat() != null) offre.setTypeContrat(dto.getTypeContrat());
        if (dto.getExperienceMinAnnees() != null) offre.setExperienceMinAnnees(dto.getExperienceMinAnnees());
        if (dto.getNiveauEtudesMin() != null) offre.setNiveauEtudesMin(dto.getNiveauEtudesMin());
        if (dto.getLanguesRequises() != null) offre.setLanguesRequises(dto.getLanguesRequises());
        if (dto.getDateLimiteCandidature() != null) offre.setDateLimiteCandidature(dto.getDateLimiteCandidature());
        if (dto.getNombrePostes() != null) offre.setNombrePostes(dto.getNombrePostes());

        offre = offreEmploiRepository.save(offre);
        return mapToDTO(offre);
    }

    /**
     * Supprime une offre d'emploi.
     *
     * @param offreId l'ID de l'offre
     */
    @Transactional
    public void deleteOffre(Long offreId) {
        Recruteur recruteur = getAuthenticatedRecruteur();

        OffreEmploi offre = offreEmploiRepository.findById(offreId)
            .orElseThrow(() -> new ResourceNotFoundException("Offre", offreId));

        // Vérifier que l'offre appartient au recruteur
        if (!offre.getRecruteur().getId().equals(recruteur.getId())) {
            throw new BusinessException("Vous n'avez pas les droits pour supprimer cette offre");
        }

        // Marquer comme inactive au lieu de supprimer
        offre.setActif(false);
        offre.setStatut(OffreEmploi.StatutOffre.ANNULEE);
        offreEmploiRepository.save(offre);

        log.info("Offre supprimée: ID={}", offreId);
    }

    /**
     * Obtient une offre par son ID.
     *
     * @param offreId l'ID de l'offre
     * @return l'offre
     */
    public OffreEmploiDTO getOffreById(Long offreId) {
        OffreEmploi offre = offreEmploiRepository.findById(offreId)
            .orElseThrow(() -> new ResourceNotFoundException("Offre", offreId));
        return mapToDTO(offre);
    }

    /**
     * Recherche des offres avec pagination.
     *
     * @param page le numéro de page
     * @param size la taille de la page
     * @param titre le titre à rechercher (optionnel)
     * @param localisation la localisation (optionnelle)
     * @return les offres paginées
     */
    public PaginationResponseDTO<OffreEmploiDTO> searchOffres(int page, int size, String titre, String localisation) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("dateCreation").descending());

        Page<OffreEmploi> result;
        if (titre != null && !titre.isBlank()) {
            result = offreEmploiRepository.findByTitreContainingIgnoreCase(titre, pageable);
        } else {
            result = offreEmploiRepository.findByStatutAndActifTrue(OffreEmploi.StatutOffre.PUBLIEE, pageable);
        }

        return PaginationResponseDTO.fromPage(result.map(this::mapToDTO));
    }

    /**
     * Publie une offre d'emploi.
     *
     * @param offreId l'ID de l'offre
     * @return l'offre publiée
     */
    @Transactional
    public OffreEmploiDTO publishOffre(Long offreId) {
        Recruteur recruteur = getAuthenticatedRecruteur();

        OffreEmploi offre = offreEmploiRepository.findById(offreId)
            .orElseThrow(() -> new ResourceNotFoundException("Offre", offreId));

        // Vérifier que l'offre appartient au recruteur
        if (!offre.getRecruteur().getId().equals(recruteur.getId())) {
            throw new BusinessException("Vous n'avez pas les droits pour publier cette offre");
        }

        offre.setStatut(OffreEmploi.StatutOffre.PUBLIEE);
        offre.setDatePublication(LocalDate.now());
        offre = offreEmploiRepository.save(offre);

        log.info("Offre publiée: ID={}", offreId);
        return mapToDTO(offre);
    }

    /**
     * Clôture une offre d'emploi.
     *
     * @param offreId l'ID de l'offre
     * @return l'offre clôturée
     */
    @Transactional
    public OffreEmploiDTO closeOffre(Long offreId) {
        Recruteur recruteur = getAuthenticatedRecruteur();

        OffreEmploi offre = offreEmploiRepository.findById(offreId)
            .orElseThrow(() -> new ResourceNotFoundException("Offre", offreId));

        // Vérifier que l'offre appartient au recruteur
        if (!offre.getRecruteur().getId().equals(recruteur.getId())) {
            throw new BusinessException("Vous n'avez pas les droits pour clôturer cette offre");
        }

        offre.setStatut(OffreEmploi.StatutOffre.CLOSE);
        offre.setActif(false);
        offre = offreEmploiRepository.save(offre);

        log.info("Offre clôturée: ID={}", offreId);
        return mapToDTO(offre);
    }

    /**
     * Obtient les offres publiées récentes.
     *
     * @param limit le nombre maximum d'offres
     * @return la liste des offres récentes
     */
    public List<OffreEmploiDTO> getRecentOffres(int limit) {
        List<OffreEmploi> offres = offreEmploiRepository.findOffresRecentes(LocalDate.now().minusWeeks(2));
        return offres.stream()
            .limit(limit)
            .map(this::mapToDTO)
            .toList();
    }

    private Recruteur getAuthenticatedRecruteur() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        // First try RecruteurRepository
        return recruteurRepository.findByEmail(email)
            .orElseGet(() -> {
                // Fallback: try UtilisateurRepository and cast
                Utilisateur utilisateur = utilisateurRepository.findByEmail(email)
                    .orElseThrow(() -> new ResourceNotFoundException("Recruteur non trouvé"));

                if (utilisateur instanceof Recruteur) {
                    return (Recruteur) utilisateur;
                }
                throw new ResourceNotFoundException("Recruteur non trouvé - l'utilisateur n'est pas un recruteur");
            });
    }

    private String generateReference() {
        return "OFF-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private OffreEmploiDTO mapToDTO(OffreEmploi offre) {
        return OffreEmploiDTO.builder()
            .id(offre.getId())
            .reference(offre.getReference())
            .titre(offre.getTitre())
            .description(offre.getDescription())
            .competencesRequises(offre.getCompetencesRequises())
            .profilRecherche(offre.getProfilRecherche())
            .salaireMin(offre.getSalaireMin())
            .salaireMax(offre.getSalaireMax())
            .devise(offre.getDevise())
            .frequencePaiement(offre.getFrequencePaiement())
            .localisation(offre.getLocalisation())
            .ville(offre.getVille())
            .pays(offre.getPays())
            .teletravail(offre.getTeletravail())
            .typeContrat(offre.getTypeContrat().name())
            .statut(offre.getStatut().name())
            .experienceMinAnnees(offre.getExperienceMinAnnees())
            .niveauEtudesMin(offre.getNiveauEtudesMin())
            .languesRequises(offre.getLanguesRequises())
            .datePublication(offre.getDatePublication())
            .dateLimiteCandidature(offre.getDateLimiteCandidature())
            .nombrePostes(offre.getNombrePostes())
            .nombreCandidatures(offre.getNombreCandidatures())
            .actif(offre.getActif())
            .entrepriseId(offre.getEntreprise().getId())
            .nomEntreprise(offre.getEntreprise().getNom())
            .logoEntreprise(offre.getEntreprise().getLogoPath())
            .dateCreation(offre.getDateCreation())
            .build();
    }
}
