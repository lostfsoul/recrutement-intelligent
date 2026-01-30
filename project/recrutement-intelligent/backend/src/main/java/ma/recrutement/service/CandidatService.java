package ma.recrutement.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ma.recrutement.dto.*;
import ma.recrutement.entity.Candidat;
import ma.recrutement.entity.Competence;
import ma.recrutement.entity.Experience;
import ma.recrutement.exception.ResourceNotFoundException;
import ma.recrutement.repository.CandidatRepository;
import ma.recrutement.repository.CandidatureRepository;
import ma.recrutement.util.FileStorageUtil;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service pour la gestion des candidats.
 *
 * @author Recrutement Team
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CandidatService {

    private final CandidatRepository candidatRepository;
    private final CandidatureRepository candidatureRepository;
    private final FileStorageUtil fileStorageUtil;

    /**
     * Obtient le profil du candidat connecté.
     *
     * @return le profil du candidat
     */
    public CandidatProfileDTO getMyProfile() {
        Candidat candidat = getAuthenticatedCandidat();
        return mapToProfileDTO(candidat);
    }

    /**
     * Met à jour le profil du candidat connecté.
     *
     * @param dto les données à mettre à jour
     * @return le profil mis à jour
     */
    @Transactional
    public CandidatDTO updateMyProfile(CandidatDTO dto) {
        Candidat candidat = getAuthenticatedCandidat();

        if (dto.getTelephone() != null) candidat.setTelephone(dto.getTelephone());
        if (dto.getTitrePosteRecherche() != null) candidat.setTitrePosteRecherche(dto.getTitrePosteRecherche());
        if (dto.getDisponibiliteImmediate() != null) candidat.setDisponibiliteImmediate(dto.getDisponibiliteImmediate());
        if (dto.getDateDisponibilite() != null) candidat.setDateDisponibilite(dto.getDateDisponibilite());
        if (dto.getPretentionSalarialeMin() != null) candidat.setPretentionSalarialeMin(dto.getPretentionSalarialeMin());
        if (dto.getPretentionSalarialeMax() != null) candidat.setPretentionSalarialeMax(dto.getPretentionSalarialeMax());
        if (dto.getMobilite() != null) candidat.setMobilite(dto.getMobilite());
        if (dto.getLinkedinUrl() != null) candidat.setLinkedinUrl(dto.getLinkedinUrl());
        if (dto.getGithubUrl() != null) candidat.setGithubUrl(dto.getGithubUrl());
        if (dto.getPortefolioUrl() != null) candidat.setPortefolioUrl(dto.getPortefolioUrl());
        if (dto.getPresentation() != null) candidat.setPresentation(dto.getPresentation());

        candidat = candidatRepository.save(candidat);
        return mapToDTO(candidat);
    }

    /**
     * Upload le CV du candidat.
     *
     * @param file le fichier CV
     * @return la réponse de l'upload
     */
    @Transactional
    public CvUploadDTO uploadCv(MultipartFile file) {
        Candidat candidat = getAuthenticatedCandidat();

        try {
            // Stocker le fichier
            String cvPath = fileStorageUtil.storeCv(file, candidat.getId());
            candidat.setCvPath(cvPath);

            // Extraire le texte du CV (à implémenter avec Apache PDFBox/POI)
            String cvText = extractTextFromCv(file);
            candidat.setCvText(cvText);

            candidat = candidatRepository.save(candidat);

            return CvUploadDTO.builder()
                .cvPath(cvPath)
                .cvText(cvText)
                .extractionSuccessful(cvText != null && !cvText.isBlank())
                .message("CV uploadé avec succès")
                .nombreCompetences(candidat.getCompetences().size())
                .nombreExperiences(candidat.getExperiences().size())
                .build();

        } catch (IOException e) {
            log.error("Erreur lors de l'upload du CV", e);
            throw new RuntimeException("Erreur lors de l'upload du CV", e);
        }
    }

    /**
     * Obtient le CV du candidat.
     *
     * @return le chemin du CV
     */
    public String getMyCv() {
        Candidat candidat = getAuthenticatedCandidat();
        if (candidat.getCvPath() == null) {
            throw new ResourceNotFoundException("Aucun CV trouvé pour ce candidat");
        }
        return candidat.getCvPath();
    }

    /**
     * Supprime le CV du candidat.
     */
    @Transactional
    public void deleteMyCv() {
        Candidat candidat = getAuthenticatedCandidat();

        if (candidat.getCvPath() != null) {
            fileStorageUtil.deleteFile(candidat.getCvPath());
        }

        candidat.setCvPath(null);
        candidat.setCvText(null);
        candidat.setCvVectorId(null);

        candidatRepository.save(candidat);
    }

    /**
     * Ajoute une compétence au candidat.
     *
     * @param competenceDto la compétence à ajouter
     * @return la compétence créée
     */
    @Transactional
    public CompetenceDTO addCompetence(CompetenceDTO competenceDto) {
        Candidat candidat = getAuthenticatedCandidat();

        Competence competence = Competence.builder()
            .nom(competenceDto.getNom())
            .categorie(competenceDto.getCategorie())
            .niveau(competenceDto.getNiveau())
            .anneesExperience(competenceDto.getAnneesExperience())
            .certifiee(competenceDto.getCertifiee() != null ? competenceDto.getCertifiee() : false)
            .derniereUtilisation(competenceDto.getDerniereUtilisation())
            .candidat(candidat)
            .build();

        candidat.ajouterCompetence(competence);
        Competence savedCompetence = candidatRepository.save(candidat).getCompetences().stream()
            .filter(c -> c.getNom().equals(competence.getNom()))
            .findFirst()
            .orElse(competence);

        return mapCompetenceToDTO(savedCompetence);
    }

    /**
     * Supprime une compétence du candidat.
     *
     * @param competenceId l'ID de la compétence
     */
    @Transactional
    public void deleteCompetence(Long competenceId) {
        Candidat candidat = getAuthenticatedCandidat();

        Competence competence = candidat.getCompetences().stream()
            .filter(c -> c.getId().equals(competenceId))
            .findFirst()
            .orElseThrow(() -> new ResourceNotFoundException("Compétence", competenceId));

        candidat.retirerCompetence(competence);
        candidatRepository.save(candidat);
    }

    /**
     * Ajoute une expérience au candidat.
     *
     * @param experienceDto l'expérience à ajouter
     * @return l'expérience créée
     */
    @Transactional
    public ExperienceDTO addExperience(ExperienceDTO experienceDto) {
        Candidat candidat = getAuthenticatedCandidat();

        Experience experience = Experience.builder()
            .titre(experienceDto.getTitre())
            .entreprise(experienceDto.getEntreprise())
            .typeEntreprise(experienceDto.getTypeEntreprise())
            .secteur(experienceDto.getSecteur())
            .localisation(experienceDto.getLocalisation())
            .dateDebut(experienceDto.getDateDebut())
            .dateFin(experienceDto.getDateFin())
            .emploiActuel(experienceDto.getEmploiActuel() != null ? experienceDto.getEmploiActuel() : false)
            .description(experienceDto.getDescription())
            .responsabilites(experienceDto.getResponsabilites())
            .accomplissements(experienceDto.getAccomplissements())
            .outilsUtilises(experienceDto.getOutilsUtilises())
            .candidat(candidat)
            .build();

        candidat.ajouterExperience(experience);
        Experience savedExperience = candidatRepository.save(candidat).getExperiences().stream()
            .filter(e -> e.getTitre().equals(experience.getTitre()) && e.getEntreprise().equals(experience.getEntreprise()))
            .findFirst()
            .orElse(experience);

        return mapExperienceToDTO(savedExperience);
    }

    /**
     * Supprime une expérience du candidat.
     *
     * @param experienceId l'ID de l'expérience
     */
    @Transactional
    public void deleteExperience(Long experienceId) {
        Candidat candidat = getAuthenticatedCandidat();

        Experience experience = candidat.getExperiences().stream()
            .filter(e -> e.getId().equals(experienceId))
            .findFirst()
            .orElseThrow(() -> new ResourceNotFoundException("Expérience", experienceId));

        candidat.retirerExperience(experience);
        candidatRepository.save(candidat);
    }

    /**
     * Obtient les candidatures du candidat.
     *
     * @return la liste des candidatures
     */
    public List<CandidatureDTO> getMyCandidatures() {
        Candidat candidat = getAuthenticatedCandidat();
        return candidatureRepository.findByCandidatId(candidat.getId()).stream()
            .map(this::mapCandidatureToDTO)
            .collect(Collectors.toList());
    }

    private Candidat getAuthenticatedCandidat() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        return candidatRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("Candidat non trouvé"));
    }

    private String extractTextFromCv(MultipartFile file) {
        // À implémenter avec Apache PDFBox et POI
        // Retourne un placeholder pour l'instant
        return "Texte extrait du CV - À implémenter avec PDFBox/POI";
    }

    private CandidatDTO mapToDTO(Candidat candidat) {
        return CandidatDTO.builder()
            .id(candidat.getId())
            .email(candidat.getEmail())
            .nom(candidat.getNom())
            .prenom(candidat.getPrenom())
            .telephone(candidat.getTelephone())
            .cvPath(candidat.getCvPath())
            .titrePosteRecherche(candidat.getTitrePosteRecherche())
            .disponibiliteImmediate(candidat.getDisponibiliteImmediate())
            .dateDisponibilite(candidat.getDateDisponibilite())
            .pretentionSalarialeMin(candidat.getPretentionSalarialeMin())
            .pretentionSalarialeMax(candidat.getPretentionSalarialeMax())
            .mobilite(candidat.getMobilite())
            .linkedinUrl(candidat.getLinkedinUrl())
            .githubUrl(candidat.getGithubUrl())
            .portefolioUrl(candidat.getPortefolioUrl())
            .presentation(candidat.getPresentation())
            .statut(candidat.getStatut().name())
            .emailVerifie(candidat.getEmailVerifie())
            .dateCreation(candidat.getDateCreation())
            .build();
    }

    private CandidatProfileDTO mapToProfileDTO(Candidat candidat) {
        return CandidatProfileDTO.builder()
            .candidat(mapToDTO(candidat))
            .competences(candidat.getCompetences().stream()
                .map(this::mapCompetenceToDTO)
                .collect(Collectors.toList()))
            .experiences(candidat.getExperiences().stream()
                .map(this::mapExperienceToDTO)
                .collect(Collectors.toList()))
            .nombreCompetences(candidat.getCompetences().size())
            .nombreExperiences(candidat.getExperiences().size())
            .nombreCandidatures((int) candidatureRepository.countByCandidatId(candidat.getId()))
            .build();
    }

    private CompetenceDTO mapCompetenceToDTO(Competence competence) {
        return CompetenceDTO.builder()
            .id(competence.getId())
            .nom(competence.getNom())
            .categorie(competence.getCategorie())
            .niveau(competence.getNiveau())
            .anneesExperience(competence.getAnneesExperience())
            .certifiee(competence.getCertifiee())
            .derniereUtilisation(competence.getDerniereUtilisation())
            .build();
    }

    private ExperienceDTO mapExperienceToDTO(Experience experience) {
        return ExperienceDTO.builder()
            .id(experience.getId())
            .titre(experience.getTitre())
            .entreprise(experience.getEntreprise())
            .typeEntreprise(experience.getTypeEntreprise())
            .secteur(experience.getSecteur())
            .localisation(experience.getLocalisation())
            .dateDebut(experience.getDateDebut())
            .dateFin(experience.getDateFin())
            .emploiActuel(experience.getEmploiActuel())
            .description(experience.getDescription())
            .responsabilites(experience.getResponsabilites())
            .accomplissements(experience.getAccomplissements())
            .outilsUtilises(experience.getOutilsUtilises())
            .build();
    }

    private CandidatureDTO mapCandidatureToDTO(ma.recrutement.entity.Candidature candidature) {
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
