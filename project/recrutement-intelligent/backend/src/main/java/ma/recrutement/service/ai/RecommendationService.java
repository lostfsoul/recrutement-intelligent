package ma.recrutement.service.ai;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ma.recrutement.entity.Candidat;
import ma.recrutement.entity.Competence;
import ma.recrutement.entity.OffreEmploi;
import ma.recrutement.repository.CandidatRepository;
import ma.recrutement.repository.OffreEmploiRepository;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service pour les recommandations basées sur l'IA.
 *
 * @author Recrutement Team
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendationService {

    private final ChatClient chatClient;
    private final CandidatRepository candidatRepository;
    private final OffreEmploiRepository offreEmploiRepository;

    /**
     * Obtient des recommandations d'offres pour un candidat.
     *
     * @param candidatId l'ID du candidat
     * @return la liste des recommandations
     */
    public List<OffreEmploi> getRecommendationsForCandidat(Long candidatId) {
        Candidat candidat = candidatRepository.findById(candidatId)
            .orElseThrow(() -> new RuntimeException("Candidat non trouvé"));

        log.info("Génération de recommandations pour le candidat: {}", candidatId);

        // Obtenir les compétences du candidat
        final Set<String> candidatCompetences = candidat.getCompetences().stream()
            .map(Competence::getNom)
            .map(String::toLowerCase)
            .collect(Collectors.toSet());

        // Chercher les offres correspondantes
        List<OffreEmploi> allOffres = offreEmploiRepository.findByStatutAndActifTrueOrderByDatePublicationDesc(
            OffreEmploi.StatutOffre.PUBLIEE
        );

        // Filtrer et scorer les offres
        return allOffres.stream()
            .map(offre -> {
                int score = 20; // Score de base
                // Ajouter des points pour les compétences correspondantes
                if (offre.getCompetencesRequises() != null && !offre.getCompetencesRequises().isBlank()) {
                    String[] competencesRequises = offre.getCompetencesRequises().toLowerCase().split("[,;\\n]+");
                    for (String comp : competencesRequises) {
                        if (candidatCompetences.contains(comp.trim().toLowerCase())) {
                            score += 15;
                        }
                    }
                }
                final OffreEmploi finalOffre = offre;
                final int finalScore = score;
                return new Object(){
                    OffreEmploi offre = finalOffre;
                    int score = finalScore;
                };
            })
            .filter(o -> o.score > 30)
            .sorted((a, b) -> Integer.compare(b.score, a.score))
            .limit(10)
            .map(o -> o.offre)
            .collect(Collectors.toList());
    }

    /**
     * Obtient des recommandations de candidats pour une offre.
     *
     * @param offreId l'ID de l'offre
     * @return la liste des candidats recommandés
     */
    public List<Candidat> getRecommendationsForOffre(Long offreId) {
        OffreEmploi offre = offreEmploiRepository.findById(offreId)
            .orElseThrow(() -> new RuntimeException("Offre non trouvée"));

        log.info("Génération de recommandations pour l'offre: {}", offreId);

        // Extraire les compétences requises
        Set<String> competencesRequises = extractCompetencesFromOffre(offre);

        // Chercher les candidats correspondants
        List<Candidat> allCandidats = candidatRepository.findAll();

        // Filtrer et scorer les candidats
        return allCandidats.stream()
            .filter(c -> c.getCvText() != null && !c.getCvText().isBlank())
            .map(c -> new Object(){Candidat candidat = c; int score = calculateCandidatScore(c, offre, competencesRequises);})
            .filter(o -> o.score > 30)
            .sorted((a, b) -> Integer.compare(b.score, a.score))
            .limit(10)
            .map(o -> o.candidat)
            .collect(Collectors.toList());
    }

    /**
     * Calcule le score de matching pour une offre/candidat.
     */
    private int calculateScore(OffreEmploi offre, Candidat candidat, Set<String> candidatCompetences) {
        int score = 0;

        // Score de base
        score += 20;

        // Correspondance des compétences
        if (offre.getCompetencesRequises() != null && !offre.getCompetencesRequises().isBlank()) {
            String[] competencesRequises = offre.getCompetencesRequises().toLowerCase().split("[,;\\n]+");
            int matchCount = 0;
            for (String comp : competencesRequises) {
                String compTrimmed = comp.trim().toLowerCase();
                if (candidatCompetences.contains(compTrimmed)) {
                    matchCount++;
                    score += 15;
                }
            }
        }

        // Correspondance de la localisation
        if (offre.getLocalisation() != null && candidat.getMobilite() != null) {
            String offreLoc = offre.getLocalisation().toLowerCase();
            String candidatMob = candidat.getMobilite().toLowerCase();
            if (candidatMob.contains(offreLoc) || offreLoc.contains(candidatMob) || candidatMob.contains("maroc")) {
                score += 10;
            }
        }

        // Correspondance du salaire
        if (offre.getSalaireMin() != null && candidat.getPretentionSalarialeMax() != null) {
            if (offre.getSalaireMin() >= candidat.getPretentionSalarialeMin() &&
                offre.getSalaireMin() <= candidat.getPretentionSalarialeMax()) {
                score += 10;
            }
        }

        // Disponibilité
        if (candidat.getDisponibiliteImmediate()) {
            score += 5;
        }

        // Expérience
        if (offre.getExperienceMinAnnees() != null) {
            // Calculer l'expérience totale du candidat
            int totalExperience = candidat.getExperiences().stream()
                .mapToInt(e -> e.calculerDureeMois())
                .sum();
            int anneesExperience = totalExperience / 12;

            if (anneesExperience >= offre.getExperienceMinAnnees()) {
                score += 10;
            }
        }

        return Math.min(score, 100);
    }

    /**
     * Calcule le score d'un candidat pour une offre.
     */
    private int calculateCandidatScore(Candidat candidat, OffreEmploi offre, Set<String> offreCompetences) {
        int score = 0;

        // Score de base
        score += 20;

        // Correspondance des compétences
        Set<String> candidatComps = candidat.getCompetences().stream()
            .map(Competence::getNom)
            .map(String::toLowerCase)
            .collect(Collectors.toSet());

        int matchCount = 0;
        for (String comp : offreCompetences) {
            if (candidatComps.contains(comp)) {
                matchCount++;
                score += 15;
            }
        }

        // Expérience
        if (offre.getExperienceMinAnnees() != null) {
            int totalExperience = candidat.getExperiences().stream()
                .mapToInt(e -> e.calculerDureeMois())
                .sum();
            int anneesExperience = totalExperience / 12;

            if (anneesExperience >= offre.getExperienceMinAnnees()) {
                score += 15;
            }
        }

        // Disponibilité
        if (candidat.getDisponibiliteImmediate()) {
            score += 10;
        }

        return Math.min(score, 100);
    }

    /**
     * Extrait les compétences depuis le texte d'une offre.
     */
    private Set<String> extractCompetencesFromOffre(OffreEmploi offre) {
        Set<String> competences = new java.util.HashSet<>();

        if (offre.getCompetencesRequises() != null && !offre.getCompetencesRequises().isBlank()) {
            String[] tokens = offre.getCompetencesRequises().toLowerCase().split("[,;\\n\\s]+");
            for (String token : tokens) {
                if (token.length() > 2) {
                    competences.add(token.trim());
                }
            }
        }

        return competences;
    }

    /**
     * Génère une explication de la recommandation.
     *
     * @param offre l'offre
     * @param candidat le candidat
     * @return l'explication
     */
    public String generateRecommendationExplanation(OffreEmploi offre, Candidat candidat) {
        try {
            String prompt = buildExplanationPrompt(offre, candidat);
            return chatClient.prompt()
                .user(prompt)
                .call()
                .content();
        } catch (Exception e) {
            log.error("Erreur lors de la génération de l'explication", e);
            return "Recommandation basée sur l'analyse de vos compétences et de l'offre.";
        }
    }

    private String buildExplanationPrompt(OffreEmploi offre, Candidat candidat) {
        return String.format("""
            En tant qu'expert en recrutement, explique pourquoi ce candidat pourrait correspondre à cette offre.

            Offre: %s chez %s
            Description: %s
            Compétences requises: %s

            Candidat: %s %s
            Compétences: %s
            Expériences: %s

            Explique en 2-3 phrases les points forts de la correspondance.
            """,
            offre.getTitre(),
            offre.getEntreprise().getNom(),
            offre.getDescription(),
            offre.getCompetencesRequises(),
            candidat.getPrenom(),
            candidat.getNom(),
            candidat.getCompetences().stream().map(Competence::getNom).collect(Collectors.joining(", ")),
            candidat.getExperiences().stream().map(e -> e.getTitre() + " chez " + e.getEntreprise()).collect(Collectors.joining(", "))
        );
    }
}
