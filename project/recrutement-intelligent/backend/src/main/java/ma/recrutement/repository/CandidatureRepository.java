package ma.recrutement.repository;

import ma.recrutement.entity.Candidature;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository pour l'entité Candidature.
 * Fournit les opérations CRUD et de recherche spécifiques aux candidatures.
 *
 * @author Recrutement Team
 * @version 1.0.0
 */
@Repository
public interface CandidatureRepository extends JpaRepository<Candidature, Long>, JpaSpecificationExecutor<Candidature> {

    /**
     * Trouve toutes les candidatures d'un candidat.
     *
     * @param candidatId l'ID du candidat
     * @return la liste des candidatures
     */
    List<Candidature> findByCandidatId(Long candidatId);

    /**
     * Trouve toutes les candidatures d'un candidat avec pagination.
     *
     * @param candidatId l'ID du candidat
     * @param pageable les informations de pagination
     * @return la page des candidatures
     */
    Page<Candidature> findByCandidatId(Long candidatId, Pageable pageable);

    /**
     * Trouve toutes les candidatures pour une offre.
     *
     * @param offreId l'ID de l'offre
     * @return la liste des candidatures
     */
    List<Candidature> findByOffreId(Long offreId);

    /**
     * Trouve toutes les candidatures pour une offre avec pagination.
     *
     * @param offreId l'ID de l'offre
     * @param pageable les informations de pagination
     * @return la page des candidatures
     */
    Page<Candidature> findByOffreId(Long offreId, Pageable pageable);

    /**
     * Trouve les candidatures par statut.
     *
     * @param statut le statut de la candidature
     * @return la liste des candidatures
     */
    List<Candidature> findByStatut(Candidature.StatutCandidature statut);

    /**
     * Trouve les candidatures par statut avec pagination.
     *
     * @param statut le statut de la candidature
     * @param pageable les informations de pagination
     * @return la page des candidatures
     */
    Page<Candidature> findByStatut(Candidature.StatutCandidature statut, Pageable pageable);

    /**
     * Trouve les candidatures par statut pour une offre spécifique.
     *
     * @param offreId l'ID de l'offre
     * @param statut le statut de la candidature
     * @return la liste des candidatures
     */
    List<Candidature> findByOffreIdAndStatut(Long offreId, Candidature.StatutCandidature statut);

    /**
     * Vérifie si un candidat a déjà postulé à une offre.
     *
     * @param candidatId l'ID du candidat
     * @param offreId l'ID de l'offre
     * @return true si une candidature existe
     */
    boolean existsByCandidatIdAndOffreId(Long candidatId, Long offreId);

    /**
     * Trouve une candidature par candidat et offre.
     *
     * @param candidatId l'ID du candidat
     * @param offreId l'ID de l'offre
     * @return la candidature trouvée ou Optional vide
     */
    Optional<Candidature> findByCandidatIdAndOffreId(Long candidatId, Long offreId);

    /**
     * Trouve les candidatures non vues par le recruteur pour une offre.
     *
     * @param offreId l'ID de l'offre
     * @return la liste des candidatures non vues
     */
    List<Candidature> findByOffreIdAndVuParRecruteurFalse(Long offreId);

    /**
     * Trouve les candidatures vues par le recruteur pour une offre.
     *
     * @param offreId l'ID de l'offre
     * @return la liste des candidatures vues
     */
    List<Candidature> findByOffreIdAndVuParRecruteurTrue(Long offreId);

    /**
     * Trouve les candidatures par score de matching minimum.
     *
     * @param scoreMin le score minimum
     * @return la liste des candidatures
     */
    List<Candidature> findByScoreMatchingGreaterThanEqualOrderByScoreMatchingDesc(Integer scoreMin);

    /**
     * Trouve les candidatures récentes pour un candidat.
     *
     * @param candidatId l'ID du candidat
     * @param pageable les informations de pagination
     * @return la page des candidatures
     */
    Page<Candidature> findByCandidatIdOrderByDateCandidatureDesc(Long candidatId, Pageable pageable);

    /**
     * Trouve les candidatures récentes pour un candidat (sans pagination).
     *
     * @param candidatId l'ID du candidat
     * @return la liste des candidatures
     */
    List<Candidature> findByCandidatIdOrderByDateCandidatureDesc(Long candidatId);

    /**
     * Trouve les candidatures récentes pour une offre.
     *
     * @param offreId l'ID de l'offre
     * @param pageable les informations de pagination
     * @return la page des candidatures
     */
    Page<Candidature> findByOffreIdOrderByDateCandidatureDesc(Long offreId, Pageable pageable);

    /**
     * Trouve les candidatures récentes pour une offre (sans pagination).
     *
     * @param offreId l'ID de l'offre
     * @return la liste des candidatures
     */
    List<Candidature> findByOffreIdOrderByDateCandidatureDesc(Long offreId);

    /**
     * Compte le nombre de candidatures pour une offre.
     *
     * @param offreId l'ID de l'offre
     * @return le nombre de candidatures
     */
    long countByOffreId(Long offreId);

    /**
     * Compte le nombre de candidatures par statut.
     *
     * @param statut le statut de la candidature
     * @return le nombre de candidatures
     */
    long countByStatut(Candidature.StatutCandidature statut);

    /**
     * Compte le nombre de candidatures pour une offre par statut.
     *
     * @param offreId l'ID de l'offre
     * @param statut le statut de la candidature
     * @return le nombre de candidatures
     */
    long countByOffreIdAndStatut(Long offreId, Candidature.StatutCandidature statut);

    /**
     * Trouve les candidatures pour les offres d'une entreprise.
     *
     * @param entrepriseId l'ID de l'entreprise
     * @return la liste des candidatures
     */
    @Query("SELECT c FROM Candidature c JOIN c.offre o WHERE o.entreprise.id = :entrepriseId")
    List<Candidature> findByEntrepriseId(@Param("entrepriseId") Long entrepriseId);

    /**
     * Trouve les candidatures pour les offres d'une entreprise avec pagination.
     *
     * @param entrepriseId l'ID de l'entreprise
     * @param pageable les informations de pagination
     * @return la page des candidatures
     */
    @Query("SELECT c FROM Candidature c JOIN c.offre o WHERE o.entreprise.id = :entrepriseId")
    Page<Candidature> findByEntrepriseId(@Param("entrepriseId") Long entrepriseId, Pageable pageable);

    /**
     * Compte le nombre de candidatures par candidat.
     *
     * @param candidatId l'ID du candidat
     * @return le nombre de candidatures
     */
    long countByCandidatId(Long candidatId);

    /**
     * Trouve les candidatures qui n'ont pas encore été notées (feedback vide).
     *
     * @param candidatId l'ID du candidat
     * @return la liste des candidatures sans feedback
     */
    @Query("SELECT c FROM Candidature c WHERE c.candidat.id = :candidatId AND " +
           "(c.statut = 'REFUSE' OR c.statut = 'OFFRE_ACCEPT') AND " +
           "c.feedbackCandidat IS NULL")
    List<Candidature> findCandidaturesSansFeedback(@Param("candidatId") Long candidatId);

    /**
     * Compte le nombre de candidatures par mois pour une offre.
     *
     * @param offreId l'ID de l'offre
     * @return liste de statistiques
     */
    @Query("SELECT FUNCTION('DATE_TRUNC', 'month', c.dateCandidature), COUNT(c) " +
           "FROM Candidature c WHERE c.offre.id = :offreId " +
           "GROUP BY FUNCTION('DATE_TRUNC', 'month', c.dateCandidature)")
    List<Object[]> countByMonthForOffre(@Param("offreId") Long offreId);
}
