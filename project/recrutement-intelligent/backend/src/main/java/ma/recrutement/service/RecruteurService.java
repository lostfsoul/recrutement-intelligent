package ma.recrutement.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ma.recrutement.dto.*;
import ma.recrutement.entity.Entreprise;
import ma.recrutement.entity.Recruteur;
import ma.recrutement.exception.ResourceNotFoundException;
import ma.recrutement.repository.EntrepriseRepository;
import ma.recrutement.repository.OffreEmploiRepository;
import ma.recrutement.repository.RecruteurRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service pour la gestion des recruteurs.
 *
 * @author Recrutement Team
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RecruteurService {

    private final RecruteurRepository recruteurRepository;
    private final EntrepriseRepository entrepriseRepository;
    private final OffreEmploiRepository offreEmploiRepository;

    /**
     * Obtient le profil du recruteur connecté.
     *
     * @return le profil du recruteur
     */
    public RecruteurDTO getMyProfile() {
        Recruteur recruteur = getAuthenticatedRecruteur();
        return mapToDTO(recruteur);
    }

    /**
     * Met à jour le profil du recruteur connecté.
     *
     * @param dto les données à mettre à jour
     * @return le profil mis à jour
     */
    @Transactional
    public RecruteurDTO updateMyProfile(RecruteurDTO dto) {
        Recruteur recruteur = getAuthenticatedRecruteur();

        if (dto.getTelephone() != null) recruteur.setTelephone(dto.getTelephone());
        if (dto.getNomEntreprise() != null) recruteur.setNomEntreprise(dto.getNomEntreprise());
        if (dto.getPoste() != null) recruteur.setPoste(dto.getPoste());
        if (dto.getLinkedinUrl() != null) recruteur.setLinkedinUrl(dto.getLinkedinUrl());

        recruteur = recruteurRepository.save(recruteur);
        return mapToDTO(recruteur);
    }

    /**
     * Crée une nouvelle entreprise pour le recruteur.
     *
     * @param dto les données de l'entreprise
     * @return l'entreprise créée
     */
    @Transactional
    public EntrepriseDTO createEntreprise(EntrepriseCreateDTO dto) {
        Recruteur recruteur = getAuthenticatedRecruteur();

        // Vérifier si une entreprise avec le même nom existe déjà pour ce recruteur
        if (entrepriseRepository.existsByNomAndRecruteurId(dto.getNom(), recruteur.getId())) {
            throw new IllegalArgumentException("Vous avez déjà une entreprise avec ce nom");
        }

        Entreprise entreprise = Entreprise.builder()
            .nom(dto.getNom())
            .description(dto.getDescription())
            .secteur(dto.getSecteur())
            .tailleEntreprise(dto.getTailleEntreprise())
            .localisation(dto.getLocalisation())
            .adresse(dto.getAdresse())
            .codePostal(dto.getCodePostal())
            .ville(dto.getVille())
            .pays(dto.getPays() != null ? dto.getPays() : "Maroc")
            .siteWeb(dto.getSiteWeb())
            .emailContact(dto.getEmailContact())
            .telephoneContact(dto.getTelephoneContact())
            .dateFondation(dto.getDateFondation())
            .statutValidation(Entreprise.StatutValidation.EN_ATTENTE)
            .active(true)
            .recruteur(recruteur)
            .build();

        recruteur.ajouterEntreprise(entreprise);
        entreprise = entrepriseRepository.save(entreprise);

        return mapEntrepriseToDTO(entreprise);
    }

    /**
     * Obtient la liste des entreprises du recruteur.
     *
     * @return la liste des entreprises
     */
    public List<EntrepriseDTO> getMyEntreprises() {
        Recruteur recruteur = getAuthenticatedRecruteur();
        return recruteur.getEntreprises().stream()
            .map(this::mapEntrepriseToDTO)
            .collect(Collectors.toList());
    }

    /**
     * Obtient une entreprise par son ID.
     *
     * @param entrepriseId l'ID de l'entreprise
     * @return l'entreprise
     */
    public EntrepriseDTO getEntreprise(Long entrepriseId) {
        Recruteur recruteur = getAuthenticatedRecruteur();

        Entreprise entreprise = recruteur.getEntreprises().stream()
            .filter(e -> e.getId().equals(entrepriseId))
            .findFirst()
            .orElseThrow(() -> new ResourceNotFoundException("Entreprise", entrepriseId));

        return mapEntrepriseToDTO(entreprise);
    }

    /**
     * Met à jour une entreprise.
     *
     * @param entrepriseId l'ID de l'entreprise
     * @param dto les données à mettre à jour
     * @return l'entreprise mise à jour
     */
    @Transactional
    public EntrepriseDTO updateEntreprise(Long entrepriseId, EntrepriseCreateDTO dto) {
        Recruteur recruteur = getAuthenticatedRecruteur();

        Entreprise entreprise = recruteur.getEntreprises().stream()
            .filter(e -> e.getId().equals(entrepriseId))
            .findFirst()
            .orElseThrow(() -> new ResourceNotFoundException("Entreprise", entrepriseId));

        if (dto.getNom() != null) entreprise.setNom(dto.getNom());
        if (dto.getDescription() != null) entreprise.setDescription(dto.getDescription());
        if (dto.getSecteur() != null) entreprise.setSecteur(dto.getSecteur());
        if (dto.getTailleEntreprise() != null) entreprise.setTailleEntreprise(dto.getTailleEntreprise());
        if (dto.getLocalisation() != null) entreprise.setLocalisation(dto.getLocalisation());
        if (dto.getAdresse() != null) entreprise.setAdresse(dto.getAdresse());
        if (dto.getCodePostal() != null) entreprise.setCodePostal(dto.getCodePostal());
        if (dto.getVille() != null) entreprise.setVille(dto.getVille());
        if (dto.getPays() != null) entreprise.setPays(dto.getPays());
        if (dto.getSiteWeb() != null) entreprise.setSiteWeb(dto.getSiteWeb());
        if (dto.getEmailContact() != null) entreprise.setEmailContact(dto.getEmailContact());
        if (dto.getTelephoneContact() != null) entreprise.setTelephoneContact(dto.getTelephoneContact());
        if (dto.getDateFondation() != null) entreprise.setDateFondation(dto.getDateFondation());

        entreprise = entrepriseRepository.save(entreprise);
        return mapEntrepriseToDTO(entreprise);
    }

    /**
     * Upload le logo d'une entreprise.
     *
     * @param entrepriseId l'ID de l'entreprise
     * @param file le fichier logo
     * @return l'entreprise mise à jour
     */
    @Transactional
    public EntrepriseDTO uploadLogo(Long entrepriseId, MultipartFile file) {
        Recruteur recruteur = getAuthenticatedRecruteur();

        Entreprise entreprise = recruteur.getEntreprises().stream()
            .filter(e -> e.getId().equals(entrepriseId))
            .findFirst()
            .orElseThrow(() -> new ResourceNotFoundException("Entreprise", entrepriseId));

        try {
            ma.recrutement.util.FileStorageUtil fileStorageUtil = new ma.recrutement.util.FileStorageUtil();
            fileStorageUtil.init();
            String logoPath = fileStorageUtil.storeLogo(file, entrepriseId);
            entreprise.setLogoPath(logoPath);
            entreprise = entrepriseRepository.save(entreprise);
        } catch (IOException e) {
            log.error("Erreur lors de l'upload du logo", e);
            throw new RuntimeException("Erreur lors de l'upload du logo", e);
        }

        return mapEntrepriseToDTO(entreprise);
    }

    /**
     * Obtient les offres du recruteur.
     *
     * @return la liste des offres
     */
    public List<OffreEmploiDTO> getMyOffres() {
        Recruteur recruteur = getAuthenticatedRecruteur();
        return offreEmploiRepository.findByRecruteurId(recruteur.getId()).stream()
            .map(this::mapOffreToDTO)
            .collect(Collectors.toList());
    }

    private Recruteur getAuthenticatedRecruteur() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        return recruteurRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("Recruteur non trouvé"));
    }

    private RecruteurDTO mapToDTO(Recruteur recruteur) {
        return RecruteurDTO.builder()
            .id(recruteur.getId())
            .email(recruteur.getEmail())
            .nom(recruteur.getNom())
            .prenom(recruteur.getPrenom())
            .telephone(recruteur.getTelephone())
            .verified(recruteur.getVerified())
            .nomEntreprise(recruteur.getNomEntreprise())
            .poste(recruteur.getPoste())
            .linkedinUrl(recruteur.getLinkedinUrl())
            .statut(recruteur.getStatut().name())
            .nombreEntreprises(recruteur.getEntreprises().size())
            .nombreOffres((int) offreEmploiRepository.count())
            .dateCreation(recruteur.getDateCreation())
            .build();
    }

    private EntrepriseDTO mapEntrepriseToDTO(Entreprise entreprise) {
        return EntrepriseDTO.builder()
            .id(entreprise.getId())
            .nom(entreprise.getNom())
            .description(entreprise.getDescription())
            .secteur(entreprise.getSecteur())
            .tailleEntreprise(entreprise.getTailleEntreprise())
            .localisation(entreprise.getLocalisation())
            .adresse(entreprise.getAdresse())
            .codePostal(entreprise.getCodePostal())
            .ville(entreprise.getVille())
            .pays(entreprise.getPays())
            .siteWeb(entreprise.getSiteWeb())
            .logoPath(entreprise.getLogoPath())
            .emailContact(entreprise.getEmailContact())
            .telephoneContact(entreprise.getTelephoneContact())
            .dateFondation(entreprise.getDateFondation())
            .statutValidation(entreprise.getStatutValidation().name())
            .active(entreprise.getActive())
            .recruteurId(entreprise.getRecruteur().getId())
            .nombreOffres(entreprise.getOffres().size())
            .dateCreation(entreprise.getDateCreation())
            .build();
    }

    private OffreEmploiDTO mapOffreToDTO(ma.recrutement.entity.OffreEmploi offre) {
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
