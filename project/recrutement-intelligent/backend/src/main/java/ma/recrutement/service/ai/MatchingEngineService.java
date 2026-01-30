package ma.recrutement.service.ai;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ma.recrutement.dto.MatchingResultDTO;
import ma.recrutement.entity.Candidat;
import ma.recrutement.entity.OffreEmploi;
import ma.recrutement.repository.CandidatRepository;
import ma.recrutement.repository.OffreEmploiRepository;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service pour le matching intelligent CV/Offres utilisant Spring AI et Vector Store.
 *
 * @author Recrutement Team
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MatchingEngineService {

    private final VectorStore vectorStore;
    private final ChatClient chatClient;
    private final CandidatRepository candidatRepository;
    private final OffreEmploiRepository offreEmploiRepository;

    /**
     * Indexe un CV dans le vector store.
     *
     * @param candidatId l'ID du candidat
     */
    public void indexCv(Long candidatId) {
        Candidat candidat = candidatRepository.findById(candidatId)
            .orElseThrow(() -> new RuntimeException("Candidat non trouvé"));

        if (candidat.getCvText() == null || candidat.getCvText().isBlank()) {
            log.warn("Impossible d'indexer le CV du candidat {}: pas de texte", candidatId);
            return;
        }

        // Créer le document avec le CV
        String documentId = "cv-" + candidatId;
        Document document = new Document(
            documentId,
            candidat.getCvText(),
            java.util.Map.of(
                "candidatId", candidatId.toString(),
                "type", "cv",
                "nom", candidat.getNom() + " " + candidat.getPrenom()
            )
        );

        // Ajouter au vector store
        List<Document> documents = List.of(document);
        vectorStore.add(documents);

        // Mettre à jour le candidat avec l'ID du vector
        candidat.setCvVectorId(documentId);
        candidatRepository.save(candidat);

        log.info("CV indexé pour le candidat: {}", candidatId);
    }

    /**
     * Indexe une offre dans le vector store.
     *
     * @param offreId l'ID de l'offre
     */
    public void indexOffre(Long offreId) {
        OffreEmploi offre = offreEmploiRepository.findById(offreId)
            .orElseThrow(() -> new RuntimeException("Offre non trouvée"));

        // Construire le texte à indexer
        String texte = buildOffreText(offre);

        // Créer le document
        String documentId = "offre-" + offreId;
        Document document = new Document(
            documentId,
            texte,
            java.util.Map.of(
                "offreId", offreId.toString(),
                "type", "offre",
                "titre", offre.getTitre(),
                "entreprise", offre.getEntreprise().getNom()
            )
        );

        // Ajouter au vector store
        List<Document> documents = List.of(document);
        vectorStore.add(documents);

        // Mettre à jour l'offre avec l'ID du vector
        offre.setVectorId(documentId);
        offreEmploiRepository.save(offre);

        log.info("Offre indexée: {}", offreId);
    }

    /**
     * Trouve les candidats correspondants pour une offre.
     *
     * @param offreId l'ID de l'offre
     * @param limit le nombre maximum de résultats
     * @return la liste des candidats correspondants
     */
    public List<MatchingResultDTO> findMatchingCandidates(Long offreId, int limit) {
        OffreEmploi offre = offreEmploiRepository.findById(offreId)
            .orElseThrow(() -> new RuntimeException("Offre non trouvée"));

        String query = buildOffreText(offre);

        // Rechercher dans le vector store
        List<Document> results = vectorStore.similaritySearch(
            org.springframework.ai.vectorstore.SearchRequest.query(query).withTopK(limit)
        );

        return results.stream()
            .filter(doc -> "cv".equals(doc.getMetadata().get("type")))
            .map(doc -> {
                Long candidatId = Long.parseLong((String) doc.getMetadata().get("candidatId"));
                Candidat candidat = candidatRepository.findById(candidatId).orElse(null);
                if (candidat == null) return null;

                // Use a default score - the vector store handles similarity internally
                return calculateMatching(offre, candidat, 75);
            })
            .filter(java.util.Objects::nonNull)
            .collect(Collectors.toList());
    }

    /**
     * Trouve les offres correspondantes pour un candidat.
     *
     * @param candidatId l'ID du candidat
     * @param limit le nombre maximum de résultats
     * @return la liste des offres correspondantes
     */
    public List<MatchingResultDTO> findMatchingOffres(Long candidatId, int limit) {
        Candidat candidat = candidatRepository.findById(candidatId)
            .orElseThrow(() -> new RuntimeException("Candidat non trouvé"));

        if (candidat.getCvText() == null || candidat.getCvText().isBlank()) {
            log.warn("Impossible de trouver des offres pour le candidat {}: pas de CV", candidatId);
            return new ArrayList<>();
        }

        String query = candidat.getCvText();

        // Rechercher dans le vector store
        List<Document> results = vectorStore.similaritySearch(
            org.springframework.ai.vectorstore.SearchRequest.query(query).withTopK(limit)
        );

        return results.stream()
            .filter(doc -> "offre".equals(doc.getMetadata().get("type")))
            .map(doc -> {
                Long offreId = Long.parseLong((String) doc.getMetadata().get("offreId"));
                OffreEmploi offre = offreEmploiRepository.findById(offreId).orElse(null);
                if (offre == null) return null;

                // Use a default score - the vector store handles similarity internally
                return calculateMatching(offre, candidat, 75);
            })
            .filter(java.util.Objects::nonNull)
            .collect(Collectors.toList());
    }

    /**
     * Calcule le score de matching entre une offre et un candidat.
     *
     * @param offre l'offre
     * @param candidat le candidat
     * @param vectorScore le score de similarité vectorielle
     * @return le résultat de matching
     */
    private MatchingResultDTO calculateMatching(OffreEmploi offre, Candidat candidat, double vectorScore) {
        int scoreMatching = (int) (vectorScore * 100);

        String recommendation;
        Boolean recommande;

        if (scoreMatching >= 80) {
            recommendation = "Excellent match! Le profil correspond très bien aux exigences du poste.";
            recommande = true;
        } else if (scoreMatching >= 60) {
            recommendation = "Bon match. Le profil correspond aux critères principaux.";
            recommande = true;
        } else if (scoreMatching >= 40) {
            recommendation = "Match partiel. Certaines compétences sont manquantes.";
            recommande = false;
        } else {
            recommendation = "Match faible. Le profil ne correspond pas aux exigences.";
            recommande = false;
        }

        return MatchingResultDTO.builder()
            .offreId(offre.getId())
            .offreTitre(offre.getTitre())
            .nomEntreprise(offre.getEntreprise().getNom())
            .candidatId(candidat.getId())
            .candidatNom(candidat.getNom())
            .candidatPrenom(candidat.getPrenom())
            .scoreMatching(scoreMatching)
            .scoreCompetences((int) (vectorScore * 100))
            .scoreExperience(0) // À calculer
            .scoreFormation(0) // À calculer
            .competencesMatch(new ArrayList<>()) // À extraire
            .competencesManquantes(new ArrayList<>()) // À extraire
            .recommendation(recommendation)
            .recommande(recommande)
            .reason("Basé sur l'analyse sémantique du CV et de l'offre")
            .build();
    }

    /**
     * Construit le texte représentatif d'une offre pour l'indexation.
     *
     * @param offre l'offre
     * @return le texte
     */
    private String buildOffreText(OffreEmploi offre) {
        StringBuilder sb = new StringBuilder();
        sb.append(offre.getTitre()).append("\n");
        sb.append(offre.getDescription()).append("\n");
        if (offre.getCompetencesRequises() != null) {
            sb.append("Compétences requises: ").append(offre.getCompetencesRequises()).append("\n");
        }
        if (offre.getProfilRecherche() != null) {
            sb.append("Profil recherché: ").append(offre.getProfilRecherche()).append("\n");
        }
        sb.append("Type de contrat: ").append(offre.getTypeContrat()).append("\n");
        sb.append("Localisation: ").append(offre.getLocalisation()).append("\n");
        if (offre.getNiveauEtudesMin() != null) {
            sb.append("Niveau d'études: ").append(offre.getNiveauEtudesMin()).append("\n");
        }
        return sb.toString();
    }
}
